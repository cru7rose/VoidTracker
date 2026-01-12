#!/bin/bash

echo "🧹 Cleaning up corrupted Docker images..."

# List of images identified as corrupt
IMAGES=(
    "confluentinc/cp-kafka:7.5.0"
    "confluentinc/cp-kafka:7.6.0"
    "postgis/postgis:15-3.3-alpine"
    "postgis/postgis:16-3.3-alpine"
    "neo4j:5.15.0"
)

for img in "${IMAGES[@]}"; do
    if docker image inspect "$img" >/dev/null 2>&1; then
        echo "Removing $img..."
        docker rmi -f "$img"
    else
        echo "$img not found, skipping removal."
    fi
done

echo "⬇️  Pulling fresh images..."
docker pull confluentinc/cp-kafka:7.6.0
docker pull postgis/postgis:15-3.3-alpine
docker pull neo4j:5.17.0

echo "✅ Docker cleanup complete. You can now run ./start-sup.sh"
