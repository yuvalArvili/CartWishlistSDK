import os
import uuid
from fastapi import APIRouter, File, UploadFile, HTTPException

router = APIRouter(prefix="/api", tags=["upload"])

UPLOAD_DIR = "/app/uploads"
ALLOWED_TYPES = {"image/jpeg", "image/png", "image/webp", "image/gif"}
MAX_BYTES = 8 * 1024 * 1024


@router.post("/upload")
async def upload_image(file: UploadFile = File(...)):
    if file.content_type not in ALLOWED_TYPES:
        raise HTTPException(status_code=400, detail="Only JPEG, PNG, WEBP, and GIF images are accepted")
    data = await file.read()
    if len(data) > MAX_BYTES:
        raise HTTPException(status_code=413, detail="File exceeds the 8 MB limit")
    ext = os.path.splitext(file.filename or "")[1].lower()
    if ext not in {".jpg", ".jpeg", ".png", ".webp", ".gif"}:
        ext = ".jpg"
    filename = uuid.uuid4().hex + ext
    os.makedirs(UPLOAD_DIR, exist_ok=True)
    with open(os.path.join(UPLOAD_DIR, filename), "wb") as f:
        f.write(data)
    return {"url": f"http://localhost:8000/uploads/{filename}"}
