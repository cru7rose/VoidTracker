#!/bin/bash
# 🔄 Automatyczne przywracanie połączenia SSH bez restartu serwera
# Rozwiązuje problem z koniecznością restartowania serwera przy zrywaniu połączenia

set -e

echo "🔄 Konfiguracja automatycznego reconnect SSH..."

# 1. Napraw nazwę serwisu w skryptach
echo "🔧 Aktualizowanie skryptów (ssh zamiast sshd)..."

# Zaktualizuj improve_ssh_stability.sh
if [ -f "/root/VoidTracker/scripts/improve_ssh_stability.sh" ]; then
    sed -i 's/systemctl restart sshd/systemctl restart ssh/g' /root/VoidTracker/scripts/improve_ssh_stability.sh
    sed -i 's/systemctl reload sshd/systemctl reload ssh/g' /root/VoidTracker/scripts/improve_ssh_stability.sh
    sed -i 's/journalctl -u ssh/journalctl -u ssh/g' /root/VoidTracker/scripts/improve_ssh_stability.sh
fi

# 2. Utwórz systemd service do monitorowania i automatycznego reconnect SSH
echo "📋 Tworzenie systemd service dla SSH watchdog..."

SSH_WATCHDOG_SERVICE="/etc/systemd/system/ssh-watchdog.service"
cat > "$SSH_WATCHDOG_SERVICE" << 'EOF'
[Unit]
Description=SSH Connection Watchdog - Auto-reconnect bez restartu serwera
After=network.target ssh.service
Requires=ssh.service

[Service]
Type=simple
ExecStart=/usr/local/bin/ssh-watchdog.sh
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

# 3. Utwórz skrypt watchdog
WATCHDOG_SCRIPT="/usr/local/bin/ssh-watchdog.sh"
cat > "$WATCHDOG_SCRIPT" << 'EOF'
#!/bin/bash
# Watchdog dla SSH - monitoruje połączenia i automatycznie przywraca bez restartu

LOG_FILE="/var/log/ssh-watchdog.log"
SSH_SERVICE="ssh.service"
CHECK_INTERVAL=30

log_message() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

check_ssh_service() {
    if systemctl is-active --quiet "$SSH_SERVICE"; then
        return 0
    else
        return 1
    fi
}

check_ssh_connections() {
    # Sprawdź czy są aktywne połączenia SSH
    ACTIVE_CONNECTIONS=$(who | grep -c "pts/" || echo "0")
    
    # Sprawdź czy SSH nasłuchuje na porcie 22
    SSH_LISTENING=$(ss -tlnp | grep -c ":22 " || echo "0")
    
    if [ "$SSH_LISTENING" -eq "0" ]; then
        return 1
    fi
    
    return 0
}

restore_ssh_without_restart() {
    log_message "⚠️  Wykryto problem z SSH. Próba przywrócenia bez restartu..."
    
    # Metoda 1: Reload konfiguracji (nie restartuje połączeń)
    if systemctl reload "$SSH_SERVICE" 2>/dev/null; then
        log_message "✅ SSH zreloadowany (bez restartu połączeń)"
        sleep 5
        return 0
    fi
    
    # Metoda 2: Restart tylko jeśli reload nie działa
    if ! check_ssh_service; then
        log_message "⚠️  Reload nie zadziałał, restartowanie SSH..."
        systemctl restart "$SSH_SERVICE"
        sleep 5
    fi
    
    return 0
}

# Główna pętla
log_message "🚀 SSH Watchdog uruchomiony"

while true; do
    if ! check_ssh_service; then
        log_message "❌ SSH service nie działa!"
        restore_ssh_without_restart
    elif ! check_ssh_connections; then
        log_message "⚠️  SSH nie nasłuchuje na porcie 22"
        restore_ssh_without_restart
    else
        # Wszystko OK, loguj co 10 minut
        if [ $(($(date +%s) % 600)) -lt "$CHECK_INTERVAL" ]; then
            ACTIVE=$(who | grep -c "pts/" || echo "0")
            log_message "✅ SSH działa poprawnie. Aktywne połączenia: $ACTIVE"
        fi
    fi
    
    sleep "$CHECK_INTERVAL"
