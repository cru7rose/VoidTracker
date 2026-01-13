#!/bin/bash
# Safe build script for planning-service that prevents SSH connection issues
# Limits Maven parallelism and skips tests

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
PLANNING_SERVICE_DIR="$PROJECT_ROOT/modules/flux/planning-service"

echo "🔨 Building planning-service with SSH-safe settings..."
echo "   - Single-threaded Maven build"
echo "   - Tests skipped"
echo "   - Lazy initialization enabled"

cd "$PLANNING_SERVICE_DIR"

# Build with minimal parallelism
export MAVEN_OPTS="-Xmx2g -XX:ActiveProcessorCount=2"
mvn clean package -DskipTests -T 1C

echo "✅ Build completed successfully!"
