# 🌱 Spring Comm DDD

도메인 주도 설계(DDD)와 헥사고날 아키텍처(Hexagonal Architecture)를 기반으로 한 커뮤니티 서비스 백엔드 프로젝트입니다.  
(레딧/포럼 형태의 구조를 목표로 합니다.)

---

## 🧱 Architecture

본 프로젝트는 **헥사고날 아키텍처(Hexagonal Architecture)** 를 기반으로 구성됩니다.

```
src/main/java/com/y11i/springcommddd/
├── comments/           # 댓글 도메인
├── config/             # JPA, Security 설정
├── iam/                # 사용자 인증 및 계정 관리 도메인 (Identity & Access Management)
├── posts/              # 게시글 도메인
├── shared/             # 공통 도메인 마커 인터페이스
└── SpringCommDddApplication.java
```

### 계층 구성

| 계층 | 패키지 | 역할 |
|------|---------|------|
| **Domain** | `*.domain` | 엔티티, 애그리거트, VO, 리포지토리 인터페이스 정의 |
| **Application** | `*.application` | 유스케이스/서비스 로직 (포트 조립) |
| **Infrastructure** | `*.infrastructure` | JPA 어댑터, 보안, 외부 시스템 구현체 |
| **API** | `*.api` | REST Controller, DTO, 요청/응답 어댑터 |

> `@Access(AccessType.FIELD)` 를 사용하여 JPA 접근을 필드 단위로 제한하고,  
> `@Getter` 대신 명시적 접근자(`memberId()`, `title()` 등)를 직접 정의합니다.

---

## ⚙️ Dependencies

`build.gradle` 주요 의존성:

```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'org.springframework.boot:spring-boot-starter-web'
compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'org.springframework.security:spring-security-test'
runtimeOnly 'org.mariadb.jdbc:mariadb-java-client'
```

---

## 📦 Implemented Domains (현재 구현 완료)

### 🧍 IAM (Identity & Access Management)
- **Aggregate Root**: `Member`
- **Value Objects**: `MemberId`, `Email`, `DisplayName`, `PasswordHash`
- **Repository**: `MemberRepository` + `JpaMemberRepository`
- **기능**
    - 회원 등록 (`Member.register(...)`)
    - 비밀번호 변경 (`setNewPassword`)
    - 이메일 변경 / 닉네임 변경
    - 상태 전이 (`activate`, `suspend`, `markDeleted`)
    - 비밀번호 리셋 요구 플래그 (`requirePasswordReset`)
- **상태**
    - `MemberStatus`: ACTIVE, SUSPENDED, DELETED

### 📝 Posts
- **Aggregate Root**: `Post`
- **Value Objects**: `PostId`, `Title`, `Content`
- **Repository**: `PostRepository` + `JpaPostRepository`
- **기능**
    - 게시글 작성 (`Post.create(...)`)
    - 제목/내용 수정 (`rename`, `rewrite`)
    - 상태 전이 (`publish`, `archive`, `delete`)
- **상태**
    - `PostStatus`: DRAFT, PUBLISHED, ARCHIVED, DELETED

---

## 🚧 Upcoming Domains (추후 구현 예정 순서)

1. **Community (서브레딧/게시판 개념)**
    - `Community`, `CommunityId`, `CommunityRepository`
    - 게시글(`Post`)에 `communityId` 추가
    - 아카이브된 커뮤니티엔 글 작성 금지

2. **Comments**
    - `Comment`, `CommentId`, `CommentRepository`
    - `PostId`, `MemberId` 참조
    - 대댓글(parentId) 구조
    - 소프트 삭제(`status=DELETED`)

3. **Votes (Upvote / Downvote)**
    - `PostVote`, `CommentVote`
    - 복합 PK(`target_id`, `voter_id`) 유니크
    - `value` = 1 / -1
    - 합산 집계 또는 denormalized counter 방식

4. **Media (Images / Videos)**
    - `Media`, `MediaId`, `MediaRepository`
    - `ownerId`, `postId` 참조
    - `status`: PENDING → READY / FAILED
    - 게시글 본문 내 첨부 기능

5. **Token / Session**
    - RefreshToken 관리, 만료/회전 정책
    - 이메일 인증, 비밀번호 재설정 토큰 등

---

## 🧭 개발 원칙

- **DDD Tactical Pattern**
    - Entity / ValueObject / AggregateRoot 명확 구분
    - 상태 전이는 도메인 메서드로만 수행
- **Hexagonal Architecture**
    - Domain이 Framework에 의존하지 않음
    - 모든 외부 연동(JPA, 보안, API)은 Port/Adapter로 분리
- **Persistence**
    - JPA (Hibernate) + MariaDB
    - `@Access(AccessType.FIELD)` 적용
    - Optimistic Locking (`@Version`) 사용

---

## 🧪 테스트 및 실행

```bash
./gradlew clean build
./gradlew bootRun
```

---

## 📄 License

MIT License (c) 2025 y11i
