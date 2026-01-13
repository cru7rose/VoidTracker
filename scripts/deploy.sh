#!/bin/bash
# Deploy built JARs to remote server via SCP
# Requires deploy.conf configuration file

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
    echo -e "${RED}❌ deploy.conf not found!${NC}"
    echo -e "${CYAN}💡 Copy deploy.conf.example to deploy.conf and configure it:${NC}"
    echo "   cp deploy.conf.example deploy.conf"
    echo "   # Edit deploy.conf with your server details"
    exit 1
fi

source "$CONFIG_FILE"

# Validate configuration
if [ -z "$DEPLOY_SERVER_HOST" ] || [ "$DEPLOY_SERVER_HOST" = "your-server.com" ]; then
    echo -e "${RED}❌ DEPLOY_SERVER_HOST not configured in deploy.conf${NC}"
    exit 1
fi

if [ -z "$DEPLOY_SERVER_USER" ]; then
    echo -e "${RED}❌ DEPLOY_SERVER_USER not configured in deploy.conf${NC}"
    exit 1
fi

# Build SSH command
SSH_OPTS="-p ${DEPLOY_SERVER_PORT:-22}"
if [ -n "$DEPLOY_SSH_KEY" ] && [ -f "${DEPLOY_SSH_KEY/#\~/$HOME}" ]; then
    SSH_OPTS="$SSH_OPTS -i ${DEPLOY_SSH_KEY/#\~/$HOME}"
fi

SSH_CMD="ssh $SSH_OPTS ${DEPLOY_SERVER_USER}@${DEPLOY_SERVER_HOST}"
SCP_CMD="scp $SSH_OPTS"

# Remote paths
REMOTE_BASE="${DEPLOY_REMOTE_BASE_DIR:-/root/VoidTracker}"
REMOTE_JAR_DIR="${DEPLOY_REMOTE_JAR_DIR:-$REMOTE_BASE/modules}"

echo "╔════════════════════════════════════════╗"
echo "║   DEPLOY TO SERVER                      ║"
echo "╚════════════════════════════════════════╝"
echo ""
echo -e "${CYAN}🌐 Server:${NC} ${DEPLOY_SERVER_USER}@${DEPLOY_SERVER_HOST}:${DEPLOY_SERVER_PORT:-22}"
echo -e "${CYAN}📁 Remote base:${NC} $REMOTE_BASE"
echo ""

# Test SSH connection
echo -e "${CYAN}🔌 Testing SSH connection...${NC}"
if ! $SSH_CMD "echo 'Connection OK'" > /dev/null 2>&1; then
    echo -e "${RED}❌ Cannot connect to server!${NC}"
    echo -e "${YELLOW}💡 Check:${NC}"
    echo "   - Server is reachable"
    echo "   - SSH key is configured correctly"
    echo "   - User has access to $REMOTE_BASE"
    exit 1
fi
echo -e "${GREEN}✅ SSH connection OK${NC}"
echo ""

# Create backup directory on remote if needed
if [ "${DEPLOY_BACKUP_OLD_JARS:-1}" = "1" ]; then
    BACKUP_DIR="$REMOTE_BASE/.deploy-backups/$(date +%Y%m%d_%H%M%S)"
    echo -e "${CYAN}📦 Creating backup directory...${NC}"
    $SSH_CMD "mkdir -p $BACKUP_DIR" || true
    echo -e "${GREEN}✅ Backup directory: $BACKUP_DIR${NC}"
    echo ""
fi

# Deploy services
DEPLOYED=0
FAILED=0

