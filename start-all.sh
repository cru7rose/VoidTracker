#!/bin/bash

# ===================================================================== 
# VoidTracker 2.0 - Application Startup Script
# Starts: Spring Boot Services (Nexus/Titan/Flux) + Vue Frontend (Ghost)
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

echo -e "${CYAN}"
echo "╔════════════════════════════════════════╗"
echo "║   VOID-FLOW APPLICATION STARTUP        ║"
echo "╚════════════════════════════════════════╝"
echo -e "${NC}"

# Function: Check if infrastructure is ready
check_infrastructure() {
    echo -e "${CYAN}🔍 Verifying Infrastructure...${NC}"
    
    local required_ports=(5434 9094 7474)
    local port_names=("PostgreSQL" "Kafka" "Neo4j")
    
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
    local max_wait=${3:-60}
    local elapsed=0
    
    echo -e "${CYAN}⏳ Waiting for $name health check...${NC}"
    while ! curl -s -f "$url" > /dev/null 2>&1; do
        if [ $elapsed -ge $max_wait ]; then
            echo -e "${RED}❌ Timeout waiting for $name health endpoint${NC}"
            return 1
        fi
        echo "   ...waiting ($elapsed/$max_wait seconds)"
        sleep 3
        elapsed=$((elapsed + 3))
    done
    echo -e "${GREEN}✅ $name is healthy${NC}"
    return 0
}

# Step 0: Check infrastructure
check_infrastructure

# Step 1: Smart Rebuild - Only changed services
if [ "$SKIP_BUILD" != "1" ]; then
    echo -e "\n${CYAN}📦 Checking services for rebuild...${NC}"
    
    # Function: Rebuild service if source newer than JAR
    rebuild_if_needed() {
        local service_name=$1
        local service_dir=$2
        local jar_name=$3
        
        local jar_path="$service_dir/target/$jar_name"
        local src_path="$service_dir/src"
        
        if [ ! -f "$jar_path" ]; then
            echo -e "${YELLOW}🔨 $service_name: JAR not found, building...${NC}"
            (cd "$service_dir" && mvn clean package -DskipTests -q)
            return
        fi
        
        # Check if any source file is newer than JAR
        if [ -n "$(find "$src_path" -type f -newer "$jar_path" 2>/dev/null)" ]; then
            echo -e "${YELLOW}🔨 $service_name: Source changes detected, rebuilding...${NC}"
            (cd "$service_dir" && mvn clean package -DskipTests -q)
        else
            echo -e "${GREEN}✅ $service_name: Using existing JAR (up to date)${NC}"
        fi
    }
    
    # Rebuild services selectively
    rebuild_if_needed "IAM Service" "modules/nexus/iam-service" "iam-app-1.0.0-SNAPSHOT.jar"
    rebuild_if_needed "Order Service" "modules/nexus/order-service" "order-service-1.0.0-SNAPSHOT.jar"
    rebuild_if_needed "Planning Service" "modules/flux/planning-service" "planning-service-1.0.0-SNAPSHOT.jar"
    
    echo -e "${GREEN}✅ Build check complete${NC}"
else
    echo -e "${YELLOW}⏭️  Skipping build (SKIP_BUILD=1)${NC}"
fi

# Step 2: Build Frontend (if needed)
echo -e "\n${CYAN}📦 Building Frontend (Void-Flow)...${NC}"
cd modules/web/voidtracker-web

if [ ! -d "node_modules" ]; then
    echo "Installing npm dependencies..."
    npm install
fi

# Start setup for Frontend (Ensure port 5173 is free)
if lsof -i :5173 > /dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  Port 5173 is occupied. Attempting to clear...${NC}"
    lsof -ti :5173 | xargs kill -9 2>/dev/null || true
    sleep 2
    if lsof -i :5173 > /dev/null 2>&1; then
         echo -e "${RED}❌ Cannot clear port 5173. Please check manually.${NC}"
         exit 1
    fi
    echo -e "${GREEN}✅ Port 5173 cleared.${NC}"
fi

# Check if already running
if is_running "$PID_DIR/frontend.pid"; then
    echo -e "${YELLOW}⚠️  Frontend already running (PID: $(cat $PID_DIR/frontend.pid))${NC}"
else
    echo "Starting Vite dev server..."
    nohup npm run dev > "$SCRIPT_DIR/logs/frontend.log" 2>&1 &
    echo $! > "$PID_DIR/frontend.pid"
    echo -e "${GREEN}✅ Frontend started (PID: $!)${NC}"
fi

cd "$SCRIPT_DIR"

# Step 3: Start Backend Services
echo -e "\n${CYAN}� Starting Backend Services...${NC}"

# Start iam-service
if is_running "$PID_DIR/iam-service.pid"; then
    echo -e "${YELLOW}⚠️  IAM Service already running${NC}"
