# 🌱 Spring Comm DDD

도메인 주도 설계(DDD)와 헥사고날 아키텍처(Hexagonal Architecture)를 기반으로 한 **커뮤니티(레딧 스타일) 백엔드** 프로젝트입니다.  
현재 목표는 **REST API** + **세션 기반 인증(쿠키/Redis)** + **MariaDB** 환경에서 안정적인 도메인 모델을 제공하는 것입니다.

---

# POST API LIST
1. 작성(초안 생성)
- POST /api/posts
- 권한: 인증 + emailVerified=true
- Body: { "communityId": "<uuid>", "title": "...", "content": "..." }
- 응답: 201 Created + Location: /api/posts/{postId} + 본문(선택)
- 검증: 제목 길이, 본문 길이, 커뮤니티 존재/작성 권한
2. 단건 조회
- GET /api/posts/{postId}
- 권한: 공개(커뮤니티 규칙에 따름)
- 응답: 200 OK (초안은 작성자/모더레이터만, 보관은 정책대로 404/200)
3. 목록 조회 (페이징)
- GET /api/posts?communityId=<uuid>&status=PUBLISHED&page=0&size=20&sort=createdAt,desc
- 권한: 공개(커뮤니티 규칙), 초안은 본인만
- 응답: 200 OK (페이지네이션 메타 포함)
4. 수정(제목/본문)
- PATCH /api/posts/{postId}
- 권한: 작성자(또는 모더레이터/관리자 정책) + !ARCHIVED
- Body 예: { "title": "...", "content": "..." } (부분 필드만 허용)
- 응답: 200 OK (갱신된 리소스) 또는 204 No Content
- 도메인: rename, rewrite 호출 (보관 상태면 409/403)
5. 게시(Publish)
- POST /api/posts/{postId}/publish
- 권한: 작성자(또는 모더레이터 승인 모델이면 모더레이터) + 현재 DRAFT
- 응답: 204 No Content
- 도메인: publish()
6. 보관(Archive) = 소프트 삭제
- POST /api/posts/{postId}/archive
- 권한: 작성자/모더레이터
- 응답: 204 No Content
- 도메인: archive() (소프트 삭제를 별도 status로 두지 않고 ARCHIVED로 일원화 추천)
7. 복구(Restore)
- POST /api/posts/{postId}/restore
- 권한: 작성자/모더레이터
- 응답: 204 No Content
- 도메인: restore() (현재 설계상 PUBLISHED로 복귀)
- 하드 삭제가 꼭 필요하면 관리자 전용: DELETE /api/admin/posts/{postId} (감사 로그 필수)

---

## 🧭 주요 특징

- **DDD + Hexagonal**: 도메인 순수성 유지, Port/Adapter 분리
- **JPA(Hibernate) + MariaDB**: 감사(Auditing), 낙관적 락(@Version) 등 활용
- **세션 인증(쿠키)**: Spring Security + Spring Session( Redis 저장소 )
- **권한 모델**: 전역 역할(USER/ADMIN) + 커뮤니티별 모더레이터
- **투표(업/다운) & 집계 분리**: Post/Comment에 집계, 개별 투표는 별도 애그리게잇
- **미디어(이미지/영상)**: PostAsset으로 정렬/메타/썸네일 관리
- **문서화**: 한국어 Javadoc 정리

---

## 🧱 아키텍처

프로젝트는 **헥사고날 아키텍처**를 따릅니다.

```
src/main/java/com/y11i/springcommddd/
├── comments/           # 댓글 도메인
├── communities/        # 커뮤니티 도메인 (+ 모더레이터 서브도메인)
├── config/             # JPA Auditing, Security, WebSecurity 설정
├── iam/                # 사용자/인증(회원) 도메인
├── posts/              # 게시글 도메인 (+ media 서브도메인)
├── shared/             # 공통 마커/VO
├── votes/              # 투표 도메인 (PostVote/CommentVote)
└── SpringCommDddApplication.java
```

### 계층 구성

| 계층 | 패키지 | 역할 |
|------|--------|------|
| **Domain** | `*.domain` | 엔티티/애그리게잇/VO/리포지토리 인터페이스 |
| **Application** | `*.application` | 유스케이스/서비스(도메인 조립) |
| **Infrastructure** | `*.infrastructure` | JPA 어댑터, 보안, 외부 연동 구현체 |
| **API** | `*.api` | REST Controller, DTO 어댑터 |

> JPA는 `@Access(AccessType.FIELD)`를 사용하고, Lombok 대신 **명시적 접근자**(`memberId()`, `title()` 등)를 사용합니다.

---

## ⚙️ 기술 스택 & 의존성

- **Java** 25
- **Spring Boot** 3.5.6
- Spring Data JPA, Spring Security, Spring Web
- **Spring Session (Redis)**, Spring Data Redis
- **MariaDB** (JDBC Driver)
- **Flyway** (마이그레이션)
- Validation (`jakarta.validation`), Testcontainers(테스트)

