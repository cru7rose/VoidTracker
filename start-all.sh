#!/bin/bash

# ===================================================================== 
# VoidTracker 2.0 - Application Startup Script (Wrapper)
# Convenience script that starts all services by calling individual scripts
# For granular control, use: start-iam.sh, start-order.sh, start-planning.sh, start-frontend.sh
# =====================================================================

set -e

# Navigate to project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colors
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${CYAN}"
echo "╔════════════════════════════════════════╗"
echo "║   VOID-FLOW APPLICATION STARTUP        ║"
echo "║   (Using individual service scripts)   ║"
echo "╚════════════════════════════════════════╝"
echo -e "${NC}"

# Check if infrastructure is ready
echo -e "${CYAN}🔍 Verifying Infrastructure...${NC}"
if ! nc -z localhost 5434 2>/dev/null || ! nc -z localhost 9094 2>/dev/null; then
    echo -e "${YELLOW}⚠️  Infrastructure not ready. Starting infrastructure first...${NC}"
    ./start-sup.sh
fi

# Start services in order
echo -e "\n${CYAN}🚀 Starting all services...${NC}"

echo -e "\n${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
./start-iam.sh

echo -e "\n${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
./start-order.sh

echo -e "\n${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
./start-planning.sh

echo -e "\n${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
./start-frontend.sh

# Success Summary
echo -e "\n${GREEN}"
echo "╔════════════════════════════════════════╗"
echo "║   SYSTEM OPERATIONAL ✅                 ║"
echo "╚════════════════════════════════════════╝"
echo -e "${NC}"
echo "───────────────────────────────────────────"
echo -e "🖥️  ${GREEN}Frontend:${NC}         http://localhost:5173"
echo -e "📦 ${GREEN}Order Service:${NC}    http://localhost:8091"
echo -e "🔐 ${GREEN}IAM Service:${NC}      http://localhost:8081"
echo -e "🧠 ${GREEN}Planning Service:${NC} http://localhost:8093"
echo "───────────────────────────────────────────"
echo -e "${CYAN}Logs available in: ./logs/${NC}"
echo -e "${CYAN}PIDs stored in: .pids/${NC}"
echo ""
echo -e "${YELLOW}💡 Tip: Use individual scripts for granular control:${NC}"
echo -e "   ${CYAN}./start-iam.sh${NC}      - Start IAM Service only"
echo -e "   ${CYAN}./start-order.sh${NC}    - Start Order Service only"
echo -e "   ${CYAN}./start-planning.sh${NC} - Start Planning Service only"
echo -e "   ${CYAN}./start-frontend.sh${NC} - Start Frontend only"
echo ""
echo -e "${YELLOW}🔮 Press Cmd+K in browser to activate Oracle${NC}"
echo ""
