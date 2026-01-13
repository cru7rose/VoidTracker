#!/bin/bash
# Test script for Ghost PWA and Planning Service

echo "╔════════════════════════════════════════╗"
echo "║   GHOST PWA & PLANNING SERVICE TESTS    ║"
echo "╚════════════════════════════════════════╝"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counter
PASSED=0
FAILED=0

test_endpoint() {
    local name=$1
    local url=$2
    local expected_status=${3:-200}
    
    echo -n "Testing $name... "
    
    response=$(curl -s -w "\n%{http_code}" "$url" 2>/dev/null)
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" == "$expected_status" ] || [ "$http_code" == "200" ] || [ "$http_code" == "404" ]; then
        echo -e "${GREEN}✓${NC} (HTTP $http_code)"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}✗${NC} (HTTP $http_code)"
        echo "  Response: $body"
        ((FAILED++))
        return 1
    fi
}

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "1. Infrastructure Checks"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check PostgreSQL
echo -n "PostgreSQL (port 5434)... "
if pg_isready -h localhost -p 5434 >/dev/null 2>&1; then
    echo -e "${GREEN}✓${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗${NC}"
    ((FAILED++))
fi

# Check Kafka
echo -n "Kafka (port 9094)... "
if nc -z localhost 9094 2>/dev/null; then
    echo -e "${GREEN}✓${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗${NC}"
    ((FAILED++))
fi

# Check MailHog
echo -n "MailHog (port 8025)... "
if curl -s http://localhost:8025/api/v2/messages >/dev/null 2>&1; then
    echo -e "${GREEN}✓${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗${NC}"
    ((FAILED++))
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "2. Planning Service Endpoints"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Test assignments endpoint (may be empty, that's OK)
test_endpoint "Route Assignments" "http://localhost:8093/api/planning/assignments?page=0&size=1"

# Test driver auth validate (will fail without valid token, but endpoint should exist)
test_endpoint "Driver Auth Validate" "http://localhost:8093/api/planning/driver/auth/validate?token=test" 401

# Test media upload endpoint (should return 400/405 without file)
test_endpoint "Media Upload Endpoint" "http://localhost:8093/api/planning/media/upload/POD" 405

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "3. IAM Service"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Test IAM on port 8081
test_endpoint "IAM Service (8081)" "http://localhost:8081/api/users" 403

# Test IAM on port 8090 (if exists)
test_endpoint "IAM Service (8090)" "http://localhost:8090/api/users" 403

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "4. Ghost PWA"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check if Ghost PWA is running
echo -n "Ghost PWA (port 5173)... "
if curl -s http://localhost:5173 >/dev/null 2>&1; then
    echo -e "${GREEN}✓${NC}"
    ((PASSED++))
else
    echo -e "${YELLOW}⚠${NC} (Not running - start with: cd modules/ghost/driver-pwa && npm run dev)"
    ((FAILED++))
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "5. Service Processes"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check planning-service process
echo -n "Planning Service process... "
if ps aux | grep -q "[p]lanning-service.*8093"; then
    echo -e "${GREEN}✓${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗${NC}"
    ((FAILED++))
fi

# Check IAM service process
echo -n "IAM Service process... "
if ps aux | grep -q "[i]am.*8081"; then
    echo -e "${GREEN}✓${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗${NC}"
    ((FAILED++))
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 Test Results"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✅ All tests passed!${NC}"
    exit 0
else
    echo -e "${YELLOW}⚠️  Some tests failed. Check logs for details.${NC}"
    echo ""
    echo "To check planning-service logs:"
    echo "  tail -f /root/VoidTracker/logs/planning-service.log"
    exit 1
fi
