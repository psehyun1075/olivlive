# live-control-service

라이브 커머스 플랫폼의 라이브 세션 메타데이터와 IVS 연계를 담당한다.

## 기술 스택

- Java 21, Spring Boot 3.3.5, Gradle
- PostgreSQL + Flyway
- Actuator (health, readiness, prometheus)

## 환경 변수

| 변수 | 기본값 | 설명 |
|------|--------|------|
| DB_URL | `jdbc:postgresql://localhost:5432/livecontrol` | 데이터베이스 URL |
| DB_USERNAME | `livecontrol` | DB 사용자 |
| DB_PASSWORD | `livecontrol` | DB 비밀번호 |

## 로컬 실행

```bash
./gradlew bootRun
```

### Docker

```bash
docker build -t live-control-service .
docker run -e DB_URL=... -e DB_USERNAME=... -e DB_PASSWORD=... -p 8080:8080 live-control-service
```

## API 엔드포인트

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | /api/v1/live-sessions | 세션 생성 |
| GET | /api/v1/live-sessions/{id} | 세션 단건 조회 |
| POST | /api/v1/live-sessions/{id}/go-live | 상태 전이: READY → LIVE |
| POST | /api/v1/live-sessions/{id}/end | 상태 전이: LIVE → ENDED |
| GET | /internal/live-sessions/{id}/attachability | 상품 attach 가능 여부 (service-to-service) |

> **Note**: `ivs_channel_arn`, `ivs_playback_url`은 내부 linkage metadata 컬럼으로 DB에 저장되지만
> public API 응답에는 노출하지 않는다. docs/live-control-api-contracts.md의 GET 응답 예시와
> 차이가 있으며 해당 문서는 별도 업데이트 필요.

## Health / Metrics

- `GET /actuator/health`
- `GET /actuator/health/liveness`
- `GET /actuator/health/readiness`
- `GET /actuator/prometheus`

## 패키지 구조

```
com.olivelive.livecontrol
├── common/
│   ├── exception/   ErrorCode, LiveControlException, ErrorResponse, GlobalExceptionHandler
│   └── ulid/        UlidGenerator
└── session/
    ├── domain/      LiveSession, SessionStatus
    ├── repository/  LiveSessionRepository
    ├── service/     LiveSessionService
    ├── controller/  LiveSessionController, InternalLiveSessionController
    └── dto/         CreateSessionRequest, LiveSessionResponse, SessionStatusResponse, AttachabilityResponse
```
