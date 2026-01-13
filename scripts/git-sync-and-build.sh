#!/bin/bash

# Git Sync and Build Script
# Pulls changes from GitHub and rebuilds services on server
# Usage: ./scripts/git-sync-and-build.sh [service1,service2,...]

set -e

REMOTE_BRANCH="${GIT_BRANCH:-main}"
SERVICES="${1:-iam,order,planning}"
SKIP_BUILD="${SKIP_BUILD:-0}"

echo "╔════════════════════════════════════════╗"
echo "║   GIT SYNC & BUILD                     ║"
echo "╚════════════════════════════════════════╝"
echo ""
echo "📦 Services: $SERVICES"
echo "🌿 Branch: $REMOTE_BRANCH"
echo ""

# Change to project root
cd "$(dirname "$0")/.."

# Check if we're in a git repo
if [ ! -d .git ]; then
  echo "❌ Not a git repository"
  exit 1
fi

# Get current commit
CURRENT_COMMIT=$(git rev-parse HEAD 2>/dev/null || echo "none")
echo "📍 Current commit: ${CURRENT_COMMIT:0:8}"

# Fetch latest changes
echo ""
echo "📥 Fetching from GitHub..."
git fetch origin "$REMOTE_BRANCH" || {
  echo "⚠️  Failed to fetch. Continuing with local build..."
}

# Check if there are changes
REMOTE_COMMIT=$(git rev-parse "origin/$REMOTE_BRANCH" 2>/dev/null || echo "$CURRENT_COMMIT")

if [ "$CURRENT_COMMIT" = "$REMOTE_COMMIT" ] && [ "$CURRENT_COMMIT" != "none" ]; then
  echo "✅ Already up to date (${CURRENT_COMMIT:0:8})"
  if [ "$SKIP_BUILD" = "1" ]; then
    echo "⏭️  Skipping build (SKIP_BUILD=1)"
    exit 0
  fi
else
  echo "🔄 New changes detected: ${REMOTE_COMMIT:0:8}"
  echo ""
  echo "📥 Pulling changes..."
  git pull origin "$REMOTE_BRANCH" || {
    echo "⚠️  Pull failed. Check for conflicts or local changes."
    echo "💡 To see local changes: git status"
    exit 1
  }
  echo "✅ Pulled successfully"
fi

# Show what changed
echo ""
echo "📋 Recent changes:"
git log --oneline -5 "$CURRENT_COMMIT..HEAD" 2>/dev/null || git log --oneline -5 -1

# Build services if not skipped
if [ "$SKIP_BUILD" != "1" ]; then
  echo ""
  echo "🔨 Building services..."
  echo ""
  
  # Build danxils-commons first (dependency)
  if [ -d "modules/nexus/danxils-commons" ]; then
    echo "📦 Building danxils-commons..."
    cd modules/nexus/danxils-commons
    mvn clean install -DskipTests -T 1C || {
      echo "❌ Failed to build danxils-commons"
      exit 1
    }
    cd - > /dev/null
    echo "✅ danxils-commons built"
  fi
  
  # Build requested services
  IFS=',' read -ra SERVICE_ARRAY <<< "$SERVICES"
  for service in "${SERVICE_ARRAY[@]}"; do
    service=$(echo "$service" | xargs) # trim whitespace
    
    case "$service" in
      iam)
        echo ""
        echo "🔨 Building IAM Service..."
        cd modules/nexus/iam-service
        mvn clean package -DskipTests -T 1C || {
          echo "❌ Failed to build IAM Service"
          exit 1
        }
        cd - > /dev/null
        echo "✅ IAM Service built"
        ;;
        
      order)
        echo ""
        echo "🔨 Building Order Service..."
        cd modules/nexus/order-service
        mvn clean package -DskipTests -T 1C || {
          echo "❌ Failed to build Order Service"
          exit 1
        }
        cd - > /dev/null
        echo "✅ Order Service built"
        ;;
        
      planning)
        echo ""
        echo "🔨 Building Planning Service..."
        cd modules/flux/planning-service
        mvn clean package -DskipTests -T 1C || {
          echo "❌ Failed to build Planning Service"
          exit 1
        }
        cd - > /dev/null
        echo "✅ Planning Service built"
        ;;
        
      *)
        echo "⚠️  Unknown service: $service (skipping)"
        ;;
    esac
  done
  
  echo ""
  echo "✅ All services built successfully"
else
  echo "⏭️  Build skipped (SKIP_BUILD=1)"
fi

echo ""
echo "╔════════════════════════════════════════╗"
echo "║   SYNC & BUILD COMPLETE ✅              ║"
echo "╚════════════════════════════════════════╝"
echo ""
echo "📍 Current commit: $(git rev-parse HEAD | cut -c1-8)"
echo "📦 Services ready: $SERVICES"
echo ""
echo "💡 To restart services:"
for service in "${SERVICE_ARRAY[@]}"; do
  service=$(echo "$service" | xargs)
  echo "   SKIP_BUILD=1 ./stop-${service}.sh && SKIP_BUILD=1 ./start-${service}.sh"
done
