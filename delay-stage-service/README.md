# Delay Stage Service

## Overview

Tracks a delay stage for each hub. This service is part of LogisticsConnect Stage 2. It is an independent Maven module.

## Run

From this directory:

```bash
mvn test
mvn package
java -jar target/delay-stage-service.jar
```

The service listens on port `7052`.

## Endpoints

- `GET /health` returns `OK`.
- `GET /delay-stage/{hubId}` returns the normalized hub ID and current stage. A hub without an explicit stage defaults to stage `0`.
- `POST /delay-stage/{hubId}` updates the stage. Send a JSON body such as `{"stage": 3}`.

Valid stages are whole numbers from `0` through `8`. Invalid JSON, missing or non-integer stage values, and out-of-range values receive HTTP `400`.

Hub IDs are trimmed and normalized to uppercase for lookups. Stage values are currently stored in memory, so they reset when the service restarts. This is a prototype limitation, not durable persistence.
