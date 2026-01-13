#!/bin/bash

# Webhook endpoint for GitHub Actions
# Triggers pull and build on server after successful GitHub Actions build
# Usage: curl -X POST http://server:8080/webhook/pull-build?services=iam,order,planning

set -e

# Get services from query param or default
SERVICES="${1:-iam,order,planning}"
LOG_FILE="/root/VoidTracker/logs/webhook-pull-build.log"

# Ensure log directory exists
mkdir -p "$(dirname "$LOG_FILE")"

# Log function
log() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*" | tee -a "$LOG_FILE"
}

log "╔════════════════════════════════════════╗"
log "║   WEBHOOK: PULL & BUILD                ║"
log "╚════════════════════════════════════════╝"
log ""
log "📦 Services: $SERVICES"
log ""

# Change to project root
cd /root/VoidTracker

# Run git sync and build
log "🔄 Running git-sync-and-build..."
/root/VoidTracker/scripts/git-sync-and-build.sh "$SERVICES" 2>&1 | tee -a "$LOG_FILE"

EXIT_CODE=${PIPESTATUS[0]}

if [ $EXIT_CODE -eq 0 ]; then
  log ""
  log "╔════════════════════════════════════════╗"
  log "║   WEBHOOK SUCCESS ✅                   ║"
  log "╚════════════════════════════════════════╝"
  exit 0
else
  log ""
  log "╔════════════════════════════════════════╗"
  log "║   WEBHOOK FAILED ❌                    ║"
  log "╚════════════════════════════════════════╝"
  exit 1
fi
