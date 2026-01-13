#!/bin/bash
# Script to create/ensure 'cruz' user exists with admin role (for user)
# This script creates the main user account for the project owner

set -e

API_URL="${IAM_SERVICE_URL:-http://localhost:8081}"
USERNAME="cruz"
PASSWORD="meduza91"
EMAIL="cruz@voidtracker.com"
DB_NAME="vt_iam_service"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}👤 Ensuring User Account: $USERNAME${NC}"

# Check if IAM service is running
if ! curl -s -f "${API_URL}/actuator/health" > /dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  IAM service not responding at ${API_URL}${NC}"
    echo -e "${YELLOW}   Creating user via database directly...${NC}"
    
    # Fallback: Direct database access
    docker exec -i postgres psql -U postgres -d $DB_NAME <<EOF 2>/dev/null || true
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
    echo -e "${GREEN}✅ User creation attempted via database${NC}"
    echo -e "${YELLOW}💡 Start IAM service to verify: ./start-iam.sh${NC}"
    exit 0
fi

# Method 1: Try registration
echo -e "\n🔹 Attempting registration..."
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
    echo -e "${GREEN}✅ User registered successfully${NC}"
elif echo "$BODY" | grep -q "already exists\|duplicate"; then
    echo -e "${YELLOW}ℹ️  User already exists, proceeding to verify...${NC}"
else
    echo -e "${YELLOW}⚠️  Registration returned HTTP $HTTP_CODE: $BODY${NC}"
fi

# Method 2: Promote to admin via database (if needed)
echo -e "\n🔹 Ensuring admin role..."
docker exec -i postgres psql -U postgres -d $DB_NAME <<EOF 2>/dev/null || true
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

# Method 3: Verify login works
echo -e "\n🔹 Verifying login credentials..."
LOGIN_RESPONSE=$(curl -s -X POST "${API_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"$USERNAME\", \"password\": \"$PASSWORD\"}")

TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken // empty' 2>/dev/null || echo "")

if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    echo -e "${GREEN}✅ Login successful! User '$USERNAME' is ready.${NC}"
    echo -e "${GREEN}   Token preview: ${TOKEN:0:20}...${NC}"
    
    # Verify admin role
    USER_DETAILS=$(curl -s -X GET "${API_URL}/api/users" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" 2>/dev/null || echo "{}")
    
    if echo "$USER_DETAILS" | jq -e '.roles[] | select(. == "ROLE_ADMIN")' > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Admin role verified${NC}"
    else
        echo -e "${YELLOW}⚠️  Admin role not found. User may need role assignment.${NC}"
    fi
else
    echo -e "${RED}❌ Login failed for '$USERNAME'${NC}"
    echo -e "${RED}   Response: $LOGIN_RESPONSE${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}🎯 User '$USERNAME' is ready!${NC}"
echo -e "${GREEN}   Username: $USERNAME${NC}"
echo -e "${GREEN}   Password: $PASSWORD${NC}"
echo -e "${GREEN}   Email: $EMAIL${NC}"
