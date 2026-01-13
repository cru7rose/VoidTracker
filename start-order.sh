#!/bin/bash

# ===================================================================== 
# VoidTracker 2.0 - Order Service Startup Script
# Starts: Order Service only
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
echo "║   ORDER SERVICE STARTUP                ║"
echo "╚════════════════════════════════════════╝"
echo -e "${NC}"

# Function: Check if infrastructure is ready
check_infrastructure() {
    echo -e "${CYAN}🔍 Verifying Infrastructure...${NC}"
    
    local required_ports=(5434 9094)
    local port_names=("PostgreSQL" "Kafka")
    
    for i in "${!required_ports[@]}"; do
        local port=${required_ports[$i]}
        local name=${port_names[$i]}
        
        if ! nc -z localhost $port 2>/dev/null; then
            echo -e "${RED}❌ $name is not reachable on port $port!${NC}"
            echo -e "${YELLOW}💡 Please run './start-sup.sh' first to start infrastructure${NC}"
            exit 1
        fi
    done
    
    echo -e "${GREEN}✅ Infrastructure is running${NC}"
}

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

# Function: Wait for service health
wait_for_health() {
    local url=$1
    local name=$2
    local max_wait=${3:-180}
    local elapsed=0
    local retry_count=0
    local max_retries=60
    local base_delay=2
    
    echo -e "${CYAN}⏳ Waiting for $name health check (timeout: ${max_wait}s)...${NC}"
    
    while [ $retry_count -lt $max_retries ] && [ $elapsed -lt $max_wait ]; do
        if curl -s -f --connect-timeout 5 --max-time 10 "$url" > /dev/null 2>&1; then
            echo -e "${GREEN}✅ $name is healthy (after ${elapsed}s)${NC}"
            return 0
        fi
        
        local delay=$((base_delay + (retry_count / 10)))
        if [ $delay -gt 5 ]; then
            delay=5
        fi
        
        sleep $delay
        elapsed=$((elapsed + delay))
        retry_count=$((retry_count + 1))
        
        if [ $((elapsed % 10)) -eq 0 ] || [ $retry_count -eq 1 ]; then
            echo "   ...waiting ($elapsed/$max_wait seconds, attempt $retry_count)"
        fi
    done
    
    if [ $elapsed -ge $max_wait ]; then
        echo -e "${RED}❌ Timeout waiting for $name health endpoint after ${elapsed}s${NC}"
        echo -e "${YELLOW}💡 Check logs: tail -f logs/order-service.log${NC}"
        return 1
    fi
    
    return 1
}

# Step 0: Check infrastructure
check_infrastructure

# Step 1: Build (if needed)
if [ "$SKIP_BUILD" != "1" ]; then
    echo -e "\n${CYAN}📦 Building Order Service...${NC}"
    cd modules/nexus/order-service
    
    jar_path="target/order-service-1.0.0-SNAPSHOT.jar"
    src_path="src"
    
    if [ ! -f "$jar_path" ] || [ -n "$(find "$src_path" -type f -newer "$jar_path" 2>/dev/null)" ]; then
        echo -e "${YELLOW}🔨 Building Order Service...${NC}"
        mvn clean package -DskipTests -T 1C -q
        echo -e "${GREEN}✅ Build complete${NC}"
    else
        echo -e "${GREEN}✅ Using existing JAR (up to date)${NC}"
    fi
    
    cd "$SCRIPT_DIR"
else
    echo -e "${YELLOW}⏭️  Skipping build (SKIP_BUILD=1)${NC}"
fi

# Step 2: Start Service
echo -e "\n${CYAN}🚀 Starting Order Service...${NC}"

if is_running "$PID_DIR/order-service.pid"; then
    echo -e "${YELLOW}⚠️  Order Service already running (PID: $(cat $PID_DIR/order-service.pid))${NC}"
else
    cd modules/nexus/order-service
    nohup java -jar target/order-service-1.0.0-SNAPSHOT.jar > "$LOGS_DIR/order-service.log" 2>&1 &
    echo $! > "$PID_DIR/order-service.pid"
    echo -e "${GREEN}✅ Order Service started (PID: $!)${NC}"
    cd "$SCRIPT_DIR"
    
    # Wait for health
    sleep 8
    wait_for_health "http://localhost:8091/actuator/health" "Order Service" 180 || echo -e "${YELLOW}⚠️  Health check timeout (service may still be starting)${NC}"
fi

# Success
echo -e "\n${GREEN}"
echo "╔════════════════════════════════════════╗"
echo "║   ORDER SERVICE OPERATIONAL ✅          ║"
echo "╚════════════════════════════════════════╝"
echo -e "${NC}"
echo "───────────────────────────────────────────"
echo -e "📦 ${GREEN}Order Service:${NC}    http://localhost:8091"
echo "───────────────────────────────────────────"
echo -e "${CYAN}Logs: tail -f logs/order-service.log${NC}"
echo -e "${CYAN}PID: $(cat $PID_DIR/order-service.pid)${NC}"
echo ""
