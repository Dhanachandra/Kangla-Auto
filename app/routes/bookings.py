from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session

from .. import models, schemas
from ..auth import get_current_user
from ..database import get_db

router = APIRouter(prefix="/api/bookings", tags=["bookings"])

BASE_FARE = 30.0
FARE_PER_KM = 15.0
MAX_PAGE_SIZE = 200

# role -> {new_status: set of allowed current statuses}
TRANSITIONS = {
    "rider": {"cancelled": {"pending", "accepted"}},
    "driver": {"accepted": {"pending"}, "completed": {"accepted"}},
}


def compute_fare(distance_km: float | None) -> float:
    if not distance_km:
        return BASE_FARE
    billable_km = max(1, int(distance_km + 0.5))
    return float(BASE_FARE + billable_km * FARE_PER_KM)


def booking_out(booking: models.Booking) -> schemas.BookingOut:
    return schemas.BookingOut(
        id=booking.id,
        user_id=booking.user_id,
        user_name=booking.user.name,
        driver_id=booking.driver_id,
        driver_name=booking.driver.name if booking.driver else None,
        pickup=booking.pickup,
        dropoff=booking.dropoff,
        distance_km=booking.distance_km,
        fare=booking.fare,
        status=booking.status,
        created_at=booking.created_at,
    )


def _get_booking_or_404(db: Session, booking_id: int) -> models.Booking:
    booking = db.get(models.Booking, booking_id)
    if booking is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Booking not found")
    return booking


@router.post("", response_model=schemas.BookingOut)
def create_booking(
    payload: schemas.BookingCreate,
    current_user: models.User = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> schemas.BookingOut:
    if current_user.role != "rider":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN, detail="Only riders can create bookings"
        )
    booking = models.Booking(
        user_id=current_user.id,
        pickup=payload.pickup.strip(),
        dropoff=payload.dropoff.strip(),
        distance_km=payload.distance_km,
        fare=compute_fare(payload.distance_km),
        status="pending",
    )
    db.add(booking)
    db.commit()
    db.refresh(booking)
    return booking_out(booking)


@router.get("/my", response_model=list[schemas.BookingOut])
def my_bookings(
    current_user: models.User = Depends(get_current_user),
    db: Session = Depends(get_db),
    offset: int = Query(default=0, ge=0),
    limit: int = Query(default=50, ge=1, le=MAX_PAGE_SIZE),
) -> list[schemas.BookingOut]:
    query = db.query(models.Booking)
    if current_user.role == "rider":
        query = query.filter(models.Booking.user_id == current_user.id)
    elif current_user.role == "driver":
        query = query.filter(models.Booking.driver_id == current_user.id)
    bookings = (
        query.order_by(models.Booking.created_at.desc()).offset(offset).limit(limit).all()
    )
    return [booking_out(b) for b in bookings]


@router.get("/pending", response_model=list[schemas.BookingOut])
def pending_bookings(
    current_user: models.User = Depends(get_current_user),
    db: Session = Depends(get_db),
    offset: int = Query(default=0, ge=0),
    limit: int = Query(default=50, ge=1, le=MAX_PAGE_SIZE),
) -> list[schemas.BookingOut]:
    if current_user.role != "driver":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN, detail="Only drivers can view the queue"
        )
    bookings = (
        db.query(models.Booking)
        .filter(models.Booking.status == "pending")
        .order_by(models.Booking.created_at.asc())
        .offset(offset)
        .limit(limit)
        .all()
    )
    return [booking_out(b) for b in bookings]


@router.patch("/{booking_id}", response_model=schemas.BookingOut)
def update_booking_status(
    booking_id: int,
    payload: schemas.BookingStatusUpdate,
    current_user: models.User = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> schemas.BookingOut:
    booking = _get_booking_or_404(db, booking_id)
    role_transitions = TRANSITIONS.get(current_user.role)
    if role_transitions is None:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Role cannot update bookings")

    allowed_from = role_transitions.get(payload.status)
    if allowed_from is None:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail=f"Role '{current_user.role}' cannot set status '{payload.status}'",
        )
    if booking.status not in allowed_from:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail=f"Cannot move booking from '{booking.status}' to '{payload.status}'",
        )
    if current_user.role == "rider" and booking.user_id != current_user.id:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Not your booking")
    if current_user.role == "driver":
        if payload.status == "accepted":
            if booking.driver_id is not None:
                raise HTTPException(
                    status_code=status.HTTP_409_CONFLICT, detail="Booking already assigned"
                )
            booking.driver_id = current_user.id
        elif booking.driver_id != current_user.id:
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Not your booking")

    booking.status = payload.status
    db.commit()
    db.refresh(booking)
    return booking_out(booking)