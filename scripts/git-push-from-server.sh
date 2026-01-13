#!/bin/bash

# Git Push from Server Script
# Pushes local changes to GitHub
# Usage: ./scripts/git-push-from-server.sh [commit-message]

set -e

COMMIT_MSG="${1:-chore: Update from server}"
BRANCH="${GIT_BRANCH:-main}"

echo "╔════════════════════════════════════════╗"
echo "║   GIT PUSH FROM SERVER                 ║"
echo "╚════════════════════════════════════════╝"
echo ""

cd "$(dirname "$0")/.."

# Check if we're in a git repo
if [ ! -d .git ]; then
  echo "❌ Not a git repository"
  exit 1
fi

# Check for changes
if [ -z "$(git status --porcelain)" ]; then
  echo "✅ No changes to commit"
  exit 0
fi

# Show status
echo "📋 Changes to commit:"
git status --short | head -10
echo ""

# Add all changes
echo "📦 Staging changes..."
git add -A

# Commit
echo "💾 Committing: $COMMIT_MSG"
git commit -m "$COMMIT_MSG" || {
  echo "⚠️  Commit failed (maybe no changes?)"
  exit 1
}

# Get commit hash
COMMIT_HASH=$(git rev-parse HEAD | cut -c1-8)
echo "✅ Committed: $COMMIT_HASH"
echo ""

# Push to GitHub
echo "📤 Pushing to GitHub (origin/$BRANCH)..."
if git push origin "$BRANCH"; then
  echo ""
  echo "╔════════════════════════════════════════╗"
  echo "║   PUSH SUCCESSFUL ✅                   ║"
  echo "╚════════════════════════════════════════╝"
  echo ""
  echo "📍 Commit: $COMMIT_HASH"
  echo "🌿 Branch: $BRANCH"
  echo "🔗 GitHub: https://github.com/cru7rose/VoidTracker/commit/$COMMIT_HASH"
  echo ""
  echo "💡 GitHub Actions will now:"
  echo "   1. Build services for verification"
  echo "   2. Upload build logs"
  echo ""
  echo "📊 View Actions: https://github.com/cru7rose/VoidTracker/actions"
else
  echo ""
  echo "❌ Push failed"
  echo ""
  echo "💡 Possible issues:"
  echo "   - Authentication required (run ./scripts/setup-git-push.sh)"
  echo "   - Remote branch diverged (run git pull first)"
  echo "   - Network issues"
  exit 1
fi
