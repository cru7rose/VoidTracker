import jwt
import datetime
import base64

# Secret from application.yml
secret_base64 = "dGVzdC1zZWNyZXQta2V5LWZvci1sb2NhbC1kZXZlbG9wbWVudC1vbmx5LXBsZWFzZS1jaGFuZ2UtaW4tcHJvZHVjdGlvbg=="
secret = base64.b64decode(secret_base64)

payload = {
    "sub": "cruz",
    "roles": ["ADMIN", "DISPATCHER"],
    "iat": datetime.datetime.utcnow(),
    "exp": datetime.datetime.utcnow() + datetime.timedelta(hours=1)
}

token = jwt.encode(payload, secret, algorithm="HS256")
print(token)
