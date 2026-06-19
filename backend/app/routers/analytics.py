from fastapi import APIRouter, Depends
from sqlalchemy import select, func
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db
from app.models import AnalyticsEvent, Cart
from app.schemas import AnalyticsResponse, ProductStat

router = APIRouter(prefix="/api", tags=["analytics"])

_EVENT_CART_ADD      = "cart_add"
_EVENT_CART_CLEAR    = "cart_clear"
_EVENT_CART_SHARE    = "cart_share"
_EVENT_WISHLIST_ADD  = "wishlist_add"


@router.get("/analytics", response_model=AnalyticsResponse)
async def get_analytics(db: AsyncSession = Depends(get_db)):
    """
    Returns aggregated analytics for the web portal.
    All queries run in a single round-trip using subqueries.
    """
    # ── Event counts ──────────────────────────────────────────────────────────
    count_q = await db.execute(
        select(AnalyticsEvent.event_type, func.count().label("n"))
        .group_by(AnalyticsEvent.event_type)
    )
    counts: dict[str, int] = {row.event_type: row.n for row in count_q}

    # ── Top products per event type ───────────────────────────────────────────
    async def top_products(event_type: str, limit: int = 5) -> list[ProductStat]:
        rows = await db.execute(
            select(
                AnalyticsEvent.product_id,
                AnalyticsEvent.product_name,
                func.count().label("n"),
            )
            .where(AnalyticsEvent.event_type == event_type)
            .group_by(AnalyticsEvent.product_id, AnalyticsEvent.product_name)
            .order_by(func.count().desc())
            .limit(limit)
        )
        return [
            ProductStat(product_id=r.product_id, product_name=r.product_name or r.product_id, count=r.n)
            for r in rows
        ]

    # ── Active carts (non-empty items_json) ───────────────────────────────────
    active_result = await db.execute(
        select(func.count()).where(func.json_array_length(Cart.items_json) > 0)
    )
    active_carts = active_result.scalar_one_or_none() or 0

    top_wishlisted, top_carted = (
        await top_products(_EVENT_WISHLIST_ADD),
        await top_products(_EVENT_CART_ADD),
    )

    return AnalyticsResponse(
        cart_adds=counts.get(_EVENT_CART_ADD, 0),
        cart_clears=counts.get(_EVENT_CART_CLEAR, 0),
        cart_shares=counts.get(_EVENT_CART_SHARE, 0),
        wishlist_adds=counts.get(_EVENT_WISHLIST_ADD, 0),
        active_carts=active_carts,
        top_wishlisted=top_wishlisted,
        top_carted=top_carted,
    )
