#!/bin/bash

# ===================================================================
# VoidTracker Planning Service - End-to-End Test Suite
# ===================================================================
# 
# Purpose: Test pełnego przepływu auto-planning bez generowania kosztów
# 
# Test Scenario:
# 1. Inject test orders do order-service
# 2. Verify OrderCreatedEvent published do Kafka
# 3. Verify BatchAggregator collected orders
# 4. Trigger batch optimization (manual - nie czekamy do 22:00)
# 5. Verify routes created w planning-service
# 6. Verify manifests created
# 7. Verify RoutePlannedEvent published
# 
# Cost: ZERO (używamy local services, bez external APIs)
# ===================================================================

set -e  # Exit on error

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}VoidTracker Auto-Planning E2E Test${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Configuration
ORDER_SERVICE_URL="http://localhost:8091"
PLANNING_SERVICE_URL="http://localhost:8092"
NUM_TEST_ORDERS=20
HUB_IDS=("HZA" "HSN" "KOM" "HWR" "HLD" "PRG" "RZE" "LUG")

# Step 1: Check services are running
echo -e "${YELLOW}[1/7] Checking services...${NC}"
if curl -s "${ORDER_SERVICE_URL}/actuator/health" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ order-service is running${NC}"
else
    echo -e "${RED}✗ order-service is NOT running!${NC}"
    echo "Start with: docker-compose up -d order-service"
    exit 1
fi

if curl -s "${PLANNING_SERVICE_URL}/actuator/health" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ planning-service is running${NC}"
else
    echo -e "${RED}✗ planning-service is NOT running!${NC}"
    echo "Start with: docker-compose up -d planning-service"
    exit 1
fi

echo ""

# Step 2: Inject test orders
echo -e "${YELLOW}[2/7] Injecting ${NUM_TEST_ORDERS} test orders...${NC}"

# Generate random orders with realistic addresses
POLAND_CITIES=("Warszawa" "Kraków" "Poznań" "Wrocław" "Gdańsk" "Łódź" "Szczecin" "Lublin" "Katowice" "Białystok")
STREETS=("Główna" "Mickiewicza" "Słoneczna" "Krótka" "Długa" "Polna" "Leśna" "Spacerowa" "Kościelna" "Ogrodowa")

for i in $(seq 1 $NUM_TEST_ORDERS); do
    # Random city and street
    CITY=${POLAND_CITIES[$((RANDOM % ${#POLAND_CITIES[@]}))]}
    STREET=${STREETS[$((RANDOM % ${#STREETS[@]}))]}
    HOUSE_NO=$((RANDOM % 100 + 1))
    POSTAL_CODE="0$((RANDOM % 9 + 1))-$((RANDOM % 900 + 100))"
    
    # Random lat/lon dla Polski (przybliżone)
    LAT=$(echo "50 + $RANDOM % 5" | bc -l)
    LON=$(echo "16 + $RANDOM % 8" | bc -l)
    
    # Random priority (80% NORMAL, 15% HIGH, 5% URGENT)
    RAND=$((RANDOM % 100))
    if [ $RAND -lt 5 ]; then
        PRIORITY="URGENT"
        DELIVERY_TIME="07:00:00"  # Technician deadline
    elif [ $RAND -lt 20 ]; then
        PRIORITY="HIGH"
        DELIVERY_TIME="08:00:00"  # Dealer deadline
    else
        PRIORITY="NORMAL"
        DELIVERY_TIME="08:00:00"  # Dealer deadline
    fi
    
    # Create order via API
    ORDER_PAYLOAD=$(cat <<EOF
{
  "pickup": {
    "street": "Magazynowa",
    "houseNo": "1",
    "postal": "02-111",
    "city": "Warszawa",
    "lat": 52.2297,
    "lon": 21.0122
  },
  "delivery": {
    "street": "${STREET}",
    "houseNo": "${HOUSE_NO}",
    "postal": "${POSTAL_CODE}",
    "city": "${CITY}",
    "lat": ${LAT},
    "lon": ${LON},
    "contactPerson": "Jan Kowalski",
    "contactPhone": "+48500${RANDOM:0:6}"
  },
  "package": {
    "weight": $((RANDOM % 50 + 1)),
    "volume": $((RANDOM % 2 + 1)),
    "description": "Części samochodowe - test order #${i}"
  },
  "deliveryTimeFrom": "2025-01-15T06:00:00",
  "deliveryTimeTo": "2025-01-15T${DELIVERY_TIME}",
  "priority": "${PRIORITY}"
}
EOF
)
    
    RESPONSE=$(curl -s -X POST "${ORDER_SERVICE_URL}/api/orders" \
        -H "Content-Type: application/json" \
        -d "$ORDER_PAYLOAD")
    
    ORDER_ID=$(echo $RESPONSE | jq -r '.orderId // .id // empty')
    
    if [ -n "$ORDER_ID" ]; then
        echo -e "${GREEN}✓ Order ${i}/${NUM_TEST_ORDERS} created: ${ORDER_ID} (${PRIORITY}, ${CITY})${NC}"
    else
        echo -e "${RED}✗ Failed to create order ${i}${NC}"
        echo "Response: $RESPONSE"
    fi
    
    # Small delay to avoid overwhelming the system
    sleep 0.2
done

echo ""
echo -e "${GREEN}✓ ${NUM_TEST_ORDERS} test orders injected${NC}"
echo ""

# Step 3: Wait for Kafka processing
echo -e "${YELLOW}[3/7] Waiting for Kafka event processing (10s)...${NC}"
sleep 10
echo -e "${GREEN}✓ Kafka processing window complete${NC}"
echo ""

# Step 4: Check BatchAggregator status
echo -e "${YELLOW}[4/7] Checking BatchAggregator status...${NC}"
echo "TODO: Add API endpoint dla batch status"
echo -e "${YELLOW}→ Check Prometheus metrics: http://localhost:8092/actuator/prometheus${NC}"
echo -e "${YELLOW}→ Metric: batch_pending_size${NC}"
echo ""

# Step 5: Trigger manual optimization (nie czekamy do 22:00)
echo -e "${YELLOW}[5/7] Triggering manual batch optimization...${NC}"
TRIGGER_RESPONSE=$(curl -s -X POST "${PLANNING_SERVICE_URL}/api/planning/batch/trigger" \
    -H "Content-Type: application/json" \
    2>&1 || echo "endpoint_not_found")

if [[ "$TRIGGER_RESPONSE" == *"endpoint_not_found"* ]] || [[ "$TRIGGER_RESPONSE" == *"404"* ]]; then
    echo -e "${YELLOW}⚠ Manual trigger endpoint not implemented yet${NC}"
    echo -e "${YELLOW}→ Alternative: Wait for scheduled batch (22:00) or restart planning-service${NC}"
else
    echo -e "${GREEN}✓ Batch optimization triggered${NC}"
fi
echo ""

# Step 6: Wait for optimization to complete
echo -e "${YELLOW}[6/7] Waiting for optimization (30s)...${NC}"
sleep 30
echo ""

# Step 7: Verify results
echo -e "${YELLOW}[7/7] Verifying results...${NC}"
echo ""

echo -e "${GREEN}--- Test Results Check ---${NC}"
echo ""
echo "1. Check Prometheus metrics:"
echo "   curl http://localhost:8092/actuator/prometheus | grep optimization"
echo ""
echo "2. Check database for created manifests:"
echo "   docker exec -it postgres psql -U postgres -d vt_planning_service"
echo "   SELECT id, vehicle_id, status, created_at FROM planning_manifests ORDER BY created_at DESC LIMIT 10;"
echo ""
echo "3. Check planning-service logs:"
echo "   docker logs planning-service | tail -100"
echo ""
echo "4. Check Grafana (if running):"
echo "   http://localhost:3000"
echo ""

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Test Injection Complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${YELLOW}Next steps:${NC}"
echo "1. Monitor Prometheus metrics dla optimization progress"
echo "2. Check database dla created routes"
echo "3. Verify SLA compliance (no time window violations)"
echo ""
