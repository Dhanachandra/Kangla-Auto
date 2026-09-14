from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from .. import models, schemas
from ..database import get_db

router = APIRouter(prefix="/api/drivers", tags=["drivers"])


@router.get("", response_model=list[schemas.UserOut])
def list_drivers(
    db: Session = Depends(get_db),
    offset: int = Query(default=0, ge=0),
    limit: int = Query(default=100, ge=1, le=500),
) -> list[models.User]:
    return (
        db.query(models.User)
        .filter(models.User.role == "driver")
        .order_by(models.User.name.asc())
        .offset(offset)
        .limit(limit)
        .all()
    )