package com.example.cartwishlist.sync

import android.content.Context
import androidx.work.*
import com.example.cartwishlist.db.AppDatabase
import com.example.cartwishlist.network.RetrofitClient
import com.example.cartwishlist.network.dto.AnalyticsEventDto
import com.example.cartwishlist.network.dto.CartItemDto
import com.example.cartwishlist.network.dto.SyncRequest
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    private val db     = AppDatabase.getInstance(context)
    private val clientId = inputData.getString(KEY_CLIENT_ID) ?: "unknown"
    private val deviceId = inputData.getString(KEY_DEVICE_ID) ?: "unknown"

    override suspend fun doWork(): Result {
        val api = runCatching { RetrofitClient.getApi() }.getOrNull()
            ?: return Result.failure()  // SDK not initialized

        val cartItems   = db.cartDao().getAll()
        val unsynced    = db.analyticsEventDao().getUnsynced()

        // Nothing to sync — skip network call
        if (cartItems.isEmpty() && unsynced.isEmpty()) return Result.success()

        val request = SyncRequest(
            clientId = clientId,
            deviceId = deviceId,
            cartItems = cartItems.map {
                CartItemDto(
                    productId      = it.productId,
                    productName    = it.productName,
                    productPrice   = it.productPrice,
                    productImageUrl = it.productImageUrl,
                    quantity       = it.quantity,
                )
            },
            analyticsEvents = unsynced.map {
                AnalyticsEventDto(
                    eventType   = it.eventType,
                    productId   = it.productId,
                    productName = it.productName,
                    timestamp   = Instant.ofEpochMilli(it.timestamp)
                        .atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                )
            },
        )

        return runCatching {
            api.sync(request)
            db.analyticsEventDao().markSynced(unsynced.map { it.id })
            db.analyticsEventDao().deleteAllSynced()
            Result.success()
        }.getOrElse {
            // Retry with exponential back-off (WorkManager handles the schedule)
            if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val KEY_CLIENT_ID = "client_id"
        const val KEY_DEVICE_ID = "device_id"
        private const val MAX_ATTEMPTS = 5
        private const val WORK_NAME = "cwsk_sync"

        /** Enqueue a one-shot sync triggered by a local change. */
        fun enqueueOnce(context: Context, clientId: String, deviceId: String) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setInputData(
                    workDataOf(KEY_CLIENT_ID to clientId, KEY_DEVICE_ID to deviceId)
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }

        /** Enqueue a recurring background sync (every 15 minutes when online). */
        fun enqueueRecurring(context: Context, clientId: String, deviceId: String) {
            val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setInputData(
                    workDataOf(KEY_CLIENT_ID to clientId, KEY_DEVICE_ID to deviceId)
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME + "_periodic",
                    ExistingPeriodicWorkPolicy.KEEP,
                    request,
                )
        }
    }
}
