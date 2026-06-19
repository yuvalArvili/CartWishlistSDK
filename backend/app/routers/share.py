import hashlib
import json
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db
from app.models import SharedCart
from app.schemas import ShareCartRequest, ShareCartResponse, SharedCartResponse

router = APIRouter(prefix="/api", tags=["share"])


def _make_short_id(items_json: str) -> str:
    """Deterministic 10-char ID from cart contents so identical carts reuse the same link."""
    return hashlib.sha256(items_json.encode()).hexdigest()[:10]


@router.post("/share", response_model=ShareCartResponse, status_code=status.HTTP_201_CREATED)
async def share_cart(body: ShareCartRequest, db: AsyncSession = Depends(get_db)):
    """Saves the cart snapshot and returns a short shareable ID."""
    items_payload = [item.model_dump() for item in body.items]
    short_id = _make_short_id(json.dumps(items_payload, sort_keys=True))

    existing = await db.get(SharedCart, short_id)
    if existing is None:
        db.add(SharedCart(short_id=short_id, items_json=items_payload))
        await db.commit()

    return ShareCartResponse(short_id=short_id)


@router.get("/share/{short_id}", response_model=SharedCartResponse)
async def get_shared_cart(short_id: str, db: AsyncSession = Depends(get_db)):
    """Retrieves a previously shared cart by its short ID."""
    shared = await db.get(SharedCart, short_id)
    if shared is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Share link not found or expired.")

    return SharedCartResponse(
        short_id=shared.short_id,
        items=shared.items_json,
        created_at=shared.created_at,
    )
