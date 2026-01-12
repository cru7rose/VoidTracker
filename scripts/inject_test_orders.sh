#!/bin/bash

BASE_URL="http://localhost:8091/api/orders"

for i in {1..5}
do
  LAT=$(echo "52.237049 + ($RANDOM % 100 - 50) * 0.001" | bc)
  LON=$(echo "21.017532 + ($RANDOM % 100 - 50) * 0.001" | bc)
  
  # Ensure valid JSON numbers (bc can output .123 instead of 0.123 which works in JSON usually, but let's be safe)
  # Actually bc output is fine for most parsers.

  # Construct date strings
  NOW=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
  LATER=$(date -u -v+4H +"%Y-%m-%dT%H:%M:%SZ")
  
  echo "Creating Order $i..."
  
  curl -v -X POST "$BASE_URL" \
    -H "Content-Type: application/json" \
    -d '{
      "customerId": "CUST_'$i'",
      "priority": "NORMAL",
      "remark": "E2E Test Order",
      "pickupAddress": {
        "name": "Warehouse",
        "street": "Magazynowa",
        "streetNumber": "1",
        "postalCode": "00-001",
        "city": "Warszawa",
        "country": "PL",
        "lat": 52.237049,
        "lon": 21.017532
      },
      "deliveryAddress": {
        "customerName": "Customer '$i'",
        "street": "Testowa",
        "streetNumber": "1",
        "postalCode": "00-002",
        "city": "Warszawa",
        "country": "PL",
        "lat": '$LAT',
        "lon": '$LON',
        "sla": "'$LATER'"
      },
      "packageDetails": {
        "weight": 10.0,
        "volume": 0.1,
        "count": 1,
        "description": "Test Package"
      },
      "pickupTimeFrom": "'$NOW'",
      "pickupTimeTo": "'$LATER'",
      "deliveryTimeFrom": "'$NOW'",
      "deliveryTimeTo": "'$LATER'"
    }'
    echo ""
done
