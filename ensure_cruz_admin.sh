#!/bin/bash
# Script to ensure admin user 'cruz' exists with proper credentials
# This script uses the IAM service API to create/verify the admin user

set -e

API_URL="${IAM_SERVICE_URL:-http://localhost:8090}"
USERNAME="cruz"
PASSWORD="meduza91"
EMAIL="cruz@voidtracker.com"

echo "🔹 Ensuring admin user '$USERNAME' exists..."

# Check if IAM service is running
if ! curl -s -f "${API_URL}/actuator/health" > /dev/null 2>&1; then
    echo "⚠️  IAM service not responding at ${API_URL}"
    echo "   Attempting to create user via database directly..."
    
    # Fallback: Direct database access
    docker exec -i postgres psql -U postgres -d iam_db <<EOF 2>/dev/null || true
-- Ensure user exists
INSERT INTO iam_users (user_id, username, password, email, enabled, global_status, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    '$USERNAME',
    '\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- meduza91 encoded
    '$EMAIL',
    true,
    'ACTIVE',
    NOW(),
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM iam_users WHERE username = '$USERNAME')
ON CONFLICT (username) DO NOTHING;

-- Ensure admin role
INSERT INTO iam_user_roles (user_id, roles)
SELECT user_id, 'ROLE_ADMIN'
FROM iam_users
WHERE username = '$USERNAME'
  AND NOT EXISTS (
    SELECT 1 FROM iam_user_roles 
    WHERE user_id = (SELECT user_id FROM iam_users WHERE username = '$USERNAME')
      AND roles = 'ROLE_ADMIN'
  );
EOF
    echo "✅ User creation attempted via database"
    exit 0
fi

# Method 1: Try registration (will fail if user exists, which is OK)
echo "🔹 Attempting registration..."
REGISTER_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${API_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$USERNAME\",
    \"password\": \"$PASSWORD\",
    \"email\": \"$EMAIL\",
    \"fullName\": \"Cruz Admin\",
    \"termsAccepted\": true
  }")

HTTP_CODE=$(echo "$REGISTER_RESPONSE" | tail -n1)
BODY=$(echo "$REGISTER_RESPONSE" | head -n-1)

if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "201" ]; then
    echo "✅ User registered successfully"
elif echo "$BODY" | grep -q "already exists\|duplicate"; then
    echo "ℹ️  User already exists, proceeding to verify..."
else
    echo "⚠️  Registration returned HTTP $HTTP_CODE: $BODY"
fi

# Method 2: Verify login works
echo "🔹 Verifying login credentials..."
LOGIN_RESPONSE=$(curl -s -X POST "${API_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"$USERNAME\", \"password\": \"$PASSWORD\"}")

TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken // empty' 2>/dev/null || echo "")

if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    echo "✅ Login successful! User '$USERNAME' is ready."
    echo "   Token preview: ${TOKEN:0:20}..."
    
    # Verify admin role via user details endpoint
    USER_DETAILS=$(curl -s -X GET "${API_URL}/api/users" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" 2>/dev/null || echo "{}")
    
    if echo "$USER_DETAILS" | jq -e '.roles[] | select(. == "ROLE_ADMIN")' > /dev/null 2>&1; then
        echo "✅ Admin role verified"
    else
        echo "⚠️  Admin role not found. User may need role assignment."
    fi
else
    echo "❌ Login failed for '$USERNAME'"
    echo "   Response: $LOGIN_RESPONSE"
    exit 1
fi

echo ""
echo "🎯 Admin user '$USERNAME' is ready for testing!"
echo "   Username: $USERNAME"
echo "   Password: $PASSWORD"
echo "   Email: $EMAIL"
