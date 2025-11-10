# SpringComm API 테스트 스크립트 사용법

`scctl`은 로그인/CSRF/쿠키 처리까지 자동으로 해주는 **통합 실행기**입니다.
각 엔드포인트 테스트는 `scripts/cases/*.sh`로 모듈화되어 있고, 공통 로직은 `scripts/lib.sh`가 담당합니다.

## 폴더 구조

```
scripts/
  scctl                # 메인 실행기 (subcommand)
  lib.sh               # 공통 함수 (로그인, CSRF, 요청, 로깅 등)
  cleanup.sh           # 오래된 아티팩트/쿠키 정리
  env/
    dev.env            # 환경 변수 (BASE_URL, EMAIL, PASSWORD)
    stage.env
    prod.env
  cases/
    patch-display-name.sh  # /api/my-page/display-name
artifacts/             # 각 호출의 응답 원문 저장 (디버깅용)
```

> Git에서는 `artifacts/`, `*.cookies`, `*.log` 등은 `.gitignore`에 추가되어 있어야 합니다.

---

## 사전 준비

* macOS/Linux의 bash 환경
* `curl`, `jq` 설치
* `scripts/env/dev.env`에 기본 값 세팅:

  ```bash
  # scripts/env/dev.env
  BASE_URL=http://localhost:8080
  EMAIL=demo@demoapi.com
  PASSWORD=StrongPassword4231!
  ```

---

## 빠른 시작

```bash
# 실행 권한 부여 (최초 1회)
chmod +x scripts/scctl scripts/cleanup.sh scripts/cases/*.sh

# 표시명 변경 테스트 (기본값 사용)
scripts/scctl run patch-display-name --name "new demo username"
```

성공 시:

* 자동으로 로그인 → CSRF 토큰 발급 → PATCH 요청 수행
* 결과 원문이 `artifacts/`에 저장됩니다.

---

## 명령어 개요

```bash
scripts/scctl [global options] <command> [command options]
```

### Global options

* `--env <dev|stage|prod>`: 환경 선택 (기본: `dev`)
* `--cookies <path>`: 쿠키 파일 경로 지정 (지정 없으면 임시 파일)
* `--keep-cookies`: 실행 후 쿠키 유지(임시 파일 자동 삭제 비활성화)
* `-v|--verbose`: 상세 로그 출력
* `-h|--help`: 도움말

### Commands

* `login` : 세션 생성(로그인)
* `csrf` : CSRF 마스킹 토큰 발급
* `logout` : 현재 세션 로그아웃
* `patch-display-name` : `/api/my-page/display-name` 테스트 (별칭)
* `run <case> [args...]` : `scripts/cases/<case>.sh` 직접 실행

---

## 사용 예시

### 1) 가장 간단한 실행

```bash
scripts/scctl run patch-display-name --name "hello world"
```

### 2) 환경/세션 유지하면서 실행

```bash
# stage 환경 + 쿠키 보존
scripts/scctl --env stage --cookies ./.session.stage.cookies --keep-cookies \
  run patch-display-name --name "stage user"
```

### 3) 로그인 → CSRF → 개별 케이스 실행 (수동)

```bash
scripts/scctl login
scripts/scctl csrf
scripts/scctl run patch-display-name --name "manual flow"
# 필요 시
scripts/scctl logout
```

### 4) 상세 로그 보기

```bash
scripts/scctl -v run patch-display-name --name "see details"
```

---

## 아티팩트와 쿠키 관리

* 기본값: `scctl`은 **임시 쿠키 파일**을 사용하고, 실행 종료 시 자동 삭제합니다.

* 세션을 유지하거나 여러 케이스를 연달아 실행하려면:

  ```bash
  scripts/scctl --cookies ./.session.dev.cookies --keep-cookies run patch-display-name --name "foo"
  ```

* 아티팩트(요청/응답 원문)는 `artifacts/`에 저장됩니다.
  디버깅/회고에 유용하니, 필요 없으면 정리 스크립트를 쓰세요.

### 주기적 정리

```bash
# 7일 지난 아티팩트/쿠키 삭제
scripts/cleanup.sh 7

# CI/크론에서도 사용 가능
```

---

## 새 케이스(엔드포인트) 추가 방법

1. `scripts/cases/`에 새 파일 추가 (예: `change-email.sh`)
2. 내부에서 공통 함수 사용:

   ```bash
   #!/usr/bin/env bash
   set -euo pipefail
   source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib.sh"

   NEW_EMAIL="new@example.com"
   while [[ $# -gt 0 ]]; do
     case "$1" in
       --email) NEW_EMAIL="$2"; shift 2;;
       -h|--help) echo "Usage: scctl run change-email --email <value>"; exit 0;;
       *) echo "Unknown arg: $1"; exit 1;;
     esac
   done

   # 1) 세션 없으면 로그인
   if ! grep -q 'SESSION' "${COOKIES_PATH}" 2>/dev/null; then
     sc_login
   end

   # 2) CSRF 토큰 준비
   sc_csrf

   # 3) PATCH 호출
   OUT=$(sc_patch_json "/api/my-page/email" "{\"email\":\"${NEW_EMAIL}\"}") || {
     echo "❌ 실패 (세부는 ${OUT} 참고)"; cat "${OUT}"; exit 1;
   }

   echo "✅ 완료"
   tail -n1 "${OUT}" | jq .
   ```
3. 실행:

   ```bash
   scripts/scctl run change-email --email "new@example.com"
   ```

---

## 문제 해결(FAQ)

### 403 Forbidden

* **대부분 CSRF/세션 불일치**입니다.
* `scctl`은 자동으로 CSRF 마스킹 토큰을 **헤더**로 보내고, 쿠키는 **자동 관리**합니다.
* 서버 로그에서 `org.springframework.security.web.csrf` DEBUG를 켜면 원인(미스매치/세션 없음)이 바로 찍힙니다.

### 500 Internal Server Error

* 비즈니스/도메인 예외가 전역 핸들러에서 500으로 포장된 경우가 흔합니다.
  개발 중에는 400/404/409/405/415 등으로 **명시 매핑**해두면 원인이 바로 보입니다.

### 쿠키가 계속 남아요

* 기본은 자동 삭제(임시 파일)입니다. `--keep-cookies`를 사용했거나 `--cookies`로 직접 경로를 지정한 경우 수동 삭제하거나 `scripts/cleanup.sh`로 정리하세요.

---

## 팁

* **환경 변수로 BASE_URL/EMAIL/PASSWORD 재정의** 가능:

  ```bash
  BASE_URL=http://localhost:9000 EMAIL=alice@example.com PASSWORD=secret \
    scripts/scctl run patch-display-name --name "alice"
  ```
* 서버를 재시작했다면 **세션/CSRF 토큰도 다시** 갱신해야 합니다. `scctl`은 로그인/CSRF를 자동으로 처리합니다.
* `artifacts/`의 원문 파일은 장애시 빠르게 원인을 재현/공유할 때 유용합니다.
