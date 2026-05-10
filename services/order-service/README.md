# order-service

라이브 커머스 플랫폼의 주문과 체크아웃 흐름을 담당한다.

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
| `DB_URL` | `jdbc:postgresql://localhost:5432/order` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `order` | DB 사용자명 |
| `DB_PASSWORD` | `order` | DB 비밀번호 |

### 빌드 및 실행

```bash
# 테스트
./gradlew test

# 빌드
./gradlew bootJar

# 실행
java -jar build/libs/order-service-0.0.1-SNAPSHOT.jar
```

### Docker

```bash
docker build -t order-service .
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/order \
  -e DB_USERNAME=order \
  -e DB_PASSWORD=order \
  order-service
```

## API 엔드포인트

Base Path: `/api/v1`

| Method | Path | 설명 |
|--------|------|------|
| POST | /orders | 주문 생성 (mock 결제 자동 처리) |
| GET | /orders/{id} | 주문 단건 조회 |

상세 스펙은 [docs/order-api-contracts.md](../../docs/order-api-contracts.md) 참고.

## Health / Metrics

| 경로 | 설명 |
|------|------|
| `GET /actuator/health` | 전체 health |
| `GET /actuator/health/liveness` | Liveness probe |
| `GET /actuator/health/readiness` | Readiness probe |
| `GET /actuator/prometheus` | Prometheus metrics |

## 패키지 구조

```
com.olivelive.order
├── common/exception   - 에러 코드, 예외, 전역 핸들러
├── common/ulid        - ULID ID 생성기
└── order/             - 주문 도메인 (domain, repository, service, controller, dto)
```
