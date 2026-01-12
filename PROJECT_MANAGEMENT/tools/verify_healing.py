import urllib.request
import urllib.parse
import json
import time

BASE_URL = "http://localhost:8091/api"

def get_json(url):
    try:
        with urllib.request.urlopen(url) as response:
            return json.loads(response.read().decode())
    except Exception as e:
        print(f"GET Error {url}: {e}")
        return None

def post_json(url, data):
    try:
        req = urllib.request.Request(url, method="POST")
        req.add_header('Content-Type', 'application/json')
        jsondata = json.dumps(data).encode('utf-8')
        req.add_header('Content-Length', len(jsondata))
        
        with urllib.request.urlopen(req, jsondata) as response:
            return response.status, response.read().decode()
    except Exception as e:
        print(f"POST Error {url}: {e}")
        return None, None

def main():
    print("Fetching test order...")
    orders = get_json(f"{BASE_URL}/orders")
    
    print(f"Orders response type: {type(orders)}")
    # print(f"Orders response: {json.dumps(orders, indent=2)}")
    
    if isinstance(orders, dict) and 'content' in orders:
        orders = orders['content']

    if not orders:
        print("No orders found!")
        return

    order = orders[0]
    order_id = order['orderId']
    print(f"Testing with Order ID: {order_id}")

    # Initial State
    initial_addr = order.get('delivery', {})
    print(f"Initial Confidence: {initial_addr.get('confidenceScore')}")
    print(f"Initial Source: {initial_addr.get('source')}")

    # Simulate Scan
    payload = {
        "assetId": order_id,
        "scanType": "DELIVERY_SUCCESS",
        "timestamp": "2023-12-12T12:00:00Z",
        "lat": 52.5200,
        "lon": 13.4050,
        "metadata": {
            "accuracy": 5.0,
            "distance": 0.0
        }
    }
    print("Sending Scan Event...")
    status, _ = post_json(f"{BASE_URL}/scan-events", payload)
    print(f"Scan Status: {status}")

    print("Waiting for Async processing...")
    time.sleep(2)

    # Verify
    updated_order = get_json(f"{BASE_URL}/orders/{order_id}")
    updated_addr = updated_order.get('delivery', {})
    
    print("-" * 20)
    print(f"Updated Confidence: {updated_addr.get('confidenceScore')}")
    print(f"Updated Source: {updated_addr.get('source')}")
    
    if updated_addr.get('confidenceScore') == 0.9 and updated_addr.get('source') == "DRIVER_SCAN":
        print("SUCCESS: Address Healed!")
    else:
        print("FAILURE: Address NOT Healed.")

if __name__ == "__main__":
    main()
