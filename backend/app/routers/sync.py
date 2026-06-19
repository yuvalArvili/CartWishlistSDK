from datetime import datetime, timezone
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db
from app.models import Cart, AnalyticsEvent
from app.schemas import SyncRequest, SyncResponse

router = APIRouter(prefix="/api", tags=["sync"])


@router.post("/sync", response_model=SyncResponse, status_code=status.HTTP_200_OK)
async def sync(body: SyncRequest, db: AsyncSession = Depends(get_db)):
    """
    Receives the client's full current cart state and a batch of analytics events.
    Upserts the cart row, then bulk-inserts new analytics events.
    Returns the server timestamp so the client can track when it last synced.
    """
    # ── Upsert cart ───────────────────────────────────────────────────────────
    result = await db.execute(
        select(Cart).where(
            Cart.client_id == body.client_id,
            Cart.device_id == body.device_id,
        )
    )
    cart = result.scalar_one_or_none()

    items_payload = [item.model_dump() for item in body.cart_items]

    if cart is None:
        cart = Cart(
            client_id=body.client_id,
            device_id=body.device_id,
            items_json=items_payload,
        )
        db.add(cart)
    else:
        cart.items_json = items_payload
        cart.updated_at = datetime.now(timezone.utc)

    # ── Bulk-insert analytics events ──────────────────────────────────────────
    if body.analytics_events:
        db.add_all(
            AnalyticsEvent(
                event_type=e.event_type,
                product_id=e.product_id,
                product_name=e.product_name,
                timestamp=e.timestamp,
            )
            for e in body.analytics_events
        )

    await db.commit()
    await db.refresh(cart)

    return SyncResponse(synced_at=datetime.now(timezone.utc), cart_id=cart.id)
