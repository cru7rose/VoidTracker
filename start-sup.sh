#!/bin/bash

# =====================================================
# VoidTracker 2.0 - Infrastructure Startup Script
# Starts: PostgreSQL, Kafka, Neo4j, Redis, MailHog, n8n
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
echo "║   VOID-FLOW INFRASTRUCTURE STARTUP     ║"
echo "╚════════════════════════════════════════╝"
echo -e "${NC}"

# Function: Check if port is already in use (idempotency)
check_port() {
    local port=$1
    local name=$2
    if nc -z localhost $port 2>/dev/null; then
        echo -e "${YELLOW}⚠️  $name already running on port $port${NC}"
        return 0
    else
        return 1
    fi
}

# Function: Wait for port to open
wait_for_port() {
    local port=$1
    local name=$2
    local max_wait=${3:-60}
    local elapsed=0
    
    echo -e "${CYAN}⏳ Waiting for $name (port $port)...${NC}"
    while ! nc -z localhost $port 2>/dev/null; do
        if [ $elapsed -ge $max_wait ]; then
            echo -e "${RED}❌ Timeout waiting for $name on port $port${NC}"
            return 1
        fi
        echo -e "   ...waiting ($elapsed/$max_wait seconds)"
        sleep 2
        elapsed=$((elapsed + 2))
    done
    echo -e "${GREEN}✅ $name ready on port $port${NC}"
    return 0
}

# Docker compose will create the network automatically
# No manual creation needed

# 2. Start Infrastructure (DB, Kafka, Neo4j)
echo -e "\n${CYAN}🏗️  Starting Core Infrastructure...${NC}"
docker-compose -f docker-compose.infra.yml up -d

# 3. Start Support Services (MailHog, n8n)
echo -e "\n${CYAN}🛠️  Starting Support Services...${NC}"
docker-compose -f docker-compose.support.yml up -d

# 4. Health Checks with Timeout
echo -e "\n${CYAN}🏥 Performing Health Checks...${NC}"

# PostgreSQL - Check via container command
echo -e "${CYAN}⏳ Waiting for PostgreSQL...${NC}"
max_wait=60
elapsed=0
until docker exec postgres pg_isready -U postgres > /dev/null 2>&1; do
    if [ $elapsed -ge $max_wait ]; then
        echo -e "${RED}❌ Timeout waiting for PostgreSQL${NC}"
        exit 1
    fi
    echo "   ...waiting for Postgres ($elapsed/$max_wait seconds)"
    sleep 2
    elapsed=$((elapsed + 2))
done
echo -e "${GREEN}✅ PostgreSQL ready${NC}"

# Kafka (port 9094 external)
wait_for_port 9094 "Kafka" 90 || exit 1

# Neo4j HTTP (port 7474)
echo -e "${CYAN}⏳ Waiting for Neo4j...${NC}"
max_wait=60
elapsed=0
until curl -s -f http://localhost:7474 > /dev/null 2>&1; do
    if [ $elapsed -ge $max_wait ]; then
        echo -e "${RED}❌ Timeout waiting for Neo4j${NC}"
        exit 1
    fi
    echo "   ...waiting for Neo4j ($elapsed/$max_wait seconds)"
    sleep 2
    elapsed=$((elapsed + 2))
done
echo -e "${GREEN}✅ Neo4j ready on port 7474${NC}"

# Optional: Redis, MailHog (non-critical)
wait_for_port 6379 "Redis" 30 || echo -e "${YELLOW}⚠️  Redis optional service not ready${NC}"
wait_for_port 8025 "MailHog" 20 || echo -e "${YELLOW}⚠️  MailHog optional service not ready${NC}"

# 5. Success Summary
echo -e "\n${GREEN}"
echo "╔════════════════════════════════════════╗"
echo "║   INFRASTRUCTURE READY ✅               ║"
echo "╚════════════════════════════════════════╝"
echo -e "${NC}"
echo "───────────────────────────────────────────"
echo -e "🐘 ${GREEN}PostgreSQL:${NC}      Port 5434 (External)"
echo -e "🕸️  ${GREEN}Kafka:${NC}           Port 9094 (External)"
echo -e "🔷 ${GREEN}Neo4j:${NC}           http://localhost:7474"
echo -e "📧 ${YELLOW}MailHog:${NC}         http://localhost:8025"
echo -e "🤖 ${YELLOW}n8n:${NC}             http://localhost:5678"
echo "───────────────────────────────────────────"
echo -e "${CYAN}Next: Run ${GREEN}./start-all.sh${CYAN} to start applications${NC}"
echo ""
