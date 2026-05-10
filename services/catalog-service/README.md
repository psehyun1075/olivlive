# catalog-service

라이브 커머스 플랫폼의 상품 카탈로그와 라이브 세션 노출 상품 매핑을 담당한다.

## 기술 스택

- Java 21, Spring Boot 3.3.5, Gradle
- PostgreSQL + Flyway (DB 마이그레이션)
- Actuator (health, readiness, prometheus)

## 로컬 실행

### 사전 조건
- JDK 21
- PostgreSQL (또는 Docker)

### 환경 변수

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/catalog` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `catalog` | DB 사용자명 |
| `DB_PASSWORD` | `catalog` | DB 비밀번호 |

### 빌드 및 실행

```bash
# 테스트
./gradlew test

# 빌드
./gradlew bootJar

# 실행
java -jar build/libs/catalog-service-0.0.1-SNAPSHOT.jar
```

### Docker

```bash
docker build -t catalog-service .
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/catalog \
  -e DB_USERNAME=catalog \
  -e DB_PASSWORD=catalog \
  catalog-service
```

## API 엔드포인트

Base Path: `/api/v1`

| Method | Path | 설명 |
|--------|------|------|
| POST | /products | 상품 등록 |
| GET | /products | 상품 목록 조회 |
| GET | /products/{id} | 상품 단건 조회 |
| POST | /live-products | 라이브 세션 상품 매핑 등록 |
| GET | /live-products/{liveSessionId} | 세션별 노출 상품 목록 조회 |

상세 스펙은 [docs/api-contracts.md](../../docs/api-contracts.md) 참고.

## Health / Metrics

| 경로 | 설명 |
|------|------|
| `GET /actuator/health` | 전체 health |
| `GET /actuator/health/liveness` | Liveness probe |
| `GET /actuator/health/readiness` | Readiness probe |
| `GET /actuator/prometheus` | Prometheus metrics |

## live-control-service 연동

현재 `catalog.live-control.stub=true` (기본값)로 설정되어 있어 세션 검증을 통과한다.
live-control-service 완성 후 `LiveControlClient` 인터페이스의 HTTP 구현체를 추가하고
`catalog.live-control.stub=false`로 전환한다.

## 패키지 구조

```
com.olivelive.catalog
├── common/exception   - 에러 코드, 예외, 전역 핸들러
├── common/ulid        - ULID ID 생성기
├── product/           - 상품 도메인 (entity, repo, service, controller, dto)
└── liveproduct/       - 라이브 상품 매핑 도메인 (entity, repo, client, service, controller, dto)
```
