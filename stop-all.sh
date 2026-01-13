#!/bin/bash

# =====================================================
# VoidTracker 2.0 - Application Shutdown Script
# Stops: Spring Boot Services + Vue Frontend
# =====================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colors
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

PID_DIR="$SCRIPT_DIR/.pids"

echo -e "${CYAN}"
echo "╔════════════════════════════════════════╗"
echo "║   VOID-FLOW APPLICATION SHUTDOWN       ║"
echo "╚════════════════════════════════════════╝"
echo -e "${NC}"

# Function: Gracefully kill process
kill_gracefully() {
    local pid_file=$1
    local name=$2
    
    if [ ! -f "$pid_file" ]; then
        echo -e "${YELLOW}⚠️  No PID file for $name${NC}"
        return
    fi
    
    local pid=$(cat "$pid_file")
    
    if ! ps -p $pid > /dev/null 2>&1; then
        echo -e "${YELLOW}⚠️  $name (PID: $pid) not running${NC}"
        rm -f "$pid_file"
        return
    fi
    
    echo -e "${CYAN}🛑 Stopping $name (PID: $pid)...${NC}"
    
    # Send SIGTERM (graceful shutdown)
    kill -15 $pid 2>/dev/null || true
    
    # Wait up to 10 seconds
    local waited=0
    while ps -p $pid > /dev/null 2>&1 && [ $waited -lt 10 ]; do
        echo "   ...waiting for graceful shutdown ($waited/10)"
        sleep 1
        waited=$((waited + 1))
    done
    
    # Force kill if still running
    if ps -p $pid > /dev/null 2>&1; then
        echo -e "${RED}⚠️  Process still running, sending SIGKILL...${NC}"
        kill -9 $pid 2>/dev/null || true
        sleep 1
    fi
    
    rm -f "$pid_file"
    echo -e "${GREEN}✅ $name stopped${NC}"
}

# Stop Backend Services
echo -e "\n${CYAN}🛑 Stopping Backend Services...${NC}"

kill_gracefully "$PID_DIR/planning-service.pid" "Planning Service"
kill_gracefully "$PID_DIR/order-service.pid" "Order Service"
kill_gracefully "$PID_DIR/iam-service.pid" "IAM Service"
kill_gracefully "$PID_DIR/dashboard-service.pid" "Dashboard Service"

# Stop Frontend
echo -e "\n${CYAN}🛑 Stopping Frontend...${NC}"
kill_gracefully "$PID_DIR/frontend.pid" "Frontend (Vite)"

# Also kill any orphaned npm/vite processes
echo -e "${CYAN}🧹 Cleaning up orphaned processes...${NC}"
pkill -f "vite" 2>/dev/null || true
pkill -f "vue.*dev" 2>/dev/null || true

# CRITICAL: Force clear port 5173 (Zombie Killer)
if lsof -i :5173 > /dev/null 2>&1; then
    echo -e "${RED}🧟 Found zombie process on port 5173 - Killing it...${NC}"
    lsof -ti :5173 | xargs kill -9 2>/dev/null || true
    sleep 1
fi

# Stop Docker containers (if using docker-compose mode)
MODE="${1:-default}"
if [ "$MODE" = "docker" ] || [ "$MODE" = "all" ]; then
    echo -e "\n${CYAN}🐳 Stopping Docker containers...${NC}"
    docker compose -f docker-compose.yml down || true
fi

# Success
echo -e "\n${GREEN}"
echo "╔════════════════════════════════════════╗"
echo "║   APPLICATIONS STOPPED ✅               ║"
echo "╚════════════════════════════════════════╝"
echo -e "${NC}"
echo -e "${CYAN}Infrastructure still running. Use ./stop-sup.sh to stop it.${NC}"
echo ""
