#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
docker compose -f docker/docker-compose.yml up -d redis postgres
mvn -q spring-boot:run -Dspring-boot.run.profiles=dev
