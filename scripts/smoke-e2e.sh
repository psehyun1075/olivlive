#!/usr/bin/env bash
set -euo pipefail

CATALOG_URL="${CATALOG_URL:-http://localhost:8081/api/v1}"
LIVE_URL="${LIVE_URL:-http://localhost:8082/api/v1}"
ORDER_URL="${ORDER_URL:-http://localhost:8083/api/v1}"

echo "[1] create live session"
LIVE_ID=$(curl -s -X POST "$LIVE_URL/live-sessions" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "테스트 라이브",
    "host_id": "host_001"
  }' | jq -r '.id')
echo "LIVE_ID=$LIVE_ID"

echo "[2] create product"
PRODUCT_ID=$(curl -s -X POST "$CATALOG_URL/products" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "무선 이어폰",
    "description": "노이즈 캔슬링 지원 블루투스 이어폰",
    "price": 89000,
    "stock_quantity": 200
  }' | jq -r '.id')
echo "PRODUCT_ID=$PRODUCT_ID"

echo "[3] attach product to READY live session"
ATTACH_STATUS=$(curl -s -o /tmp/attach-ok.json -w "%{http_code}" -X POST "$CATALOG_URL/live-products" \
  -H "Content-Type: application/json" \
  -d "{
    \"live_session_id\": \"$LIVE_ID\",
    \"product_id\": \"$PRODUCT_ID\",
    \"display_order\": 1
  }")
test "$ATTACH_STATUS" = "201"
cat /tmp/attach-ok.json | jq

echo "[4] query live products"
curl -s "$CATALOG_URL/live-products/$LIVE_ID" | jq

echo "[5] go live"
GO_LIVE_STATUS=$(curl -s -o /tmp/go-live.json -w "%{http_code}" -X POST "$LIVE_URL/live-sessions/$LIVE_ID/go-live")
test "$GO_LIVE_STATUS" = "200"
cat /tmp/go-live.json | jq

echo "[6] create second product"
PRODUCT_ID_2=$(curl -s -X POST "$CATALOG_URL/products" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "휴대용 충전기",
    "description": "고속 충전 지원",
    "price": 49000,
    "stock_quantity": 100
  }' | jq -r '.id')
echo "PRODUCT_ID_2=$PRODUCT_ID_2"

echo "[7] attach after go-live must fail with 409"
ATTACH_FAIL_STATUS=$(curl -s -o /tmp/attach-fail.json -w "%{http_code}" -X POST "$CATALOG_URL/live-products" \
  -H "Content-Type: application/json" \
  -d "{
    \"live_session_id\": \"$LIVE_ID\",
    \"product_id\": \"$PRODUCT_ID_2\",
    \"display_order\": 2
  }")
test "$ATTACH_FAIL_STATUS" = "409"
cat /tmp/attach-fail.json || true

echo "[8] attach with non-existent live session must fail with 404"
NOT_FOUND_STATUS=$(curl -s -o /tmp/live-not-found.json -w "%{http_code}" -X POST "$CATALOG_URL/live-products" \
  -H "Content-Type: application/json" \
  -d "{
    \"live_session_id\": \"live_not_exists\",
    \"product_id\": \"$PRODUCT_ID\",
    \"display_order\": 3
  }")
test "$NOT_FOUND_STATUS" = "404"
cat /tmp/live-not-found.json | jq

echo "[9] empty live products returns items: []"
EMPTY_LIVE_ID=$(curl -s -X POST "$LIVE_URL/live-sessions" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "빈 라이브",
    "host_id": "host_002"
  }' | jq -r '.id')
echo "EMPTY_LIVE_ID=$EMPTY_LIVE_ID"
curl -s "$CATALOG_URL/live-products/$EMPTY_LIVE_ID" | jq

echo "[10] create order"
ORDER_CREATE_STATUS=$(curl -sS -o /tmp/order-create.json -w "%{http_code}" -X POST "$ORDER_URL/orders" \
  -H "Content-Type: application/json" \
  -d "{
    \"buyer_id\": \"buyer_001\",
    \"live_session_id\": \"$LIVE_ID\",
    \"items\": [
      {
        \"product_id\": \"$PRODUCT_ID\",
        \"product_name\": \"무선 이어폰\",
        \"unit_price\": 89000,
        \"quantity\": 1
      }
    ]
  }")

test "$ORDER_CREATE_STATUS" = "201"
ORDER_ID=$(jq -er '.id' /tmp/order-create.json)
echo "ORDER_ID=$ORDER_ID"
cat /tmp/order-create.json | jq

echo "[11] get order"
curl -s "$ORDER_URL/orders/$ORDER_ID" | jq