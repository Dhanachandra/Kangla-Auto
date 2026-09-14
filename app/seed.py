from . import models, security
from .database import SessionLocal

DEMO_USERS = [
    {"name": "Iboyaima", "phone": "9876500001", "password": "driver123", "role": "driver"},
    {"name": "Premananda", "phone": "9876500002", "password": "driver123", "role": "driver"},
    {"name": "Ningthoujam", "phone": "9876500003", "password": "rider123", "role": "rider"},
]


def seed() -> None:
    db = SessionLocal()
    try:
        if db.query(models.User).count() > 0:
            return
        for user in DEMO_USERS:
            db.add(
                models.User(
                    name=user["name"],
                    phone=user["phone"],
                    password_hash=security.hash_password(user["password"]),
                    role=user["role"],
                )
            )
        db.commit()
    finally:
        db.close()