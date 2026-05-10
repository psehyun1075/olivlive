# live-control-service API Contracts

Base Path: `/api/v1` (public), `/internal` (service-to-service)  
Content-Type: `application/json`

---

## Live Sessions

### POST /api/v1/live-sessions

라이브 세션을 생성한다. 생성된 세션은 `READY` 상태로 시작한다.

**Request Body**

```json
{
  "title": "여름 특가 라이브",
  "host_id": "host_01HXYZ",
  "scheduled_at": "2026-05-20T14:00:00Z",
  "ivs_channel_arn": "arn:aws:ivs:ap-northeast-2:123456789012:channel/abcDefGh"
}
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| title | string | Y | 1–200자 |
| host_id | string | Y | non-empty |
| scheduled_at | string (ISO 8601) | N | 예정 방송 시각; 과거 시각도 허용 |
| ivs_channel_arn | string | N | IVS 채널 ARN; 미지정 시 NULL 저장 |

**Response: 201 Created**

```json
{
  "id": "live_01HXYZ",
  "title": "여름 특가 라이브",
  "host_id": "host_01HXYZ",
  "status": "READY",
  "scheduled_at": "2026-05-20T14:00:00Z",
  "started_at": null,
  "ended_at": null,
  "ivs_channel_arn": "arn:aws:ivs:ap-northeast-2:123456789012:channel/abcDefGh",
  "ivs_playback_url": null,
  "created_at": "2026-05-10T09:00:00Z",
  "updated_at": "2026-05-10T09:00:00Z"
}
```

**Error Responses**

| 상태 코드 | 에러 코드 | 사유 |
|-----------|-----------|------|
| 400 | `INVALID_REQUEST` | 필수 필드 누락 또는 title 길이 초과 |

---

### GET /api/v1/live-sessions/{id}

라이브 세션 단건을 조회한다.

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| id | string | 세션 ID |

**Response: 200 OK**

```json
{
  "id": "live_01HXYZ",
  "title": "여름 특가 라이브",
  "host_id": "host_01HXYZ",
  "status": "LIVE",
  "scheduled_at": "2026-05-20T14:00:00Z",
  "started_at": "2026-05-20T14:01:00Z",
  "ended_at": null,
  "ivs_channel_arn": "arn:aws:ivs:ap-northeast-2:123456789012:channel/abcDefGh",
  "ivs_playback_url": "https://a1b2c3d4e5f6.ap-northeast-2.playback.live-video.net/api/video/v1/...",
  "created_at": "2026-05-10T09:00:00Z",
  "updated_at": "2026-05-20T14:01:00Z"
}
```

**Error Responses**

| 상태 코드 | 에러 코드 | 사유 |
|-----------|-----------|------|
| 404 | `LIVE_SESSION_NOT_FOUND` | 해당 ID의 세션 없음 |

---

### POST /api/v1/live-sessions/{id}/go-live

세션 상태를 `READY` → `LIVE`로 전이한다. `started_at`을 현재 시각으로 기록한다.

**Request Body**: 없음

**Response: 200 OK**

```json
{
  "id": "live_01HXYZ",
  "status": "LIVE",
  "started_at": "2026-05-20T14:01:00Z",
  "updated_at": "2026-05-20T14:01:00Z"
}
```

**Error Responses**

| 상태 코드 | 에러 코드 | 사유 |
|-----------|-----------|------|
| 404 | `LIVE_SESSION_NOT_FOUND` | 해당 ID의 세션 없음 |
| 409 | `SESSION_NOT_READY` | 현재 상태가 `READY`가 아님 |

---

### POST /api/v1/live-sessions/{id}/end

세션 상태를 `LIVE` → `ENDED`로 전이한다. `ended_at`을 현재 시각으로 기록한다.

**Request Body**: 없음

**Response: 200 OK**

```json
{
  "id": "live_01HXYZ",
  "status": "ENDED",
  "ended_at": "2026-05-20T15:30:00Z",
  "updated_at": "2026-05-20T15:30:00Z"
}
```

**Error Responses**

| 상태 코드 | 에러 코드 | 사유 |
|-----------|-----------|------|
| 404 | `LIVE_SESSION_NOT_FOUND` | 해당 ID의 세션 없음 |
| 409 | `SESSION_NOT_LIVE` | 현재 상태가 `LIVE`가 아님 |

---

## Internal (Service-to-Service)

### GET /internal/live-sessions/{id}/attachability

catalog-service가 상품을 라이브 세션에 매핑하기 전에 세션 존재 여부와 attach 가능 여부를 확인한다.

`attachable: true`는 세션 상태가 `READY`일 때만 반환된다.

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| id | string | 세션 ID |

**Response: 200 OK**

```json
{
  "session_id": "live_01HXYZ",
  "attachable": true,
  "status": "READY"
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| session_id | string | 요청한 세션 ID |
| attachable | boolean | `true` = 상품 attach 허용 (`READY` 상태), `false` = 불허 |
| status | string | 현재 세션 상태 (`READY` \| `LIVE` \| `ENDED`) |

**Error Responses**

| 상태 코드 | 에러 코드 | 사유 |
|-----------|-----------|------|
| 404 | `LIVE_SESSION_NOT_FOUND` | 해당 ID의 세션 없음 |

> catalog-service는 `attachable: false` 응답을 받으면 `409 LIVE_SESSION_NOT_ATTACHABLE`을 호출자에게 반환한다.
> `404`를 받으면 `404 LIVE_SESSION_NOT_FOUND`를 반환한다.

---

## 상태 머신

```
READY ──go-live──► LIVE ──end──► ENDED
```

| 상태 | 상품 attach 허용 | 설명 |
|------|-----------------|------|
| READY | Y | 세션 생성 직후, 방송 준비 중 |
| LIVE | N | 방송 진행 중 |
| ENDED | N | 방송 종료 |

---

## 공통 에러 형식

```json
{
  "error": {
    "code": "LIVE_SESSION_NOT_FOUND",
    "message": "Live session not found"
  }
}
```

| 필드 | 설명 |
|------|------|
| code | 기계가 파싱 가능한 에러 식별자 |
| message | 사람이 읽을 수 있는 설명 |
