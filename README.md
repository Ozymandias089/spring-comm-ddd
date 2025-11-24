# 🌱 Spring Comm DDD

도메인 주도 설계(DDD)와 헥사고날 아키텍처(Hexagonal Architecture)를 기반으로 한 **커뮤니티(레딧 스타일) 백엔드** 프로젝트입니다.  
현재 목표는 **REST API** + **세션 기반 인증(쿠키/Redis)** + **MariaDB** 환경에서 안정적인 도메인 모델을 제공하는 것입니다.

---

## 🧱 아키텍처

프로젝트는 **헥사고날 아키텍처**를 따릅니다.

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
