#!/bin/bash
# Main script to setup both demo admin and cruz user
# This script creates both users needed for testing and development

set -e

# Colors
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${CYAN}"
echo "╔════════════════════════════════════════╗"
echo "║   USER SETUP                            ║"
echo "╚════════════════════════════════════════╝"
echo -e "${NC}"

# Check if IAM service is running
API_URL="${IAM_SERVICE_URL:-http://localhost:8081}"
if ! curl -s -f "${API_URL}/actuator/health" > /dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  IAM service not running${NC}"
    echo -e "${YELLOW}   Creating users via database directly...${NC}"
    echo ""
fi

# Setup Demo Admin
echo -e "${CYAN}👷 Setting up Demo Admin (for testing)...${NC}"
./setup-demo-admin.sh

echo ""

# Setup Cruz User
echo -e "${CYAN}👤 Setting up Cruz User (for development)...${NC}"
./setup-cruz-user.sh

echo ""
echo -e "${GREEN}"
echo "╔════════════════════════════════════════╗"
echo "║   USERS READY ✅                       ║"
echo "╚════════════════════════════════════════╝"
echo -e "${NC}"
echo ""
echo "📋 Available Users:"
echo ""
echo "  👷 Demo Admin (for testing):"
echo "     Username: demo_admin"
echo "     Password: demo123"
echo ""
echo "  👤 Cruz User (for development):"
echo "     Username: cruz"
echo "     Password: meduza91"
echo ""
