#!/usr/bin/env bash
set -euo pipefail

# 공통 함수/경로
BASE_CASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${BASE_CASE_DIR}/../lib.sh"

# 기본값(옵션으로 덮어쓰기 가능)
NEW_EMAIL=""
CURRENT_PW="${PASSWORD:-}"
NEW_PW=""
NEW_NAME="new demo username"
PROFILE_URL="https://cdn.example.com/users/demo/avatar.png"
BANNER_URL="https://cdn.example.com/users/demo/banner.jpg"

# 각 스텝 on/off
DO_EMAIL=true
DO_PASSWORD=true
DO_NAME=true
DO_PROFILE=true
DO_BANNER=true

usage() {
  cat <<EOF
Usage: scctl run suite-mypage [options]

Description:
  하나의 세션으로 순서대로 실행합니다.
    1) 로그인
    2) 이메일 변경 (옵션: --email)
    3) 비밀번호 변경 (옵션: --current, --new)
    4) 닉네임 변경 (옵션: --name)
    5) 프로필 이미지 변경 (옵션: --avatar)
    6) 배너 이미지 변경 (옵션: --banner)
    7) 최종 /api/my-page 조회

Options:
  --email <addr>        새 이메일 주소          (지정 없으면 스텝 건너뜀)
  --current <pw>        현재 비밀번호           (기본: env의 PASSWORD 값)
  --new <pw>            새 비밀번호             (지정 없으면 스텝 건너뜀)
  --name <display>      새 닉네임               (기본: "new demo username")
  --avatar <url>        새 프로필 이미지 URL    (기본: ${PROFILE_URL})
  --banner <url>        새 배너 이미지 URL      (기본: ${BANNER_URL})

  --skip-email          이메일 변경 스킵
  --skip-password       비밀번호 변경 스킵
  --skip-name           닉네임 변경 스킵
  --skip-profile        프로필 이미지 변경 스킵
  --skip-banner         배너 이미지 변경 스킵

  -h, --help            도움말

Examples:
  scctl run suite-mypage --email "user+demo@example.com" \\
                         --current "StrongPassword4231!" \\
                         --new "EvenStronger!2025" \\
                         --name "demo user v2" \\
                         --avatar "https://cdn.example.com/u/demo/avatar.png" \\
                         --banner "https://cdn.example.com/u/demo/banner.jpg"

  # stage 환경 + 세션 유지
  scctl --env stage --cookies ./.session.stage.cookies --keep-cookies \\
        run suite-mypage --email "stage.user@example.com" --current "old" --new "new#2025!"
EOF
}

# 옵션 파싱
while [[ $# -gt 0 ]]; do
  case "$1" in
    suite-mypage) shift ;;
    --email)   NEW_EMAIL="$2"; DO_EMAIL=true; shift 2;;
    --current) CURRENT_PW="$2"; shift 2;;
    --new)     NEW_PW="$2"; DO_PASSWORD=true; shift 2;;
    --name)    NEW_NAME="$2"; DO_NAME=true; shift 2;;
    --avatar)  PROFILE_URL="$2"; DO_PROFILE=true; shift 2;;
    --banner)  BANNER_URL="$2"; DO_BANNER=true; shift 2;;

    --skip-email)    DO_EMAIL=false; shift;;
    --skip-password) DO_PASSWORD=false; shift;;
    --skip-name)     DO_NAME=false; shift;;
    --skip-profile)  DO_PROFILE=false; shift;;
    --skip-banner)   DO_BANNER=false; shift;;

    -h|--help) usage; exit 0;;
    *) echo "Unknown arg: $1"; usage; exit 1;;
  esac
done

echo "🔧 Suite config"
echo "  BASE_URL   = ${BASE_URL}"
echo "  EMAIL(env) = ${EMAIL}"
echo "  DO: email=${DO_EMAIL} password=${DO_PASSWORD} name=${DO_NAME} profile=${DO_PROFILE} banner=${DO_BANNER}"
echo

# 0) 항상 새 세션으로 시작 (stale 쿠키 방지)
echo "🧽 reset session cookies"
rm -f "${COOKIES_PATH}" 2>/dev/null || true
sc_login


# Helper: env/dev.env 다시 로드
reload_env() {
  local env_file="${BASE_CASE_DIR}/../env/${ENV_NAME:-dev}.env"
  if [[ -f "${env_file}" ]]; then
    # shellcheck disable=SC1090
    source "${env_file}"
    export EMAIL PASSWORD
    [[ "${VERBOSE:-false}" == true ]] && echo "  (env reloaded: EMAIL=${EMAIL}, PASSWORD=****)"
  fi
}

# 1) 이메일 변경
if [[ "${DO_EMAIL}" == true && -n "${NEW_EMAIL}" ]]; then
  echo "🟦 Step 1) Change Email → ${NEW_EMAIL}"
  "${BASE_CASE_DIR}/change-email.sh" --email "${NEW_EMAIL}"

  # dev.env를 케이스 스크립트가 갱신했으므로 재적재 + 세션 재발급 권장
  reload_env
  sc_logout || true
  sc_login
  echo
else
  echo "⏭️  Skip email change"
  echo
fi

# 2) 비밀번호 변경
if [[ "${DO_PASSWORD}" == true && -n "${NEW_PW}" ]]; then
  echo "🟦 Step 2) Change Password"
  "${BASE_CASE_DIR}/change-password.sh" --current "${CURRENT_PW}" --new "${NEW_PW}"

  # dev.env 갱신 반영 후 재로그인
  reload_env
  sc_logout || true
  sc_login
  echo
else
  echo "⏭️  Skip password change"
  echo
fi

# 3) 닉네임 변경
if [[ "${DO_NAME}" == true ]]; then
  echo "🟦 Step 3) Change Display Name → ${NEW_NAME}"
  "${BASE_CASE_DIR}/patch-display-name.sh" --name "${NEW_NAME}"
  echo
else
  echo "⏭️  Skip display name"
  echo
fi

# 4) 프로필 이미지 변경
if [[ "${DO_PROFILE}" == true ]]; then
  echo "🟦 Step 4) Change Profile Image → ${PROFILE_URL}"
  "${BASE_CASE_DIR}/change-profile-image.sh" --url "${PROFILE_URL}"
  echo
else
  echo "⏭️  Skip profile image"
  echo
fi

# 5) 배너 이미지 변경
if [[ "${DO_BANNER}" == true ]]; then
  echo "🟦 Step 5) Change Banner Image → ${BANNER_URL}"
  "${BASE_CASE_DIR}/change-banner-image.sh" --url "${BANNER_URL}"
  echo
else
  echo "⏭️  Skip banner image"
  echo
fi

# 6) 최종 상태 확인
echo "🟩 Final) GET /api/my-page"
FINAL_OUT="${ARTIFACTS_DIR}/suite_mypage_final_$(date +%s).txt"
curl -s -i -b "${COOKIES_PATH}" \
  -H "Accept: application/json" \
  "${BASE_URL}/api/my-page" | tee "${FINAL_OUT}" >/dev/null
head -n1 "${FINAL_OUT}"
tail -n1 "${FINAL_OUT}" | jq . 2>/dev/null || tail -n1 "${FINAL_OUT}"

echo
echo "✅ Suite completed."
