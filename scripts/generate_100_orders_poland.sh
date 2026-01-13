#!/bin/bash

# Script to generate 100+ test orders across Poland
# Uses real Polish cities and addresses

AUTH_URL="http://localhost:8081/api/auth/login"
ORDER_URL="http://localhost:8091/api/orders"

USERNAME="cruz"
PASSWORD="meduza91"

echo "🔐 Logging in..."
LOGIN_RESP=$(curl -s -X POST "$AUTH_URL" \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"$USERNAME\", \"password\": \"$PASSWORD\"}")

TOKEN=$(echo $LOGIN_RESP | jq -r '.accessToken // .token // empty')

if [ -z "$TOKEN" ] || [ "$TOKEN" == "null" ]; then
  echo "❌ Login failed: $LOGIN_RESP"
  exit 1
fi
echo "✅ Login successful"

# Polish cities with coordinates (lat, lon)
declare -a CITIES=(
  "Warsaw:52.2297:21.0122"
  "Krakow:50.0647:19.9450"
  "Gdansk:54.3520:18.6466"
  "Wroclaw:51.1079:17.0385"
  "Poznan:52.4064:16.9252"
  "Lodz:51.7592:19.4560"
  "Katowice:50.2649:19.0238"
  "Lublin:51.2465:22.5684"
  "Bialystok:53.1325:23.1688"
  "Szczecin:53.4285:14.5528"
  "Bydgoszcz:53.1235:18.0084"
  "Torun:53.0138:18.5984"
  "Radom:51.4025:21.1471"
  "Sosnowiec:50.2863:19.1040"
  "Kielce:50.8661:20.6286"
  "Gliwice:50.2945:18.6714"
  "Zabrze:50.3249:18.7857"
  "Bytom:50.3480:18.9328"
  "Olsztyn:53.7784:20.4801"
  "Rzeszow:50.0412:21.9991"
  "Ruda:50.2584:18.8563"
  "Rybnik:50.0971:18.5418"
  "Tychy:50.1239:18.9844"
  "Dabrowa:50.3249:19.1874"
  "Elblag:54.1561:19.4045"
  "Opole:50.6711:17.9261"
  "Plock:52.5464:19.7064"
  "Walbrzych:50.7714:16.2844"
  "Gorzow:52.7368:15.2288"
  "Zielona:51.9356:15.5064"
)

# Streets in major cities
declare -a STREETS=(
  "ul. Marszalkowska"
  "ul. Nowy Swiat"
  "ul. Krakowskie Przedmiescie"
  "ul. Jerozolimskie"
  "ul. Grunwaldzka"
  "ul. Dluga"
  "ul. Swietokrzyska"
  "ul. Pilsudskiego"
  "ul. Mickiewicza"
  "ul. Sienkiewicza"
  "ul. Krolewska"
  "ul. Wroclawska"
  "ul. Warszawska"
  "ul. Grodzka"
  "ul. Florianska"
)

# Package weights (kg)
declare -a WEIGHTS=(1.5 2.0 3.5 5.0 7.5 10.0 15.0 20.0 25.0 30.0)

# Priorities
declare -a PRIORITIES=("NORMAL" "HIGH" "LOW")

# Customer IDs (will auto-create if not exists)
CUSTOMER_ID="TEST-CUSTOMER-001"

echo "📦 Generating 100+ orders across Poland..."
SUCCESS=0
FAILED=0

