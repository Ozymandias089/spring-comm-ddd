#!/usr/bin/env bash
set -euo pipefail

# 공통 함수 로드 (scctl/lib.sh 구조 재사용)
source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib.sh"

PROFILE_URL="https://example-cdn.local/images/demo.png"

usage() {
  cat <<EOF
Usage: scctl run change-profile-image [--url <profileImageUrl>]

Description:
  PATCH /api/my-page/profile-image
  인증된 사용자의 프로필 이미지 URL을 변경하고, 갱신된 프로필 JSON을 반환합니다.

Options:
  --url <profileImageUrl>  새 프로필 이미지 URL (기본: ${PROFILE_URL})
  -h, --help               도움말

Examples:
  scctl run change-profile-image --url "https://cdn.example.com/u/123/profile.jpg"
  scctl --env stage run change-profile-image --url "s3://bucket/key.jpg"
EOF
}

# 옵션 파싱
while [[ $# -gt 0 ]]; do
  case "$1" in
    --url) PROFILE_URL="$2"; shift 2;;
    -h|--help) usage; exit 0;;
    *) echo "Unknown arg: $1"; usage; exit 1;;
  esac
done

# 간단한 유효성 체크 (비어있지만 않으면 통과)
if [[ -z "${PROFILE_URL}" ]]; then
  echo "❌ --url 은 비어있을 수 없습니다"; exit 1
fi

# 1) 세션 없으면 로그인
if ! grep -q 'SESSION' "${COOKIES_PATH}" 2>/dev/null; then
  sc_login
fi

# 2) CSRF 마스킹 토큰 확보
sc_csrf

# 3) PATCH 호출
log "PATCH /api/my-page/profile-image → ${PROFILE_URL}"
BODY_JSON=$(jq -nc --arg u "${PROFILE_URL}" '{profileImageUrl:$u}')

OUT=$(sc_patch_json "/api/my-page/profile-image" "${BODY_JSON}") || {
  echo "❌ 실패 (세부는 ${OUT} 참고)"; cat "${OUT}"; exit 1;
}

# 4) 결과 출력
HTTP_CODE=$(head -n1 "${OUT}" | awk '{print $2}')
BODY=$(tail -n1 "${OUT}")

if [[ "${HTTP_CODE}" == "200" ]]; then
  echo "✅ 프로필 이미지 변경 성공 (HTTP ${HTTP_CODE})"
  echo "${BODY}" | jq . 2>/dev/null || echo "${BODY}"
else
  echo "⚠️ 응답 코드 ${HTTP_CODE} (확인 필요)"
  echo "${BODY}" | jq . 2>/dev/null || echo "${BODY}"
  exit 1
fi
