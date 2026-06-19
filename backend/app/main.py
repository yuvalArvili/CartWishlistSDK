import os
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from sqlalchemy import text

from app.database import engine, Base
from app.routers import sync, share, analytics, products, upload

UPLOAD_DIR = "/app/uploads"


@asynccontextmanager
async def lifespan(app: FastAPI):
    os.makedirs(UPLOAD_DIR, exist_ok=True)
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
        # Add store_id column to existing products tables (safe to run every time)
        await conn.execute(text(
            "ALTER TABLE products ADD COLUMN IF NOT EXISTS "
            "store_id VARCHAR(64) NOT NULL DEFAULT 'default';"
        ))
        await conn.execute(text(
            "CREATE INDEX IF NOT EXISTS ix_products_store_id ON products(store_id);"
        ))
    yield


app = FastAPI(
    title="CartWishlist SDK API",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*", "null"],  # "null" allows file:// portal origin
    allow_methods=["*"],
    allow_headers=["*"],
    allow_origin_regex=".*",      # catch-all for any origin including file://
)

app.include_router(sync.router)
app.include_router(share.router)
app.include_router(analytics.router)
app.include_router(products.router)
app.include_router(upload.router)

app.mount("/uploads", StaticFiles(directory=UPLOAD_DIR), name="uploads")


@app.get("/health")
async def health():
    return {"status": "ok"}
