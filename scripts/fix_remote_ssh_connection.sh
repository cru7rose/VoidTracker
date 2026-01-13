#!/bin/bash
# 🔧 Skrypt naprawy problemów z Remote-SSH/Cursor
# Rozwiązuje problemy z tunelowaniem portów i zrywaniem połączenia

set -e

echo "🔧 Naprawa konfiguracji Remote-SSH/Cursor..."

# 1. Sprawdź i zwiększ limity systemowe dla port forwarding
echo "📊 Sprawdzanie limitów systemowych..."

# Zwiększ limit otwartych plików (ważne dla wielu portów)
if ! grep -q "# VoidTracker SSH Limits" /etc/security/limits.conf 2>/dev/null; then
    cat >> /etc/security/limits.conf << 'EOF'

# VoidTracker SSH Limits - dla Remote-SSH port forwarding
root soft nofile 65536
root hard nofile 65536
* soft nofile 65536
* hard nofile 65536
EOF
    echo "✅ Limity systemowe zaktualizowane"
else
    echo "ℹ️  Limity już skonfigurowane"
fi

# 2. Zwiększ limity dla sesji SSH
if [ -f /etc/systemd/system/ssh.service.d/limits.conf ]; then
    echo "ℹ️  Plik limits.conf już istnieje"
else
    mkdir -p /etc/systemd/system/ssh.service.d
    cat > /etc/systemd/system/ssh.service.d/limits.conf << 'EOF'
[Service]
LimitNOFILE=65536
LimitNPROC=4096
EOF
    systemctl daemon-reload
    echo "✅ Limity dla SSH service zaktualizowane"
fi

# 3. Sprawdź i zaktualizuj sysctl dla port forwarding
echo "⚙️  Konfiguracja sysctl dla port forwarding..."

SYSCTL_CONF="/etc/sysctl.d/99-ssh-port-forwarding.conf"
cat > "$SYSCTL_CONF" << 'EOF'
# VoidTracker SSH Port Forwarding Optimization
# Zwiększone limity dla Remote-SSH tunelowania

# Zwiększ limit połączeń TCP
net.core.somaxconn = 4096
net.ipv4.tcp_max_syn_backlog = 4096

# Zwiększ limity dla port forwarding
net.ipv4.ip_local_port_range = 10000 65535

# Keep-alive dla TCP
net.ipv4.tcp_keepalive_time = 300
net.ipv4.tcp_keepalive_probes = 5
net.ipv4.tcp_keepalive_intvl = 15

# Zwiększ limity dla połączeń
net.core.netdev_max_backlog = 5000
EOF

sysctl -p "$SYSCTL_CONF" 2>/dev/null || true
echo "✅ Konfiguracja sysctl zaktualizowana"

# 4. Sprawdź konfigurację SSH
echo "🔍 Sprawdzanie konfiguracji SSH..."

SSH_CONFIG="/etc/ssh/sshd_config"

# Sprawdź czy MaxSessions jest wystarczająco duże
if grep -q "^MaxSessions" "$SSH_CONFIG"; then
    CURRENT_MAX=$(grep "^MaxSessions" "$SSH_CONFIG" | awk '{print $2}')
    if [ "$CURRENT_MAX" -lt 50 ]; then
        echo "⚠️  MaxSessions jest za małe ($CURRENT_MAX), zwiększanie do 50..."
        sed -i "s/^MaxSessions.*/MaxSessions 50/" "$SSH_CONFIG"
    fi
else
    echo "MaxSessions 50" >> "$SSH_CONFIG"
fi

# Sprawdź ClientAliveInterval
if grep -q "^ClientAliveInterval" "$SSH_CONFIG"; then
    CURRENT_INTERVAL=$(grep "^ClientAliveInterval" "$SSH_CONFIG" | awk '{print $2}')
    if [ "$CURRENT_INTERVAL" -gt 30 ]; then
        echo "⚠️  ClientAliveInterval jest za duże ($CURRENT_INTERVAL), zmniejszanie do 30..."
        sed -i "s/^ClientAliveInterval.*/ClientAliveInterval 30/" "$SSH_CONFIG"
    fi
fi

# 5. Utwórz skrypt do monitorowania port forwarding
MONITOR_SCRIPT="/usr/local/bin/monitor_port_forwarding.sh"
cat > "$MONITOR_SCRIPT" << 'EOF'
#!/bin/bash
# Monitor port forwarding i połączeń SSH

LOG_FILE="/var/log/port_forwarding.log"
TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')

# Liczba aktywnych tuneli
ACTIVE_TUNNELS=$(ss -tn | grep -c ":9094\|:5434\|:7687" || echo "0")

# Liczba aktywnych sesji SSH
ACTIVE_SSH=$(who | grep -c "pts/" || echo "0")

# Sprawdź czy są problemy z portami
FAILED_PORTS=$(journalctl -u ssh -n 100 --no-pager | grep -i "Connection refused\|channel.*open failed" | wc -l)

echo "[$TIMESTAMP] Aktywne tunele: $ACTIVE_TUNNELS, Sesje SSH: $ACTIVE_SSH, Błędy: $FAILED_PORTS" >> "$LOG_FILE"

