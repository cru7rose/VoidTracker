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

# Function: Wait for port to open with retry logic
wait_for_port() {
    local port=$1
    local name=$2
    local max_wait=${3:-120}  # Increased default timeout to 2 minutes
    local elapsed=0
    local retry_count=0
    local base_delay=2
    
    echo -e "${CYAN}⏳ Waiting for $name (port $port, timeout: ${max_wait}s)...${NC}"
    
    while [ $retry_count -lt 100 ] && [ $elapsed -lt $max_wait ]; do
        if nc -z -w 2 localhost $port 2>/dev/null; then
            echo -e "${GREEN}✅ $name ready on port $port (after ${elapsed}s)${NC}"
            return 0
        fi
        
        # Exponential backoff: 2s, 3s, 4s, 5s, then cap at 5s
        local delay=$((base_delay + (retry_count / 10)))
        if [ $delay -gt 5 ]; then
            delay=5
        fi
        
        sleep $delay
        elapsed=$((elapsed + delay))
        retry_count=$((retry_count + 1))
        
        # Show progress every 10 seconds
        if [ $((elapsed % 10)) -eq 0 ] || [ $retry_count -eq 1 ]; then
            echo -e "   ...waiting ($elapsed/$max_wait seconds, attempt $retry_count)"
        fi
    done
    
    if [ $elapsed -ge $max_wait ]; then
        echo -e "${RED}❌ Timeout waiting for $name on port $port after ${elapsed}s${NC}"
        return 1
    fi
    
    return 1
}

# Docker compose will create the network automatically
# No manual creation needed

# 2. Start Infrastructure (DB, Kafka, Neo4j)
echo -e "\n${CYAN}🏗️  Starting Core Infrastructure...${NC}"
docker compose -f docker-compose.infra.yml up -d

# 3. Start Support Services (MailHog, n8n)
echo -e "\n${CYAN}🛠️  Starting Support Services...${NC}"
docker compose -f docker-compose.support.yml up -d

# 4. Health Checks with Timeout
echo -e "\n${CYAN}🏥 Performing Health Checks...${NC}"

# PostgreSQL - Check via container command with increased timeout
echo -e "${CYAN}⏳ Waiting for PostgreSQL...${NC}"
max_wait=120  # Increased to 2 minutes
elapsed=0
retry_count=0
until docker exec postgres pg_isready -U postgres > /dev/null 2>&1; do
    if [ $elapsed -ge $max_wait ]; then
        echo -e "${RED}❌ Timeout waiting for PostgreSQL after ${elapsed}s${NC}"
        echo -e "${YELLOW}💡 Check container: docker logs postgres${NC}"
        exit 1
    fi
    
    # Exponential backoff
    delay=$((2 + (retry_count / 10)))
    if [ $delay -gt 5 ]; then
        delay=5
    fi
    
    if [ $((elapsed % 10)) -eq 0 ] || [ $retry_count -eq 0 ]; then
        echo "   ...waiting for Postgres ($elapsed/$max_wait seconds, attempt $((retry_count + 1)))"
    fi
    
    sleep $delay
    elapsed=$((elapsed + delay))
    retry_count=$((retry_count + 1))
done
echo -e "${GREEN}✅ PostgreSQL ready (after ${elapsed}s)${NC}"

# Initialize databases
echo -e "${CYAN}🗄️  Initializing databases...${NC}"
docker exec -i postgres psql -U postgres -d postgres <<'EOF' 2>/dev/null || true
SELECT 'CREATE DATABASE vt_iam_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'vt_iam_service')\gexec
SELECT 'CREATE DATABASE voidtracker_orders' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'voidtracker_orders')\gexec
SELECT 'CREATE DATABASE voidtracker_planning' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'voidtracker_planning')\gexec
SELECT 'CREATE DATABASE voidtracker_crm' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'voidtracker_crm')\gexec
EOF
echo -e "${GREEN}✅ Databases initialized${NC}"

# Kafka (port 9094 external) - increased timeout
wait_for_port 9094 "Kafka" 120 || exit 1

# Neo4j HTTP (port 7474) - improved with retry logic
echo -e "${CYAN}⏳ Waiting for Neo4j...${NC}"
max_wait=120  # Increased to 2 minutes
elapsed=0
retry_count=0
until curl -s -f --connect-timeout 5 --max-time 10 http://localhost:7474 > /dev/null 2>&1; do
    if [ $elapsed -ge $max_wait ]; then
        echo -e "${RED}❌ Timeout waiting for Neo4j after ${elapsed}s${NC}"
        echo -e "${YELLOW}💡 Check container: docker logs neo4j${NC}"
        exit 1
    fi
    
    # Exponential backoff
    delay=$((2 + (retry_count / 10)))
    if [ $delay -gt 5 ]; then
        delay=5
    fi
    
    if [ $((elapsed % 10)) -eq 0 ] || [ $retry_count -eq 0 ]; then
        echo "   ...waiting for Neo4j ($elapsed/$max_wait seconds, attempt $((retry_count + 1)))"
    fi
    
    sleep $delay
    elapsed=$((elapsed + delay))
    retry_count=$((retry_count + 1))
done
echo -e "${GREEN}✅ Neo4j ready on port 7474 (after ${elapsed}s)${NC}"

# Optional: Redis, MailHog (non-critical) - increased timeouts
wait_for_port 6379 "Redis" 60 || echo -e "${YELLOW}⚠️  Redis optional service not ready${NC}"
wait_for_port 8025 "MailHog" 40 || echo -e "${YELLOW}⚠️  MailHog optional service not ready${NC}"

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
