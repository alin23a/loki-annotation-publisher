#!/usr/bin/env bash
# publish-annotation.sh
# Publish a Grafana annotation to Loki without the frontend.
#
# Positional args:
#   $1  date       e.g. 2024-01-15
#   $2  version    e.g. 1.2.3
#   $3  comment    e.g. "Deploy to prod"
#   $4  instances  comma-separated 3-digit numbers, e.g. "422,423,425"
#
# Environment variable overrides:
#   LOKI_URL       (default: http://localhost:3100)
#   LOKI_APP_NAME  (default: grafana-annotation-publisher)
#   LOKI_ENV       (default: local)
#   JAR_PATH       path to the fat JAR (default: auto-detected)
#
# Examples:
#   ./publish-annotation.sh 2024-01-15 1.2.3 "Deploy to prod" "422,423,425"
#   LOKI_URL=http://loki.internal:3100 ./publish-annotation.sh 2024-01-15 1.2.3 "Hotfix" "422"

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEFAULT_JAR="${SCRIPT_DIR}/annotation-publisher/target/annotation-publisher-0.0.1-SNAPSHOT.jar"
JAR="${JAR_PATH:-${DEFAULT_JAR}}"

if [[ ! -f "$JAR" ]]; then
  echo "[ERROR] JAR not found at: $JAR"
  echo "        Build it first:  cd annotation-publisher && mvn package -DskipTests"
  exit 1
fi

DATE="${1:-}"
VERSION="${2:-}"
COMMENT="${3:-}"
INSTANCES="${4:-}"

if [[ -z "$DATE" || -z "$VERSION" || -z "$COMMENT" || -z "$INSTANCES" ]]; then
  echo "Usage: $0 <date> <version> <comment> <instances>"
  echo ""
  echo "  date       YYYY-MM-DD                      e.g. 2024-01-15"
  echo "  version    semver                          e.g. 1.2.3"
  echo "  comment    free-text annotation comment"
  echo "  instances  comma-separated 3-digit numbers e.g. \"422 423 425\""
  echo ""
  echo "Env vars: LOKI_URL, LOKI_APP_NAME, LOKI_ENV, JAR_PATH"
  exit 1
fi

CMD=(
  java -cp "$JAR"
  com.loki.annotationpublisher.AnnotationPublisherCli
  --date      "$DATE"
  --version   "$VERSION"
  --comment   "$COMMENT"
  --instances "$INSTANCES"
  --loki-url "${LOKI_URL:-http://localhost:3100}"
  --app-name "${LOKI_APP_NAME:-grafana-annotation-publisher}"
  --env      "${LOKI_ENV:-local}"
)

"${CMD[@]}"
