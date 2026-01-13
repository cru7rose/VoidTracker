#!/bin/bash

# Restart Services Script
# Restarts services after deployment
# Usage: ./scripts/restart-services.sh [service1,service2,...]

set -e

SERVICES="${1:-iam,order,planning}"
SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

echo "╔════════════════════════════════════════╗"
echo "║   RESTART SERVICES                      ║"
echo "╚════════════════════════════════════════╝"
echo ""
echo "📦 Services: $SERVICES"
echo ""

cd "$SCRIPT_DIR"

# Restart each service
IFS=',' read -ra SERVICE_ARRAY <<< "$SERVICES"
for service in "${SERVICE_ARRAY[@]}"; do
  service=$(echo "$service" | xargs) # trim whitespace
  
  case "$service" in
    iam)
      echo "🔄 Restarting IAM Service..."
      SKIP_BUILD=1 ./stop-iam.sh 2>/dev/null || true
      sleep 2
      SKIP_BUILD=1 ./start-iam.sh
      echo "✅ IAM Service restarted"
      ;;
      
    order)
      echo "🔄 Restarting Order Service..."
      SKIP_BUILD=1 ./stop-order.sh 2>/dev/null || true
      sleep 2
      SKIP_BUILD=1 ./start-order.sh
      echo "✅ Order Service restarted"
      ;;
      
    planning)
      echo "🔄 Restarting Planning Service..."
      SKIP_BUILD=1 ./stop-planning.sh 2>/dev/null || true
      sleep 2
      SKIP_BUILD=1 ./start-planning.sh
      echo "✅ Planning Service restarted"
      ;;
      
    *)
      echo "⚠️  Unknown service: $service (skipping)"
      ;;
  esac
  
  echo ""
done

echo "╔════════════════════════════════════════╗"
echo "║   RESTART COMPLETE ✅                   ║"
echo "╚════════════════════════════════════════╝"
echo ""
echo "📦 Services restarted: $SERVICES"
echo ""
echo "💡 Check service health:"
for service in "${SERVICE_ARRAY[@]}"; do
  service=$(echo "$service" | xargs)
  case "$service" in
    iam) PORT="8090" ;;
    order) PORT="8091" ;;
    planning) PORT="8093" ;;
    *) continue ;;
  esac
  echo "   curl http://localhost:$PORT/actuator/health"
done
