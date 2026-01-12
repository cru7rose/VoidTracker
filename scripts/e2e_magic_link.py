#!/usr/bin/env python3
import urllib.request
import urllib.parse
import json
import sys

BASE_URL = "http://localhost:81/api"
USERNAME = "cruz"
PASSWORD = "meduza91"
DRIVER_EMAIL = "cruz@voidtracker.com"

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
                    return json.loads(resp_data) if resp_data.startswith('{') or resp_data.startswith('[') else resp_data
                return {}
            else:
                print(f"Request failed: {response.status}")
                return None
    except urllib.error.HTTPError as e:
        print(f"HTTP Error {e.code}: {e.reason}")
        error_body = e.read().decode("utf-8")
        print(error_body)
        return None
    except Exception as e:
        print(f"Error: {e}")
        return None

def login():
    print("Logging in...")
    data = {"username": USERNAME, "password": PASSWORD}
    response = make_request(f"{BASE_URL}/auth/login", method="POST", data=data)
    if response and "accessToken" in response:
        print(f"✅ Logged in successfully as {USERNAME}")
        return response["accessToken"]
    return None

def get_driver_routes(token):
    """Get routes for the current driver"""
    print("\nFetching driver routes...")
    headers = {"Authorization": f"Bearer {token}"}
    response = make_request(f"{BASE_URL}/driver/route", method="GET", headers=headers)
    return response

def generate_magic_link(token, driver_id, route_id, email):
    """Generate magic link for driver access"""
    print(f"\nGenerating Magic Link for driver {driver_id}...")
    print(f"Route ID: {route_id}")
    print(f"Email: {email}")
    
    headers = {"Authorization": f"Bearer {token}"}
    
    # Build query parameters
    params = {
        "driverId": driver_id,
        "routeId": route_id
    }
    if email:
        params["email"] = email
    
    query_string = urllib.parse.urlencode(params)
    url = f"{BASE_URL}/driver/auth/generate-link?{query_string}"
    
    response = make_request(url, method="POST", headers=headers)
    return response

if __name__ == "__main__":
    token = login()
    if not token:
        print("❌ Login failed")
        sys.exit(1)
    
    # For this E2E test, we'll use the driver ID and vehicle ID we know
    DRIVER_ID = "6f44b5fa-0b9d-466f-8504-73c5d69a7384"  # cruz user ID
    VEHICLE_ID = "6890fa2c-317e-44b6-ade6-ddc62e099de0"  # The vehicle we assigned
    
    # Try to get driver routes first to find the route ID
    driver_route = get_driver_routes(token)
    
    if driver_route and "routeId" in driver_route:
        route_id = driver_route["routeId"]
        print(f"✅ Found driver route: {route_id}")
    else:
        # If no route found via API, we'll use the vehicle ID as route ID
        # (this depends on how the system is set up)
        print(f"⚠️  No route found via driver API, using vehicle ID as route ID")
        route_id = VEHICLE_ID
    
    # Generate magic link
    magic_link = generate_magic_link(token, DRIVER_ID, route_id, DRIVER_EMAIL)
    
    if magic_link:
        print(f"\n✅ Magic Link Generated Successfully!")
        print(f"🔗 Link: {magic_link}")
        print(f"📧 Email should be sent to: {DRIVER_EMAIL}")
    else:
        print(f"\n❌ Failed to generate magic link")
        sys.exit(1)
