
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
    print(f"Login Response: {response}")
    if response and "accessToken" in response:
        return response["accessToken"]
    return None

def get_pending_orders(token):
    print("Fetching pending orders...")
    headers = {"Authorization": f"Bearer {token}"}
    response = make_request(f"{BASE_URL}/orders", method="GET", headers=headers)
    
    if not response:
        return []

    orders = []
    if isinstance(response, dict) and "content" in response:
        orders = response["content"]
    elif isinstance(response, list):
        orders = response
    
    # Filter for PENDING/NEW
    pending_ids = [o["orderId"] for o in orders if o.get("status") in ["PENDING", "NEW", "READY_FOR_PLANNING"]]
    return pending_ids

def trigger_optimization(token, order_ids):
    if not order_ids:
        print("No orders to optimize.")
        return

    print(f"Optimizing {len(order_ids)} orders...")
    headers = {"Authorization": f"Bearer {token}"}
    payload = {
        "orderIds": order_ids,
        "vehicleIds": []
    }
    
    response = make_request(f"{BASE_URL}/planning/optimization/optimize", method="POST", data=payload, headers=headers)
    if response:
        print("Optimization Triggered Successfully!")
        print(json.dumps(response, indent=2))
    else:
        print("Optimization Failed.")

if __name__ == "__main__":
    token = login()
    if token:
        ids = get_pending_orders(token)
        print(f"Found {len(ids)} pending orders.")
        trigger_optimization(token, ids)
