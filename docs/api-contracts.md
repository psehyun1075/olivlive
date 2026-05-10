
# catalog-service API Contracts

Base Path: `/api/v1`  
Content-Type: `application/json`

---

## Products

### POST /products

상품을 등록한다.

**Request Body**

```json
{
  "name": "무선 이어폰",
  "description": "노이즈 캔슬링 지원 블루투스 이어폰",
  "price": 89000,
  "stock_quantity": 200
}
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| name | string | Y | 1–200자 |
| description | string | N | 최대 2000자 |
| price | integer | Y | > 0, 원 단위 |
| stock_quantity | integer | Y | >= 0 |

**Response: 201 Created**

```json
{
  "id": "prod_01HXYZ",
  "name": "무선 이어폰",
  "description": "노이즈 캔슬링 지원 블루투스 이어폰",
  "price": 89000,
  "stock_quantity": 200,
  "status": "active",
  "created_at": "2026-05-10T09:00:00Z",
  "updated_at": "2026-05-10T09:00:00Z"
}
```

**Error Responses**

| 상태 코드 | 사유 |
|-----------|------|
| 400 | 필수 필드 누락 또는 유효성 실패 |

---

### GET /products

상품 목록을 조회한다.

**Query Parameters**

| 파라미터 | 타입 | 기본값 | 설명 |
|----------|------|--------|------|
| status | string | active | `active` \| `inactive` \| `all` |
| page | integer | 1 | 1-based 페이지 번호 |
| limit | integer | 20 | 페이지당 항목 수, 최대 100 |

**Response: 200 OK**

```json
{
  "items": [
    {
      "id": "prod_01HXYZ",
      "name": "무선 이어폰",
      "price": 89000,
      "stock_quantity": 200,
      "status": "active",
      "created_at": "2026-05-10T09:00:00Z",
      "updated_at": "2026-05-10T09:00:00Z"
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 1
  }
}
```

목록 응답에서 `description` 필드는 포함하지 않는다.

---

### GET /products/{id}

상품 단건을 조회한다.

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| id | string | 상품 ID |

**Response: 200 OK**

```json
{
  "id": "prod_01HXYZ",
  "name": "무선 이어폰",
  "description": "노이즈 캔슬링 지원 블루투스 이어폰",
  "price": 89000,
  "stock_quantity": 200,
  "status": "active",
  "created_at": "2026-05-10T09:00:00Z",
  "updated_at": "2026-05-10T09:00:00Z"
}
```

**Error Responses**

| 상태 코드 | 사유 |
|-----------|------|
| 404 | 해당 ID의 상품 없음 |

---

## Live Products

### POST /live-products

라이브 세션에 상품을 매핑한다.

**Request Body**

```json
{
  "live_session_id": "live_ABC123",
  "product_id": "prod_01HXYZ",
  "display_order": 1
}
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| live_session_id | string | Y | non-empty string; 형식·길이는 catalog-service가 검증하지 않음 |
| product_id | string | Y | 존재하는 products.id, status = active |
| display_order | integer | Y | >= 1, 같은 세션 내 노출 순서 |

**검증 단계**

catalog-service는 매핑을 저장하기 전에 아래 순서로 검증한다.

1. **상품 존재 확인** — catalog-service 자체 DB에서 `product_id` 조회. 없으면 `404 PRODUCT_NOT_FOUND`.
   `live_session_id`는 이 단계에서 형식·길이를 검증하지 않는다. non-empty 여부만 확인한다.
2. **상품 활성 상태 확인** — `products.status = active`가 아니면 `400 INVALID_PRODUCT_STATUS`.
3. **라이브 세션 존재 확인** — live-control-service API를 호출해 `live_session_id` 유효성 검증. 세션이 없으면 `404 LIVE_SESSION_NOT_FOUND`.
4. **라이브 세션 attach 가능 상태 확인** — live-control-service가 반환한 세션 상태가 상품 추가를 허용하지 않으면 `409 LIVE_SESSION_NOT_ATTACHABLE`.
5. **중복 매핑 확인** — `(live_session_id, product_id)` 쌍이 이미 존재하면 `409 DUPLICATE_LIVE_PRODUCT`.

> catalog-service는 라이브 세션 상태를 소유하지 않는다.
> 세션 존재 여부와 attach 가능 여부는 live-control-service가 단독으로 판단하며,
> catalog-service는 그 결과를 신뢰하고 에러를 전파한다.

**Response: 201 Created**

```json
{
  "id": "lp_01HABC",
  "live_session_id": "live_ABC123",
  "product_id": "prod_01HXYZ",
  "display_order": 1,
  "is_active": true,
  "created_at": "2026-05-10T09:05:00Z"
}
```

**Error Responses**

| 상태 코드 | 에러 코드 | 사유 |
|-----------|-----------|------|
| 400 | `INVALID_REQUEST` | 필수 필드 누락, live_session_id 빈 문자열, 또는 display_order < 1 |
| 400 | `INVALID_PRODUCT_STATUS` | product_id가 inactive 상품을 참조 |
| 404 | `PRODUCT_NOT_FOUND` | product_id에 해당하는 상품 없음 |
| 404 | `LIVE_SESSION_NOT_FOUND` | live_session_id가 live-control-service에 존재하지 않음 |
| 409 | `LIVE_SESSION_NOT_ATTACHABLE` | 세션 상태가 상품 추가를 허용하지 않음 (예: ended, archived) |
| 409 | `DUPLICATE_LIVE_PRODUCT` | (live_session_id, product_id) 쌍이 이미 존재 |

---

### GET /live-products/{liveSessionId}

특정 라이브 세션의 노출 상품 목록을 조회한다.

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| liveSessionId | string | 라이브 세션 ID |

**Query Parameters**

| 파라미터 | 타입 | 기본값 | 설명 |
|----------|------|--------|------|
| is_active | boolean | true | `true`: 활성 매핑만, `false`: 비활성만, 생략 시 true |

**Response: 200 OK**

```json
{
  "live_session_id": "live_ABC123",
  "items": [
    {
      "id": "lp_01HABC",
      "product_id": "prod_01HXYZ",
      "name": "무선 이어폰",
      "price": 89000,
      "stock_quantity": 200,
      "display_order": 1,
      "is_active": true,
      "created_at": "2026-05-10T09:05:00Z"
    }
  ]
}
```

결과는 `display_order` 오름차순으로 정렬된다.  
세션에 매핑된 상품이 없으면 `items: []`를 반환하고 404를 반환하지 않는다.

---

## 공통 에러 형식

```json
{
  "error": {
    "code": "PRODUCT_NOT_FOUND",
    "message": "Product not found"
  }
}
```

| 필드 | 설명 |
|------|------|
| code | 기계가 파싱 가능한 에러 식별자 |
| message | 사람이 읽을 수 있는 설명 |
