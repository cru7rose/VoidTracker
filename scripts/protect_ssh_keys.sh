#!/bin/bash
# 🛡️ Skrypt ochrony kluczy SSH przed modyfikacją
# Uruchom ten skrypt, aby zabezpieczyć klucze SSH

set -e

SSH_DIR="/root/.ssh"
BACKUP_DIR="/root/.ssh_backup_$(date +%Y%m%d_%H%M%S)"

echo "🛡️  Ochrona kluczy SSH..."

# 1. Utwórz backup przed zmianami
if [ -d "$SSH_DIR" ]; then
    echo "💾 Tworzenie backupu..."
    mkdir -p "$BACKUP_DIR"
    cp -a "$SSH_DIR"/* "$BACKUP_DIR/" 2>/dev/null || true
    chmod 700 "$BACKUP_DIR"
    echo "✅ Backup utworzony: $BACKUP_DIR"
fi

# 2. Ustaw poprawne uprawnienia
echo "🔐 Ustawianie uprawnień..."
if [ -d "$SSH_DIR" ]; then
    chmod 700 "$SSH_DIR"
    
    # Klucze prywatne: 600
    find "$SSH_DIR" -type f ! -name "*.pub" ! -name "known_hosts" ! -name "config" -exec chmod 600 {} \;
    
    # Klucze publiczne: 644
    find "$SSH_DIR" -type f -name "*.pub" -exec chmod 644 {} \;
    
    # Known hosts i config: 644
    [ -f "$SSH_DIR/known_hosts" ] && chmod 644 "$SSH_DIR/known_hosts"
    [ -f "$SSH_DIR/config" ] && chmod 644 "$SSH_DIR/config"
    
    echo "✅ Uprawnienia ustawione"
fi

# 3. Ustaw immutable flag na authorized_keys (najsilniejsza ochrona)
echo "🔒 Ustawianie immutable flag..."
if [ -f "$SSH_DIR/authorized_keys" ]; then
    # Sprawdź czy już jest ustawiony
    if lsattr "$SSH_DIR/authorized_keys" 2>/dev/null | grep -q "i"; then
        echo "ℹ️  Immutable flag już ustawiony"
    else
        chattr +i "$SSH_DIR/authorized_keys" 2>/dev/null && echo "✅ Immutable flag ustawiony" || echo "⚠️  Nie można ustawić immutable (może wymagać root)"
    fi
else
    echo "⚠️  authorized_keys nie istnieje"
fi

# 4. Utwórz skrypt do bezpiecznego zarządzania kluczami
MANAGE_SCRIPT="/usr/local/bin/manage_ssh_keys.sh"
cat > "$MANAGE_SCRIPT" << 'EOF'
#!/bin/bash
# Bezpieczne zarządzanie kluczami SSH

SSH_DIR="/root/.ssh"
AUTHORIZED_KEYS="$SSH_DIR/authorized_keys"

case "$1" in
    add)
        if [ -z "$2" ]; then
            echo "Użycie: $0 add <public_key>"
            exit 1
        fi
        
        # Tymczasowo usuń immutable
        chattr -i "$AUTHORIZED_KEYS" 2>/dev/null || true
        
        # Dodaj klucz
        echo "$2" >> "$AUTHORIZED_KEYS"
        
        # Przywróć immutable
        chattr +i "$AUTHORIZED_KEYS"
        
        echo "✅ Klucz dodany"
        ;;
    
    remove)
        if [ -z "$2" ]; then
            echo "Użycie: $0 remove <key_fingerprint_or_comment>"
            exit 1
        fi
        
        # Tymczasowo usuń immutable
        chattr -i "$AUTHORIZED_KEYS" 2>/dev/null || true
        
        # Usuń klucz (szukaj po komentarzu lub fingerprint)
        sed -i "/$2/d" "$AUTHORIZED_KEYS"
        
        # Przywróć immutable
        chattr +i "$AUTHORIZED_KEYS"
        
        echo "✅ Klucz usunięty"
        ;;
    
    list)
        echo "📋 Zarejestrowane klucze SSH:"
        cat "$AUTHORIZED_KEYS" | while read line; do
            if [ -n "$line" ] && [[ ! "$line" =~ ^# ]]; then
                # Wyciągnij komentarz (ostatnia część klucza)
                comment=$(echo "$line" | awk '{print $NF}')
                echo "  - $comment"
            fi
        done
        ;;
    
    unlock)
        echo "🔓 Tymczasowe odblokowanie authorized_keys..."
        chattr -i "$AUTHORIZED_KEYS" 2>/dev/null || true
        echo "⚠️  Pamiętaj zablokować ponownie: $0 lock"
        ;;
    
    lock)
        echo "🔒 Blokowanie authorized_keys..."
        chattr +i "$AUTHORIZED_KEYS"
        echo "✅ Zablokowane"
        ;;
    
    status)
        if lsattr "$AUTHORIZED_KEYS" 2>/dev/null | grep -q "i"; then
            echo "🔒 Status: ZABLOKOWANY (immutable)"
        else
            echo "🔓 Status: ODBLOKOWANY"
        fi
        ;;
    
    *)
        echo "Zarządzanie kluczami SSH"
        echo ""
        echo "Użycie: $0 {add|remove|list|unlock|lock|status}"
        echo ""
        echo "Komendy:"
        echo "  add <public_key>     - Dodaj nowy klucz"
        echo "  remove <pattern>     - Usuń klucz"
        echo "  list                 - Lista kluczy"
        echo "  unlock               - Tymczasowo odblokuj (do edycji)"
        echo "  lock                 - Zablokuj ponownie"
        echo "  status               - Sprawdź status blokady"
        exit 1
        ;;
esac
EOF

chmod +x "$MANAGE_SCRIPT"
echo "✅ Skrypt zarządzania utworzony: $MANAGE_SCRIPT"

# 5. Utwórz hook do monitorowania zmian
MONITOR_HOOK="/usr/local/bin/ssh_key_change_alert.sh"
cat > "$MONITOR_HOOK" << 'EOF'
#!/bin/bash
# Alert przy zmianie kluczy SSH

SSH_DIR="/root/.ssh"
AUTHORIZED_KEYS="$SSH_DIR/authorized_keys"
LOG_FILE="/var/log/ssh_key_changes.log"
ALERT_EMAIL="${SSH_ALERT_EMAIL:-}"  # Ustaw zmienną środowiskową

log_alert() {
    local message="$1"
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    echo "[$timestamp] ⚠️  $message" >> "$LOG_FILE"
    
    # Wyślij email jeśli skonfigurowany
    if [ -n "$ALERT_EMAIL" ] && command -v mail &>/dev/null; then
        echo "$message" | mail -s "SSH Key Change Alert" "$ALERT_EMAIL"
    fi
}

# Sprawdź czy authorized_keys został zmodyfikowany
if [ -f "$AUTHORIZED_KEYS" ]; then
    # Porównaj z ostatnim backupem
    LAST_BACKUP=$(ls -td /root/.ssh_backup_* 2>/dev/null | head -1)
    
    if [ -n "$LAST_BACKUP" ] && [ -f "$LAST_BACKUP/authorized_keys" ]; then
        if ! diff -q "$AUTHORIZED_KEYS" "$LAST_BACKUP/authorized_keys" &>/dev/null; then
            log_alert "WYKRYTO ZMIANĘ W authorized_keys!"
            log_alert "Różnice:"
            diff "$AUTHORIZED_KEYS" "$LAST_BACKUP/authorized_keys" >> "$LOG_FILE" 2>&1
        fi
    fi
fi

# Sprawdź czy immutable flag został usunięty
if [ -f "$AUTHORIZED_KEYS" ]; then
    if ! lsattr "$AUTHORIZED_KEYS" 2>/dev/null | grep -q "i"; then
        log_alert "IMMUTABLE FLAG ZOSTAŁ USUNIĘTY Z authorized_keys!"
        log_alert "Przywracanie ochrony..."
        chattr +i "$AUTHORIZED_KEYS" 2>/dev/null || true
    fi
fi
EOF

chmod +x "$MONITOR_HOOK"
echo "✅ Hook monitorowania utworzony: $MONITOR_HOOK"

# 6. Dodaj do crontab (sprawdzaj co minutę)
(crontab -l 2>/dev/null | grep -v "ssh_key_change_alert"; echo "* * * * * $MONITOR_HOOK") | crontab -

# 7. Utwórz dokumentację
cat > "$SSH_DIR/README_PROTECTION.md" << 'EOF'
# 🛡️ Ochrona Kluczy SSH

## Status Ochrony

Klucze SSH są chronione przed modyfikacją przez:
1. ✅ Immutable flag (chattr +i) na authorized_keys
2. ✅ Ograniczone uprawnienia (600 dla kluczy prywatnych)
3. ✅ Automatyczne monitorowanie zmian
4. ✅ Regularne backupy

## Zarządzanie Kluczami

Użyj skryptu: `/usr/local/bin/manage_ssh_keys.sh`

Przykłady:
```bash
# Dodaj klucz
sudo /usr/local/bin/manage_ssh_keys.sh add "ssh-ed25519 AAAAC3... key_name"

# Lista kluczy
sudo /usr/local/bin/manage_ssh_keys.sh list

# Usuń klucz
sudo /usr/local/bin/manage_ssh_keys.sh remove "key_name"

# Tymczasowo odblokuj (do edycji)
sudo /usr/local/bin/manage_ssh_keys.sh unlock
# ... wykonaj edycje ...
sudo /usr/local/bin/manage_ssh_keys.sh lock

# Sprawdź status
sudo /usr/local/bin/manage_ssh_keys.sh status
```

## Logi

- Zmiany kluczy: `/var/log/ssh_key_changes.log`
- Monitorowanie: `/var/log/ssh_key_monitor.log`

## Backupy

Backupy są przechowywane w: `/root/.ssh_backup_YYYYMMDD_HHMMSS/`

Aby przywrócić z backupu:
```bash
sudo cp /root/.ssh_backup_YYYYMMDD_HHMMSS/* /root/.ssh/
```
EOF

echo ""
echo "✅ Ochrona kluczy SSH skonfigurowana!"
echo ""
echo "📋 Podsumowanie:"
echo "   - Backup: $BACKUP_DIR"
echo "   - Immutable flag: $(lsattr $SSH_DIR/authorized_keys 2>/dev/null | grep -q 'i' && echo 'TAK' || echo 'NIE')"
echo "   - Skrypt zarządzania: $MANAGE_SCRIPT"
echo "   - Monitorowanie: $MONITOR_HOOK (co minutę)"
echo ""
echo "📖 Dokumentacja: $SSH_DIR/README_PROTECTION.md"
echo ""
echo "⚠️  WAŻNE:"
echo "   - Użyj '$MANAGE_SCRIPT' do zarządzania kluczami"
echo "   - Sprawdź logi: tail -f /var/log/ssh_key_changes.log"
echo ""
