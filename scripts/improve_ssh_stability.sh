#!/bin/bash
# 🔧 Skrypt poprawy stabilności połączeń SSH
# Rozwiązuje problemy z zrywaniem połączenia

set -e

SSH_CONFIG="/etc/ssh/sshd_config"
SSH_CONFIG_BACKUP="/etc/ssh/sshd_config.backup.$(date +%Y%m%d_%H%M%S)"

echo "🔧 Konfiguracja SSH dla maksymalnej stabilności..."

# 1. Utwórz backup konfiguracji
if [ -f "$SSH_CONFIG" ]; then
    cp "$SSH_CONFIG" "$SSH_CONFIG_BACKUP"
    echo "✅ Backup utworzony: $SSH_CONFIG_BACKUP"
fi

# 2. Sprawdź czy konfiguracja już istnieje
if grep -q "# VoidTracker SSH Stability Config" "$SSH_CONFIG" 2>/dev/null; then
    echo "⚠️  Konfiguracja już istnieje. Aktualizowanie..."
    # Usuń starą konfigurację
    sed -i '/# VoidTracker SSH Stability Config/,/# End VoidTracker SSH Stability Config/d' "$SSH_CONFIG"
fi

# 3. Dodaj optymalne ustawienia dla stabilności
cat >> "$SSH_CONFIG" << 'EOF'

# VoidTracker SSH Stability Config
# Optymalne ustawienia dla stabilności połączenia i bezpieczeństwa
# Specjalnie zoptymalizowane dla Remote-SSH/Cursor

# Keep-Alive - BARDZO AGRESYWNE ustawienia dla Remote-SSH
ClientAliveInterval 30
ClientAliveCountMax 10
TCPKeepAlive yes

# Timeout ustawienia - zwiększone dla Remote-SSH
LoginGraceTime 300
MaxStartups 50:30:200

# Kompresja (może pomóc przy wolnych połączeniach)
Compression yes

# Kontrola przepustowości - ZWIĘKSZONE limity dla Remote-SSH
MaxSessions 50
MaxAuthTries 6

# Ustawienia dla Remote-SSH (VS Code/Cursor) - PORT FORWARDING
AllowTcpForwarding yes
GatewayPorts no
X11Forwarding no
PermitTunnel yes

# Zwiększone limity dla port forwarding (ważne dla Remote-SSH)
MaxOpenFiles 65536

# Ustawienia dla stabilności tunelowania
StreamLocalBindUnlink yes

# Zabezpieczenia
PermitRootLogin prohibit-password
PubkeyAuthentication yes
PasswordAuthentication no
PermitEmptyPasswords no

# Ochrona przed brute-force
MaxAuthTries 6
LoginGraceTime 300

# Logowanie dla debugowania (można zmienić na DEBUG dla szczegółowych logów)
LogLevel INFO

# Dodatkowe ustawienia dla stabilności
UseDNS no
GSSAPIAuthentication no
# End VoidTracker SSH Stability Config
EOF

echo "✅ Konfiguracja SSH zaktualizowana"

# 4. Utwórz/aktualizuj konfigurację klienta SSH
CLIENT_CONFIG="$HOME/.ssh/config"
mkdir -p "$HOME/.ssh"
chmod 700 "$HOME/.ssh"

if [ ! -f "$CLIENT_CONFIG" ]; then
    touch "$CLIENT_CONFIG"
    chmod 600 "$CLIENT_CONFIG"
fi

# Dodaj/aktualizuj konfigurację dla VoidTracker
if grep -q "Host voidtracker" "$CLIENT_CONFIG" 2>/dev/null; then
    echo "⚠️  Konfiguracja już istnieje. Aktualizowanie..."
    # Usuń starą konfigurację
    sed -i '/# VoidTracker Server/,/^$/d' "$CLIENT_CONFIG"
fi

cat >> "$CLIENT_CONFIG" << 'EOF'

# VoidTracker Server - Stabilna konfiguracja dla Remote-SSH/Cursor
Host voidtracker
    HostName YOUR_SERVER_IP_HERE
    User root
    IdentityFile ~/.ssh/voidtracker_ed25519
    
    # BARDZO AGRESYWNE Keep-Alive dla Remote-SSH
    ServerAliveInterval 15
    ServerAliveCountMax 10
    TCPKeepAlive yes
    
    # Timeouty
    ConnectTimeout 60
    ConnectionAttempts 5
    
    # Kompresja i optymalizacja
    Compression yes
    CompressionLevel 6
    
    # Automatyczne ponowne połączenie - ZWIĘKSZONE
    ControlMaster auto
    ControlPath ~/.ssh/control-%h-%p-%r
    ControlPersist 1h
    ControlAutoReconnect yes
    
    # Ustawienia dla port forwarding (ważne dla Remote-SSH)
    ForwardAgent no
    ForwardX11 no
    
    # Retry i reconnect
    ReconnectLimit 10
    
    # Optymalizacja dla Remote-SSH
    ExitOnForwardFailure no
    StrictHostKeyChecking accept-new
    UserKnownHostsFile ~/.ssh/known_hosts
