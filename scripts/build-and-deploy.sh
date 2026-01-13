#!/bin/bash
# Build locally and deploy to server in one command
# This is the main script you'll use for automated deployment

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "╔════════════════════════════════════════╗"
echo "║   BUILD & DEPLOY AUTOMATION              ║"
echo "╚════════════════════════════════════════╝"
echo ""

# Step 1: Build locally
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "STEP 1: Building services locally..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
"$SCRIPT_DIR/build-local.sh"

if [ $? -ne 0 ]; then
    echo "❌ Build failed! Aborting deployment."
    exit 1
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "STEP 2: Deploying to server..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
"$SCRIPT_DIR/deploy.sh"

if [ $? -ne 0 ]; then
    echo "❌ Deployment failed!"
    exit 1
fi

echo ""
echo "╔════════════════════════════════════════╗"
echo "║   BUILD & DEPLOY COMPLETE ✅            ║"
echo "╚════════════════════════════════════════╝"
echo ""
echo "🎉 All done! Services are built and deployed."
echo ""
echo "💡 To restart services on server:"
echo "   ssh user@server"
echo "   cd /root/VoidTracker"
echo "   SKIP_BUILD=1 ./start-iam.sh"
echo "   SKIP_BUILD=1 ./start-order.sh"
echo "   SKIP_BUILD=1 ./start-planning.sh"
