# Production Setup Guide

## 1. Start the backend

```bash
cd backend
docker compose up --build
```

- API runs at  http://localhost:8000
- Swagger docs at  http://localhost:8000/docs
- PostgreSQL on port 5432

The server creates all tables on first start. Use Alembic for migrations before shipping to production.

---

## 2. Update the Android app

In your `Application.onCreate()`, pass the server URL and your SDK key:

```kotlin
CartWishlistSdk.init(
    context   = this,
    sdkKey    = "cwsk_live_a7f2d901e4b83c6f3f9a",  // from the portal
    serverUrl = "http://10.0.2.2:8000/",             // 10.0.2.2 = localhost from emulator
    clientId  = "my_shop",
    deviceId  = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID),
)
```

- `serverUrl = null` → local-only mode (SharedPreferences, no sync)
- `serverUrl = "http://..."` → Room + WorkManager + Retrofit enabled

WorkManager syncs automatically every 15 minutes when the device is online.
To force an immediate sync (e.g. after checkout):

```kotlin
CartWishlistSdk.syncNow(context)
```

---

## 3. Start the React portal

```bash
# Create the app once
npx create-react-app portal-react --template typescript
cd portal-react

# Copy the generated hooks/ and components/ files into src/

# Set the API base URL
echo "REACT_APP_API_BASE=http://localhost:8000" > .env.local

npm start
```

Use the `Dashboard` component anywhere:

```tsx
import Dashboard from './components/Dashboard';

function App() {
  return <Dashboard />;
}
```

---

## Architecture summary

```
Android SDK (offline-first)
│
├── User action  ──► CartManager / WishlistManager
│                        │
│                        ├── writes to Room DB (instant, offline)
│                        └── records AnalyticsEvent (synced = false)
│
└── WorkManager SyncWorker (fires when online)
         │
         ├── reads unsynced events + current cart from Room
         ├── POST /api/sync  ──► FastAPI ──► PostgreSQL
         └── marks events as synced

Web Portal (React)
└── GET /api/analytics every 30s  ──► FastAPI aggregates PostgreSQL
```

## Cart sharing — before vs after

| | Before | After |
|---|---|---|
| Share code | ~2000-char Base64 string | 10-char short ID (`abc1234567`) |
| Works offline | Yes | Falls back to Base64 |
| Stored on server | No | Yes — survives app reinstall |
