#!/bin/bash

# Configuration
# Using the Gateway port 81 for all services
AUTH_URL="http://localhost:81/api/auth/login"
ORDER_URL="http://localhost:81/api/orders"

USERNAME="dispatcher"
PASSWORD="admin123"

# 1. Login
echo "Logging in..."
LOGIN_RESP=$(curl -s -X POST "$AUTH_URL" \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"$USERNAME\", \"password\": \"$PASSWORD\"}")

# Extract token using jq (ensure jq is installed)
TOKEN=$(echo $LOGIN_RESP | jq -r '.accessToken')

if [ "$TOKEN" == "null" ] || [ -z "$TOKEN" ]; then
  echo "Login failed: $LOGIN_RESP"
  exit 1
fi
echo "Login successful. Token acquired."

# 2. Create Order A (Small)
echo "Creating Order A..."
ORDER_A_PAYLOAD='{
  "customerId": "00000000-0000-0000-0000-000000000001",
  "priority": "STANDARD",
  "remark": "Order A - Small - E2E Test",
  "pickupAddress": {
    "street": "ul. Prosta",
    "streetNumber": "1",
    "postalCode": "00-001",
    "city": "Warsaw",
    "country": "POL",
    "lat": 52.2297,
    "lon": 21.0122
  },
  "deliveryAddress": {
    "customerName": "Receiver A",
    "street": "ul. Zlota",
    "streetNumber": "44",
    "postalCode": "00-120",
    "city": "Warsaw",
    "country": "POL",
    "phone": "+48111111111",
    "lat": 52.2320,
    "lon": 21.0060
  },
  "packageDetails": {
    "weight": 1.0,
    "description": "Small Box",
    "quantity": 1,
    "dimensions": "10x10x10"
  }
}'

curl -s -X POST "$ORDER_URL" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "$ORDER_A_PAYLOAD" | jq .id

# 3. Create Order B (Large)
echo -e "\nCreating Order B..."
ORDER_B_PAYLOAD='{
  "customerId": "00000000-0000-0000-0000-000000000001",
  "priority": "HIGH",
  "remark": "Order B - Large - E2E Test",
  "pickupAddress": {
    "street": "ul. Prosta",
    "streetNumber": "1",
    "postalCode": "00-001",
    "city": "Warsaw",
    "country": "POL",
    "lat": 52.2297,
    "lon": 21.0122
  },
  "deliveryAddress": {
    "customerName": "Receiver B",
    "street": "ul. Wilcza",
    "streetNumber": "10",
    "postalCode": "00-500",
    "city": "Warsaw",
    "country": "POL",
    "phone": "+48222222222",
    "lat": 52.2250,
    "lon": 21.0200
  },
  "packageDetails": {
    "weight": 50.0,
    "description": "Large Box",
    "quantity": 1,
    "dimensions": "50x50x50"
  }
}'

curl -s -X POST "$ORDER_URL" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "$ORDER_B_PAYLOAD" | jq .id

echo -e "\nOrders created."
