# 🌱 Spring Comm DDD

도메인 주도 설계(DDD)와 헥사고날 아키텍처(Hexagonal Architecture)를 기반으로 한 **커뮤니티(레딧 스타일) 백엔드** 프로젝트입니다.  
현재 목표는 **REST API** + **세션 기반 인증(쿠키/Redis)** + **MariaDB** 환경에서 안정적인 도메인 모델을 제공하는 것입니다.

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

## 📁 현재 프로젝트 구조

```
🍏 ~/GitHub/spring-comm-ddd/ [main] tree src/main/java/com/y11i/springcommddd/
src/main/java/com/y11i/springcommddd/
├── comments
│   ├── api
│   ├── application
│   ├── domain
│   │   ├── Comment.java
│   │   ├── CommentBody.java
│   │   ├── CommentId.java
│   │   ├── CommentRepository.java
│   │   └── CommentStatus.java
│   └── infrastructure
│       ├── CommentRepositoryAdapter.java
│       └── JpaCommentRepository.java
├── communities
│   ├── api
│   ├── application
│   ├── domain
│   │   ├── Community.java
│   │   ├── CommunityId.java
│   │   ├── CommunityName.java
│   │   ├── CommunityNameKey.java
│   │   ├── CommunityRepository.java
│   │   └── CommunityStatus.java
│   ├── infrastructure
│   │   ├── CommuityRepositoryAdapter.java
│   │   └── JpaCommunityRepository.java
│   └── moderators
│       ├── domain
│       │   ├── CommunityModerator.java
│       │   ├── CommunityModeratorId.java
│       │   └── CommunityModeratorRepository.java
│       └── infrastructure
│           ├── CommunityModeratorRepositoryAdapter.java
│           └── JpaCommunityModeratorRepository.java
├── config
│   ├── JpaAuditingConfig.java
│   ├── SecurityConfig.java
│   └── WebSecurityConfig.java
├── iam
│   ├── api
│   ├── application
│   ├── domain
│   │   ├── DisplayName.java
│   │   ├── Email.java
│   │   ├── Member.java
│   │   ├── MemberId.java
│   │   ├── MemberRepository.java
│   │   ├── MemberRole.java
│   │   ├── MemberStatus.java
│   │   └── PasswordHash.java
│   └── infrastructure
│       ├── JpaMemberRepository.java
│       ├── MemberAuthProvider.java
│       └── MemberRepositoryAdapter.java
├── posts
│   ├── api
│   ├── application
│   ├── domain
│   │   ├── Content.java
│   │   ├── Post.java
│   │   ├── PostId.java
│   │   ├── PostRepository.java
│   │   ├── PostStatus.java
│   │   └── Title.java
│   ├── infrastructure
│   │   ├── JpaPostRepository.java
│   │   └── PostRepositoryAdapter.java
│   └── media
│       ├── api
│       ├── application
│       ├── domain
│       │   ├── MediaType.java
│       │   ├── PostAsset.java
│       │   ├── PostAssetId.java
│       │   ├── PostAssetRepository.java
│       │   └── Url.java
│       └── infrastructure
│           ├── JpaPostAssetRepository.java
│           └── PostAssetRepositoryAdapter.java
├── shared
│   └── domain
│       ├── AggregateRoot.java
│       ├── DomainEntity.java
│       ├── ImageUrl.java
│       └── ValueObject.java
├── SpringCommDddApplication.java
└── votes
    ├── api
    ├── application
    ├── domain
    │   ├── CommentVote.java
    │   ├── CommentVoteId.java
    │   ├── CommentVoteRepository.java
    │   ├── MyCommentVote.java
    │   ├── MyPostVote.java
    │   ├── MyVoteValue.java
    │   ├── PostVote.java
    │   ├── PostVoteId.java
    │   └── PostVoteRepository.java
    └── infrastructure
        ├── CommentVoteRepositoryAdapter.java
        ├── JpaCommentVoteRepository.java
        ├── JpaPostVoteRepository.java
        └── PostVoteRepositoryAdapter.java
```

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

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.5.6'
    id 'io.spring.dependency-management' version '1.1.7'
}

java {
    toolchain { languageVersion = JavaLanguageVersion.of(25) }
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation "org.springframework.session:spring-session-data-redis"
    implementation "org.springframework.boot:spring-boot-starter-data-redis"
    implementation "org.springframework.boot:spring-boot-starter-validation"

    runtimeOnly "org.mariadb.jdbc:mariadb-java-client"

    implementation "org.flywaydb:flyway-core"
    implementation "org.flywaydb:flyway-mysql"

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
    testImplementation "org.testcontainers:junit-jupiter"
    testImplementation "org.testcontainers:mariadb"
}
```

---

## 🔐 인증/인가 (세션 + Redis)

- **쿠키 기반 세션**: 로그인 성공 시 `JSESSIONID` 쿠키 발급 → Spring Session이 **Redis**에 세션 저장
- **보안 플래그**: `Secure`, `HttpOnly`, `SameSite=Lax/Strict` 권장
- **CSRF**: 쿠키 인증이면 활성 권장(프런트에서 `X-CSRF-TOKEN` 헤더 전송)

`application.properties` 예시:
```properties
# MariaDB
spring.datasource.url=jdbc:mariadb://localhost:3306/spring_comm
spring.datasource.username=root
spring.datasource.password=pass
spring.jpa.hibernate.ddl-auto=validate

# Redis (Spring Session)
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.session.store-type=redis
spring.session.redis.namespace=spring:session
server.servlet.session.timeout=30m

# Hibernate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
```

### Security 구성 메모
- `MemberAuthProvider`: 이메일/패스워드 인증 + 전역 역할(ROLE_USER/ROLE_ADMIN) + 커뮤니티별 권한(`COMMUNITY_MOD:<communityId>`)
- 메서드 보안: `@EnableMethodSecurity` + `@PreAuthorize(...)`

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