deploy_service() {
    local service=$1
    local local_jar=$2
    local remote_dir=$3
    local jar_name=$(basename "$local_jar")
    
    if [ ! -f "$local_jar" ]; then
        echo -e "${RED}❌ JAR not found: $local_jar${NC}"
        echo -e "${YELLOW}💡 Run ./scripts/build-local.sh first${NC}"
        return 1
    fi
    
    echo -e "${CYAN}📤 Deploying $service...${NC}"
    echo -e "   From: $local_jar"
    echo -e "   To:   ${DEPLOY_SERVER_USER}@${DEPLOY_SERVER_HOST}:$remote_dir/target/"
    
    # Create remote directory
    $SSH_CMD "mkdir -p $remote_dir/target" || true
    
    # Backup old JAR if exists
    if [ "${DEPLOY_BACKUP_OLD_JARS:-1}" = "1" ]; then
        $SSH_CMD "if [ -f '$remote_dir/target/$jar_name' ]; then cp '$remote_dir/target/$jar_name' '$BACKUP_DIR/$jar_name' 2>/dev/null || true; fi" || true
    fi
    
    # Copy JAR
    if $SCP_CMD "$local_jar" "${DEPLOY_SERVER_USER}@${DEPLOY_SERVER_HOST}:$remote_dir/target/$jar_name"; then
        JAR_SIZE=$(du -h "$local_jar" | cut -f1)
        echo -e "${GREEN}✅ $service deployed ($JAR_SIZE)${NC}"
        ((DEPLOYED++))
        return 0
    else
        echo -e "${RED}❌ $service deployment failed${NC}"
        ((FAILED++))
        return 1
    fi
}

# Deploy IAM Service
if [ "${DEPLOY_IAM_SERVICE:-1}" = "1" ]; then
    LOCAL_JAR="$PROJECT_ROOT/modules/nexus/iam-service/target/iam-app-1.0.0-SNAPSHOT.jar"
    REMOTE_DIR="$REMOTE_JAR_DIR/nexus/iam-service"
    deploy_service "IAM Service" "$LOCAL_JAR" "$REMOTE_DIR"
    echo ""
fi

# Deploy Order Service
if [ "${DEPLOY_ORDER_SERVICE:-1}" = "1" ]; then
    LOCAL_JAR="$PROJECT_ROOT/modules/nexus/order-service/target/order-service-1.0.0-SNAPSHOT.jar"
    REMOTE_DIR="$REMOTE_JAR_DIR/nexus/order-service"
    deploy_service "Order Service" "$LOCAL_JAR" "$REMOTE_DIR"
    echo ""
fi

# Deploy Planning Service
if [ "${DEPLOY_PLANNING_SERVICE:-1}" = "1" ]; then
    LOCAL_JAR="$PROJECT_ROOT/modules/flux/planning-service/target/planning-service-1.0.0-SNAPSHOT.jar"
    REMOTE_DIR="$REMOTE_JAR_DIR/flux/planning-service"
    deploy_service "Planning Service" "$LOCAL_JAR" "$REMOTE_DIR"
    echo ""
fi

# Summary
echo "╔════════════════════════════════════════╗"
echo "║   DEPLOY SUMMARY                         ║"
echo "╚════════════════════════════════════════╝"
echo ""
if [ $DEPLOYED -gt 0 ]; then
    echo -e "${GREEN}✅ Deployed: $DEPLOYED service(s)${NC}"
fi
if [ $FAILED -gt 0 ]; then
    echo -e "${RED}❌ Failed: $FAILED service(s)${NC}"
    exit 1
fi

if [ $DEPLOYED -eq 0 ]; then
    echo -e "${YELLOW}⚠️  No services deployed (all disabled in deploy.conf?)${NC}"
    exit 0
fi

echo ""
echo -e "${GREEN}✅ Deployment complete!${NC}"
echo ""
echo "💡 Next steps:"
echo "   1. SSH to server: ssh ${DEPLOY_SERVER_USER}@${DEPLOY_SERVER_HOST}"
echo "   2. Restart services:"
if [ "${DEPLOY_IAM_SERVICE:-1}" = "1" ]; then
    echo "      cd $REMOTE_BASE && ./stop-iam.sh && ./start-iam.sh"
fi
if [ "${DEPLOY_ORDER_SERVICE:-1}" = "1" ]; then
    echo "      cd $REMOTE_BASE && ./stop-order.sh && SKIP_BUILD=1 ./start-order.sh"
fi
if [ "${DEPLOY_PLANNING_SERVICE:-1}" = "1" ]; then
    echo "      cd $REMOTE_BASE && ./stop-planning.sh && SKIP_BUILD=1 ./start-planning.sh"
fi
