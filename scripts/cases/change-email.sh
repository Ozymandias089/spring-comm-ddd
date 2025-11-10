#!/usr/bin/env bash
set -euo pipefail

# 공통 함수 로드
source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib.sh"

NEW_EMAIL="new-email@example.com"

usage() {
  cat <<EOF
Usage: scctl run change-email [--email <address>]

Description:
  /api/my-page/email 엔드포인트를 테스트합니다.
  인증된 사용자의 이메일을 새 값으로 변경하는 PATCH 요청을 수행합니다.

Options:
  --email <address>   변경할 새 이메일 주소 (기본: ${NEW_EMAIL})
  -h, --help          도움말

Examples:
  scctl run change-email --email "user+1@example.com"
  scctl --env stage run change-email --email "stage@example.com"
EOF
}

# 옵션 파싱
while [[ $# -gt 0 ]]; do
  case "$1" in
    --email) NEW_EMAIL="$2"; shift 2;;
    -h|--help) usage; exit 0;;
    *) echo "Unknown arg: $1"; usage; exit 1;;
  esac
done

# 1️⃣ 세션 없으면 로그인
if ! grep -q 'SESSION' "${COOKIES_PATH}" 2>/dev/null; then
  sc_login
fi

# 2️⃣ CSRF 마스킹 토큰 확보
sc_csrf

# 3️⃣ PATCH /api/my-page/email 호출
log "PATCH /api/my-page/email → ${NEW_EMAIL}"
BODY_JSON="{\"email\":\"${NEW_EMAIL}\"}"

OUT=$(sc_patch_json "/api/my-page/email" "${BODY_JSON}") || {
  echo "❌ 실패 (세부 내용은 ${OUT} 파일 참고)"
  cat "${OUT}"
  exit 1
}

# 4️⃣ 결과 출력
HTTP_CODE=$(head -n1 "${OUT}" | awk '{print $2}')
BODY=$(tail -n1 "${OUT}")

if [[ "${HTTP_CODE}" == "200" ]]; then
  echo "✅ 이메일 변경 성공 (HTTP 200)"
  echo "${BODY}" | jq . 2>/dev/null || echo "${BODY}"

  # dev.env 갱신
  ENV_FILE="env/dev.env"
  if [[ -f "${ENV_FILE}" ]]; then
    echo "🧩 dev.env 갱신 중..."
    sed -i.bak "s|^EMAIL=.*|EMAIL=\"${NEW_EMAIL}\"|" "${ENV_FILE}"
    echo "✅ dev.env 업데이트 완료 (${ENV_FILE})"
  else
    echo "⚠️ env/dev.env 파일이 없어 이메일을 반영하지 못했습니다."
  fi
else
  echo "⚠️ 응답 코드 ${HTTP_CODE}"
  exit 1
fi

# 5️⃣ (선택) 변경된 프로필 확인
# 필요 시 주석 해제하여 확인 가능
 log "GET /api/my-page (변경 결과 확인)"
 GET_OUT="${ARTIFACTS_DIR}/get_mypage_after_change_email_$(date +%s).txt"
 curl -s -i -b "${COOKIES_PATH}" -H "Accept: application/json" \
   "${BASE_URL}/api/my-page" | tee "${GET_OUT}" >/dev/null
 tail -n1 "${GET_OUT}" | jq . || true
