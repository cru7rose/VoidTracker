#!/bin/bash
echo "Waiting for iam_db tables..."
RETRIES=30
for i in $(seq 1 $RETRIES); do
    docker exec -i postgres psql -U postgres -d iam_db -c '\dt' | grep user_organization_access > /dev/null
    if [ $? -eq 0 ]; then
        echo "Tables found!"
        exit 0
    fi
    echo "Waiting for tables... ($i/$RETRIES)"
    sleep 5
done
echo "Timeout waiting for tables."
exit 1
