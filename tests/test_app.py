import os

os.environ["DATABASE_URL"] = "sqlite:///./data/test.db"
os.environ["SEED_ON_STARTUP"] = "false"
os.environ["SECRET_KEY"] = "test-secret-key-that-is-long-enough-1234567890"

import pytest
from fastapi.testclient import TestClient

from app.database import Base, engine
from app.main import app


@pytest.fixture(scope="session", autouse=True)
def fresh_db():
    Base.metadata.drop_all(bind=engine)
    Base.metadata.create_all(bind=engine)
    yield


@pytest.fixture()
def client(fresh_db):
    with TestClient(app) as c:
        yield c


def register(client, name, phone, password, role):
    res = client.post(
        "/api/auth/register",
        json={"name": name, "phone": phone, "password": password, "role": role},
    )
    assert res.status_code == 200, res.text
    return res.json()


def auth_headers(token):
    return {"Authorization": f"Bearer {token}"}


def test_register_and_login(client):
    data = register(client, "Test Rider", "9999000001", "secret123", "rider")
    assert data["token"]
    assert data["user"]["role"] == "rider"

    res = client.post("/api/auth/login", json={"phone": "9999000001", "password": "secret123"})
    assert res.status_code == 200
    assert res.json()["user"]["name"] == "Test Rider"

    res = client.post("/api/auth/login", json={"phone": "9999000001", "password": "wrongpw"})
    assert res.status_code == 401


def test_duplicate_phone_rejected(client):
    register(client, "Dup", "9999000002", "secret123", "rider")
    res = client.post(
        "/api/auth/register",
        json={"name": "Dup2", "phone": "9999000002", "password": "secret123", "role": "rider"},
    )
    assert res.status_code == 409


def test_me_requires_auth(client):
    assert client.get("/api/auth/me").status_code == 401


def test_only_riders_can_book(client):
    driver = register(client, "Test Driver", "9999000010", "secret123", "driver")
    res = client.post(
        "/api/bookings",
        json={"pickup": "A", "dropoff": "B"},
        headers=auth_headers(driver["token"]),
    )
    assert res.status_code == 403


def test_booking_lifecycle(client):
    rider = register(client, "Life Rider", "9999000020", "secret123", "rider")
    driver = register(client, "Life Driver", "9999000021", "secret123", "driver")

    create = client.post(
        "/api/bookings",
        json={"pickup": "Kangla", "dropoff": "Bazar", "distance_km": 2.5},
        headers=auth_headers(rider["token"]),
    )
    assert create.status_code == 200
    booking = create.json()
    assert booking["status"] == "pending"
    assert booking["fare"] == 30 + 3 * 15  # rounds 2.5 up to 3 km

    pending = client.get(
        "/api/bookings/pending", headers=auth_headers(driver["token"])
    ).json()
    assert any(b["id"] == booking["id"] for b in pending)

    accept = client.patch(
        f"/api/bookings/{booking['id']}",
        json={"status": "accepted"},
        headers=auth_headers(driver["token"]),
    )
    assert accept.status_code == 200
    assert accept.json()["driver_id"] == driver["user"]["id"]

    cannot_double_accept = client.patch(
        f"/api/bookings/{booking['id']}",
        json={"status": "completed"},
        headers=auth_headers(rider["token"]),
    )
    assert cannot_double_accept.status_code == 403

    complete = client.patch(
        f"/api/bookings/{booking['id']}",
        json={"status": "completed"},
        headers=auth_headers(driver["token"]),
    )
    assert complete.status_code == 200
    assert complete.json()["status"] == "completed"

    mine = client.get("/api/bookings/my", headers=auth_headers(rider["token"])).json()
    assert any(b["id"] == booking["id"] and b["status"] == "completed" for b in mine)


def test_rider_can_cancel_own_booking(client):
    rider = register(client, "Cancel Rider", "9999000030", "secret123", "rider")
    created = client.post(
        "/api/bookings",
        json={"pickup": "X", "dropoff": "Y"},
        headers=auth_headers(rider["token"]),
    ).json()

    other = register(client, "Cancel Other", "9999000031", "secret123", "rider")
    blocked = client.patch(
        f"/api/bookings/{created['id']}",
        json={"status": "cancelled"},
        headers=auth_headers(other["token"]),
    )
    assert blocked.status_code == 403

    cancelled = client.patch(
        f"/api/bookings/{created['id']}",
        json={"status": "cancelled"},
        headers=auth_headers(rider["token"]),
    )
    assert cancelled.status_code == 200
    assert cancelled.json()["status"] == "cancelled"


def test_drivers_public_list(client):
    register(client, "Public Driver", "9999000040", "secret123", "driver")
    res = client.get("/api/drivers")
    assert res.status_code == 200
    names = [d["name"] for d in res.json()]
    assert "Public Driver" in names


def test_health(client):
    res = client.get("/health")
    assert res.status_code == 200
    body = res.json()
    assert body["status"] == "ok"
    assert body["database"] == "ok"