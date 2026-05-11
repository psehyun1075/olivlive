# order-service Data Model

DB: PostgreSQL

---

## 테이블: orders

주문 헤더 정보를 저장한다.

```sql
CREATE TABLE orders (
  id              VARCHAR(32)   PRIMARY KEY,
  buyer_id        VARCHAR(100)  NOT NULL,
  live_session_id TEXT,
  status          VARCHAR(20)   NOT NULL DEFAULT 'PAID',
  total_amount    INTEGER       NOT NULL CHECK (total_amount > 0),
  payment_status  VARCHAR(20)   NOT NULL DEFAULT 'MOCK_PAID',
  created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);
```

### 컬럼 상세

| 컬럼 | 타입 | Nullable | 기본값 | 설명 |
|------|------|----------|--------|------|
| id | VARCHAR(32) | N | — | ULID 형식, 애플리케이션에서 생성 |
| buyer_id | VARCHAR(100) | N | — | 구매자 식별자 |
| live_session_id | TEXT | Y | NULL | 라이브 세션 ID; opaque 문자열, order-service는 형식을 정의하지 않음 |
| status | VARCHAR(20) | N | `'PAID'` | 주문 상태 (`PENDING` \| `PAID` \| `CANCELLED`) |
| total_amount | INTEGER | N | — | 주문 총액 (원 단위), 0 초과 |
| payment_status | VARCHAR(20) | N | `'MOCK_PAID'` | 결제 상태 (`MOCK_PAID`) |
| created_at | TIMESTAMPTZ | N | NOW() | 생성 시각 |
| updated_at | TIMESTAMPTZ | N | NOW() | 최종 수정 시각 |

### 주문 상태 (`status`)

| 값 | 설명 | 전환 조건 |
|----|------|-----------|
| `PENDING` | 주문 생성, 결제 대기 | 생성 직후 (mock 흐름에서는 즉시 PAID로 전환) |
| `PAID` | 결제 완료 | mock 결제 성공 후; 현재 항상 성공 |
| `CANCELLED` | 취소 | 향후 취소 API 구현 시 사용 (현재 범위 밖) |

### 결제 상태 (`payment_status`)

| 값 | 설명 |
|----|------|
| `MOCK_PAID` | mock 결제 완료; 현재 유일하게 사용 |

> mock 흐름: POST /orders 수신 → order(PENDING) 생성 → mock 결제 → status=PAID, payment_status=MOCK_PAID → 저장 → 201 반환.  
> 실제 결제 연동 시 상태 머신을 확장할 수 있도록 VARCHAR 타입으로 유지한다.

### 제약 조건

| 이름 | 종류 | 대상 |
|------|------|------|
| orders_pkey | PRIMARY KEY | id |
| orders_total_amount_check | CHECK | total_amount > 0 |

### 인덱스

| 이름 | 컬럼 | 목적 |
|------|------|------|
| idx_orders_buyer_id | buyer_id | 구매자별 주문 조회 (향후) |
| idx_orders_created_at | created_at DESC | 주문 목록 정렬 (향후) |

---

## 테이블: order_items

주문 라인 아이템을 저장한다. 상품 정보는 주문 시점의 스냅샷을 저장한다.

`product_id`는 catalog-service가 소유하는 opaque identifier다.
order-service는 catalog-service DB를 직접 참조하지 않으며, cross-service DB foreign key를 두지 않는다.

```sql
CREATE TABLE order_items (
  id           VARCHAR(32)   PRIMARY KEY,
  order_id     VARCHAR(32)   NOT NULL REFERENCES orders(id),
  product_id   TEXT          NOT NULL,
  product_name VARCHAR(200)  NOT NULL,
  unit_price   INTEGER       NOT NULL CHECK (unit_price > 0),
  quantity     INTEGER       NOT NULL CHECK (quantity >= 1),
  subtotal     INTEGER       NOT NULL CHECK (subtotal > 0)
);
```

### 컬럼 상세

| 컬럼 | 타입 | Nullable | 기본값 | 설명 |
|------|------|----------|--------|------|
| id | VARCHAR(32) | N | — | ULID 형식, 애플리케이션에서 생성 |
| order_id | VARCHAR(32) | N | — | orders.id 참조 |
| product_id | TEXT | N | — | catalog-service의 opaque identifier; DB FK 없음 |
| product_name | VARCHAR(200) | N | — | 주문 시점 상품명 스냅샷 |
| unit_price | INTEGER | N | — | 주문 시점 단가 스냅샷 (원 단위), 0 초과 |
| quantity | INTEGER | N | — | 주문 수량, 1 이상 |
| subtotal | INTEGER | N | — | unit_price × quantity; 불변성을 위해 저장 |

### 제약 조건

| 이름 | 종류 | 대상 |
|------|------|------|
| order_items_pkey | PRIMARY KEY | id |
| order_items_order_fk | FOREIGN KEY | order_id → orders(id) |
| order_items_unit_price_check | CHECK | unit_price > 0 |
| order_items_quantity_check | CHECK | quantity >= 1 |
| order_items_subtotal_check | CHECK | subtotal > 0 |

### 인덱스

| 이름 | 컬럼 | 목적 |
|------|------|------|
| idx_order_items_order_id | order_id | 주문별 아이템 조회 |

---

## 테이블 관계

```
orders (1) ──────── (N) order_items
    id ◄────────────────── order_id

order_items.product_id
    └─ application-level 참조 → catalog-service 상품 ID
       (DB FK 없음, 주문 시점 스냅샷으로 불변성 보장)
```

---

## ID 생성 전략

| 테이블 | 형식 | 생성 위치 |
|--------|------|-----------|
| orders.id | `ord_` + ULID | 애플리케이션 레이어 |
| order_items.id | `oi_` + ULID | 애플리케이션 레이어 |

ULID를 사용하면 시간 순 정렬이 가능하고 UUID와 달리 URL 안전 문자열이다.

---

## 마이그레이션 순서

1. `orders` 테이블 생성
2. `order_items` 테이블 생성 (orders FK 의존)