`build.gradle` 주요 설정:

---

## 🔐 인증/인가 (세션 + Redis)

- **쿠키 기반 세션**: 로그인 성공 시 `JSESSIONID` 쿠키 발급 → Spring Session이 **Redis**에 세션 저장
- **보안 플래그**: `Secure`, `HttpOnly`, `SameSite=Lax/Strict` 권장
- **CSRF**: 쿠키 인증이면 활성 권장(프런트에서 `X-CSRF-TOKEN` 헤더 전송)

---

## 🧍 IAM 도메인

- **Aggregate**: `Member`
- **VO**: `MemberId`, `Email`, `DisplayName`, `PasswordHash`
- **상태**: `MemberStatus` = ACTIVE, SUSPENDED, DELETED
- **역할**: `MemberRole` = USER, ADMIN ( `member_roles` @ElementCollection )
- **주요 동작**: `register`, `rename`, `changeEmail`, `setNewPassword`, `activate/suspend/markDeleted`, `requirePasswordReset`

---

## 🏘️ Community & Moderators

- **Community**: `Community`, `CommunityId`, `CommunityName`, `CommunityNameKey`, `CommunityStatus`
- **Moderators** (서브도메인): `CommunityModerator` 애그리게잇, 유니크 `(community_id, member_id)`  
  전역 ADMIN과 별도로 **커뮤니티별 모더레이션** 권한을 부여

---

## 📝 Posts & Media

- **Post**: `Post`, `PostId`, `Title`, `Content`, `PostStatus`
- **집계**: `upCount`, `downCount` (점수는 `score()`)
- **Media (PostAsset)**: 이미지/영상 자산. `display_order`, `srcUrl`, `thumbUrl`, `mime/width/height/duration`, `alt/caption`
    - 내부 업로드는 PostAsset으로 관리
    - 외부 링크(Imgur/Streamable 등)는 본문에 URL만 두고 **프런트 임베드** 권장(oEmbed)

---

## 💬 Comments

- **Comment**: 트리/대댓글( `parentId`, `depth` ), 소프트 삭제(`DELETED`)
- 루트/자식/전체 조회 리포지토리 API 제공
- 투표 집계 필드(`upCount`, `downCount`) 보유

---

## ⬆️ Votes

- **PostVote / CommentVote**: 개별 투표 애그리게잇(유니크: 타깃+voter)
- **집계 vs myVote** 분리:
    - 집계는 Post/Comment의 카운터
    - **myVote(−1/0/+1)** 은 로그인 사용자 기준으로 **배치 조회**(`findMyVotesBy…Ids`)하여 View에 조립

---

## 🚀 실행 & 개발

### 1) MariaDB/Redis 준비
- MariaDB: `spring_comm` 데이터베이스 생성, 사용자/패스워드 설정
- Redis: 로컬 6379 실행 (Docker 권장)

```bash
# MariaDB (docker 예시)
docker run -d --name mariadb -e MARIADB_ROOT_PASSWORD=pass -e MARIADB_DATABASE=spring_comm -p 3306:3306 mariadb:11

# Redis
docker run -d --name redis -p 6379:6379 redis:7
```

### 2) 설정
`src/main/resources/application.properties` 또는 `application.yml`에 DB/Redis 설정 추가(상단 예시 참조).

### 3) 빌드/실행
```bash
./gradlew clean build
./gradlew bootRun
```

### 4) 테스트

```bash
# 단위 테스트 시
./gradlew test
# 통합 테스트 시
./gradlew test -DincludeTags=integration -DexcludeTags=
```
---

## 🔧 운영 팁

- **세션 확장**: Spring Session + Redis로 다중 인스턴스 세션 공유
- **쿠키 정책**: HTTPS 환경에서 `Secure` + `HttpOnly` + `SameSite` 적절히 설정
- **CSRF**: 쿠키 세션이면 활성, 프런트는 `X-CSRF-TOKEN` 전송
- **인덱스**: `post_votes(voter_id)`, `comment_votes(voter_id)` 보조 인덱스 권장
- **집계 재빌드 배치(옵션)**: 드문 불일치 대비해 정기적으로 투표 테이블에서 카운터 재계산
- **외부 링크 임베드**: 일단 본문 URL만 저장 → 프런트 oEmbed로 미리보기. SEO/SSR 필요 시 서버 캐시 테이블 추가

---

## 🗺️ 로드맵 (단기)

- API 계층 구현 (로그인/로그아웃, 커뮤니티/포스트/댓글/투표/미디어)
- Flyway 마이그레이션 스크립트 정식화
- 링크 프리뷰 캐시(옵션)
- 모더레이터 워크플로우(위임/회수 이벤트) 정리
- E2E/계약 테스트 추가

---

## 📝 라이선스

MIT License (c) 2025 y11i
