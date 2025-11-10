#!/usr/bin/env bash
set -euo pipefail

# ------------------ Defaults (CLI로 덮어쓰기 가능) ------------------
BASE_URL="${BASE_URL:-http://localhost:8080}"
EMAIL="${EMAIL:-demo@demoapi.com}"
PASSWORD="${PASSWORD:-StrongPassword4231!}"
NEW_DISPLAY_NAME="${NEW_DISPLAY_NAME:-new demo username}"
COOKIE_JAR="${COOKIE_JAR:-cookies.txt}"
DO_LOGOUT=false

# ------------------ Args ------------------
usage() {
  cat <<EOF
Usage: $(basename "$0") [--base <url>] [--email <email>] [--password <pw>] [--name <displayName>] [--logout]

Examples:
  $(basename "$0")
  $(basename "$0") --name "my cool name"
  BASE_URL=http://localhost:9000 $(basename "$0") --logout
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --base) BASE_URL="$2"; shift 2;;
    --email) EMAIL="$2"; shift 2;;
    --password) PASSWORD="$2"; shift 2;;
    --name) NEW_DISPLAY_NAME="$2"; shift 2;;
    --logout) DO_LOGOUT=true; shift;;
    -h|--help) usage; exit 0;;
    *) echo "Unknown arg: $1"; usage; exit 1;;
  esac
done

echo "🔧 Config"
echo "  BASE_URL        = ${BASE_URL}"
echo "  EMAIL           = ${EMAIL}"
echo "  NEW_DISPLAY_NAME= ${NEW_DISPLAY_NAME}"
echo "  COOKIE_JAR      = ${COOKIE_JAR}"
echo

# ------------------ 1) Login ------------------
echo "🔹 Step 1) 로그인"
curl -s -i -c "$COOKIE_JAR" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${EMAIL}\",\"password\":\"${PASSWORD}\"}" \
  "${BASE_URL}/api/login" | tee login_response.txt > /dev/null

HTTP_CODE=$(head -n1 login_response.txt | awk '{print $2}')
if [[ "$HTTP_CODE" != "200" ]]; then
  echo "❌ 로그인 실패 (HTTP $HTTP_CODE)"; cat login_response.txt; exit 1
fi
echo "✅ 로그인 성공. SESSION 저장 → $COOKIE_JAR"
echo

# ------------------ 2) CSRF ------------------
echo "🔹 Step 2) CSRF 토큰 발급"
MASKED=$(curl -s -i -b "$COOKIE_JAR" -c "$COOKIE_JAR" "${BASE_URL}/api/csrf" \
        | tail -n1 | jq -r '.token')
if [[ -z "$MASKED" || "$MASKED" == "null" ]]; then
  echo "❌ 마스킹 토큰 추출 실패"; exit 1
fi
RAW_COOKIE_TOKEN=$(grep -m1 'XSRF-TOKEN' "$COOKIE_JAR" | awk '{print $7}')
echo "  header(X-XSRF-TOKEN) = $MASKED"
echo "  cookie(XSRF-TOKEN)   = $RAW_COOKIE_TOKEN"
echo "✅ CSRF 준비 완료"
echo

# ------------------ 3) PATCH display-name ------------------
echo "🔹 Step 3) PATCH /api/my-page/display-name"
curl -s -i -b "$COOKIE_JAR" \
  -X PATCH \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: ${MASKED}" \
  -d "{\"displayName\":\"${NEW_DISPLAY_NAME}\"}" \
  "${BASE_URL}/api/my-page/display-name" | tee patch_response.txt > /dev/null

HTTP_CODE=$(head -n1 patch_response.txt | awk '{print $2}')
if [[ "$HTTP_CODE" != "200" ]]; then
  echo "❌ PATCH 실패 (HTTP $HTTP_CODE)"; cat patch_response.txt; exit 1
fi
echo "✅ PATCH 성공"
echo

# ------------------ 4) GET /api/my-page (검증) ------------------
echo "🔹 Step 4) 변경사항 검증 (GET /api/my-page)"
curl -s -i -b "$COOKIE_JAR" \
  -H "Accept: application/json" \
  "${BASE_URL}/api/my-page" | tee get_mypage.txt > /dev/null
HTTP_CODE=$(head -n1 get_mypage.txt | awk '{print $2}')
if [[ "$HTTP_CODE" != "200" ]]; then
  echo "❌ 검증 실패 (HTTP $HTTP_CODE)"; cat get_mypage.txt; exit 1
fi
echo "✅ 검증 성공. 응답 표시명:"
tail -n1 get_mypage.txt | jq -r '.displayName'
echo

# ------------------ 5) (옵션) 로그아웃 ------------------
if $DO_LOGOUT; then
  echo "🔹 Step 5) 로그아웃"
  curl -s -i -b "$COOKIE_JAR" \
    -X POST \
    -H "X-XSRF-TOKEN: ${MASKED}" \
    "${BASE_URL}/api/logout" | tee logout_response.txt > /dev/null
  HTTP_CODE=$(head -n1 logout_response.txt | awk '{print $2}')
  if [[ "$HTTP_CODE" != "204" ]]; then
    echo "❌ 로그아웃 실패 (HTTP $HTTP_CODE)"; cat logout_response.txt; exit 1
  fi
  echo "✅ 로그아웃 성공"
fi

echo
echo "🟢 All done!"
