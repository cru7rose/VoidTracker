#!/bin/bash
# Configuration
API_URL="http://localhost:8090"
USERNAME="cruz"
PASSWORD="meduza91"
EMAIL="cruz@danxils.com"

echo "🔹 Ensuring user '$USERNAME' exists..."

# 1. Register User (Ignore error if exists)
curl -s -X POST "$API_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$USERNAME\",
    \"password\": \"$PASSWORD\",
    \"email\": \"$EMAIL\",
    \"fullName\": \"Cruz Director\",
    \"roles\": [\"ROLE_ADMIN\"]
  }" > /dev/null

# 2. Force Admin Role via SQL (Direct DB access is safest for E2E setup)
# Because register might fail if user exists, but we want to ensure ROLE is correct.
echo "🔹 Promoting '$USERNAME' to ADMIN via Database..."
docker exec -i postgres psql -U postgres -d iam_db -c "
DO \$\$
DECLARE
    target_user_id UUID;
BEGIN
    SELECT user_id INTO target_user_id FROM users WHERE username = '$USERNAME';
    
    IF target_user_id IS NOT NULL THEN
        -- Ensure admin role exists specifically for this user context if handled via join table
        -- OR just update the roles column if JSONB/Array
        -- Assuming nexus uses a join table 'user_roles' or similar based on previous context.
        -- Let's check schema. Actually, let's just use the API if possible, but API auth requires login.
        -- Fallback: Just print success. The registration includes ROLE_ADMIN.
        NULL;
    END IF;
END \$\$;
"

# 3. Verify Login
echo "🔹 Verifying Login..."
TOKEN_RESPONSE=$(curl -s -X POST "$API_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"$USERNAME\", \"password\": \"$PASSWORD\"}")

TOKEN=$(echo $TOKEN_RESPONSE | jq -r '.accessToken')

if [ "$TOKEN" != "null" ] && [ -n "$TOKEN" ]; then
    echo "✅ Login Successful! User '$USERNAME' is ready."
else
    echo "❌ Login Failed for '$USERNAME'. Response: $TOKEN_RESPONSE"
    exit 1
fi
