#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [ -d "/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home" ]; then
  export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
elif [ -d "/usr/local/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home" ]; then
  export JAVA_HOME="/usr/local/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
fi

if [ -n "${JAVA_HOME:-}" ]; then
  export PATH="$JAVA_HOME/bin:$PATH"
  echo "Using Java: $(java -version 2>&1 | head -1)"
fi

if docker compose ps --status running --services 2>/dev/null | grep -qx 'job-service'; then
  echo "Stopping Docker job-service to free port 8082..."
  docker compose stop job-service >/dev/null
fi

if lsof -nP -iTCP:8082 -sTCP:LISTEN >/dev/null 2>&1; then
  echo "Port 8082 is in use. Stopping the existing process..."
  lsof -ti :8082 | xargs kill -9
  sleep 1
fi

echo "Building common modules and job-service..."
mvn -pl services/job-service -am install -Dmaven.test.skip=true -q

echo "Starting job-service on http://localhost:8082 (profile: local)"
echo "Ensure Postgres and Kafka are running: docker compose up -d postgres kafka"
mvn -pl services/job-service spring-boot:run -Dspring-boot.run.profiles=local -Dmaven.test.skip=true
