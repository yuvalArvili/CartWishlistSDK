from fastapi import APIRouter, Depends, Header, HTTPException
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db
from app.models import Product
from app.schemas import ProductSchema, ProductCreate

router = APIRouter(prefix="/api", tags=["products"])


def _require_store(x_store_id: str = Header(..., description="Tenant store identifier")) -> str:
    if not x_store_id.strip():
        raise HTTPException(status_code=400, detail="X-Store-ID header must not be empty")
    return x_store_id.strip()


@router.get("/products", response_model=list[ProductSchema])
async def list_products(
    store_id: str = Depends(_require_store),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        select(Product)
        .where(Product.store_id == store_id)
        .order_by(Product.created_at.asc())
    )
    return result.scalars().all()


@router.post("/products", response_model=ProductSchema, status_code=201)
async def create_product(
    body: ProductCreate,
    store_id: str = Depends(_require_store),
    db: AsyncSession = Depends(get_db),
):
    product = Product(
        store_id=store_id,
        name=body.name,
        price=body.price,
        image_url=body.image_url,
        description=body.description,
    )
    db.add(product)
    await db.commit()
    await db.refresh(product)
    return product


@router.delete("/products/{product_id}", status_code=204)
async def delete_product(
    product_id: str,
    store_id: str = Depends(_require_store),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        select(Product).where(Product.id == product_id, Product.store_id == store_id)
    )
    product = result.scalar_one_or_none()
    if not product:
        raise HTTPException(status_code=404, detail="Product not found")
    await db.delete(product)
    await db.commit()
