#!/bin/bash

# Base URLs
ORDER_SERVICE_URL="http://localhost:8091/api/ingestion/orders"
PLANNING_SERVICE_URL="http://localhost:8093/optimization/optimize"

echo "🚀 Injecting 5 Test Orders into Order Service..."

# Order 1: Centrum Science Tower
curl -X POST "$ORDER_SERVICE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "externalId": "VOID-001",
    "clientExternalId": "CLI-TEST",
    "properties": {
        "deliveryAddress": {
            "city": "Warsaw",
            "street": "Złota",
            "streetNumber": "44",
            "postalCode": "00-120",
            "lat": 52.2297,
            "lon": 21.0122
        },
        "packageDetails": {
            "weight": 5.0,
            "volume": 0.1
        },
        "deliveryTimeFrom": "2026-06-01T08:00:00Z",
        "deliveryTimeTo": "2026-06-01T16:00:00Z"
    }
}'
echo -e "\nOrder 1 Sent."

# Order 2: Mokotow Plaza
curl -X POST "$ORDER_SERVICE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "externalId": "VOID-002",
    "clientExternalId": "CLI-TEST",
    "properties": {
        "deliveryAddress": {
            "city": "Warsaw",
            "street": "Postępu",
            "streetNumber": "15",
            "postalCode": "02-676",
            "lat": 52.1833,
            "lon": 21.0000
        },
        "packageDetails": {
            "weight": 12.0,
            "volume": 0.3
        },
        "deliveryTimeFrom": "2026-06-01T09:00:00Z",
        "deliveryTimeTo": "2026-06-01T17:00:00Z"
    }
}'
echo -e "\nOrder 2 Sent."

# Order 3: Praga Koneser
curl -X POST "$ORDER_SERVICE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "externalId": "VOID-003",
    "clientExternalId": "CLI-TEST",
    "properties": {
        "deliveryAddress": {
            "city": "Warsaw",
            "street": "Plac Konesera",
            "streetNumber": "2",
            "postalCode": "03-736",
            "lat": 52.2550,
            "lon": 21.0425
        },
        "packageDetails": {
            "weight": 2.5,
            "volume": 0.05
        },
        "deliveryTimeFrom": "2026-06-01T10:00:00Z",
        "deliveryTimeTo": "2026-06-01T14:00:00Z"
    }
}'
echo -e "\nOrder 3 Sent."

# Order 4: Wola Spire
curl -X POST "$ORDER_SERVICE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "externalId": "VOID-004",
    "clientExternalId": "CLI-TEST",
    "properties": {
        "deliveryAddress": {
            "city": "Warsaw",
            "street": "Plac Europejski",
            "streetNumber": "1",
            "postalCode": "00-844",
            "lat": 52.2331,
            "lon": 20.9844
        },
        "packageDetails": {
            "weight": 8.0,
            "volume": 0.2
        },
        "deliveryTimeFrom": "2026-06-01T08:30:00Z",
        "deliveryTimeTo": "2026-06-01T15:00:00Z"
    }
}'
echo -e "\nOrder 4 Sent."

# Order 5: Wilanow Palace
curl -X POST "$ORDER_SERVICE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "externalId": "VOID-005",
    "clientExternalId": "CLI-TEST",
    "properties": {
        "deliveryAddress": {
            "city": "Warsaw",
            "street": "Stanisława Kostki Potockiego",
            "streetNumber": "10",
            "postalCode": "02-958",
            "lat": 52.1643,
            "lon": 21.0894
        },
        "packageDetails": {
            "weight": 20.0,
            "volume": 0.5
        },
        "deliveryTimeFrom": "2026-06-01T11:00:00Z",
        "deliveryTimeTo": "2026-06-01T18:00:00Z"
    }
}'
echo -e "\nOrder 5 Sent."

echo -e "\n\n🧠 Triggering VrpOptimizer (Planning Service)..."
curl -X POST "$PLANNING_SERVICE_URL" \
  -H "Content-Type: application/json" \
  -d '{}'

echo -e "\n\n✅ Done! Check 'Dispatch' -> 'Void-Map' in the UI to see the optimized routes."
