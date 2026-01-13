#!/bin/bash

# =====================================================
# VoidTracker 2.0 - Infrastructure Shutdown Script
# Stops: PostgreSQL, Kafka, Neo4j, Redis, MailHog, n8n
# =====================================================

set -e

# Colors
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

MODE="${1:-default}"

echo -e "${CYAN}"
echo "╔════════════════════════════════════════╗"
echo "║   INFRASTRUCTURE SHUTDOWN              ║"
echo "╚════════════════════════════════════════╝"
echo -e "${NC}"

# Stop Support Services
echo -e "${CYAN}🛠️  Stopping Support Services (MailHog, n8n)...${NC}"
docker compose -f docker-compose.support.yml down || true
echo -e "${GREEN}✅ Support services stopped${NC}"

# Stop Infrastructure
if [ "$MODE" = "preserve" ]; then
    echo -e "${YELLOW}📦 Stopping infrastructure (PRESERVING DATA)...${NC}"
    docker compose -f docker-compose.infra.yml stop
elif [ "$MODE" = "nuke" ]; then
    echo -e "${RED}💣 Stopping infrastructure + REMOVING VOLUMES (DATA LOSS)...${NC}"
    read -p "Are you sure? Type 'YES' to confirm: " -r
    echo
    if [[ $REPLY == "YES" ]]; then
        docker compose -f docker-compose.infra.yml down -v --remove-orphans
        echo -e "${RED}✅ Infrastructure nuked (volumes deleted)${NC}"
    else
        echo -e "${YELLOW}❌ Cancelled${NC}"
        exit 0
    fi
else
    echo -e "${CYAN}🏗️  Stopping infrastructure (default)...${NC}"
    docker compose -f docker-compose.infra.yml down
fi

echo -e "${GREEN}✅ Infrastructure stopped${NC}"

# Success
echo -e "\n${GREEN}"
echo "╔════════════════════════════════════════╗"
echo "║   INFRASTRUCTURE STOPPED ✅             ║"
echo "╚════════════════════════════════════════╝"
echo -e "${NC}"
echo ""
echo -e "${YELLOW}Modes:${NC}"
echo -e "  ${CYAN}./stop-sup.sh${NC}          - Stop (default)"
echo -e "  ${CYAN}./stop-sup.sh preserve${NC} - Stop but keep containers (faster restart)"
echo -e "  ${CYAN}./stop-sup.sh nuke${NC}     - Stop + delete all data (⚠️  destructive)"
echo ""
