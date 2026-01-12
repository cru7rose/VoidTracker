#!/bin/bash

# Configuration
API_URL="http://localhost:8091/api"
PLANNING_URL="http://localhost:8093/api"

echo "🧪 Starting Phase 2 Verification..."

# 1. Ingest Test Orders
echo "📦 Ingesting 3 Test Orders..."

cat > order1.json <<EOF
{
  "externalId": "TEST-ORD-001",
  "clientExternalId": "CLIENT-001",
  "properties": {
    "weight": 10.5,
    "volume": 0.5,
    "delivery_lat": 52.2319,
    "delivery_lon": 21.0067,
    "delivery_city": "Warsaw",
    "delivery_address": "Złota 44"
  }
}
EOF

cat > order2.json <<EOF
{
  "externalId": "TEST-ORD-002",
  "clientExternalId": "CLIENT-001",
  "properties": {
    "weight": 5.0,
    "volume": 0.2,
    "delivery_lat": 52.2297,
    "delivery_lon": 21.0122,
    "delivery_city": "Warsaw",
    "delivery_address": "Aleje Jerozolimskie 65"
  }
}
EOF

cat > order3.json <<EOF
{
  "externalId": "TEST-ORD-003",
  "clientExternalId": "CLIENT-001",
  "properties": {
    "weight": 15.0,
    "volume": 1.0,
    "delivery_lat": 52.2400,
    "delivery_lon": 21.0200,
    "delivery_city": "Warsaw",
    "delivery_address": "Dobra 56"
  }
}
EOF

ID1=$(curl -s -X POST -H "Content-Type: application/json" -d @order1.json $API_URL/ingestion/orders)
echo "  ✅ Order 1 Created: $ID1"
ID2=$(curl -s -X POST -H "Content-Type: application/json" -d @order2.json $API_URL/ingestion/orders)
echo "  ✅ Order 2 Created: $ID2"
ID3=$(curl -s -X POST -H "Content-Type: application/json" -d @order3.json $API_URL/ingestion/orders)
echo "  ✅ Order 3 Created: $ID3"

# 2. Trigger Optimization
echo "🚀 Triggering Optimization..."

# Clean IDs (remove quotes if any)
ID1=$(echo $ID1 | tr -d '"')
ID2=$(echo $ID2 | tr -d '"')
ID3=$(echo $ID3 | tr -d '"')

cat > optimize.json <<EOF
{
  "model": "SOLVERTECH",
  "orderIds": ["$ID1", "$ID2", "$ID3"]
}
EOF

RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d @optimize.json $PLANNING_URL/optimization/optimize)
echo "  ✅ Optimization Triggered. Response length: ${#RESPONSE}"

# 3. Check Logs for Verification
echo "🔍 Checking Logs..."
sleep 5

echo "--- GATEKEEPER SERVICE LOGS ---"
grep "Gatekeeper" logs/planning-service.log | tail -n 5

echo "--- WEBSOCKET BROADCAST LOGS ---"
grep "WebSocket" logs/planning-service.log | tail -n 5
grep "SimpMessagingTemplate" logs/planning-service.log | tail -n 5

echo "--- OPTIMIZATION LOGS ---"
grep "Timefold" logs/planning-service.log | tail -n 5

# Cleanup
rm order1.json order2.json order3.json optimize.json
