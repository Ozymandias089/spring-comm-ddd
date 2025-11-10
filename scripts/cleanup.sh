#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ARTIFACTS_DIR="${ROOT_DIR}/artifacts"

DAYS="${1:-7}" # 며칠 지난 파일 삭제 (기본 7일)

echo "🧹 cleaning artifacts older than ${DAYS} days in ${ARTIFACTS_DIR}"
find "${ARTIFACTS_DIR}" -type f -mtime +"${DAYS}" -print -delete 2>/dev/null || true

echo "🧹 cleaning stray cookie jars (*.cookies) older than ${DAYS} days"
find "${ROOT_DIR}" -name "*.cookies" -type f -mtime +"${DAYS}" -print -delete 2>/dev/null || true

echo "✅ cleanup done"
