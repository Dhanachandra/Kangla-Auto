from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field

VALID_ROLES = r"^(rider|driver)$"


class UserCreate(BaseModel):
    name: str = Field(min_length=1, max_length=100)
    phone: str = Field(min_length=6, max_length=20)
    password: str = Field(min_length=6, max_length=100)
    role: str = Field(default="rider", pattern=VALID_ROLES)


class Login(BaseModel):
    phone: str = Field(min_length=6, max_length=20)
    password: str = Field(min_length=1)


class UserOut(BaseModel):
    id: int
    name: str
    phone: str
    role: str
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)


class AuthResponse(BaseModel):
    token: str
    user: UserOut


class BookingCreate(BaseModel):
    pickup: str = Field(min_length=1, max_length=200)
    dropoff: str = Field(min_length=1, max_length=200)
    distance_km: float | None = Field(default=None, gt=0)


class BookingStatusUpdate(BaseModel):
    status: str = Field(pattern=r"^(accepted|cancelled|completed)$")


class BookingOut(BaseModel):
    id: int
    user_id: int
    user_name: str
    driver_id: int | None
    driver_name: str | None
    pickup: str
    dropoff: str
    distance_km: float | None
    fare: float
    status: str
    created_at: datetime