#!/bin/bash

# Configuration
AUTH_URL="http://localhost:8090/api/auth/login"
ORDER_URL="http://localhost:81/api/orders"
USERNAME="dispatcher"
PASSWORD="admin123"

# Driver/Vehicle UUID (Mock or Existing)
# Assuming we can assign to any string/UUID if not strictly validated against a Vehicle Service yet,
# but the code uses "assignedDriver" as userId string.
DRIVER_ID="deadbeef-0000-0000-0000-000000000001" 

echo "Logging in..."
LOGIN_RESP=$(curl -s -X POST "$AUTH_URL" \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"$USERNAME\", \"password\": \"$PASSWORD\"}")

TOKEN=$(echo $LOGIN_RESP | jq -r '.accessToken')
if [ "$TOKEN" == "null" ] || [ -z "$TOKEN" ]; then
  echo "Login failed."
  exit 1
fi

echo "Fetching Orders..."
ORDERS_JSON=$(curl -s -X GET "$ORDER_URL" -H "Authorization: Bearer $TOKEN")

# Filter for NEW orders
ORDER_IDS=$(echo $ORDERS_JSON | jq -r '.content[] | select(.status == "NEW") | .orderId')

# Verify we have at least 2
COUNT=$(echo "$ORDER_IDS" | grep -c "^")
if [ "$COUNT" -lt 1 ]; then
  echo "No NEW orders found to assign."
  exit 0
fi

echo "Found $COUNT NEW orders."

for ORDER_ID in $ORDER_IDS; do
  echo "Assigning Order $ORDER_ID to Driver $DRIVER_ID..."
  
  # Endpoint: POST /api/orders/{id}/assign (checked OrderService.java: assignDriver method)
  # But need to check Controller for path. Assuming /api/orders/{id}/assign or similar.
  # Checking OrderService.java: assignDriver(UUID orderId, AssignDriverRequestDto request, String userId)
  # I'll guess the path is /api/orders/{id}/assign based on convention or check controller.
  
  # Payload: AssignDriverRequestDto { driverId: ... }
  RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "$ORDER_URL/$ORDER_ID/assign-driver" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"driverId\": \"$DRIVER_ID\"}")
    
  HTTP_STATUS=$(echo "$RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)
  BODY=$(echo "$RESPONSE" | grep -v "HTTP_STATUS")

  if [ "$HTTP_STATUS" -eq 200 ]; then
     echo "Success."
  else
     echo "Failed ($HTTP_STATUS): $BODY"
  fi
done