EOF
    echo "✅ Konfiguracja klienta SSH utworzona/zaktualizowana w $CLIENT_CONFIG"
    echo "⚠️  Pamiętaj zmienić YOUR_SERVER_IP_HERE na prawdziwy IP!"

# 5. Sprawdź i popraw uprawnienia kluczy SSH
echo "🔐 Sprawdzanie uprawnień kluczy SSH..."
if [ -d "$HOME/.ssh" ]; then
    chmod 700 "$HOME/.ssh"
    find "$HOME/.ssh" -type f -name "*.pub" -exec chmod 644 {} \;
    find "$HOME/.ssh" -type f ! -name "*.pub" -exec chmod 600 {} \;
    echo "✅ Uprawnienia poprawione"
fi

# 6. Test konfiguracji SSH
echo "🧪 Testowanie konfiguracji SSH..."
if sshd -t; then
    echo "✅ Konfiguracja SSH jest poprawna"
else
    echo "❌ Błąd w konfiguracji SSH! Przywracanie backupu..."
    cp "$SSH_CONFIG_BACKUP" "$SSH_CONFIG"
    exit 1
fi

# 7. Restart SSH (jeśli nie jesteśmy w sesji SSH)
echo ""
echo "⚠️  WAŻNE: Aby zastosować zmiany, uruchom:"
echo "   sudo systemctl restart ssh"
echo ""
echo "   LUB jeśli jesteś w sesji SSH, użyj:"
echo "   sudo systemctl reload ssh"
echo ""

# 8. Utwórz skrypt do monitorowania połączeń SSH
MONITOR_SCRIPT="/usr/local/bin/monitor_ssh_connections.sh"
cat > "$MONITOR_SCRIPT" << 'EOF'
#!/bin/bash
# Monitor aktywnych połączeń SSH i logowanie

LOG_FILE="/var/log/ssh_connections.log"
TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')

# Liczba aktywnych połączeń
ACTIVE_CONNECTIONS=$(who | grep -c "pts/" || echo "0")

# Sprawdź czy są długie połączenia (potencjalne problemy)
LONG_CONNECTIONS=$(who | awk '{print $1, $3, $4}' | while read user time date; do
    # Prosta logika - jeśli połączenie trwa > 24h, może być problem
    echo "$user $time $date"
done)

echo "[$TIMESTAMP] Aktywne połączenia SSH: $ACTIVE_CONNECTIONS" >> "$LOG_FILE"

# Sprawdź logi SSH pod kątem błędów
RECENT_ERRORS=$(journalctl -u ssh -n 20 --no-pager | grep -i "error\|failed\|timeout" | wc -l)
if [ "$RECENT_ERRORS" -gt 0 ]; then
    echo "[$TIMESTAMP] ⚠️  Wykryto $RECENT_ERRORS błędów w logach SSH" >> "$LOG_FILE"
fi
EOF

chmod +x "$MONITOR_SCRIPT"
echo "✅ Skrypt monitorowania połączeń utworzony"

# 9. Dodaj do crontab
(crontab -l 2>/dev/null | grep -v "monitor_ssh_connections"; echo "*/10 * * * * $MONITOR_SCRIPT") | crontab -

# 10. Utwórz skrypt do automatycznego reconnect
RECONNECT_SCRIPT="$HOME/.ssh/reconnect_voidtracker.sh"
cat > "$RECONNECT_SCRIPT" << 'EOF'
#!/bin/bash
# Automatyczne ponowne połączenie z VoidTracker

SERVER_IP="${1:-YOUR_SERVER_IP_HERE}"
SSH_KEY="${HOME}/.ssh/voidtracker_ed25519"

if [ ! -f "$SSH_KEY" ]; then
    echo "❌ Klucz SSH nie znaleziony: $SSH_KEY"
    exit 1
fi

# Sprawdź czy połączenie działa
if ssh -o ConnectTimeout=5 -i "$SSH_KEY" root@"$SERVER_IP" "echo 'Connection OK'" &>/dev/null; then
    echo "✅ Połączenie działa"
    exit 0
else
    echo "⚠️  Połączenie zerwane, próba ponownego połączenia..."
    ssh -i "$SSH_KEY" \
        -o ServerAliveInterval=60 \
        -o ServerAliveCountMax=3 \
        -o TCPKeepAlive=yes \
        root@"$SERVER_IP"
fi
EOF

chmod +x "$RECONNECT_SCRIPT"
echo "✅ Skrypt reconnect utworzony: $RECONNECT_SCRIPT"

echo ""
echo "✅ Konfiguracja zakończona!"
echo ""
echo "📋 Następne kroki:"
echo "   1. Edytuj $CLIENT_CONFIG i ustaw prawdziwy IP serwera"
echo "   2. Edytuj $RECONNECT_SCRIPT i ustaw prawdziwy IP serwera"
echo "   3. Zrestartuj SSH: sudo systemctl restart ssh"
echo "   4. Przetestuj połączenie: ssh voidtracker"
echo ""
echo "📊 Monitorowanie:"
echo "   - Logi połączeń: /var/log/ssh_connections.log"
echo "   - Logi SSH: journalctl -u ssh -f"
echo ""
