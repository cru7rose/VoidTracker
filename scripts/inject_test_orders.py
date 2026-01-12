import requests
import json
import random
import time
from datetime import datetime, timedelta

ORDER_SERVICE_URL = "http://localhost:8091/api/orders"

def create_order(customer_id, lat, lon):
    payload = {
        "customerId": customer_id,
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
            "customerName": f"Customer {customer_id}",
            "street": "Testowa",
            "streetNumber": str(random.randint(1, 100)),
            "postalCode": "00-002",
            "city": "Warszawa",
            "country": "PL",
            "lat": lat,
            "lon": lon,
            "sla": (datetime.now() + timedelta(hours=4)).isoformat() + "Z"
        },
        "packageDetails": {
            "weight": 10.0,
            "volume": 0.1,
            "count": 1,
            "description": "Test Package"
        },
        "pickupTimeFrom": datetime.now().isoformat() + "Z",
        "pickupTimeTo": (datetime.now() + timedelta(hours=2)).isoformat() + "Z",
        "deliveryTimeFrom": (datetime.now() + timedelta(hours=2)).isoformat() + "Z",
        "deliveryTimeTo": (datetime.now() + timedelta(hours=6)).isoformat() + "Z"
    }

    try:
        response = requests.post(ORDER_SERVICE_URL, json=payload, headers={"Content-Type": "application/json"})
        if response.status_code == 201:
            print(f"Created order for {customer_id}: {response.json()['orderId']}")
            return response.json()['orderId']
        else:
            print(f"Failed to create order: {response.status_code} - {response.text}")
            return None
    except Exception as e:
        print(f"Error creating order: {e}")
        return None

if __name__ == "__main__":
    # Warsaw coordinates
    base_lat = 52.237049
    base_lon = 21.017532

    # Create 5 random orders around Warsaw
    for i in range(5):
        # Random offset roughly within 10km
        lat_offset = (random.random() - 0.5) * 0.1
        lon_offset = (random.random() - 0.5) * 0.1
        create_order(f"CUST_{i}", base_lat + lat_offset, base_lon + lon_offset)
