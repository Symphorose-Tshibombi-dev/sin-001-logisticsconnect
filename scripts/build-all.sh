#!/usr/bin/env sh
set -eu
for service in ingestion-service hub-service delay-stage-service transit-service; do
  echo "== Building $service =="
  (cd "$service" && mvn test package)
done
