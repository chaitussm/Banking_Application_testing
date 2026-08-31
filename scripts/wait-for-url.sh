#!/usr/bin/env bash
# Wait until a URL returns HTTP 2xx/3xx.
set -euo pipefail

URL="${1:?Usage: wait-for-url.sh <url> [timeout_seconds]}"
TIMEOUT="${2:-90}"
START="$(date +%s)"

echo "Waiting for ${URL} (timeout ${TIMEOUT}s)..."
while true; do
  if curl -fsS --max-time 3 "${URL}" >/dev/null 2>&1; then
    echo "Ready: ${URL}"
    exit 0
  fi
  NOW="$(date +%s)"
  if (( NOW - START >= TIMEOUT )); then
    echo "Timed out waiting for ${URL}" >&2
    exit 1
  fi
  sleep 2
done
