from functools import lru_cache
from typing import Any

from pydantic import field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    app_name: str = "Kangla Auto"
    app_env: str = "development"
    app_version: str = "0.1.0"

    secret_key: str = "change-me-in-production"
    access_token_expire_minutes: int = 1440

    database_url: str = "sqlite:///./data/app.db"
    auto_create_tables: bool = True

    log_level: str = "INFO"
    seed_on_startup: bool = True

    cors_origins: str = "http://localhost:5173,http://127.0.0.1:5173"
    request_timeout_seconds: int = 60

    @property
    def cors_origin_list(self) -> list[str]:
        return [o.strip() for o in self.cors_origins.split(",") if o.strip()] or ["*"]

    @field_validator("secret_key")
    @classmethod
    def secret_key_not_default(cls, v: str) -> str:
        if v == "change-me-in-production":
            return v
        if len(v) < 32:
            raise ValueError("SECRET_KEY must be at least 32 characters in production")
        return v

    @field_validator("app_env")
    @classmethod
    def valid_env(cls, v: str) -> str:
        if v not in {"development", "staging", "production"}:
            raise ValueError("APP_ENV must be one of development, staging, production")
        return v


@lru_cache
def get_settings() -> Settings:
    return Settings()


settings = get_settings()