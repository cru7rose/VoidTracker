#!/bin/bash

# Configuration
AUTH_URL="http://localhost:81/api/auth/login"
USERS_URL="http://localhost:81/api/users"
MAGIC_LINK_URL="http://localhost:81/api/auth/magic-link/driver/generate"
USERNAME="dispatcher"
PASSWORD="admin123"

echo "Logging in..."
LOGIN_RESP=$(curl -s -X POST "$AUTH_URL" \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"$USERNAME\", \"password\": \"$PASSWORD\"}")

TOKEN=$(echo $LOGIN_RESP | jq -r '.accessToken')
if [ "$TOKEN" == "null" ] || [ -z "$TOKEN" ]; then
  echo "Login failed. Response: $LOGIN_RESP"
  exit 1
fi
echo "Login successful."

echo "---------------------------------------------------"
echo "1. Testing Fetch Drivers (GET /api/users)..."
USERS_RESP=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET "$USERS_URL" \
  -H "Authorization: Bearer $TOKEN")

HTTP_STATUS=$(echo "$USERS_RESP" | grep "HTTP_STATUS" | cut -d: -f2)
BODY=$(echo "$USERS_RESP" | grep -v "HTTP_STATUS")

if [ "$HTTP_STATUS" -eq 200 ]; then
   COUNT=$(echo "$BODY" | jq '. | length')
   echo "Success. Found $COUNT users."
   # Get first user email for magic link test
   TEST_EMAIL=$(echo "$BODY" | jq -r '.[0].email')
   echo "Using email: $TEST_EMAIL for magic link test."
else
   echo "Failed ($HTTP_STATUS): $BODY"
   exit 1
fi

echo "---------------------------------------------------"
echo "2. Testing Magic Link Generation (POST /api/auth/magic-link/driver/generate)..."
# Using query param identifier as per Store implementation
# dispatchStore.js: iamApi.post(..., null, { params: { identifier: email } }) -> ?identifier=email

MAGIC_RESP=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "${MAGIC_LINK_URL}?identifier=${TEST_EMAIL}" \
  -H "Authorization: Bearer $TOKEN")

HTTP_STATUS=$(echo "$MAGIC_RESP" | grep "HTTP_STATUS" | cut -d: -f2)
BODY=$(echo "$MAGIC_RESP" | grep -v "HTTP_STATUS")

if [ "$HTTP_STATUS" -eq 200 ]; then
   LINK=$(echo "$BODY" | jq -r '.link')
   echo "Success. Magic Link: $LINK"
else
   echo "Failed ($HTTP_STATUS): $BODY"
fi
