# SpringComm API Specification

## 1. Overview
SpringComm는 Reddit과 유사한 커뮤니티형 소셜 서비스로, 헥사고날 아키텍처와 도메인 주도 설계를 기반으로 구현됩니다. 이 명세서는 애플리케이션 계층과 외부 어댑터(웹/API)가 협력하여 도메인 모델(`communities`, `posts`, `comments`, `votes`, `iam`)을 사용하는 방식을 정의합니다.

* **버전**: v0.1 (Draft)
* **기반 아키텍처**: Hexagonal (Ports & Adapters), DDD
* **전송 포맷**: JSON (UTF-8)
* **인증**: 서버 유지 세션 (Secure HttpOnly 쿠키 기반) – `iam` 서브도메인에서 발급/검증
* **에러 처리**: RFC 7807 Problem Details 형태의 표준 에러 응답

## 2. 공통 규칙

| 항목 | 설명 |
| --- | --- |
| Base URL | `/api/v1` |
| Content-Type | `application/json` |
| 시간표현 | ISO-8601 (UTC) |
| ID 표기 | UUID 문자열 (`PostId`, `CommunityId`, `CommentId`, 등) |
| 페이징 | `page`, `size`, `sort` 쿼리 파라미터 (기본: `size=20`) |
| 필터 | 필요 시 복수 쿼리 파라미터 사용 |
| 응답 Envelope | 단건은 도메인 DTO, 컬렉션은 `{ "content": [...], "page": {...} }` |

### 2.1 표준 에러 포맷
```json
{
  "type": "https://api.springcomm.app/errors/{error-code}",
  "title": "Readable error title",
  "status": 400,
  "detail": "추가 메시지",
  "instance": "/api/v1/..."
}
```

## 3. 인증 & IAM 도메인
`iam` 도메인은 사용자, 인증 자격, 권한을 다룹니다. 사용자 식별은 `userId` (UUID) 기반이며, 서버가 발급한 세션 쿠키에 매핑된 서버측 세션 컨텍스트로 확인됩니다. 모든 인증 필요한 요청은 `Cookie: SESSION=<session-id>`와 CSRF 보호를 위한 `X-CSRF-TOKEN` 헤더(상태 변경 시)를 포함해야 합니다.

### 3.1 회원 가입
| 항목 | 값 |
| --- | --- |
| Method | `POST` |
| Path | `/api/v1/iam/users` |
| Body |
```json
{
  "username": "string",
  "email": "string",
  "password": "string"
}
```
| Response |
```json
{
  "userId": "uuid",
  "username": "string",
  "email": "string",
  "createdAt": "datetime"
}
```
| Errors | `409 Conflict` (username/email 중복), `422 Unprocessable Entity` (검증 실패) |

### 3.2 로그인 / 세션 발급
| 항목 | 값 |
| --- | --- |
| Method | `POST` |
| Path | `/api/v1/iam/sessions` |
| Body |
```json
{
  "username": "string",
  "password": "string"
}
```
| Response |
```
Status: 204 No Content
Set-Cookie: SESSION=<session-id>; HttpOnly; Secure; SameSite=Lax; Path=/
Set-Cookie: CSRF-TOKEN=<token>; Secure; SameSite=Lax; Path=/
```
| Notes | 로그인 성공 시 본문 없이 세션/CSRF 쿠키가 내려가며, 실패 시 `401 Unauthorized` |

### 3.3 세션 갱신 (슬라이딩 만료)
| 항목 | 값 |
| --- | --- |
| Method | `POST` |
| Path | `/api/v1/iam/sessions/refresh` |
| Headers |
```
Cookie: SESSION=<session-id>
X-CSRF-TOKEN: <token>
```
| Response |
```
Status: 204 No Content
Set-Cookie: SESSION=<new-session-id>; HttpOnly; Secure; SameSite=Lax; Path=/
```
| Notes | 유효한 세션을 연장하며, 만료 임박 시 클라이언트가 호출 |

