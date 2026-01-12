#!/bin/bash

###############################################################################
# DANXILS Port Conflict Resolver
# This script checks for and resolves port conflicts before starting DANXILS
###############################################################################

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║     DANXILS Port Conflict Resolver                        ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Ports used by DANXILS
PORTS=(8081 8091 8092 5173 9092 5432)
PORT_NAMES=("IAM Service" "Order Service" "Planning Service" "Web Dashboard" "Kafka" "PostgreSQL")

echo -e "${YELLOW}Checking for port conflicts...${NC}"
echo ""

CONFLICTS=0

for i in "${!PORTS[@]}"; do
    PORT=${PORTS[$i]}
    NAME=${PORT_NAMES[$i]}
    
    # Check if port is in use
    if lsof -i :$PORT > /dev/null 2>&1; then
        CONFLICTS=$((CONFLICTS + 1))
        echo -e "${RED}✗ Port $PORT ($NAME) is in use${NC}"
        
        # Show what's using it
        PROCESS=$(lsof -i :$PORT | grep LISTEN | awk '{print $1, $2}' | tail -1)
        echo -e "  Process: ${YELLOW}$PROCESS${NC}"
        
        # Offer to kill it
        PID=$(lsof -i :$PORT | grep LISTEN | awk '{print $2}' | tail -1)
        
        # Don't auto-kill Docker processes
        if [[ $PROCESS == *"docker"* ]] || [[ $PROCESS == *"com.dock"* ]]; then
            echo -e "  ${BLUE}This is a Docker process. Use: docker-compose down${NC}"
        else
            read -p "  Kill this process? (y/n): " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                kill -9 $PID
                echo -e "  ${GREEN}✓ Process killed${NC}"
            fi
        fi
        echo ""
    else
        echo -e "${GREEN}✓ Port $PORT ($NAME) is available${NC}"
    fi
done

echo ""

if [ $CONFLICTS -eq 0 ]; then
    echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║   ✓ All ports are available!                              ║${NC}"
    echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${BLUE}You can now run: ./start-danxils.sh${NC}"
else
    echo -e "${YELLOW}╔════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${YELLOW}║   ⚠ Some ports are still in use                           ║${NC}"
    echo -e "${YELLOW}╚════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${BLUE}Manual cleanup options:${NC}"
    echo -e "  • Stop Docker containers: ${YELLOW}cd TES && docker-compose down${NC}"
    echo -e "  • Kill specific process: ${YELLOW}kill -9 <PID>${NC}"
    echo -e "  • Restart your computer (last resort)"
fi

echo ""
