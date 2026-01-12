#!/usr/bin/env python3
"""
Poland-Wide Test Order Generator
Generates 100+ orders distributed across major Polish cities for visualization testing.
"""

import urllib.request
import urllib.error
import json
import random
import datetime
import uuid

BASE_URL = "http://localhost:8091/api"  # Order Service direct port
AUTH_URL = "http://localhost:8081/api/auth/login"  # IAM Service direct port
ORDERS_URL = f"{BASE_URL}/orders"

USERNAME = "cruz"
PASSWORD = "meduza91"

# Major Polish Cities with Coordinates (lat, lon)
POLISH_CITIES = [
    {"name": "Warszawa", "lat": 52.2297, "lon": 21.0122, "postal": "00-001"},
    {"name": "Kraków", "lat": 50.0647, "lon": 19.9450, "postal": "30-001"},
    {"name": "Gdańsk", "lat": 54.3520, "lon": 18.6466, "postal": "80-001"},
    {"name": "Wrocław", "lat": 51.1079, "lon": 17.0385, "postal": "50-001"},
    {"name": "Poznań", "lat": 52.4064, "lon": 16.9252, "postal": "60-001"},
    {"name": "Łódź", "lat": 51.7592, "lon": 19.4550, "postal": "90-001"},
    {"name": "Szczecin", "lat": 53.4285, "lon": 14.5528, "postal": "70-001"},
    {"name": "Lublin", "lat": 51.2465, "lon": 22.5684, "postal": "20-001"},
    {"name": "Katowice", "lat": 50.2649, "lon": 19.0238, "postal": "40-001"},
    {"name": "Białystok", "lat": 53.1325, "lon": 23.1688, "postal": "15-001"},
    {"name": "Gdynia", "lat": 54.5189, "lon": 18.5305, "postal": "81-001"},
    {"name": "Bydgoszcz", "lat": 53.1235, "lon": 18.0084, "postal": "85-001"},
    {"name": "Rzeszów", "lat": 50.0412, "lon": 21.9991, "postal": "35-001"},
]

# Hub location (Warsaw)
HUB_LAT = 52.2297
HUB_LON = 21.0122