### 3.4 로그아웃
| 항목 | 값 |
| --- | --- |
| Method | `DELETE` |
| Path | `/api/v1/iam/sessions/me` |
| Headers |
```
Cookie: SESSION=<session-id>
X-CSRF-TOKEN: <token>
```
| Response |
```
Status: 204 No Content
Set-Cookie: SESSION=; Max-Age=0; Path=/; HttpOnly; Secure; SameSite=Lax
```
| Notes | 서버 세션을 무효화하고 쿠키를 삭제 |

## 4. 커뮤니티 도메인 (`communities`)
커뮤니티는 Reddit의 Subreddit에 해당합니다. `Community` 애그리게잇은 식별자, 메타데이터, 규칙을 캡슐화합니다.

### 4.1 커뮤니티 생성
| Method | Path | Auth |
| --- | --- | --- |
| `POST` | `/api/v1/communities` | Required |
| Body |
```json
{
  "name": "string",
  "displayName": "string",
  "description": "string",
  "rules": ["string"],
  "isPrivate": false
}
```
| Response | `201 Created` + Location 헤더, Body:
```json
{
  "communityId": "uuid",
  "name": "string",
  "displayName": "string",
  "description": "string",
  "rules": ["string"],
  "isPrivate": false,
  "createdAt": "datetime"
}
```
| Errors | `409 Conflict` (name 중복), `403 Forbidden` (권한 없음)

### 4.2 커뮤니티 조회
* `GET /api/v1/communities/{communityId}` – 단건 조회
* `GET /api/v1/communities?keyword=...` – 이름/설명 검색, 페이징 지원

### 4.3 커뮤니티 가입/탈퇴
* `POST /api/v1/communities/{communityId}/members` – 가입 (Auth)
* `DELETE /api/v1/communities/{communityId}/members/me` – 탈퇴 (Auth)
* 응답은 멱등 처리, 가입 수락 정책(승인 필요 시 `202 Accepted` + 상태 추적)

### 4.4 커뮤니티 설정 변경
* `PATCH /api/v1/communities/{communityId}` – 관리자만 사용, 부분 업데이트 JSON Merge Patch 적용

## 5. 포스트 도메인 (`posts`)
`Post` 애그리게잇은 제목(`Title` 값 객체), 본문(`Content`), 작성자, 상태(`PostStatus`)를 포함합니다.

### 5.1 포스트 작성
| Method | Path | Auth |
| --- | --- | --- |
| `POST` | `/api/v1/communities/{communityId}/posts` | Required |
| Body |
```json
{
  "title": "string",
  "content": "string",
  "media": [
    {
      "url": "https://...",
      "mediaType": "IMAGE" | "VIDEO"
    }
  ]
}
```
| Response |
```json
{
  "postId": "uuid",
  "communityId": "uuid",
  "authorId": "uuid",
  "title": "string",
  "content": "string",
  "media": [
    {
      "assetId": "uuid",
      "url": "https://...",
      "mediaType": "IMAGE"
    }
  ],
  "status": "PUBLISHED",
  "createdAt": "datetime"
}
```

### 5.2 포스트 상세
* `GET /api/v1/posts/{postId}` – 조회수 증가 정책은 애플리케이션 서비스에서 처리
* `GET /api/v1/communities/{communityId}/posts?page=&sort=` – 목록
* `GET /api/v1/users/{userId}/posts` – 작성자별 아카이브

### 5.3 포스트 수정
* `PATCH /api/v1/posts/{postId}` – 작성자 또는 모더레이터, 제목/본문/미디어 수정 가능
* 요청은 JSON Merge Patch, 변경 이력은 도메인 이벤트로 발행 가능

### 5.4 포스트 상태 전환
* `POST /api/v1/posts/{postId}:lock` – 모더레이터가 댓글/투표 제한
* `POST /api/v1/posts/{postId}:archive` – 관리자 보관 처리
* `DELETE /api/v1/posts/{postId}` – 소프트 삭제 (`status=REMOVED`)

## 6. 댓글 도메인 (`comments`)
`Comment` 애그리게잇은 본문(`CommentBody`), 작성자, 부모 댓글, 투표 상태를 포함합니다.

