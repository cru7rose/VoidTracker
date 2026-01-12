#!/bin/bash

# Wait for Kafka Connect to be ready
echo "Waiting for Kafka Connect to be ready..."
while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' http://localhost:8084/)" != "200" ]]; do
    sleep 5
done

echo "Kafka Connect is ready!"

echo "Registering SQL Server Source..."
curl -X POST -H "Content-Type: application/json" --data @connectors/sqlserver-source.json http://localhost:8084/connectors

echo -e "\n\nRegistering Postgres Sink..."
curl -X POST -H "Content-Type: application/json" --data @connectors/postgres-sink.json http://localhost:8084/connectors

echo -e "\n\nChecking status..."
curl -s http://localhost:8084/connectors?expand=status | python3 -m json.tool
