from datetime import datetime
from pydantic import BaseModel, Field


# ── Shared cart items ─────────────────────────────────────────────────────────

class CartItemSchema(BaseModel):
    product_id: str
    product_name: str
    product_price: float
    product_image_url: str = ""
    quantity: int = Field(ge=1)


# ── POST /api/sync ────────────────────────────────────────────────────────────

class AnalyticsEventIn(BaseModel):
    event_type: str
    product_id: str
    product_name: str | None = None
    timestamp: datetime


class SyncRequest(BaseModel):
    client_id: str
    device_id: str
    cart_items: list[CartItemSchema]
    analytics_events: list[AnalyticsEventIn] = []


class SyncResponse(BaseModel):
    synced_at: datetime
    cart_id: int


# ── POST /api/share ───────────────────────────────────────────────────────────

class ShareCartRequest(BaseModel):
    items: list[CartItemSchema]


class ShareCartResponse(BaseModel):
    short_id: str


# ── GET /api/share/{short_id} ─────────────────────────────────────────────────

class SharedCartResponse(BaseModel):
    short_id: str
    items: list[CartItemSchema]
    created_at: datetime


# ── GET/POST/DELETE /api/products ────────────────────────────────────────────

class ProductSchema(BaseModel):
    id: str
    name: str
    price: float
    image_url: str = ""
    description: str = ""

    model_config = {"from_attributes": True}


class ProductCreate(BaseModel):
    name: str
    price: float
    image_url: str = ""
    description: str = ""


# ── GET /api/analytics ────────────────────────────────────────────────────────

class ProductStat(BaseModel):
    product_id: str
    product_name: str
    count: int


class AnalyticsResponse(BaseModel):
    cart_adds: int
    cart_clears: int
    cart_shares: int
    wishlist_adds: int
    active_carts: int
    top_wishlisted: list[ProductStat]
    top_carted: list[ProductStat]