done
EOF

chmod +x "$WATCHDOG_SCRIPT"
echo "✅ Watchdog script utworzony"

# 4. Utwórz skrypt do automatycznego reconnect z poziomu klienta
CLIENT_RECONNECT_SCRIPT="/root/VoidTracker/scripts/ssh-auto-reconnect-client.sh"
cat > "$CLIENT_RECONNECT_SCRIPT" << 'EOF'
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
EOF

chmod +x "$CLIENT_RECONNECT_SCRIPT"
echo "✅ Client reconnect script utworzony"

# 5. Utwórz systemd timer dla okresowego sprawdzania SSH (opcjonalne)
echo "⏰ Tworzenie systemd timer..."

SSH_CHECK_TIMER="/etc/systemd/system/ssh-check.timer"
cat > "$SSH_CHECK_TIMER" << 'EOF'
[Unit]
Description=SSH Connection Check Timer
Requires=ssh-check.service

[Timer]
OnBootSec=1min
OnUnitActiveSec=5min
AccuracySec=1s

[Install]
WantedBy=timers.target
EOF

SSH_CHECK_SERVICE="/etc/systemd/system/ssh-check.service"
cat > "$SSH_CHECK_SERVICE" << 'EOF'
[Unit]
Description=SSH Connection Check
After=network.target

[Service]
Type=oneshot
ExecStart=/usr/local/bin/ssh-health-check.sh
EOF

# 6. Utwórz skrypt health check
HEALTH_CHECK_SCRIPT="/usr/local/bin/ssh-health-check.sh"
cat > "$HEALTH_CHECK_SCRIPT" << 'EOF'
#!/bin/bash
# Health check dla SSH - sprawdza czy wszystko działa

LOG_FILE="/var/log/ssh-health.log"

check_ssh() {
    # Sprawdź czy service działa
    if ! systemctl is-active --quiet ssh.service; then
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] ❌ SSH service nie działa!" >> "$LOG_FILE"
        systemctl restart ssh.service
        return 1
    fi
    
    # Sprawdź czy nasłuchuje na porcie 22
    if ! ss -tlnp | grep -q ":22 "; then
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] ⚠️  SSH nie nasłuchuje na porcie 22" >> "$LOG_FILE"
        systemctl reload ssh.service
        return 1
    fi
    
    # Sprawdź konfigurację
    if ! sshd -t 2>/dev/null; then
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] ❌ Błąd w konfiguracji SSH!" >> "$LOG_FILE"
        return 1
    fi
    
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ✅ SSH działa poprawnie" >> "$LOG_FILE"
    return 0
}

check_ssh
EOF

chmod +x "$HEALTH_CHECK_SCRIPT"
echo "✅ Health check script utworzony"

# 7. Włącz serwisy
echo "🚀 Włączanie serwisów..."

systemctl daemon-reload

# Włącz watchdog (opcjonalne - może być zbyt agresywny)
read -p "Czy włączyć SSH watchdog? (może być zbyt agresywny) [y/N]: " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    systemctl enable ssh-watchdog.service
    systemctl start ssh-watchdog.service
    echo "✅ SSH watchdog włączony"
else
    echo "ℹ️  SSH watchdog pominięty (możesz włączyć później: systemctl enable --now ssh-watchdog.service)"
fi

# Włącz health check timer
systemctl enable ssh-check.timer
systemctl start ssh-check.timer
echo "✅ SSH health check timer włączony"

# 8. Utwórz dokumentację
cat > /root/SSH_AUTO_RECONNECT.md << 'EOF'
# 🔄 Automatyczne Przywracanie Połączenia SSH

## Problem
Za każdym razem jak zerwie się połączenie, musisz restartować serwer.

## Rozwiązanie

### Na Serwerze (Już Skonfigurowane)

1. **SSH Health Check Timer** - sprawdza co 5 minut czy SSH działa
   ```bash
   systemctl status ssh-check.timer
   ```

2. **SSH Watchdog** (opcjonalne) - ciągłe monitorowanie
   ```bash
   systemctl enable --now ssh-watchdog.service
   ```

### Na Lokalnym Komputerze (WAŻNE!)