for i in {1..120}; do
  # Pick random city for pickup
  PICKUP_CITY=${CITIES[$RANDOM % ${#CITIES[@]}]}
  PICKUP_NAME=$(echo $PICKUP_CITY | cut -d: -f1)
  PICKUP_LAT=$(echo $PICKUP_CITY | cut -d: -f2)
  PICKUP_LON=$(echo $PICKUP_CITY | cut -d: -f3)
  
  # Pick different city for delivery
  DELIVERY_CITY=${CITIES[$RANDOM % ${#CITIES[@]}]}
  while [ "$DELIVERY_CITY" == "$PICKUP_CITY" ]; do
    DELIVERY_CITY=${CITIES[$RANDOM % ${#CITIES[@]}]}
  done
  DELIVERY_NAME=$(echo $DELIVERY_CITY | cut -d: -f1)
  DELIVERY_LAT=$(echo $DELIVERY_CITY | cut -d: -f2)
  DELIVERY_LON=$(echo $DELIVERY_CITY | cut -d: -f3)
  
  # Random street
  PICKUP_STREET=${STREETS[$RANDOM % ${#STREETS[@]}]}
  DELIVERY_STREET=${STREETS[$RANDOM % ${#STREETS[@]}]}
  
  # Random house numbers
  PICKUP_NUM=$((RANDOM % 200 + 1))
  DELIVERY_NUM=$((RANDOM % 200 + 1))
  
  # Random postal codes (format: XX-XXX)
  PICKUP_POSTAL=$(printf "%02d-%03d" $((RANDOM % 90 + 10)) $((RANDOM % 999 + 1)))
  DELIVERY_POSTAL=$(printf "%02d-%03d" $((RANDOM % 90 + 10)) $((RANDOM % 999 + 1)))
  
  # Random weight
  WEIGHT=${WEIGHTS[$RANDOM % ${#WEIGHTS[@]}]}
  
  # Random priority
  PRIORITY=${PRIORITIES[$RANDOM % ${#PRIORITIES[@]}]}
  
  # Calculate delivery time (tomorrow 9:00-17:00)
  TOMORROW=$(date -d "tomorrow" +%Y-%m-%d)
  DELIVERY_FROM="${TOMORROW}T09:00:00Z"
  DELIVERY_TO="${TOMORROW}T17:00:00Z"
  
  # Create order payload
  ORDER_PAYLOAD=$(cat <<EOF
{
  "customerId": "$CUSTOMER_ID",
  "priority": "$PRIORITY",
  "externalReference": "TEST-ORD-$(printf %04d $i)",
  "pickupAddress": {
    "street": "$PICKUP_STREET",
    "streetNumber": "$PICKUP_NUM",
    "postalCode": "$PICKUP_POSTAL",
    "city": "$PICKUP_NAME",
    "country": "Polska",
    "lat": $PICKUP_LAT,
    "lon": $PICKUP_LON
  },
  "deliveryAddress": {
    "customerName": "Test Customer $i",
    "street": "$DELIVERY_STREET",
    "streetNumber": "$DELIVERY_NUM",
    "postalCode": "$DELIVERY_POSTAL",
    "city": "$DELIVERY_NAME",
    "country": "Polska",
    "phone": "+48$((RANDOM % 900000000 + 100000000))",
    "lat": $DELIVERY_LAT,
    "lon": $DELIVERY_LON,
    "sla": "$DELIVERY_TO"
  },
  "packageDetails": {
    "weight": $WEIGHT,
    "description": "Test Package $i",
    "quantity": 1
  },
  "deliveryTimeFrom": "$DELIVERY_FROM",
  "deliveryTimeTo": "$DELIVERY_TO"
}
EOF
)
  
  # Create order
  RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$ORDER_URL" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "$ORDER_PAYLOAD")
  
  HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
  BODY=$(echo "$RESPONSE" | head -n-1)
  
  if [ "$HTTP_CODE" == "201" ] || [ "$HTTP_CODE" == "200" ]; then
    SUCCESS=$((SUCCESS + 1))
    if [ $((i % 10)) -eq 0 ]; then
      echo "✅ Created $i orders ($SUCCESS successful, $FAILED failed)"
    fi
  else
    FAILED=$((FAILED + 1))
    echo "❌ Order $i failed (HTTP $HTTP_CODE): $(echo $BODY | jq -r '.message // .error // empty' 2>/dev/null || echo $BODY | head -c 100)"
  fi
  
  # Small delay to avoid overwhelming the server
  sleep 0.1
done

echo ""
echo "════════════════════════════════════════"
echo "📊 Summary:"
echo "   ✅ Successful: $SUCCESS"
echo "   ❌ Failed: $FAILED"
echo "   📦 Total: $((SUCCESS + FAILED))"
echo "════════════════════════════════════════"
