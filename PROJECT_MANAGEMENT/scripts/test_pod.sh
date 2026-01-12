#!/bin/bash
ORDER_ID="be5bc4fd-38e0-46d2-b46d-9344236ed1ad"
DRIVER_ID="driver@danxils.com"

echo "1. Logging in Admin..."
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login -H "Content-Type: application/json" -d '{"username": "abcd@1234.123", "password": "abcd4321"}' | jq -r .accessToken)
echo "Admin Token retrieved (Length: ${#ADMIN_TOKEN})"

echo "2. Assigning Driver to Order..."
ASSIGN_RESP=$(curl -s -X POST "http://localhost:8091/api/orders/$ORDER_ID/assign-driver" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"driverId\": \"$DRIVER_ID\"}")
echo "Assign Response: $ASSIGN_RESP"
echo ""

echo "3. Logging in Driver..."
DRIVER_TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login -H "Content-Type: application/json" -d '{"username": "driver@danxils.com", "password": "abcd4321"}' | jq -r .accessToken)
echo "Driver Token retrieved (Length: ${#DRIVER_TOKEN})"

echo "4. Confirming Pickup..."
PICKUP_RESP=$(curl -s -X POST "http://localhost:8091/api/orders/$ORDER_ID/actions/confirm-pickup" \
  -H "Authorization: Bearer $DRIVER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"scannedBarcodes": ["BC-123"]}')
echo "Pickup Response: $PICKUP_RESP"
echo ""

echo "5. Confirming Delivery..."
DELIVERY_RESP=$(curl -s -X POST "http://localhost:8091/api/orders/$ORDER_ID/actions/confirm-delivery" \
  -H "Authorization: Bearer $DRIVER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"signature": "DriverSig", "lat": 50.0, "lng": 19.0, "note": "Delivered", "photos": [], "performedServices": []}')
echo "Delivery Response: $DELIVERY_RESP"
echo ""