**Opcja 1: Użyj skryptu auto-reconnect**
```bash
# Skopiuj skrypt na lokalny komputer
scp root@SERVER:/root/VoidTracker/scripts/ssh-auto-reconnect-client.sh ~/

# Uruchom
chmod +x ~/ssh-auto-reconnect-client.sh
~/ssh-auto-reconnect-client.sh voidtracker
```

**Opcja 2: Użyj autossh (najlepsze rozwiązanie)**
```bash
# Zainstaluj autossh
# macOS: brew install autossh
# Linux: sudo apt-get install autossh

# Użyj zamiast ssh
autossh -M 20000 -f -N -R 9094:localhost:9094 voidtracker
```

**Opcja 3: Konfiguracja w ~/.ssh/config (NAJLEPSZE)**
```ini
Host voidtracker
    HostName TWOJ_IP
    User root
    IdentityFile ~/.ssh/voidtracker_ed25519
    
    # BARDZO AGRESYWNE Keep-Alive
    ServerAliveInterval 10
    ServerAliveCountMax 20
    TCPKeepAlive yes
    
    # Automatyczne reconnect
    ControlMaster auto
    ControlPath ~/.ssh/control-%h-%p-%r
    ControlPersist 2h
    ControlAutoReconnect yes
    
    # Retry
    ConnectionAttempts 10
    ConnectTimeout 30
    
    # Ważne!
    ExitOnForwardFailure no
    ServerAliveCountMax 20
```

### W Cursor/VS Code

1. Otwórz Settings (Cmd+, / Ctrl+,)
2. Szukaj: "remote.SSH.connectTimeout"
3. Ustaw na: `60`
4. Szukaj: "remote.SSH.serverAliveInterval"
5. Ustaw na: `10`
6. Szukaj: "remote.SSH.serverAliveCountMax"
7. Ustaw na: `20`

## Monitorowanie

```bash
# Sprawdź health check
systemctl status ssh-check.timer
journalctl -u ssh-check.service -f

# Sprawdź watchdog (jeśli włączony)
systemctl status ssh-watchdog.service
tail -f /var/log/ssh-watchdog.log

# Sprawdź health log
tail -f /var/log/ssh-health.log
```

## Rozwiązywanie Problemów

### Problem: Nadal trzeba restartować serwer

**Rozwiązanie:**
1. Sprawdź czy health check działa:
   ```bash
   systemctl status ssh-check.timer
   ```

2. Sprawdź logi:
   ```bash
   journalctl -u ssh-check.service -n 50
   ```

3. Włącz watchdog (bardziej agresywny):
   ```bash
   systemctl enable --now ssh-watchdog.service
   ```

### Problem: Połączenie nadal się zrywa

**Rozwiązanie:**
1. Zaktualizuj `~/.ssh/config` z ustawieniami powyżej
2. Zainstaluj i użyj `autossh` zamiast `ssh`
3. Zwiększ `ServerAliveInterval` do 5 sekund

## Najlepsze Praktyki

1. **Użyj autossh** - automatycznie reconnectuje
2. **Zwiększ Keep-Alive** - częstsze pingi
3. **Użyj ControlMaster** - utrzymuje połączenie otwarte
4. **Monitoruj logi** - sprawdzaj co się dzieje
EOF

echo ""
echo "✅ Konfiguracja zakończona!"
echo ""
echo "📋 Następne kroki:"
echo "   1. Na SERWERZE: Sprawdź status:"
echo "      systemctl status ssh-check.timer"
echo ""
echo "   2. Na LOKALNYM KOMPUTERZE:"
echo "      - Zaktualizuj ~/.ssh/config (patrz: /root/SSH_AUTO_RECONNECT.md)"
echo "      - LUB użyj skryptu: ~/ssh-auto-reconnect-client.sh"
echo "      - LUB zainstaluj autossh"
echo ""
echo "   3. W CURSOR/VS CODE:"
echo "      - Zwiększ remote.SSH.serverAliveInterval do 10"
echo "      - Zwiększ remote.SSH.serverAliveCountMax do 20"
echo ""
echo "📖 Pełna dokumentacja: /root/SSH_AUTO_RECONNECT.md"
echo ""