else
    echo -e "${CYAN}Starting IAM Service...${NC}"
    cd modules/nexus/iam-service
    nohup java -jar target/iam-app-1.0.0-SNAPSHOT.jar > "$SCRIPT_DIR/logs/iam-service.log" 2>&1 &
    echo $! > "$PID_DIR/iam-service.pid"
    echo -e "${GREEN}✅ IAM Service started (PID: $!)${NC}"
    cd "$SCRIPT_DIR"
    
    # Wait for health
    sleep 5
    wait_for_health "http://localhost:8081/actuator/health" "IAM Service" 90 || echo -e "${YELLOW}⚠️  Health check timeout (service may still be starting)${NC}"
fi

# Start planning-service
if is_running "$PID_DIR/planning-service.pid"; then
    echo -e "${YELLOW}⚠️  Planning Service already running${NC}"
else
    echo -e "${CYAN}Starting Planning Service...${NC}"
    cd modules/flux/planning-service
    nohup java -jar target/planning-service-1.0.0-SNAPSHOT.jar > "$SCRIPT_DIR/logs/planning-service.log" 2>&1 &
    echo $! > "$PID_DIR/planning-service.pid"
    echo -e "${GREEN}✅ Planning Service started (PID: $!)${NC}"
    cd "$SCRIPT_DIR"
    
    # Wait for health
    sleep 5
    wait_for_health "http://localhost:8093/actuator/health" "Planning Service" 90 || echo -e "${YELLOW}⚠️  Health check timeout (service may still be starting)${NC}"
fi

# Start order-service
if is_running "$PID_DIR/order-service.pid"; then
    # Check if JAR was updated after process start
    echo -e "${CYAN}Checking Order Service freshness...${NC}"
    jar_path="modules/nexus/order-service/target/order-service-1.0.0-SNAPSHOT.jar"
    pid=$(cat "$PID_DIR/order-service.pid")
    
    # Get process start time and JAR modification time
    if [ -f "$jar_path" ]; then
        jar_modified=$(stat -f "%m" "$jar_path" 2>/dev/null || stat -c "%Y" "$jar_path" 2>/dev/null)
        proc_started=$(ps -p $pid -o lstart= | xargs -I {} date -j -f "%a %b %d %T %Y" "{}" "+%s" 2>/dev/null || echo "0")
        
        if [ "$jar_modified" -gt "$proc_started" ] 2>/dev/null; then
            echo -e "${YELLOW}⚠️  Order Service JAR updated (restarting with new code)...${NC}"
            kill -15 $pid
            sleep 3
            rm -f "$PID_DIR/order-service.pid"
            
            echo -e "${CYAN}Starting Order Service with updated JAR...${NC}"
            cd modules/nexus/order-service
            nohup java -jar target/order-service-1.0.0-SNAPSHOT.jar > "$SCRIPT_DIR/logs/order-service.log" 2>&1 &
            echo $! > "$PID_DIR/order-service.pid"
            echo -e "${GREEN}✅ Order Service restarted (PID: $!)${NC}"
            cd "$SCRIPT_DIR"
            
            sleep 5
            wait_for_health "http://localhost:8091/actuator/health" "Order Service" 90 || echo -e "${YELLOW}⚠️  Health check timeout (service may still be starting)${NC}"
        else
            echo -e "${YELLOW}⚠️  Order Service already running (JAR up to date)${NC}"
        fi
    else
        echo -e "${YELLOW}⚠️  Order Service already running (JAR not found - skipping freshness check)${NC}"
    fi
else
    echo -e "${CYAN}Starting Order Service...${NC}"
    cd modules/nexus/order-service
    nohup java -jar target/order-service-1.0.0-SNAPSHOT.jar > "$SCRIPT_DIR/logs/order-service.log" 2>&1 &
    echo $! > "$PID_DIR/order-service.pid"
    echo -e "${GREEN}✅ Order Service started (PID: $!)${NC}"
    cd "$SCRIPT_DIR"
    
    sleep 5
    wait_for_health "http://localhost:8091/actuator/health" "Order Service" 90 || echo -e "${YELLOW}⚠️  Health check timeout (service may still be starting)${NC}"
fi

# Step 4: Success Summary
echo -e "\n${GREEN}"
echo "╔════════════════════════════════════════╗"
echo "║   SYSTEM OPERATIONAL ✅                 ║"
echo "╚════════════════════════════════════════╝"
echo -e "${NC}"
echo "───────────────────────────────────────────"
echo -e "🖥️  ${GREEN}Frontend:${NC}         http://localhost:5173"
echo -e "📦 ${GREEN}Order Service:${NC}    http://localhost:8091"
echo -e "🔐 ${GREEN}IAM Service:${NC}      http://localhost:8081"
echo -e "🧠 ${GREEN}Planning Service:${NC} http://localhost:8093"
echo "───────────────────────────────────────────"
echo -e "${CYAN}Logs available in: ./logs/${NC}"
echo -e "${CYAN}PIDs stored in: .pids/${NC}"
echo ""
echo -e "${YELLOW}🔮 Press Cmd+K in browser to activate Oracle${NC}"
echo ""
