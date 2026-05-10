# order-service API Contracts

Base Path: `/api/v1`  
Content-Type: `application/json`

---

## Orders

### POST /orders

주문을 생성한다. mock 결제가 동기적으로 처리되어 항상 `PAID` 상태로 응답한다.

**Request Body**

```json
{
  "buyer_id": "user_123",
  "live_session_id": "live_ABC123",
  "items": [
    {
      "product_id": "prod_01HXYZ",
      "product_name": "무선 이어폰",
      "unit_price": 89000,
      "quantity": 2
    }
  ]
}
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| buyer_id | string | Y | 1–100자 |
| live_session_id | string | N | opaque 문자열; order-service는 형식·유효성을 검증하지 않음 |
| items | array | Y | 1개 이상 |
| items[].product_id | string | Y | non-empty string; catalog-service의 opaque identifier |
| items[].product_name | string | Y | 1–200자; 주문 시점 스냅샷 |
| items[].unit_price | integer | Y | > 0, 원 단위; 주문 시점 스냅샷 |
| items[].quantity | integer | Y | >= 1 |

> `product_name`과 `unit_price`는 클라이언트가 체크아웃 시점에 전달한다.
> order-service는 catalog-service DB를 직접 참조하지 않는다.

**Response: 201 Created**

```json
{
  "id": "ord_01HXYZ",
  "buyer_id": "user_123",
  "live_session_id": "live_ABC123",
  "status": "PAID",
  "total_amount": 178000,
  "payment_status": "MOCK_PAID",
  "items": [
    {
      "id": "oi_01HABC",
      "product_id": "prod_01HXYZ",
      "product_name": "무선 이어폰",
      "unit_price": 89000,
      "quantity": 2,
      "subtotal": 178000
    }
  ],
  "created_at": "2026-05-10T09:00:00Z"
}
```

`total_amount`는 모든 아이템 `subtotal`의 합이다. `subtotal`은 `unit_price × quantity`로 계산한다.

**Error Responses**

| 상태 코드 | 에러 코드 | 사유 |
|-----------|-----------|------|
| 400 | `INVALID_REQUEST` | 필수 필드 누락, items 빈 배열, quantity < 1, unit_price <= 0 |

---

### GET /orders/{id}

주문 단건을 조회한다.

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| id | string | 주문 ID |

**Response: 200 OK**

```json
{
  "id": "ord_01HXYZ",
  "buyer_id": "user_123",
  "live_session_id": "live_ABC123",
  "status": "PAID",
  "total_amount": 178000,
  "payment_status": "MOCK_PAID",
  "items": [
    {
      "id": "oi_01HABC",
      "product_id": "prod_01HXYZ",
      "product_name": "무선 이어폰",
      "unit_price": 89000,
      "quantity": 2,
      "subtotal": 178000
    }
  ],
  "created_at": "2026-05-10T09:00:00Z"
}
```

**Error Responses**

| 상태 코드 | 에러 코드 | 사유 |
|-----------|-----------|------|
| 404 | `ORDER_NOT_FOUND` | 해당 ID의 주문 없음 |

---

## 공통 에러 형식

```json
{
  "error": {
    "code": "ORDER_NOT_FOUND",
    "message": "Order not found"
  }
}
```

| 필드 | 설명 |
|------|------|
| code | 기계가 파싱 가능한 에러 식별자 |
| message | 사람이 읽을 수 있는 설명 |
