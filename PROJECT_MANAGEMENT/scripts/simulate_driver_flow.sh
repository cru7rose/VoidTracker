#!/bin/bash

# Configuration
API_URL="http://localhost:8080/api"
DRIVER_ID="driver-123"
TOKEN="mock-jwt-token-integration-test"

echo "Starting Integration Test: Driver Flow Simulation"
echo "------------------------------------------------"

# 1. Create Order (Simulated via Order Service)
echo "1. Creating Test Order..."
# Note: In a real scenario, we'd POST to /api/orders. 
# For now, we assume an order exists or we create one via the dashboard API if available.
# Let's try to create one directly if the endpoint exists.
ORDER_ID=$(curl -s -X POST "$API_URL/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "customerName": "Test Customer",
    "deliveryAddress": {
      "street": "Test St",
      "streetNumber": "1",
      "city": "Test City",
      "zipCode": "00-000",
      "country": "PL"
    },
    "packages": [
      {
        "barcode": "PKG-001",
        "weight": 1.5
      }
    ]
  }' | jq -r '.orderId')

if [ "$ORDER_ID" == "null" ] || [ -z "$ORDER_ID" ]; then
    echo "Failed to create order. Using a placeholder UUID for testing endpoints (expecting 404 if not found)."
    ORDER_ID="00000000-0000-0000-0000-000000000000"
else
    echo "Order Created: $ORDER_ID"
fi

echo "------------------------------------------------"

# 2. Assign Driver (Simulated)
echo "2. Assigning Driver $DRIVER_ID to Order $ORDER_ID..."
curl -s -X POST "$API_URL/orders/$ORDER_ID/assign" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"driverId\": \"$DRIVER_ID\"}"
echo -e "\n"

echo "------------------------------------------------"

# 3. Driver: Get My Tasks
echo "3. Driver fetching tasks..."
curl -s -X GET "$API_URL/driver/tasks?driverId=$DRIVER_ID" \
  -H "Authorization: Bearer $TOKEN"
echo -e "\n"

echo "------------------------------------------------"

# 4. Driver: Confirm Pickup
echo "4. Driver confirming pickup..."
curl -s -X POST "$API_URL/driver/tasks/$ORDER_ID/confirm-pickup" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "barcodes": ["PKG-001"]
  }'
echo -e "\n"

echo "------------------------------------------------"

# 5. Driver: Submit ePoD (Signature + Photo)
echo "5. Driver submitting ePoD..."
curl -s -X POST "$API_URL/driver/tasks/$ORDER_ID/epod" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "signature": "base64_signature_data",
    "photos": ["http://example.com/photo1.jpg"],
    "location": {
      "lat": 52.2297,
      "lng": 21.0122
    },
    "timestamp": "'$(date -u +"%Y-%m-%dT%H:%M:%SZ")'"
  }'
echo -e "\n"

echo "------------------------------------------------"

# 6. Driver: Confirm Delivery
echo "6. Driver confirming delivery..."
curl -s -X POST "$API_URL/driver/tasks/$ORDER_ID/confirm-delivery" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "deliveredBarcodes": ["PKG-001"],
    "performedServices": []
  }'
echo -e "\n"

echo "------------------------------------------------"
echo "Integration Test Completed."
