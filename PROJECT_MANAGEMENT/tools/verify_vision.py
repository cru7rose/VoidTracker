import urllib.request
import json
import time

# --- Configuration ---
# Assuming order-service is running on 8091
BASE_URL = "http://localhost:8091"

def test_caption(filename):
    print(f"Testing with filename: {filename}")
    
    url = f"{BASE_URL}/api/vision/analyze"
    boundary = '----WebKitFormBoundary7MA4YWxkTrZu0gW'
    
    # Create simple multipart body
    data = []
    data.append(f'--{boundary}')
    data.append(f'Content-Disposition: form-data; name="file"; filename="{filename}"')
    data.append('Content-Type: image/jpeg')
    data.append('')
    data.append('fake_image_content')
    data.append(f'--{boundary}--')
    data.append('')
    
    body = '\r\n'.join(data).encode('utf-8')
    headers = {
        'Content-Type': f'multipart/form-data; boundary={boundary}',
        # 'Authorization': 'Bearer ...' # If needed, but previously was open for verify, assuming secured now but Role Driver?
        # If secured, we need a token. 
        # For simplicity, if secured, I might need to generate a token or temp disable.
        # Phase 9 closed it. I will see if I get 403.
    }
    
    req = urllib.request.Request(url, data=body, headers=headers, method='POST')
    
    try:
        with urllib.request.urlopen(req) as response:
            result = json.loads(response.read().decode())
            print(json.dumps(result, indent=2))
            return result
    except urllib.error.HTTPError as e:
        print(f"HTTP Error: {e.code} - {e.reason}")
        print(e.read().decode())
        return None

print("--- Vision Verification ---")
# 1. Test Good
res1 = test_caption("capture.jpg")
if res1 and res1.get('condition') == 'GOOD':
    print("PASS: Good Image detected as GOOD.")
else:
    print("FAIL: Good Image check failed.")

print("-" * 20)

# 2. Test Damaged
res2 = test_caption("damage_test.jpg")
if res2 and res2.get('condition') == 'DAMAGED':
    print("PASS: Damaged Image detected as DAMAGED.")
else:
    print("FAIL: Damaged Image check failed.")
