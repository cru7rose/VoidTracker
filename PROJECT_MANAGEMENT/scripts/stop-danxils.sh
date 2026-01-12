#!/bin/bash

###############################################################################
# DANXILS Enterprise System - Stop Script
# This script stops all running DANXILS services
###############################################################################

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║     DANXILS Enterprise System - Stop Script               ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

###############################################################################
# Stop Backend Services
###############################################################################

echo -e "${YELLOW}Stopping backend services...${NC}"

stop_service() {
    local service_name=$1
    local pid_file="$PROJECT_ROOT/logs/${service_name}.pid"
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            echo -e "${BLUE}Stopping $service_name (PID: $pid)...${NC}"
            kill $pid
            rm "$pid_file"
            echo -e "${GREEN}✓ $service_name stopped${NC}"
        else
            echo -e "${YELLOW}⚠ $service_name is not running${NC}"
            rm "$pid_file"
        fi
    else
        echo -e "${YELLOW}⚠ $service_name PID file not found${NC}"
    fi
}

stop_service "iam-service"
stop_service "order-service"
stop_service "planning-service"
stop_service "dashboard-web"

echo ""

###############################################################################
# Stop Infrastructure
###############################################################################

echo -e "${YELLOW}Stopping infrastructure...${NC}"

if [ -f "$PROJECT_ROOT/TES/docker-compose.yml" ]; then
    cd "$PROJECT_ROOT/TES"
    docker-compose down
    echo -e "${GREEN}✓ Infrastructure stopped${NC}"
fi

echo ""

###############################################################################
# Cleanup
###############################################################################

echo -e "${YELLOW}Cleaning up...${NC}"

# Kill any remaining Java processes (optional - commented out for safety)
# pkill -f "spring-boot:run"

echo -e "${GREEN}✓ Cleanup complete${NC}"
echo ""

echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║   ✓ DANXILS Enterprise System Stopped Successfully!       ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
