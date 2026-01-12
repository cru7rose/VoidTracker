#!/bin/bash

###############################################################################
# DANXILS Enterprise System - Complete Startup Script
# This script starts all microservices, databases, and frontend applications
###############################################################################

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Project root directory
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║     DANXILS Enterprise System - Startup Script            ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

###############################################################################
# Step 1: Check Prerequisites
###############################################################################

echo -e "${YELLOW}[1/7] Checking prerequisites...${NC}"

# Function to kill process on port
kill_port() {
    local port=$1
    local pid=$(lsof -t -i:$port)
    if [ -n "$pid" ]; then
        echo -e "${YELLOW}Killing process on port $port (PID: $pid)...${NC}"
        kill -9 $pid
    fi
}

# Cleanup existing services
echo -e "${BLUE}Cleaning up existing processes...${NC}"
kill_port 8081
kill_port 8091
kill_port 8092
kill_port 8094
kill_port 5173

# Cleanup Docker containers
if [ -f "$PROJECT_ROOT/docker-compose-infra.yml" ]; then
    echo -e "${BLUE}Stopping existing Docker containers...${NC}"
    docker-compose -f "$PROJECT_ROOT/docker-compose-infra.yml" down > /dev/null 2>&1 || true
    # Force remove known containers to prevent conflicts
    docker rm -f danxils-postgres danxils-kafka danxils-zookeeper danxils-mailhog > /dev/null 2>&1 || true
fi


# Check Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}✗ Docker is not installed. Please install Docker first.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Docker found${NC}"

# Check Docker Compose
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}✗ Docker Compose is not installed. Please install Docker Compose first.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Docker Compose found${NC}"

# Check Java
if ! command -v java &> /dev/null; then
    echo -e "${RED}✗ Java is not installed. Please install Java 17 or higher.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Java found: $(java -version 2>&1 | head -n 1)${NC}"

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}✗ Maven is not installed. Please install Maven.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Maven found${NC}"

# Check Node.js
if ! command -v node &> /dev/null; then
    echo -e "${RED}✗ Node.js is not installed. Please install Node.js.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Node.js found: $(node --version)${NC}"

echo ""

###############################################################################
# Step 2: Start Infrastructure (Kafka, PostgreSQL)
###############################################################################

echo -e "${YELLOW}[2/7] Starting infrastructure (Kafka, PostgreSQL)...${NC}"

# Create TES directory if it doesn't exist
mkdir -p "$PROJECT_ROOT/TES"

# Generate init-db.sql for database creation
echo -e "${BLUE}Generating database initialization script...${NC}"
cat > "$PROJECT_ROOT/TES/init-db.sql" << 'EOF'
-- Create databases for DANXILS services
SELECT 'CREATE DATABASE vt_iam_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'vt_iam_service')\gexec
SELECT 'CREATE DATABASE vt_order_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'vt_order_service')\gexec
SELECT 'CREATE DATABASE vt_planning_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'vt_planning_service')\gexec
SELECT 'CREATE DATABASE vt_tracking_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'vt_tracking_service')\gexec
SELECT 'CREATE DATABASE vt_analytics_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'vt_analytics_service')\gexec
EOF

# Start infrastructure using docker-compose
echo -e "${BLUE}Starting Docker containers...${NC}"
docker-compose -f "$PROJECT_ROOT/docker-compose-infra.yml" up -d

echo -e "${BLUE}Waiting for database to be ready...${NC}"
sleep 10

# Force database initialization
echo -e "${BLUE}Initializing databases...${NC}"
docker exec -i danxils-postgres psql -U danxils -d danxils_db < "$PROJECT_ROOT/TES/init-db.sql" > /dev/null 2>&1 || true

# Function to start a Spring Boot service
start_service() {
    local service_name=$1
    local service_dir=$2
    local port=$3
    
    if [ -d "$service_dir" ]; then
        echo -e "${BLUE}Starting $service_name on port $port...${NC}"
        cd "$service_dir"
        
        # Always build to ensure latest changes are picked up
        echo -e "${BLUE}Building $service_name...${NC}"
        mvn clean package -DskipTests > /dev/null 2>&1
        
        # Find the jar file
        local jar_file=$(find target -name "*.jar" -not -name "*.original" | head -n 1)
        
        if [ -z "$jar_file" ]; then
             echo -e "${RED}✗ Could not find jar file for $service_name${NC}"
             return 1
        fi

        # Start in background
        nohup java -jar "$jar_file" --server.port=$port > "$PROJECT_ROOT/logs/${service_name}.log" 2>&1 &
        echo $! > "$PROJECT_ROOT/logs/${service_name}.pid"
        
        echo -e "${GREEN}✓ $service_name started (PID: $(cat $PROJECT_ROOT/logs/${service_name}.pid))${NC}"
    else
        echo -e "${YELLOW}⚠ $service_name directory not found at $service_dir${NC}"
    fi
}

# ... (create logs dir) ...

# Start IAM Service
start_service "iam-service" "$PROJECT_ROOT/iam-service" "8081"
sleep 5

# Start Order Service
start_service "order-service" "$PROJECT_ROOT/order-service" "8091"
sleep 5

# Start Planning Service
start_service "planning-service" "$PROJECT_ROOT/planning-service" "8092"
sleep 5

# Start Analytics Service
start_service "analytics-service" "$PROJECT_ROOT/analytics-service" "8093"
sleep 5

