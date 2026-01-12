
import requests
import sys

BASE_URL = "http://localhost:81/api"
USERNAME = "cruz"
PASSWORD = "meduza91"

def login():
    try:
        response = requests.post(
            f"{BASE_URL}/auth/login",
            json={"username": USERNAME, "password": PASSWORD},
            headers={"Content-Type": "application/json"}
        )
        if response.status_code == 200:
            return response.json().get("token")
        return None
    except:
        return None

if __name__ == "__main__":
    t = login()
    if t:
        print(t)