### 6.1 댓글 작성
| Method | Path |
| --- | --- |
| `POST` | `/api/v1/posts/{postId}/comments` |
| Body |
```json
{
  "parentId": "uuid (optional)",
  "body": "string"
}
```
| Response |
```json
{
  "commentId": "uuid",
  "postId": "uuid",
  "parentId": "uuid or null",
  "authorId": "uuid",
  "body": "string",
  "createdAt": "datetime"
}
```

### 6.2 댓글 트리 조회
* `GET /api/v1/posts/{postId}/comments?depth=...&sort=` – 기본 depth-first 또는 time-based 정렬
* `GET /api/v1/comments/{commentId}` – 단건 조회 및 하위 스레드 반환 옵션

### 6.3 댓글 수정/삭제
* `PATCH /api/v1/comments/{commentId}` – 작성자만 본문 수정, 수정 시 `editedAt` 갱신
* `DELETE /api/v1/comments/{commentId}` – 소프트 삭제 (본문 대체 텍스트 처리)

## 7. 투표 도메인 (`votes`)
`Vote` 애그리게잇은 대상(`postId` or `commentId`), 사용자, 방향(up/down)을 캡슐화합니다. 집계 결과는 애플리케이션 서비스에서 계산/캐시.

### 7.1 포스트 투표
| Method | Path | Body |
| --- | --- | --- |
| `PUT` | `/api/v1/posts/{postId}/votes` | `{ "direction": "UP" | "DOWN" | "NEUTRAL" }` |
* 멱등성을 위해 `PUT` 사용, `NEUTRAL`은 투표 취소를 의미
* 응답은 현재 포스트의 투표 요약
```json
{
  "postId": "uuid",
  "score": 42,
  "up": 100,
  "down": 58,
  "userDirection": "UP"
}
```

### 7.2 댓글 투표
* `PUT /api/v1/comments/{commentId}/votes` – 포맷 동일

### 7.3 투표 히스토리 조회
* `GET /api/v1/users/me/votes?targetType=POST&targetId=...`

## 8. 피드 & 검색
애플리케이션 계층에서 복수 도메인을 조합해 구현.

### 8.1 개인화 피드
* `GET /api/v1/feed` – 가입한 커뮤니티 포스트, 추천 알고리즘 적용
* 쿼리 파라미터: `sort=HOT|NEW|TOP`, `time=DAY|WEEK|MONTH|YEAR|ALL`

### 8.2 글로벌 트렌드
* `GET /api/v1/feed/trending` – 전체 인기 포스트

### 8.3 검색
* `GET /api/v1/search?type=POST|COMMUNITY|USER&keyword=...`

## 9. 알림 & 도메인 이벤트 (선택)
향후 `notifications` 도메인 추가 시, 댓글/멘션/모더레이션 이벤트를 구독하도록 이벤트 발행.

* `POST /api/v1/notifications/ack` – 읽음 처리

## 10. 관리 & 모더레이션
모더레이터/관리자 권한 필요. Role은 `iam` 도메인에서 관리.

* `GET /api/v1/communities/{communityId}/reports` – 신고 목록
* `POST /api/v1/posts/{postId}:approve` – 신고 포스트 승인
* `POST /api/v1/posts/{postId}:remove` – 신고 포스트 제거
* `POST /api/v1/users/{userId}:ban` – 커뮤니티 단위 밴

## 11. 상태 점검
* `GET /actuator/health` – 기본 Spring Boot Actuator 헬스체크 (내부용)

---

### 부록 A. 도메인-API 맵핑
| 도메인 (Port) | 주요 API 어댑터 |
| --- | --- |
| `communities.domain.CommunityRepository` | `/communities`, `/feed` |
| `posts.domain.PostRepository` | `/posts`, `/feed` |
| `comments.domain.CommentRepository` | `/comments` |
| `votes.domain.VoteRepository` | `/votes` |
| `iam.domain.UserRepository` | `/iam/*` |

### 부록 B. 향후 확장 포인트
1. WebSocket 기반 실시간 알림 (`/ws/notifications`)
2. GraphQL API (`/graphql`) – 클라이언트 맞춤형 데이터 패칭
3. 외부 통합 포트 (예: 서드파티 분석 서비스)

