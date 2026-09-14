FROM python:3.12-slim AS builder

WORKDIR /src

COPY pyproject.toml README.md ./
COPY app ./app

RUN pip wheel --no-cache-dir --wheel-dir /wheels .

FROM python:3.12-slim AS runtime

WORKDIR /app

RUN adduser --disabled-password --gecos "" appuser

COPY --from=builder /wheels /wheels
COPY --chown=appuser:appuser . .

RUN pip install --no-cache-dir /wheels/* && rm -rf /wheels && mkdir -p data && chown appuser:appuser data

USER appuser
EXPOSE 8000

CMD ["gunicorn", "-c", "gunicorn.conf.py", "app.main:app"]