def make_request(url, method="GET", data=None, headers=None):
    """Make HTTP request using urllib"""
    if headers is None:
        headers = {}
    
    if data:
        json_data = json.dumps(data).encode('utf-8')
        headers['Content-Type'] = 'application/json'
    else:
        json_data = None

    req = urllib.request.Request(url, data=json_data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as response:
            if response.status >= 200 and response.status < 300:
                body = response.read()
                if body:
                    return json.loads(body)
                return {}
    except urllib.error.HTTPError as e:
        error_body = e.read().decode('utf-8')
        print(f"❌ HTTP Error {e.code}: {error_body}")
        return None
    except Exception as e:
        print(f"❌ Error: {e}")
        return None


def login():
    """Authenticate and get JWT token"""
    print(f"🔐 Logging in as {USERNAME}...")
    resp = make_request(AUTH_URL, method="POST", data={"username": USERNAME, "password": PASSWORD})
    if resp:
        token = resp.get("token") or resp.get("accessToken")
        if token:
            print("✅ Login successful.")
            return token
    print("❌ Login failed.")
    return None


def add_location_variance(lat, lon, max_offset=0.05):
    """Add random offset to coordinates to simulate multiple addresses in city"""
    lat_variance = (random.random() - 0.5) * max_offset
    lon_variance = (random.random() - 0.5) * max_offset
    return round(lat + lat_variance, 6), round(lon + lon_variance, 6)


def generate_poland_orders(token, orders_per_city=8):
    """Generate test orders distributed across Polish cities"""
    headers = {"Authorization": f"Bearer {token}"}
    customer_id = "00000000-0000-0000-0000-000000000001"
    
    created_count = 0
    total_target = len(POLISH_CITIES) * orders_per_city
    
    print(f"\n📦 Generating {total_target} orders across {len(POLISH_CITIES)} Polish cities...")
    print(f"{'='*60}")
    
    for city in POLISH_CITIES:
        city_name = city["name"]
        base_lat = city["lat"]
        base_lon = city["lon"]
        postal = city["postal"]
        
        print(f"\n📍 {city_name} (lat: {base_lat}, lon: {base_lon})")
        
        for i in range(orders_per_city):
            # Add variance to create realistic address spread within city
            delivery_lat, delivery_lon = add_location_variance(base_lat, base_lon, max_offset=0.08)
            
            # Random delivery time (1-8 hours from now)
            pickup_time = datetime.datetime.utcnow() + datetime.timedelta(hours=random.randint(1, 3))
            delivery_time = pickup_time + datetime.timedelta(hours=random.randint(2, 5))
            
            # Random package details
            weight = round(random.uniform(1, 50), 2)
            volume = round(random.uniform(0.01, 0.5), 3)
            
            payload = {
                "customerId": customer_id,
                "priority": random.choice(["NORMAL", "NORMAL", "NORMAL", "HIGH"]),  # 75% normal
                "remark": f"Poland-Wide Test: {city_name} #{i+1}",
                "pickupAddress": {
                    "customerName": "Central Hub Warsaw",
                    "street": "Magazynowa",
                    "streetNumber": "1",
                    "city": "Warszawa",
                    "postalCode": "00-001",
                    "country": "PL",
                    "lat": HUB_LAT,
                    "lon": HUB_LON
                },
                "deliveryAddress": {
                    "customerName": f"Client {city_name}-{i+1}",
                    "street": random.choice(["Główna", "Rynek", "Piłsudskiego", "Mickiewicza", "Kościuszki"]),
                    "streetNumber": str(random.randint(1, 150)),
                    "apartment": str(random.randint(1, 50)) if random.random() > 0.3 else None,
                    "city": city_name,
                    "postalCode": postal,
                    "country": "PL",
                    "lat": delivery_lat,
                    "lon": delivery_lon,
                    "sla": (delivery_time + datetime.timedelta(hours=4)).isoformat() + "Z"
                },
                "packageDetails": {
                    "weight": weight,
                    "volume": volume,
                    "description": f"Test Package ({weight}kg, {volume}m³)"
                },
                "requiredServiceCodes": [],
                "pickupTimeFrom": pickup_time.isoformat() + "Z",
                "pickupTimeTo": (pickup_time + datetime.timedelta(hours=1)).isoformat() + "Z",
                "deliveryTimeFrom": delivery_time.isoformat() + "Z",
                "deliveryTimeTo": (delivery_time + datetime.timedelta(hours=2)).isoformat() + "Z"
            }
            
            resp = make_request(ORDERS_URL, method="POST", data=payload, headers=headers)
            if resp and resp.get('orderId'):
                created_count += 1
                order_id = resp['orderId'][:8]
                print(f"  ✅ Order {created_count}/{total_target}: {order_id} ({weight}kg)")
            else:
                print(f"  ❌ Failed to create order {i+1} in {city_name}")
    
    print(f"\n{'='*60}")
    print(f"✨ Successfully created {created_count}/{total_target} orders across Poland!")
    print(f"\n💡 Next Steps:")
    print(f"   1. Open: http://localhost:5173/internal/dispatch")
    print(f"   2. Click: 'RUN OPTIMIZER PRIME' button")
    print(f"   3. View: Optimized routes covering entire Poland")
    print(f"\n🔍 To verify orders in database:")
    print(f"   curl -X GET 'http://localhost:81/api/orders?statuses=NEW' \\")
    print(f"     -H 'Authorization: Bearer <token>' | jq '.content | length'")


def main():
    print("🌌 VoidTracker Poland-Wide Order Generator")
    print("="*60)
    
    token = login()
    if not token:
        return
    
    # Generate 8 orders per city = ~104 orders total
    generate_poland_orders(token, orders_per_city=8)


if __name__ == "__main__":
    main()
