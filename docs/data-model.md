# catalog-service Data Model

DB: PostgreSQL

---

## 테이블: products

상품 정보를 저장한다.

```sql
CREATE TABLE products (
  id               VARCHAR(32)      PRIMARY KEY,
  name             VARCHAR(200)     NOT NULL,
  description      TEXT,
  price            INTEGER          NOT NULL CHECK (price > 0),
  stock_quantity   INTEGER          NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
  status           VARCHAR(20)      NOT NULL DEFAULT 'active',
  created_at       TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
  updated_at       TIMESTAMPTZ      NOT NULL DEFAULT NOW()
);
```

### 컬럼 상세

| 컬럼 | 타입 | Nullable | 기본값 | 설명 |
|------|------|----------|--------|------|
| id | VARCHAR(32) | N | — | ULID 형식, 애플리케이션에서 생성 |
| name | VARCHAR(200) | N | — | 상품명 |
| description | TEXT | Y | NULL | 상품 설명 |
| price | INTEGER | N | — | 판매 가격 (원 단위), 0 초과 |
| stock_quantity | INTEGER | N | 0 | 재고 수량, 0 이상 |
| status | VARCHAR(20) | N | 'active' | `active` \| `inactive` |
| created_at | TIMESTAMPTZ | N | NOW() | 생성 시각 |
| updated_at | TIMESTAMPTZ | N | NOW() | 최종 수정 시각 |

### 제약 조건

| 이름 | 종류 | 대상 |
|------|------|------|
| products_pkey | PRIMARY KEY | id |
| products_price_check | CHECK | price > 0 |
| products_stock_check | CHECK | stock_quantity >= 0 |

### 인덱스

| 이름 | 컬럼 | 목적 |
|------|------|------|
| idx_products_status | status | GET /products?status= 필터 |
| idx_products_created_at | created_at DESC | 목록 정렬 |

---

## 테이블: live_products

라이브 세션과 상품 간의 노출 매핑을 저장한다.

`live_session_id`는 live-control-service가 소유하는 opaque identifier다.
catalog-service는 이 값의 형식이나 길이 규칙을 정의하지 않는다.
cross-service DB foreign key는 두지 않으며, POST /live-products 요청 시
live-control-service API를 호출해 세션 존재 여부와 attach 가능 상태를 확인함으로써
애플리케이션 레벨에서 일관성을 보장한다.

```sql
CREATE TABLE live_products (
  id               VARCHAR(32)      PRIMARY KEY,
  live_session_id  TEXT             NOT NULL,
  product_id       VARCHAR(32)      NOT NULL REFERENCES products(id),
  display_order    INTEGER          NOT NULL CHECK (display_order >= 1),
  is_active        BOOLEAN          NOT NULL DEFAULT TRUE,
  created_at       TIMESTAMPTZ      NOT NULL DEFAULT NOW()
);
```

### 컬럼 상세

| 컬럼 | 타입 | Nullable | 기본값 | 설명 |
|------|------|----------|--------|------|
| id | VARCHAR(32) | N | — | ULID 형식, 애플리케이션에서 생성 |
| live_session_id | TEXT | N | — | live-control-service가 소유하는 opaque identifier; catalog-service는 형식·길이를 정의하지 않음 |
| product_id | VARCHAR(32) | N | — | products.id 참조 |
| display_order | INTEGER | N | — | 세션 내 노출 순서, 1 이상 |
| is_active | BOOLEAN | N | TRUE | 매핑 활성 여부 |
| created_at | TIMESTAMPTZ | N | NOW() | 생성 시각 |

### 제약 조건

| 이름 | 종류 | 대상 |
|------|------|------|
| live_products_pkey | PRIMARY KEY | id |
| live_products_product_fk | FOREIGN KEY | product_id → products(id) |
| live_products_unique_mapping | UNIQUE | (live_session_id, product_id) |
| live_products_order_check | CHECK | display_order >= 1 |

### 인덱스

| 이름 | 컬럼 | 목적 |
|------|------|------|
| idx_live_products_session | live_session_id, is_active | GET /live-products/{liveSessionId} |
| idx_live_products_order | live_session_id, display_order | 노출 순서 정렬 |

---

## 테이블 관계

```
products (1) ──────── (N) live_products
    id ◄────────────────── product_id

live_products.live_session_id
    └─ application-level 참조 → live-control-service 세션 ID
       (DB FK 없음, catalog-service 내 조인 불필요)
```

---

## ID 생성 전략

| 테이블 | 형식 | 생성 위치 |
|--------|------|-----------|
| products.id | `prod_` + ULID | 애플리케이션 레이어 |
| live_products.id | `lp_` + ULID | 애플리케이션 레이어 |

ULID를 사용하면 시간 순 정렬이 가능하고 UUID와 달리 URL 안전 문자열이다.

---

## 마이그레이션 순서

1. `products` 테이블 생성
2. `live_products` 테이블 생성 (products FK 의존)
