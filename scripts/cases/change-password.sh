#!/usr/bin/env bash
set -euo pipefail

# 공통 함수 로드
source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib.sh"

CURRENT_PW=""
NEW_PW=""

usage() {
  cat <<EOF
Usage: scctl run change-password --current <currentPassword> --new <newPassword>

Description:
  PATCH /api/my-page/password 를 호출하여 비밀번호를 변경합니다.
  응답이 204 No Content 이어야 성공으로 간주합니다.

Options:
  --current <pw>   현재 비밀번호 (필수)
  --new <pw>       새 비밀번호 (필수, 8~128자)
  -h, --help       도움말

Examples:
  scctl run change-password --current "StrongPassword4231!" --new "EvenStronger!2025"
  scctl --env stage --cookies ./.session.stage.cookies --keep-cookies \\
        run change-password --current "old" --new "new-secure-pass"
EOF
}

# 옵션 파싱
while [[ $# -gt 0 ]]; do
  case "$1" in
    --current) CURRENT_PW="$2"; shift 2;;
    --new) NEW_PW="$2"; shift 2;;
    -h|--help) usage; exit 0;;
    *) echo "Unknown arg: $1"; usage; exit 1;;
  esac
done

# 간단한 검증
[[ -n "${CURRENT_PW}" ]] || { echo "❌ --current 는 필수입니다"; exit 1; }
[[ -n "${NEW_PW}" ]] || { echo "❌ --new 는 필수입니다"; exit 1; }
LEN=${#NEW_PW}
(( LEN >= 8 && LEN <= 128 )) || { echo "❌ --new 는 8~128자여야 합니다"; exit 1; }

# 1) 세션 없으면 로그인
if ! grep -q 'SESSION' "${COOKIES_PATH}" 2>/dev/null; then
  sc_login
fi

# 2) CSRF 마스킹 토큰 확보
sc_csrf

# 3) PATCH /api/my-page/password 호출 (204 기대)
PATH_/api="/api/my-page/password"
BODY_JSON=$(jq -nc --arg c "${CURRENT_PW}" --arg n "${NEW_PW}" '{currentPassword:$c, newPassword:$n}')

log "PATCH ${PATH_/api} (expect 204)"
OUT="${ARTIFACTS_DIR}/patch_password_$(date +%s).txt"
curl -s -i -b "${COOKIES_PATH}" \
  -X PATCH \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: ${XSRF_TOKEN_MASKED}" \
  -d "${BODY_JSON}" \
  "${BASE_URL}${PATH_/api}" | tee "${OUT}" >/dev/null

HTTP_CODE=$(head -n1 "${OUT}" | awk '{print $2}')

if [[ "${HTTP_CODE}" == "204" ]]; then
  echo "✅ 비밀번호 변경 성공 (HTTP 204)"

  # dev.env 파일 갱신 (비밀번호 반영)
  ENV_FILE="env/dev.env"
  if [[ -f "${ENV_FILE}" ]]; then
    echo "🧩 dev.env 갱신 중..."
    sed -i.bak "s|^PASSWORD=.*|PASSWORD=\"${NEW_PW}\"|" "${ENV_FILE}"
    echo "✅ dev.env 업데이트 완료 (${ENV_FILE})"
  else
    echo "⚠️ env/dev.env 파일이 없어 비밀번호를 반영하지 못했습니다."
  fi
else
  echo "❌ 비밀번호 변경 실패 (HTTP ${HTTP_CODE})"
  exit 1
fi
