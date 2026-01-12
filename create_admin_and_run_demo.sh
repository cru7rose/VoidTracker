#!/bin/bash

# Configuration
NEXUS_URL="http://localhost:8090"
USERNAME="admin_demo"
PASSWORD="password123"
EMAIL="admin_demo@voidtracker.com"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}👷 Creating New Admin Account: $USERNAME ...${NC}"

# 1. Register User (Default as Customer)
REGISTER_PAYLOAD="{
    \"username\": \"$USERNAME\",
    \"password\": \"$PASSWORD\",
    \"email\": \"$EMAIL\",
    \"fullName\": \"Demo Admin\",
    \"termsAccepted\": true
}"

echo -e "\n🔹 Registering User via API..."
curl -s -X POST "$NEXUS_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "$REGISTER_PAYLOAD" > /dev/null

# 2. Promote to Admin (Via SQL)
echo -e "\n🔹 Promoting to ADMIN via Database..."
docker exec -i postgres psql -U postgres -d iam_db -c "
UPDATE user_organization_access 
SET role_definition_id = 'admin' 
WHERE id_user_id = (SELECT user_id FROM users WHERE username = '$USERNAME');
"

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ User promoted to ADMIN successfully!${NC}"
else
    echo -e "${RED}❌ Failed to promote user via SQL.${NC}"
    exit 1
fi

# 3. Authenticate to Verify
echo -e "\n🔹 Verifying Login..."
LOGIN_PAYLOAD="{\"username\": \"$USERNAME\", \"password\": \"$PASSWORD\"}"
LOGIN_RESPONSE=$(curl -s -X POST "$NEXUS_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "$LOGIN_PAYLOAD")

TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.accessToken')
ROLE=$(echo $LOGIN_RESPONSE | jq -r '.user.organizations[0].role')

if [ "$TOKEN" != "null" ] && [ "$ROLE" == "admin" ]; then
    echo -e "${GREEN}✅ Login Successful! Role: $ROLE${NC}"
else
    echo -e "${RED}❌ Login verification failed or role is not admin. Role: $ROLE${NC}"
    exit 1
fi

# 4. Trigger the E2E Script with new credentials
export USERNAME=$USERNAME
export PASSWORD=$PASSWORD
echo -e "\n🚀 Running E2E Demo with new Admin..."
./demo_e2e.sh
