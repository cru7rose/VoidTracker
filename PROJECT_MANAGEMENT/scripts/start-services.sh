#!/bin/bash
# DANXILS Microservices Startup Script

echo "=== DANXILS Microservices Startup ==="
echo ""

# Kill any existing Java processes on ports 8080-8099
echo "Checking for existing services..."
for port in 8080 8090 8091 8092 8094 8098; do
    PID=$(lsof -ti :$port)
    if [ ! -z "$PID" ]; then
        echo "Killing process $PID on port $port"
        kill -9 $PID 2>/dev/null
    fi
done

sleep 2
echo ""
echo "Starting services..."
echo ""

# Start each service in the background
echo "Starting order-service on port 8091..."
cd order-service && mvn spring-boot:run > ../logs/order-service.log 2>&1 &
ORDER_PID=$!


sleep 3

echo "Starting iam-service on port 8090..."
cd iam-service && mvn spring-boot:run > ../logs/iam-service.log 2>&1 &
IAM_PID=$!


sleep 3

echo "Starting planning-service on port 8092..."
cd planning-service && mvn spring-boot:run > ../logs/planning-service.log 2>&1 &
PLANNING_PID=$!


sleep 3

echo "Starting tracking-service on port 8094..."
cd tracking-service && mvn spring-boot:run > ../logs/tracking-service.log 2>&1 &
TRACKING_PID=$!


sleep 3

echo "Starting dashboard-service on port 8098..."
cd dashboard-service && mvn spring-boot:run > ../logs/dashboard-service.log 2>&1 &
DASHBOARD_PID=$!


echo ""
echo "=== Services Started ==="
echo "order-service (PID: $ORDER_PID) - http://localhost:8091"
echo "iam-service (PID: $IAM_PID) - http://localhost:8090"
echo "planning-service (PID: $PLANNING_PID) - http://localhost:8092"
echo "tracking-service (PID: $TRACKING_PID) - http://localhost:8094"
echo "dashboard-service (PID: $DASHBOARD_PID) - http://localhost:8098"
echo ""
echo "Logs are in ./logs/ directory"
echo ""
echo "To stop all services, run: pkill -f 'spring-boot:run'"
