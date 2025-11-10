#!/usr/bin/env bash
set -euo pipefail
source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib.sh"

NEW_NAME="new demo username"

# 옵션 파싱
while [[ $# -gt 0 ]]; do
  case "$1" in
    --name) NEW_NAME="$2"; shift 2;;
    -h|--help) echo "Usage: scctl run patch-display-name --name <displayName>"; exit 0;;
    *) echo "Unknown arg: $1"; exit 1;;
  esac
done

# 1) 로그인(세션 없으면)
if ! grep -q 'SESSION' "${COOKIES_PATH}" 2>/dev/null; then
  sc_login
fi

# 2) CSRF (마스킹 토큰 확보)
sc_csrf

# 3) PATCH 호출
log "PATCH /api/my-page/display-name → ${NEW_NAME}"
OUT=$(sc_patch_json "/api/my-page/display-name" "{\"displayName\":\"${NEW_NAME}\"}") || {
  echo "❌ 실패 (세부는 ${OUT} 참고)"; cat "${OUT}"; exit 1;
}

# 4) 결과 요약
HTTP_CODE=$(head -n1 "${OUT}" | awk '{print $2}')
BODY=$(tail -n1 "${OUT}")
echo "✅ HTTP ${HTTP_CODE}"
echo "${BODY}" | jq . || echo "${BODY}"
