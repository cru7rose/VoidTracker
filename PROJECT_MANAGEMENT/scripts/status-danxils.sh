#!/bin/bash

###############################################################################
# DANXILS Enterprise System - Status Check Script
# This script checks the status of all DANXILS services
###############################################################################

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║     DANXILS Enterprise System - Status Check              ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

###############################################################################
# Check Backend Services
###############################################################################

echo -e "${YELLOW}Backend Services:${NC}"
echo ""

check_service() {
    local service_name=$1
    local service_id=$2
    local url=$3
    local pid_file="$PROJECT_ROOT/logs/${service_id}.pid"
    
    printf "%-20s" "$service_name:"
    
    # Check if PID file exists
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        
        # Check if process is running
        if ps -p $pid > /dev/null 2>&1; then
            # Check if service responds
            if curl -s "$url" > /dev/null 2>&1; then
                echo -e "${GREEN}✓ Running${NC} (PID: $pid)"
            else
                echo -e "${YELLOW}⚠ Starting${NC} (PID: $pid)"
            fi
        else
            echo -e "${RED}✗ Stopped${NC}"
        fi
    else
        echo -e "${RED}✗ Not Started${NC}"
    fi
}

check_service "IAM Service" "iam-service" "http://localhost:8081/actuator/health"
check_service "Order Service" "order-service" "http://localhost:8091/actuator/health"
check_service "Planning Service" "planning-service" "http://localhost:8092/actuator/health"

echo ""

###############################################################################
# Check Frontend
###############################################################################

echo -e "${YELLOW}Frontend Applications:${NC}"
echo ""

check_service "Web Dashboard" "dashboard-web" "http://localhost:5173"

echo ""

###############################################################################
# Check Infrastructure
###############################################################################

echo -e "${YELLOW}Infrastructure:${NC}"
echo ""

# Check Kafka
printf "%-20s" "Kafka:"
if docker ps | grep -q kafka; then
    echo -e "${GREEN}✓ Running${NC}"
else
    echo -e "${RED}✗ Stopped${NC}"
fi

# Check PostgreSQL
printf "%-20s" "PostgreSQL:"
if docker ps | grep -q postgres; then
    echo -e "${GREEN}✓ Running${NC}"
else
    echo -e "${RED}✗ Stopped${NC}"
fi

echo ""

###############################################################################
# Service URLs
###############################################################################

echo -e "${YELLOW}Service URLs:${NC}"
echo ""
echo -e "  • Customer Portal:  ${BLUE}http://localhost:5173/customer${NC}"
echo -e "  • Internal Portal:  ${BLUE}http://localhost:5173/internal${NC}"
echo -e "  • Order API:        ${BLUE}http://localhost:8091/api/orders${NC}"
echo -e "  • Planning API:     ${BLUE}http://localhost:8092/api/planning${NC}"
echo -e "  • Swagger UI:       ${BLUE}http://localhost:8091/swagger-ui.html${NC}"
echo ""

###############################################################################
# Log Files
###############################################################################

echo -e "${YELLOW}Log Files:${NC}"
echo ""

if [ -d "$PROJECT_ROOT/logs" ]; then
    ls -lh "$PROJECT_ROOT/logs"/*.log 2>/dev/null | awk '{print "  " $9 " (" $5 ")"}'
else
    echo -e "  ${YELLOW}No log files found${NC}"
fi

echo ""