# Start Tracking Service
start_service "tracking-service" "$PROJECT_ROOT/tracking-service" "8094"
sleep 5

echo ""

###############################################################################
# Step 4: Wait for Services to be Ready
###############################################################################

echo -e "${YELLOW}[4/7] Waiting for services to be ready...${NC}"

# Function to check if service is ready
check_service() {
    local service_name=$1
    local url=$2
    local max_attempts=30
    local attempt=1
    
    echo -e "${BLUE}Checking $service_name...${NC}"
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$url" > /dev/null 2>&1; then
            echo -e "${GREEN}✓ $service_name is ready${NC}"
            return 0
        fi
        
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo -e "${RED}✗ $service_name failed to start${NC}"
    return 1
}

# Check services
check_service "IAM Service" "http://localhost:8081/actuator/health"
check_service "Order Service" "http://localhost:8091/actuator/health"
check_service "Planning Service" "http://localhost:8092/actuator/health"
check_service "Analytics Service" "http://localhost:8093/actuator/health"
check_service "Tracking Service" "http://localhost:8094/actuator/health"

echo ""

###############################################################################
# Step 5: Start Frontend Applications
###############################################################################

echo -e "${YELLOW}[5/7] Starting frontend applications...${NC}"

# Start Web Dashboard
if [ -d "$PROJECT_ROOT/danxils-dashboard-web" ]; then
    echo -e "${BLUE}Starting Web Dashboard...${NC}"
    cd "$PROJECT_ROOT/danxils-dashboard-web"
    
    # Install dependencies if needed
    if [ ! -d "node_modules" ]; then
        echo -e "${BLUE}Installing dependencies...${NC}"
        npm install > /dev/null 2>&1
    fi
    
    # Start in background
    nohup npm run dev > "$PROJECT_ROOT/logs/dashboard-web.log" 2>&1 &
    echo $! > "$PROJECT_ROOT/logs/dashboard-web.pid"
    
    echo -e "${GREEN}✓ Web Dashboard started (PID: $(cat $PROJECT_ROOT/logs/dashboard-web.pid))${NC}"
    sleep 3
else
    echo -e "${YELLOW}⚠ danxils-dashboard-web directory not found${NC}"
fi

echo ""

###############################################################################
# Step 6: Display Service URLs
###############################################################################

echo -e "${YELLOW}[6/7] Service URLs:${NC}"
echo ""
echo -e "${GREEN}Backend Services:${NC}"
echo -e "  • IAM Service:      ${BLUE}http://localhost:8081${NC}"
echo -e "  • Order Service:    ${BLUE}http://localhost:8091${NC}"
echo -e "  • Planning Service: ${BLUE}http://localhost:8092${NC}"
echo -e "  • Analytics Service:${BLUE}http://localhost:8093${NC}"
echo -e "  • Tracking Service: ${BLUE}http://localhost:8094${NC}"
echo ""
echo -e "${GREEN}API Documentation:${NC}"
echo -e "  • Order API Docs:   ${BLUE}http://localhost:8091/swagger-ui.html${NC}"
echo -e "  • Planning API Docs:${BLUE}http://localhost:8092/swagger-ui.html${NC}"
echo -e "  • Analytics API Docs:${BLUE}http://localhost:8093/swagger-ui.html${NC}"
echo -e "  • Tracking API Docs:${BLUE}http://localhost:8094/swagger-ui.html${NC}"
echo ""
echo -e "${GREEN}Frontend Applications:${NC}"
echo -e "  • Web Dashboard:    ${BLUE}http://localhost:5173${NC}"
echo -e "  • Customer Portal:  ${BLUE}http://localhost:5173/customer${NC}"
echo -e "  • Internal Portal:  ${BLUE}http://localhost:5173/internal${NC}"
echo ""
echo -e "${GREEN}Infrastructure:${NC}"
echo -e "  • Kafka:            ${BLUE}localhost:9092${NC}"
echo -e "  • PostgreSQL:       ${BLUE}localhost:5433${NC}"
echo ""

###############################################################################
# Step 7: Display Logs
###############################################################################

echo -e "${YELLOW}[7/7] Monitoring logs...${NC}"
echo ""
echo -e "${BLUE}Log files are available at: $PROJECT_ROOT/logs/${NC}"
echo -e "${BLUE}To view logs in real-time, use:${NC}"
echo -e "  tail -f $PROJECT_ROOT/logs/iam-service.log"
echo -e "  tail -f $PROJECT_ROOT/logs/order-service.log"
echo -e "  tail -f $PROJECT_ROOT/logs/planning-service.log"
echo -e "  tail -f $PROJECT_ROOT/logs/analytics-service.log"
echo -e "  tail -f $PROJECT_ROOT/logs/dashboard-web.log"
echo ""

###############################################################################
# Success Message
###############################################################################

echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║   ✓ DANXILS Enterprise System Started Successfully!        ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${BLUE}To stop all services, run:${NC} ./stop-danxils.sh"
echo -e "${BLUE}To view system status, run:${NC} ./status-danxils.sh"
echo ""
echo -e "${YELLOW}Press Ctrl+C to stop monitoring (services will continue running)${NC}"
echo ""

# Keep script running and show combined logs
tail -f "$PROJECT_ROOT/logs"/*.log 2>/dev/null || true