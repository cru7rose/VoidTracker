#!/bin/bash
# Script to fix Planning Service database schema issues

set -e

echo "🔧 Fixing Planning Service database schema..."

# Database connection details
DB_HOST="${POSTGRES_HOST:-localhost}"
DB_PORT="${POSTGRES_PORT:-5434}"
DB_NAME="${PLANNING_DB_NAME:-voidtracker_planning}"
DB_USER="${POSTGRES_USER:-postgres}"
DB_PASSWORD="${POSTGRES_PASSWORD:-password}"

# Check if psql is available
if command -v psql &> /dev/null; then
    echo "📊 Running schema fix using psql..."
    export PGPASSWORD="$DB_PASSWORD"
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f fix_planning_schema.sql
    unset PGPASSWORD
elif command -v docker &> /dev/null; then
    echo "🐳 Running schema fix using Docker..."
    # Try to find postgres container
    POSTGRES_CONTAINER=$(docker ps --format "{{.Names}}" | grep -i postgres | head -1)
    if [ -z "$POSTGRES_CONTAINER" ]; then
        echo "❌ No PostgreSQL container found. Please run the SQL script manually:"
        echo "   psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f fix_planning_schema.sql"
        exit 1
    fi
    echo "Using container: $POSTGRES_CONTAINER"
    docker exec -i "$POSTGRES_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" < fix_planning_schema.sql
else
    echo "❌ Neither psql nor docker found. Please run the SQL script manually:"
    echo "   psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f fix_planning_schema.sql"
    echo ""
    echo "Or connect to your database and run the contents of fix_planning_schema.sql"
    exit 1
fi

echo "✅ Schema fix completed!"
echo "🚀 You can now restart the planning service"
