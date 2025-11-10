# shellcheck shell=bash

die() { echo "❌ $*" >&2; exit 1; }
log() { echo "🔹 $*"; }
vlog() { [[ "${VERBOSE:-false}" == true ]] && echo "  $*"; }

require() {
  command -v "$1" >/dev/null 2>&1 || die "명령어 필요: $1"
}

# 로그인 (세션 생성)
sc_login() {
  require curl jq
  log "로그인..."
  local body="{\"email\":\"${EMAIL}\",\"password\":\"${PASSWORD}\"}"
  local out="${ARTIFACTS_DIR}/login_$(date +%s).txt"

  curl -s -i -c "${COOKIES_PATH}" \
    -H "Content-Type: application/json" \
    -d "${body}" \
    "${BASE_URL}/api/login" | tee "${out}" >/dev/null

  local code; code=$(head -n1 "${out}" | awk '{print $2}')
  [[ "${code}" == "200" ]] || { cat "${out}"; die "로그인 실패 (HTTP ${code})"; }
  vlog "SESSION saved to ${COOKIES_PATH}"
  echo "✅ 로그인 OK"
}

# CSRF 마스킹 토큰 발급
sc_csrf() {
  log "CSRF 토큰 발급..."

  local url="${BASE_URL}/api/csrf"
  local out="${ARTIFACTS_DIR}/csrf_$(date +%s).txt"

  curl -S --fail --show-error \
       --connect-timeout 2 --max-time 10 \
       -s -i \
       -b "${COOKIES_PATH}" -c "${COOKIES_PATH}" \
       -H "Accept: application/json" \
       "${url}" | tee "${out}" >/dev/null || {
    echo "❌ CSRF 요청 실패: ${url}"
    echo "⤷ 원문: ${out}"
    exit 1
  }

  local http_code
  http_code="$(head -n1 "${out}" | awk '{print $2}')"
  if [[ "${http_code}" != "200" ]]; then
    echo "❌ CSRF 응답 코드 비정상: ${http_code}"
    tail -n +1 "${out}"
    exit 1
  fi

  local body; body="$(tail -n1 "${out}")"

  local masked
  masked="$(echo "${body}" | jq -r '.token' 2>/dev/null || true)"
  if [[ -z "${masked}" || "${masked}" == "null" ]]; then
    echo "❌ CSRF 마스킹 토큰(JSON .token) 추출 실패"
    echo "⤷ 바디: ${body}"
    exit 1
  fi

  local cookie_raw
  cookie_raw="$(grep -i '^Set-Cookie: XSRF-TOKEN=' "${out}" | head -n1 | sed -E 's/^Set-Cookie: XSRF-TOKEN=([^;]+).*/\1/i')"
  if [[ -z "${cookie_raw}" ]]; then
    cookie_raw="$(awk '$6=="/" && $7=="XSRF-TOKEN" {print $8}' "${COOKIES_PATH}" 2>/dev/null | tail -n1)"
  fi

  export XSRF_TOKEN_MASKED="${masked}"
  export XSRF_TOKEN_COOKIE="${cookie_raw}"

  [[ "${VERBOSE:-false}" == true ]] && {
    echo "✅ 마스킹 토큰(header용): ${XSRF_TOKEN_MASKED}"
    echo "✅ 쿠키 토큰(cookie용):   ${XSRF_TOKEN_COOKIE:-<none>}"
  }
}

# 로그아웃
sc_logout() {
  require curl
  log "로그아웃..."
  local out="${ARTIFACTS_DIR}/logout_$(date +%s).txt"
  curl -s -i -b "${COOKIES_PATH}" \
    -X POST \
    -H "X-XSRF-TOKEN: ${XSRF_TOKEN_MASKED:-}" \
    "${BASE_URL}/api/logout" | tee "${out}" >/dev/null
  local code; code=$(head -n1 "${out}" | awk '{print $2}')
  [[ "${code}" == "204" ]] || { cat "${out}"; die "로그아웃 실패 (HTTP ${code}). (CSRF/세션 확인)"; }
  echo "✅ 로그아웃 OK"
}

# JSON PATCH 요청 헬퍼
sc_patch_json() {
  # $1 = path, $2 = json body
  local path="$1"
  local body="$2"
  require curl jq
  [[ -n "${XSRF_TOKEN_MASKED:-}" ]] || sc_csrf

  local out="${ARTIFACTS_DIR}/patch_$(basename "${path//\//_}")_$(date +%s).txt"
  curl -s -i -b "${COOKIES_PATH}" \
    -X PATCH \
    -H "Content-Type: application/json" \
    -H "X-XSRF-TOKEN: ${XSRF_TOKEN_MASKED}" \
    -d "${body}" \
    "${BASE_URL}${path}" | tee "${out}" >/dev/null

  local code; code=$(head -n1 "${out}" | awk '{print $2}')
  echo "${out}" # 반환: 아티팩트 파일 경로
  return $([[ "${code}" == "200" ]] && echo 0 || echo 1)
}