if [ "$FAILED_PORTS" -gt 10 ]; then
    echo "[$TIMESTAMP] ⚠️  Wykryto $FAILED_PORTS błędów port forwarding!" >> "$LOG_FILE"
fi
EOF

chmod +x "$MONITOR_SCRIPT"
echo "✅ Skrypt monitorowania utworzony"

# 6. Dodaj do crontab
(crontab -l 2>/dev/null | grep -v "monitor_port_forwarding"; echo "*/5 * * * * $MONITOR_SCRIPT") | crontab -

# 7. Utwórz skrypt do czyszczenia starych kontroli SSH
CLEANUP_SCRIPT="/usr/local/bin/cleanup_ssh_controls.sh"
cat > "$CLEANUP_SCRIPT" << 'EOF'
#!/bin/bash
# Czyszczenie starych plików kontrolnych SSH (ControlMaster)

# Usuń stare pliki kontrolne (starsze niż 1 godzinę)
find ~/.ssh -name "control-*" -type s -mmin +60 -delete 2>/dev/null || true

# Usuń stare socket files
find /tmp -name "ssh-*" -type s -mmin +60 -delete 2>/dev/null || true
EOF

chmod +x "$CLEANUP_SCRIPT"
echo "✅ Skrypt czyszczenia utworzony"

# Dodaj do crontab (co godzinę)
(crontab -l 2>/dev/null | grep -v "cleanup_ssh_controls"; echo "0 * * * * $CLEANUP_SCRIPT") | crontab -

# 8. Zrestartuj SSH (jeśli nie jesteśmy w sesji SSH)
echo ""
echo "🔄 Aby zastosować zmiany, uruchom:"
echo "   sudo systemctl restart ssh"
echo ""

# 9. Utwórz dokumentację dla użytkownika
cat > /root/REMOTE_SSH_FIX.md << 'EOF'
# 🔧 Naprawa Problemów z Remote-SSH/Cursor

## Problem
Błędy typu:
- "error while creating socks forwarding Socket closed"
- "channel X: open failed: connect failed: Connection refused"
- Połączenie się zrywa podczas używania Remote-SSH

## Rozwiązanie

### 1. Zrestartuj SSH z nowymi ustawieniami
```bash
sudo systemctl restart ssh
```

### 2. Sprawdź limity systemowe
```bash
ulimit -n
# Powinno pokazać 65536 lub więcej
```

### 3. Sprawdź konfigurację SSH
```bash
sudo grep -E "MaxSessions|ClientAliveInterval|MaxStartups" /etc/ssh/sshd_config
```

### 4. Sprawdź logi port forwarding
```bash
tail -f /var/log/port_forwarding.log
```

### 5. Sprawdź logi SSH
```bash
sudo journalctl -u ssh -f | grep -i "forward\|channel\|refused"
```

## Konfiguracja Klienta (Lokalny Komputer)

Edytuj `~/.ssh/config` na swoim lokalnym komputerze:

```ini
Host voidtracker
    HostName TWOJ_IP_SERWERA
    User root
    IdentityFile ~/.ssh/voidtracker_ed25519
    
    # BARDZO AGRESYWNE Keep-Alive
    ServerAliveInterval 15
    ServerAliveCountMax 10
    TCPKeepAlive yes
    
    # Timeouty
    ConnectTimeout 60
    ConnectionAttempts 5
    
    # Automatyczne ponowne połączenie
    ControlMaster auto
    ControlPath ~/.ssh/control-%h-%p-%r
    ControlPersist 1h
    ControlAutoReconnect yes
    
    # Ważne dla port forwarding
    ExitOnForwardFailure no
```

## Rozwiązywanie Problemów

### Problem: Nadal są błędy port forwarding

1. **Zwiększ limity:**
```bash
ulimit -n 65536
```

2. **Sprawdź czy porty nie są zajęte:**
```bash
sudo netstat -tulpn | grep -E "9094|5434|7687"
```

3. **Zrestartuj Cursor/VS Code**

### Problem: Połączenie nadal się zrywa

1. **Sprawdź Keep-Alive:**
```bash
# Na serwerze
sudo grep ClientAliveInterval /etc/ssh/sshd_config

# Powinno być: ClientAliveInterval 30
```

2. **Sprawdź firewall:**
```bash
sudo ufw status
sudo iptables -L -n | grep 22
```

3. **Zwiększ timeout w ~/.ssh/config:**
```ini
ServerAliveInterval 10
ServerAliveCountMax 20
```

## Monitorowanie

```bash
# Aktywne tunele
ss -tn | grep -E "9094|5434|7687"

# Logi port forwarding
tail -f /var/log/port_forwarding.log

# Logi SSH
sudo journalctl -u ssh -f
```
EOF

echo ""
echo "✅ Naprawa zakończona!"
echo ""
echo "📋 Następne kroki:"
echo "   1. Zrestartuj SSH: sudo systemctl restart sshd"
echo "   2. Zrestartuj Cursor/VS Code"
echo "   3. Sprawdź dokumentację: cat /root/REMOTE_SSH_FIX.md"
echo ""
echo "⚠️  WAŻNE: Po zrestartowaniu SSH może być konieczne ponowne połączenie!"
