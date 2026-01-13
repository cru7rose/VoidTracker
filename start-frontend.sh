#!/bin/bash

# ===================================================================== 
# VoidTracker 2.0 - Frontend Startup Script
# Starts: Vue Frontend (Vite) only
# =====================================================================

set -e

# Navigate to project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colors
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

PID_DIR="$SCRIPT_DIR/.pids"
mkdir -p "$PID_DIR"
LOGS_DIR="$SCRIPT_DIR/logs"
mkdir -p "$LOGS_DIR"

echo -e "${CYAN}"
echo "╔════════════════════════════════════════╗"
echo "║   FRONTEND STARTUP                     ║"
echo "╚════════════════════════════════════════╝"
echo -e "${NC}"

# Function: Check if process is running
is_running() {
    local pid_file=$1
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            return 0  # Running
        else
            rm -f "$pid_file"  # Stale PID file
            return 1
        fi
    fi
    return 1
}

# Step 1: Build Frontend (if needed)
echo -e "\n${CYAN}📦 Setting up Frontend...${NC}"
cd modules/web/voidtracker-web

if [ ! -d "node_modules" ]; then
    echo -e "${YELLOW}📥 Installing npm dependencies...${NC}"
    npm install
    echo -e "${GREEN}✅ Dependencies installed${NC}"
else
    echo -e "${GREEN}✅ Dependencies already installed${NC}"
fi

# Step 2: Clear port 5173 if occupied
if lsof -i :5173 > /dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  Port 5173 is occupied. Attempting to clear...${NC}"
    lsof -ti :5173 | xargs kill -9 2>/dev/null || true
    sleep 2
    if lsof -i :5173 > /dev/null 2>&1; then
        echo -e "${RED}❌ Cannot clear port 5173. Please check manually.${NC}"
        exit 1
    fi
    echo -e "${GREEN}✅ Port 5173 cleared${NC}"
fi

# Step 3: Start Frontend
echo -e "\n${CYAN}🚀 Starting Frontend...${NC}"

if is_running "$PID_DIR/frontend.pid"; then
    echo -e "${YELLOW}⚠️  Frontend already running (PID: $(cat $PID_DIR/frontend.pid))${NC}"
else
    echo -e "${CYAN}Starting Vite dev server...${NC}"
    nohup npm run dev > "$LOGS_DIR/frontend.log" 2>&1 &
    echo $! > "$PID_DIR/frontend.pid"
    echo -e "${GREEN}✅ Frontend started (PID: $!)${NC}"
    
    # Wait a moment for Vite to start
    sleep 3
    
    # Check if it's actually running
    if ps -p $! > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Frontend process confirmed running${NC}"
    else
        echo -e "${RED}❌ Frontend process failed to start${NC}"
        echo -e "${YELLOW}💡 Check logs: tail -f logs/frontend.log${NC}"
        exit 1
    fi
fi

cd "$SCRIPT_DIR"

# Success
echo -e "\n${GREEN}"
echo "╔════════════════════════════════════════╗"
echo "║   FRONTEND OPERATIONAL ✅              ║"
echo "╚════════════════════════════════════════╝"
echo -e "${NC}"
echo "───────────────────────────────────────────"
echo -e "🖥️  ${GREEN}Frontend:${NC}         http://localhost:5173"
echo "───────────────────────────────────────────"
echo -e "${CYAN}Logs: tail -f logs/frontend.log${NC}"
echo -e "${CYAN}PID: $(cat $PID_DIR/frontend.pid)${NC}"
echo ""
echo -e "${YELLOW}🔮 Press Cmd+K in browser to activate Oracle${NC}"
echo ""
