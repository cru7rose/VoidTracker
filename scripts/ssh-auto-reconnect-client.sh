#!/bin/bash
# Skrypt do automatycznego reconnect SSH z poziomu klienta
# Użyj tego na LOKALNYM komputerze, nie na serwerze!

SERVER_HOST="${1:-voidtracker}"
MAX_RETRIES=999999
RETRY_DELAY=5

echo "🔄 Automatyczne reconnect do $SERVER_HOST..."
echo "Naciśnij Ctrl+C aby zatrzymać"

RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    echo "[$(date '+%H:%M:%S')] Próba połączenia #$((RETRY_COUNT + 1))..."
    
    if ssh -o ConnectTimeout=10 -o ServerAliveInterval=15 -o ServerAliveCountMax=3 "$SERVER_HOST" "echo 'Połączenie OK'"; then
        echo "✅ Połączenie nawiązane! Uruchamianie sesji..."
        ssh "$SERVER_HOST"
        EXIT_CODE=$?
        
        if [ $EXIT_CODE -eq 0 ] || [ $EXIT_CODE -eq 130 ]; then
            # Normalne wyjście lub Ctrl+C
            echo "👋 Sesja zakończona"
            exit 0
        fi
    else
        echo "❌ Połączenie nieudane. Ponowienie za ${RETRY_DELAY}s..."
        sleep "$RETRY_DELAY"
    fi
    
    RETRY_COUNT=$((RETRY_COUNT + 1))
done

echo "❌ Osiągnięto maksymalną liczbę prób"
exit 1
