#!/bin/bash
# Build all services locally (on your machine, not on server)
# This avoids SSH connection issues during build

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Load configuration
CONFIG_FILE="$PROJECT_ROOT/deploy.conf"
if [ ! -f "$CONFIG_FILE" ]; then
    echo -e "${YELLOW}⚠️  deploy.conf not found. Using defaults.${NC}"
    echo -e "${CYAN}💡 Copy deploy.conf.example to deploy.conf and configure it.${NC}"
    BUILD_THREADS="${BUILD_PARALLEL_THREADS:-1C}"
    SKIP_TESTS="${BUILD_SKIP_TESTS:-1}"
else
    source "$CONFIG_FILE"
    BUILD_THREADS="${BUILD_PARALLEL_THREADS:-1C}"
    SKIP_TESTS="${BUILD_SKIP_TESTS:-1}"
fi

# Parse services to build
SERVICES_TO_BUILD=()
if [ "${DEPLOY_IAM_SERVICE:-1}" = "1" ]; then
    SERVICES_TO_BUILD+=("iam")
fi
if [ "${DEPLOY_ORDER_SERVICE:-1}" = "1" ]; then
    SERVICES_TO_BUILD+=("order")
fi
if [ "${DEPLOY_PLANNING_SERVICE:-1}" = "1" ]; then
    SERVICES_TO_BUILD+=("planning")
fi

if [ ${#SERVICES_TO_BUILD[@]} -eq 0 ]; then
    echo -e "${YELLOW}⚠️  No services selected for build.${NC}"
    exit 0
fi

echo "╔════════════════════════════════════════╗"
echo "║   LOCAL BUILD - VoidTracker Services    ║"
echo "╚════════════════════════════════════════╝"
echo ""
echo -e "${CYAN}📦 Services to build:${NC} ${SERVICES_TO_BUILD[*]}"
echo -e "${CYAN}🔧 Build threads:${NC} $BUILD_THREADS"
echo -e "${CYAN}🧪 Skip tests:${NC} $([ "$SKIP_TESTS" = "1" ] && echo "Yes" || echo "No")"
echo ""

cd "$PROJECT_ROOT"

# Build danxils-commons first (required dependency)
echo -e "${CYAN}📦 Building danxils-commons (required dependency)...${NC}"
cd modules/nexus/danxils-commons
mvn clean install -DskipTests -T "$BUILD_THREADS" -q
cd "$PROJECT_ROOT"
echo -e "${GREEN}✅ danxils-commons built${NC}"
echo ""

# Build services
for service in "${SERVICES_TO_BUILD[@]}"; do
    case $service in
        iam)
            SERVICE_DIR="modules/nexus/iam-service"
            JAR_NAME="iam-app-1.0.0-SNAPSHOT.jar"
            SERVICE_NAME="IAM Service"
            ;;
        order)
            SERVICE_DIR="modules/nexus/order-service"
            JAR_NAME="order-service-1.0.0-SNAPSHOT.jar"
            SERVICE_NAME="Order Service"
            ;;
        planning)
            SERVICE_DIR="modules/flux/planning-service"
            JAR_NAME="planning-service-1.0.0-SNAPSHOT.jar"
            SERVICE_NAME="Planning Service"
            ;;
        *)
            echo -e "${RED}❌ Unknown service: $service${NC}"
            continue
            ;;
    esac

    echo -e "${CYAN}📦 Building $SERVICE_NAME...${NC}"
    cd "$PROJECT_ROOT/$SERVICE_DIR"
    
    JAR_PATH="target/$JAR_NAME"
    
    # Check if JAR exists and is up to date
    if [ -f "$JAR_PATH" ] && [ -z "$(find src -type f -newer "$JAR_PATH" 2>/dev/null | head -1)" ]; then
        echo -e "${GREEN}✅ $SERVICE_NAME JAR is up to date${NC}"
    else
        echo -e "${YELLOW}🔨 Building $SERVICE_NAME...${NC}"
        if [ "$SKIP_TESTS" = "1" ]; then
            mvn clean package -DskipTests -T "$BUILD_THREADS" -q
        else
            mvn clean package -T "$BUILD_THREADS" -q
        fi
        
        if [ -f "$JAR_PATH" ]; then
            JAR_SIZE=$(du -h "$JAR_PATH" | cut -f1)
            echo -e "${GREEN}✅ $SERVICE_NAME built successfully ($JAR_SIZE)${NC}"
        else
            echo -e "${RED}❌ $SERVICE_NAME build failed - JAR not found${NC}"
            exit 1
        fi
    fi
    
    echo ""
done

cd "$PROJECT_ROOT"

echo "╔════════════════════════════════════════╗"
echo "║   BUILD COMPLETE ✅                     ║"
echo "╚════════════════════════════════════════╝"
echo ""
echo -e "${GREEN}✅ All services built successfully!${NC}"
echo ""
echo "📦 Built JARs:"
for service in "${SERVICES_TO_BUILD[@]}"; do
    case $service in
        iam)
            JAR_PATH="$PROJECT_ROOT/modules/nexus/iam-service/target/iam-app-1.0.0-SNAPSHOT.jar"
            ;;
        order)
            JAR_PATH="$PROJECT_ROOT/modules/nexus/order-service/target/order-service-1.0.0-SNAPSHOT.jar"
            ;;
        planning)
            JAR_PATH="$PROJECT_ROOT/modules/flux/planning-service/target/planning-service-1.0.0-SNAPSHOT.jar"
            ;;
    esac
    
    if [ -f "$JAR_PATH" ]; then
        JAR_SIZE=$(du -h "$JAR_PATH" | cut -f1)
        echo -e "   ${GREEN}✓${NC} $JAR_PATH ($JAR_SIZE)"
    fi
done
echo ""
echo "💡 Next step: Run ./scripts/deploy.sh to upload to server"
