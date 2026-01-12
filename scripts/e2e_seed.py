
import urllib.request
import urllib.error
import json
import random
import datetime
import uuid

BASE_URL = "http://localhost:81/api"
AUTH_URL = f"{BASE_URL}/auth/login"
ORDERS_URL = f"{BASE_URL}/orders"
CUSTOMERS_URL = f"{BASE_URL}/customers"
IAM_CUSTOMERS_URL = f"{BASE_URL}/customers" 

USERNAME = "cruz"
PASSWORD = "meduza91"

def make_request(url, method="GET", data=None, headers=None):
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
        print(f"HTTP Error {e.code}: {e.read().decode('utf-8')}")
    except Exception as e:
        print(f"Error: {e}")
    return None

def login():
    print(f"Logging in as {USERNAME}...")
    resp = make_request(AUTH_URL, method="POST", data={"username": USERNAME, "password": PASSWORD})
    if resp:
        token = resp.get("token") or resp.get("accessToken")
        print("Login successful.")
        return token
    return None

def get_customer(token):
    print("Fetching customer profile...")
    headers = {"Authorization": f"Bearer {token}"}
    
    # Try /me first
    # Note: Gateway might map /api/customers -> iam-service /api/customers
    url = f"{IAM_CUSTOMERS_URL}/me"
    
    # We might need to pass X-User-Id if we were calling IAM directly, but Nginx/Gateway might do it. 
    # Or maybe we just need the user ID from the token?
    # Let's assume /me works with Bearer token if the gateway is smart.
    # If not, we might need to parse the token to get the ID.
    
    # Trying GET /me
    profile = make_request(url, method="GET", headers=headers)
    if profile and 'id' in profile:
        print(f"Found profile: {profile['id']}")
        return profile['id']
        
    print("Could not find profile. Order service might fail if customer doesn't exist.")
    # Return a fallback UUID - if this fails, we know we need to fix the customer finding logic
    # But usually 'cruz' (admin) might not have a customer profile.
    # Let's try to CREATE a profile for me (POST /me)
    newUser = {
        "name": "Cruz Admin Customer",
        "contactInfo": "test@example.com"
    }
    print("Creating profile...")
    profile = make_request(url, method="POST", data=newUser, headers=headers)
    if profile and 'id' in profile:
        return profile['id']
        
    return str(uuid.uuid4())

def seed_orders():
    token = login()
    if not token:
        return

    headers = {"Authorization": f"Bearer {token}"}
    customer_id = "00000000-0000-0000-0000-000000000001" # get_customer(token)
    print(f"Using Customer ID: {customer_id}")

    # Warsaw coordinates center
    center_lat = 52.2297
    center_lon = 21.0122

    for i in range(5):
        # Random offset for lat/lon
        lat_off = (random.random() - 0.5) * 0.1
        lon_off = (random.random() - 0.5) * 0.1
        
        pickup_time = datetime.datetime.utcnow() + datetime.timedelta(hours=1)
        delivery_time = pickup_time + datetime.timedelta(hours=2)

        payload = {
            "customerId": customer_id,
            "priority": "NORMAL",
            "remark": f"Auto-generated E2E Order {i+1}",
            "pickupAddress": {
                "customerName": "Warehouse A",
                "street": "Magazynowa",
                "streetNumber": "1",
                "city": "Warszawa",
                "postalCode": "00-001",
                "country": "PL",
                "lat": center_lat,
                "lon": center_lon
            },
            "deliveryAddress": {
                "customerName": f"Client {i+1}",
                "street": "Marszalkowska",
                "streetNumber": str(random.randint(1, 150)),
                "apartment": str(random.randint(1, 50)),
                "city": "Warszawa",
                "postalCode": "00-100",
                "country": "PL",
                "lat": center_lat + lat_off,
                "lon": center_lon + lon_off,
                "sla": (delivery_time + datetime.timedelta(hours=4)).isoformat() + "Z"
            },
            "packageDetails": {
                "weight": 5.5,
                "volume": 0.02,
                "description": "Test Package"
            },
            "requiredServiceCodes": [],
            "pickupTimeFrom": pickup_time.isoformat() + "Z",
            "pickupTimeTo": (pickup_time + datetime.timedelta(hours=1)).isoformat() + "Z",
            "deliveryTimeFrom": delivery_time.isoformat() + "Z",
            "deliveryTimeTo": (delivery_time + datetime.timedelta(hours=1)).isoformat() + "Z"
        }

        print(f"Creating Order {i+1}...")
        resp = make_request(ORDERS_URL, method="POST", data=payload, headers=headers)
        if resp:
            print(f"Created Order {i+1}: {resp.get('orderId')}")

if __name__ == "__main__":
    seed_orders()
