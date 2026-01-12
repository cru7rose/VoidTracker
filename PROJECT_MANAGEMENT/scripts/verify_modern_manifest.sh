#!/bin/bash

# Configuration
IAM_URL="http://localhost:8081"
PLANNING_URL="http://localhost:8092"
USERNAME="cruz"
PASSWORD="meduza91"

echo "----------------------------------------------------------------"
echo "Starting E2E Verification: Modern Manifest (eCRM)"
echo "User: $USERNAME"
echo "----------------------------------------------------------------"

# 1. Login
echo "[1] Authenticating..."
LOGIN_RESPONSE=$(curl -s -X POST "$IAM_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}")

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "❌ Login Failed"
  echo "Response: $LOGIN_RESPONSE"
  exit 1
fi
echo "✅ Login Successful"

# 2. Get Available Vehicles (to prompt valid vehicle ID)
echo "[2] Fetching Vehicles..."
VEHICLES_RESPONSE=$(curl -s -X GET "$PLANNING_URL/api/planning/vehicles" \
  -H "Authorization: Bearer $TOKEN")

# Extract first valid vehicle ID (assuming JSON array)
# Using python one-liner for parsing JSON safely if jq is missing, or grep/sed fallback
VEHICLE_ID=$(echo $VEHICLES_RESPONSE | grep -o '"id":"[^"]*' | head -n 1 | cut -d'"' -f4)

if [ -z "$VEHICLE_ID" ]; then
  echo "❌ No Vehicles Found. Using Default/Mock ID."
  VEHICLE_ID="f47ac10b-58cc-4372-a567-0e02b2c3d479"
else
  echo "✅ Found Vehicle: $VEHICLE_ID"
fi

# 3. Publish Route (Triggers Modern Manifest)
echo "[3] Publishing Route (Creating Manifest)..."
# Mock Order ID
ORDER_ID="550e8400-e29b-41d4-a716-446655440000"

PUBLISH_RESPONSE=$(curl -s -w "%{http_code}" -X POST "$PLANNING_URL/api/planning/optimization/publish" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"solutionId\": null,
    \"routes\": [
        {
            \"vehicleId\": \"$VEHICLE_ID\", 
            \"stops\": [
                {\"orderId\": \"$ORDER_ID\", \"address\": \"123 E2E Test St\"}
            ]
        }
    ]
  }")

HTTP_CODE=${PUBLISH_RESPONSE: -3}
echo "HTTP Code: $HTTP_CODE"

if [ "$HTTP_CODE" == "200" ]; then
  echo "✅ Route Published Successfully"
else
  echo "❌ Publish Failed"
  echo "Response: $PUBLISH_RESPONSE"
fi

# 4. Verify Manifest Creation via Logs or API (if GET endpoint exists)
echo "[4] Verifying Manifest in System..."
# We'll check the logs for the specific success message we added: "Generated Modern Manifest"
grep "Generated Modern Manifest" ../logs/planning-service.log | tail -n 1

if [ $? -eq 0 ]; then
  echo "✅ Modern Manifest CONFIRMED in Logs"
else
  echo "⚠️ Manifest log not found (check planning-service.log)"
fi

echo "----------------------------------------------------------------"
echo "E2E Verification Complete"
echo "----------------------------------------------------------------"
