
import urllib.request
import urllib.parse
import json
import sys

BASE_URL = "http://localhost:81/api"
USERNAME = "cruz"
PASSWORD = "meduza91"

def make_request(url, method="GET", data=None, headers=None):
    if headers is None:
        headers = {}
    
    if data:
        data_bytes = json.dumps(data).encode("utf-8")
        headers["Content-Type"] = "application/json"
    else:
        data_bytes = None

    print(f"Requesting {method} {url}")
    req = urllib.request.Request(url, data=data_bytes, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as response:
            print(f"Response status: {response.status}")
            if response.status >= 200 and response.status < 300:
                resp_data = response.read().decode("utf-8")
                if resp_data:
                    return json.loads(resp_data)
                return {}
            else:
                print(f"Request failed: {response.status}")
                return None
    except urllib.error.HTTPError as e:
        print(f"HTTP Error {e.code}: {e.reason}")
        print(e.read().decode("utf-8"))
        return None
    except Exception as e:
        print(f"Error: {e}")
        return None

def login():
    print("Logging in...")
    data = {"username": USERNAME, "password": PASSWORD}
    response = make_request(f"{BASE_URL}/auth/login", method="POST", data=data)
    if response and "accessToken" in response:
        return response["accessToken"]
    return None

def get_orders_address_map(token):
    headers = {"Authorization": f"Bearer {token}"}
    response = make_request(f"{BASE_URL}/orders", method="GET", headers=headers)
    
    mapping = {}
    if not response:
        return mapping

    orders = []
    if isinstance(response, dict) and "content" in response:
        orders = response["content"]
    elif isinstance(response, list):
        orders = response
    
    for o in orders:
        oid = o.get("orderId")
        addr = "Unknown Address"
        if "delivery" in o and o["delivery"] and "address" in o["delivery"]:
            # Address might be object or string in DTO?
            # Looking at CreateOrderRequestDto it's DeliveryAddressDto.
            # Response likely similar.
            d = o["delivery"]
            if isinstance(d["address"], dict):
                # If backend returns entity-like structure
                 addr = d["address"].get("street", "Unknown")
            elif isinstance(d["address"], str): # If mapped to string
                 addr = d["address"]
            else:
                 # Flattened?
                 addr = str(d.get("street", "Unknown"))
        
        mapping[oid] = addr
    return mapping

def get_latest_solution(token):
    headers = {"Authorization": f"Bearer {token}"}
    return make_request(f"{BASE_URL}/planning/optimization/latest", method="GET", headers=headers)

def publish_solution(token, solution, address_map):
    if not solution or "routes" not in solution:
        print("No routes in solution.")
        return

    publish_routes = []
    
    for r in solution["routes"]:
        vehicle_id = str(r.get("vehicleId"))
        stops = []
        for act in r.get("activities", []):
            if act.get("type") == "DELIVERY": # Only publishing deliveries usually? Or Pickups too?
                # OptimizationController publishes tasks. 
                # If Pickup tasks are needed, include them. 
                # For now assume Delivery.
                oid = str(act.get("orderId"))
                addr = address_map.get(oid, "Unknown Address associated with Order")
                stops.append({
                    "orderId": oid,
                    "address": addr
                })
        
        if stops:
            publish_routes.append({
                "vehicleId": vehicle_id,
                "stops": stops
            })

    if not publish_routes:
        print("No routes to publish.")
        return

    payload = {
        "solutionId": None, # Optional
        "routes": publish_routes
    }

    headers = {"Authorization": f"Bearer {token}"}
    make_request(f"{BASE_URL}/planning/optimization/publish", method="POST", data=payload, headers=headers)
    print("Publishing complete.")

if __name__ == "__main__":
    token = login()
    if token:
        addr_map = get_orders_address_map(token)
        print(f"Loaded {len(addr_map)} order addresses.")
        solution = get_latest_solution(token)
        if solution:
            print("Loaded latest solution.")
            publish_solution(token, solution, addr_map)
