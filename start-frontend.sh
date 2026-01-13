#!/bin/bash

# =====================================================
# VoidTracker 2.0 - Frontend Dev Server Startup
# Starts: Vue/Vite frontend development server
# =====================================================

set -e

# Colors
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${CYAN}"
echo "╔════════════════════════════════════════╗"
echo "║   FRONTEND DEV SERVER STARTUP          ║"
echo "╚════════════════════════════════════════╝"
echo -e "${NC}"

# Check if we're in the right directory
if [ ! -f "modules/web/voidtracker-web/package.json" ]; then
    echo -e "${RED}❌ Error: Frontend not found at modules/web/voidtracker-web/${NC}"
    echo -e "${YELLOW}💡 Make sure you're in the VoidTracker root directory${NC}"
    exit 1
fi

# Check if node_modules exists
if [ ! -d "modules/web/voidtracker-web/node_modules" ]; then
    echo -e "${YELLOW}⚠️  node_modules not found. Installing dependencies...${NC}"
    cd modules/web/voidtracker-web
    npm install
    cd ../../..
fi

# Check if frontend is already running
if [ -f ".pids/frontend.pid" ]; then
    PID=$(cat .pids/frontend.pid)
    if ps -p $PID > /dev/null 2>&1; then
        echo -e "${YELLOW}⚠️  Frontend already running (PID: $PID)${NC}"
        echo -e "${CYAN}💡 To restart, run: ./stop-frontend.sh${NC}"
        exit 0
    else
        # Stale PID file
        rm -f .pids/frontend.pid
    fi
fi

# Create .pids and logs directories if they don't exist
mkdir -p .pids logs

# Store root directory path (before cd)
ROOT_DIR=$(pwd)

# Start frontend dev server
echo -e "${CYAN}🚀 Starting Frontend Dev Server...${NC}"
cd modules/web/voidtracker-web

# Start in background and save PID (use absolute paths)
nohup npm run dev > "$ROOT_DIR/logs/frontend-dev.log" 2>&1 &
FRONTEND_PID=$!

# Save PID (use absolute path)
echo $FRONTEND_PID > "$ROOT_DIR/.pids/frontend.pid"

cd "$ROOT_DIR"

# Wait a moment for server to start
sleep 3

# Check if process is still running
if ps -p $FRONTEND_PID > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Frontend Dev Server started (PID: $FRONTEND_PID)${NC}"
    echo ""
    echo "───────────────────────────────────────────"
    echo -e "🌐 ${GREEN}Frontend:${NC}        http://0.0.0.0:5173"
    echo -e "📝 ${CYAN}Logs:${NC}            tail -f logs/frontend-dev.log"
    echo -e "🛑 ${CYAN}Stop:${NC}            ./stop-frontend.sh"
    echo "───────────────────────────────────────────"
    echo ""
    echo -e "${CYAN}💡 Frontend is accessible from:${NC}"
    echo -e "   - Local: http://localhost:5173"
    echo -e "   - Network: http://$(hostname -I | awk '{print $1}'):5173"
    echo ""
else
    echo -e "${RED}❌ Frontend failed to start${NC}"
    echo -e "${YELLOW}💡 Check logs: tail -f logs/frontend-dev.log${NC}"
    rm -f .pids/frontend.pid
    exit 1
fi
