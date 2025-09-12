#!/usr/bin/env bash
set -euo pipefail

APP_JAR="${APP_JAR:-build/libs/SanSam-0.0.1-SNAPSHOT.jar}"
SERVICE_NAME="${SERVICE_NAME:-SanSam}"
ENVIRONMENT="${ENVIRONMENT:-local}"
OTLP_ENDPOINT="${OTLP_ENDPOINT:-http://localhost:4317}"
AGENT_JAR="${AGENT_JAR:-./opentelemetry-javaagent.jar}"

if [ ! -f "$AGENT_JAR" ]; then
  echo "[info] OpenTelemetry javaagent 다운로드 중..."
  curl -L -o "$AGENT_JAR" \
    "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar"
fi

echo "[info] Using agent: $AGENT_JAR"
echo "[info] Service: $SERVICE_NAME, Env: $ENVIRONMENT"
echo "[info] OTLP:    $OTLP_ENDPOINT"
echo "[info] App JAR: $APP_JAR"

exec java \
 -javaagent:"$AGENT_JAR" \
 -Dotel.service.name="$SERVICE_NAME" \
 -Dotel.resource.attributes="deployment.environment=$ENVIRONMENT" \
 -Dotel.exporter.otlp.endpoint="$OTLP_ENDPOINT" \
 -Dotel.exporter.otlp.protocol=grpc \
 -Dotel.traces.exporter=otlp \
 -Dotel.metrics.exporter=none \
 -Dotel.logs.exporter=none \
 -Dotel.traces.sampler=always_on \
 -Dotel.propagators=tracecontext,baggage,b3multi \
 -Dotel.instrumentation.hikari.enabled=true \
 -Dotel.instrumentation.jdbc.datasource.enabled=true \
 -jar "$APP_JAR"
