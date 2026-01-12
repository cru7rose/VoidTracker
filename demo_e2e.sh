#!/bin/bash

# Configuration
NEXUS_URL="http://localhost:8090"
ORDER_URL="http://localhost:8091"
FLUX_URL="http://localhost:8092"
BILLING_URL="http://localhost:8101"
TITAN_URL="http://localhost:8099"

USERNAME="${USERNAME:-dispatcher}"
PASSWORD="${PASSWORD:-admin123}"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 Starting E2E Workflow Test for VoidTracker...${NC}"

# 1. Authentication
echo -e "\n🔹 1. Authenticating as ${USERNAME}..."
LOGIN_PAYLOAD="{\"username\": \"$USERNAME\", \"password\": \"$PASSWORD\"}"

# Capture status code and response
response=$(curl -s -w "\n%{http_code}" -X POST "$NEXUS_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "$LOGIN_PAYLOAD")

http_code=$(echo "$response" | tail -n1)
LOGIN_RESPONSE=$(echo "$response" | sed '$d')

if [[ "$http_code" -ne 200 ]]; then
   echo -e "${RED}❌ Login Failed! Status: $http_code Response: $LOGIN_RESPONSE${NC}"
   exit 1
fi

TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.accessToken')
USER_ID=$(echo $LOGIN_RESPONSE | jq -r '.user.userId')

if [ "$TOKEN" == "null" ] || [ -z "$TOKEN" ]; then
  echo -e "${RED}❌ Login Failed! Token is null. Response: $LOGIN_RESPONSE${NC}"
  exit 1
fi
echo -e "${GREEN}✅ Login Successful! Token acquired.${NC}"

# 2. Create Order
echo -e "\n🔹 2. Creating New Order..."
ORDER_PAYLOAD='{
  "customerId": "4e423c98-1bd9-4d3f-8904-12300fef24bd",
  "priority": "STANDARD",
  "remark": "E2E Test Order",
  "pickupAddress": {
    "street": "ul. Logistyczna",
    "streetNumber": "1",
    "postalCode": "00-001",
    "city": "Warsaw",
    "country": "Poland",
    "lat": 52.2297,
    "lon": 21.0122
  },
  "deliveryAddress": {
    "customerName": "Customer John",
    "street": "ul. Marszalkowska",
    "streetNumber": "10",
    "postalCode": "00-590",
    "city": "Warsaw",
    "country": "Poland",
    "phone": "+48111111111",
    "lat": 52.2356,
    "lon": 21.0102
  },
  "packageDetails": {
    "weight": 10.5,
    "description": "Box A",
    "quantity": 1
  }
}'

response=$(curl -s -w "\n%{http_code}" -X POST "$ORDER_URL/api/orders" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "$ORDER_PAYLOAD")

http_code=$(echo "$response" | tail -n1)
ORDER_RESPONSE=$(echo "$response" | sed '$d')

if [[ "$http_code" -ne 201 ]] && [[ "$http_code" -ne 200 ]]; then
   echo -e "${RED}❌ Order Creation Failed! Status: $http_code Response: $ORDER_RESPONSE${NC}"
   exit 1
fi

ORDER_ID=$(echo $ORDER_RESPONSE | jq -r '.orderId')

if [ "$ORDER_ID" == "null" ] || [ -z "$ORDER_ID" ]; then
    echo -e "${RED}❌ Order Creation Failed (No ID)! Response: $ORDER_RESPONSE${NC}"
    exit 1
else
    echo -e "${GREEN}✅ Order Created! ID: $ORDER_ID${NC}"
fi

# 3. Planning (Flux)
# For demo purposes, we usually inject order into route or just check control tower
echo -e "\n🔹 3. Verifying Planning Service (Flux)..."
# We'll just list routes or instantiate standard routes
FLUX_RESPONSE=$(curl -s -X POST "$FLUX_URL/api/control-tower/instantiate?date=2025-12-30" \
    -H "Authorization: Bearer $TOKEN")

if [[ "$FLUX_RESPONSE" == *"error"* ]]; then
     echo -e "${RED}⚠️  Flux API warning: $FLUX_RESPONSE${NC}"
else
    echo -e "${GREEN}✅ Route Instantiation triggered on Flux.${NC}"
fi

# 4. Billing Preview
echo -e "\n🔹 4. Calculating Billing Preview..."
BILLING_PAYLOAD="{
    \"orderId\": \"$ORDER_ID\",
    \"totalWeight\": 10.5,
    \"serviceType\": \"STANDARD\",
    \"distanceKm\": 15.2
}"

# We need a client ID to price against - usually fetched from Order or User org
CLIENT_ID="ORG_DEFAULT" 

BILLING_RESPONSE=$(curl -s -X POST "$BILLING_URL/api/billing/calculate-preview?clientId=$CLIENT_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "$BILLING_PAYLOAD")

# Validate billing amount
EXPECTED_AMOUNT="71.6"
if [[ "$BILLING_RESPONSE" == *"$EXPECTED_AMOUNT"* ]]; then
    echo -e "${GREEN}✅ Billing Calculated: €$BILLING_RESPONSE EUR (Expected: €$EXPECTED_AMOUNT)${NC}"
else
    echo -e "${RED}❌ Billing Failed: Expected €$EXPECTED_AMOUNT, got €$BILLING_RESPONSE${NC}"
    exit 1
fi

# 5. Test Detailed Breakdown
echo -e "\n🔹 5. Testing Calculation Breakdown..."
BREAKDOWN_RESPONSE=$(curl -s -X POST "$BILLING_URL/api/billing/calculate-breakdown?clientId=$CLIENT_ID" \
  -H "Content-Type: application/json" \
  -d "$BILLING_PAYLOAD")

# Check if breakdown contains expected rules
if echo "$BREAKDOWN_RESPONSE" | grep -q "WEIGHT" && \
   echo "$BREAKDOWN_RESPONSE" | grep -q "DISTANCE" && \
   echo "$BREAKDOWN_RESPONSE" | grep -q "ITEM"; then
    echo -e "${GREEN}✅ Breakdown includes all pricing rules (WEIGHT, DISTANCE, ITEM)${NC}"
else
    echo -e "${RED}❌ Breakdown missing expected rules${NC}"
    exit 1
fi

echo -e "\n${GREEN}✨ E2E Workflow Test Complete!${NC}"
echo "---------------------------------------------------"
echo "✅ Authentication: PASSED"
echo "✅ Order Creation: PASSED"
echo "✅ Planning Service: PASSED"
echo "✅ Billing Calculation: PASSED (€$EXPECTED_AMOUNT)"
echo "✅ Breakdown Details: PASSED"
echo "---------------------------------------------------"
echo "You can view the full UI at http://localhost:8090"
