# live-control-service Data Model

DB: PostgreSQL

---

## 테이블: live_sessions

라이브 세션 메타데이터, 상태, IVS linkage 정보를 저장한다.

IVS 채널/플레이백 정보는 별도 테이블 없이 이 테이블의 컬럼으로 저장한다.
live-control-service는 미디어 전송을 제어하지 않으며, IVS ARN과 플레이백 URL을 참조 목적으로만 보관한다.

```sql
CREATE TABLE live_sessions (
  id               VARCHAR(32)      PRIMARY KEY,
  title            VARCHAR(200)     NOT NULL,
  host_id          VARCHAR(32)      NOT NULL,
  status           VARCHAR(10)      NOT NULL DEFAULT 'READY',
  scheduled_at     TIMESTAMPTZ,
  started_at       TIMESTAMPTZ,
  ended_at         TIMESTAMPTZ,
  ivs_channel_arn  TEXT,
  ivs_playback_url TEXT,
  created_at       TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
  updated_at       TIMESTAMPTZ      NOT NULL DEFAULT NOW(),

  CONSTRAINT live_sessions_status_check
    CHECK (status IN ('READY', 'LIVE', 'ENDED'))
);
```

### 컬럼 상세

| 컬럼 | 타입 | Nullable | 기본값 | 설명 |
|------|------|----------|--------|------|
| id | VARCHAR(32) | N | — | ULID 형식, 애플리케이션에서 생성 (`live_` prefix) |
| title | VARCHAR(200) | N | — | 세션 제목 |
| host_id | VARCHAR(32) | N | — | 방송 진행자 ID |
| status | VARCHAR(10) | N | `'READY'` | `READY` \| `LIVE` \| `ENDED` |
| scheduled_at | TIMESTAMPTZ | Y | NULL | 예정 방송 시각 |
| started_at | TIMESTAMPTZ | Y | NULL | go-live 전이 시각 |
| ended_at | TIMESTAMPTZ | Y | NULL | end 전이 시각 |
| ivs_channel_arn | TEXT | Y | NULL | AWS IVS 채널 ARN (linkage 참조용) |
| ivs_playback_url | TEXT | Y | NULL | IVS 플레이백 URL (linkage 참조용) |
| created_at | TIMESTAMPTZ | N | NOW() | 생성 시각 |
| updated_at | TIMESTAMPTZ | N | NOW() | 최종 수정 시각 |

### 제약 조건

| 이름 | 종류 | 대상 |
|------|------|------|
| live_sessions_pkey | PRIMARY KEY | id |
| live_sessions_status_check | CHECK | status IN ('READY', 'LIVE', 'ENDED') |

### 인덱스

| 이름 | 컬럼 | 목적 |
|------|------|------|
| idx_live_sessions_status | status | 상태별 조회 |
| idx_live_sessions_host | host_id | 진행자별 세션 조회 |
| idx_live_sessions_created_at | created_at DESC | 목록 최신순 정렬 |

---

## ID 생성 전략

| 테이블 | 형식 | 생성 위치 |
|--------|------|-----------|
| live_sessions.id | `live_` + ULID | 애플리케이션 레이어 |

ULID를 사용하면 시간 순 정렬이 가능하고 URL 안전 문자열이다.
catalog-service의 `prod_`, `lp_` prefix 패턴과 동일하게 맞춘다.

---

## 상태 전이와 컬럼 변경

| 전이 | 변경 컬럼 |
|------|-----------|
| 생성 (`READY`) | status = `READY`, created_at, updated_at |
| go-live (`READY → LIVE`) | status = `LIVE`, started_at = NOW(), updated_at = NOW() |
| end (`LIVE → ENDED`) | status = `ENDED`, ended_at = NOW(), updated_at = NOW() |

---

## 마이그레이션 순서

1. `V1__create_live_sessions.sql` — `live_sessions` 테이블 생성
