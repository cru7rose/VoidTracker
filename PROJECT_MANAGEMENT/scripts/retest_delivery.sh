#!/bin/bash
ORDER_ID="be5bc4fd-38e0-46d2-b46d-9344236ed1ad"
DRIVER_ID="driver@danxils.com"

echo "1. Resetting Order Status to LOAD..."
psql -h localhost -p 5433 -U danxils -d vt_order_service -c "UPDATE vt_orders SET status = 'LOAD' WHERE order_id = '$ORDER_ID';"

echo "2. Logging in Driver..."
DRIVER_TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login -H "Content-Type: application/json" -d '{"username": "driver@danxils.com", "password": "abcd4321"}' | jq -r .accessToken)

echo "3. Confirming Delivery..."
curl -v -X POST "http://localhost:8091/api/orders/$ORDER_ID/actions/confirm-delivery" \
  -H "Authorization: Bearer $DRIVER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "signature": "base64encodedSignatureData",
    "photos": ["photo1_url", "photo2_url"],
    "lat": 52.2297,
    "lng": 21.0122,
    "note": "Left at front door",
    "performedServices": [
      {
        "serviceCode": "COD",
        "result": "150.00"
      }
    ]
  }' | jq
