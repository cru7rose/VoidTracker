#!/bin/bash

API_URL="http://localhost:8091/api/orders"

for i in {1..100}
do
   # Randomize coords near Warsaw (approx 52.2 + rand, 21.0 + rand)
   # Using awk for float math
   LAT_PICKUP=$(awk -v min=52.15 -v max=52.35 'BEGIN{srand(); print min+rand()*(max-min)}')
   LON_PICKUP=$(awk -v min=20.90 -v max=21.10 'BEGIN{srand(); print min+rand()*(max-min)}')
   
   LAT_DELIVERY=$(awk -v min=52.15 -v max=52.35 'BEGIN{srand(); print min+rand()*(max-min)}')
   LON_DELIVERY=$(awk -v min=20.90 -v max=21.10 'BEGIN{srand(); print min+rand()*(max-min)}')

   # Dates (Mac utils compatible using 'date -v')
   DATE_SLA=$(date -v+2d +%Y-%m-%dT%H:%M:%SZ)
   DATE_PICKUP_FROM=$(date -v+1d +%Y-%m-%dT08:00:00Z)
   DATE_PICKUP_TO=$(date -v+1d +%Y-%m-%dT16:00:00Z)
   DATE_DELIVERY_FROM=$(date -v+2d +%Y-%m-%dT08:00:00Z)
   DATE_DELIVERY_TO=$(date -v+2d +%Y-%m-%dT16:00:00Z)

   # JSON Payload
   JSON_DATA='{
     "customerId": "cust-'$i'",
     "priority": "NORMAL",
     "remark": "Auto-generated order '$i'",
     "pickupAddress": {
       "customerName": "Sender '$i'",
       "street": "Pickup St '$i'",
       "city": "Warsaw",
       "postalCode": "00-001",
       "country": "PL",
       "lat": '$LAT_PICKUP',
       "lon": '$LON_PICKUP',
       "confidenceScore": 1.0,
       "source": "SCRIPT"
     },
     "deliveryAddress": {
       "customerName": "Receiver '$i'",
       "street": "Delivery St '$i'",
       "city": "Warsaw",
       "postalCode": "00-002",
       "country": "PL",
       "lat": '$LAT_DELIVERY',
       "lon": '$LON_DELIVERY',
       "sla": "'$DATE_SLA'",
       "confidenceScore": 1.0,
       "source": "SCRIPT"
     },
     "packageDetails": {
       "weight": 10.5,
       "width": 10,
       "height": 10,
       "length": 10,
       "type": "BOX"
     },
     "pickupTimeFrom": "'$DATE_PICKUP_FROM'",
     "pickupTimeTo": "'$DATE_PICKUP_TO'",
     "deliveryTimeFrom": "'$DATE_DELIVERY_FROM'",
     "deliveryTimeTo": "'$DATE_DELIVERY_TO'"
   }'

   curl -s -X POST "$API_URL" \
     -H "Content-Type: application/json" \
     -d "$JSON_DATA" > /dev/null

   echo "Injected order $i"
done

echo "Injection complete."
