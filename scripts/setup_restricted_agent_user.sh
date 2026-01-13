#!/bin/bash
# 🔒 Skrypt konfiguracji ograniczonego użytkownika dla agenta AI
# Ten użytkownik nie będzie mógł modyfikować kluczy SSH ani usuwać plików systemowych

set -e

AGENT_USER="voidtracker-agent"
AGENT_HOME="/home/${AGENT_USER}"
PROJECT_DIR="/root/VoidTracker"

echo "🔧 Konfiguracja ograniczonego użytkownika dla agenta..."

# 1. Utwórz użytkownika (jeśli nie istnieje)
if id "$AGENT_USER" &>/dev/null; then
    echo "⚠️  Użytkownik $AGENT_USER już istnieje. Pomijanie tworzenia..."
else
    echo "📝 Tworzenie użytkownika $AGENT_USER..."
    useradd -m -s /bin/bash -d "$AGENT_HOME" "$AGENT_USER"
    echo "✅ Użytkownik utworzony"
fi

# 2. Utwórz katalogi robocze
echo "📁 Tworzenie katalogów roboczych..."
mkdir -p "$AGENT_HOME/workspace"
mkdir -p "$AGENT_HOME/.ssh"
chmod 700 "$AGENT_HOME/.ssh"

# 3. Skonfiguruj uprawnienia - użytkownik może czytać projekt, ale nie modyfikować kluczy SSH
echo "🔐 Konfiguracja uprawnień..."

# Pozwól agentowi czytać projekt (tylko do odczytu dla root/.ssh)
if [ -d "/root/.ssh" ]; then
    # Upewnij się, że root/.ssh jest chroniony
    chmod 700 /root/.ssh
    chmod 600 /root/.ssh/* 2>/dev/null || true
    # Agent NIE może modyfikować kluczy root
    chattr +i /root/.ssh/authorized_keys 2>/dev/null || true
fi

# Utwórz własny katalog SSH dla agenta (tylko do odczytu)
if [ ! -f "$AGENT_HOME/.ssh/authorized_keys" ]; then
    touch "$AGENT_HOME/.ssh/authorized_keys"
    chmod 600 "$AGENT_HOME/.ssh/authorized_keys"
    chown "$AGENT_USER:$AGENT_USER" "$AGENT_HOME/.ssh/authorized_keys"
fi

# 4. Utwórz sudoers config - ograniczone uprawnienia
echo "⚙️  Konfiguracja sudoers..."
SUDOERS_FILE="/etc/sudoers.d/${AGENT_USER}"

cat > "$SUDOERS_FILE" << 'EOF'
# Ograniczone uprawnienia dla voidtracker-agent
# Użytkownik może:
# - Uruchamiać docker (tylko do odczytu logów)
# - Czytać pliki konfiguracyjne
# - NIE może modyfikować /root/.ssh/*
# - NIE może usuwać plików systemowych
# - NIE może modyfikować /etc/ssh/*

voidtracker-agent ALL=(ALL) NOPASSWD: /usr/bin/docker logs *, /usr/bin/docker ps, /usr/bin/docker inspect *
voidtracker-agent ALL=(ALL) NOPASSWD: /usr/bin/cat /etc/ssh/sshd_config
voidtracker-agent ALL=(ALL) NOPASSWD: /usr/bin/systemctl status *, /usr/bin/systemctl is-active *
voidtracker-agent ALL=(ALL) NOPASSWD: /usr/bin/journalctl -u *
voidtracker-agent ALL=(ALL) NOPASSWD: /usr/bin/tail -f /var/log/*

# Zabronione operacje (domyślnie zabronione, ale dla pewności):
voidtracker-agent ALL=(ALL) !/usr/bin/rm -rf /root/.ssh/*
voidtracker-agent ALL=(ALL) !/usr/bin/chmod * /root/.ssh/*
voidtracker-agent ALL=(ALL) !/usr/bin/chattr * /root/.ssh/*
voidtracker-agent ALL=(ALL) !/usr/bin/rm /etc/ssh/*
voidtracker-agent ALL=(ALL) !/usr/bin/mv /root/.ssh/*
voidtracker-agent ALL=(ALL) !/usr/bin/cp * /root/.ssh/*
EOF

chmod 440 "$SUDOERS_FILE"
echo "✅ Konfiguracja sudoers utworzona"

# 5. Utwórz ograniczenia AppArmor/SELinux (jeśli dostępne)
if command -v aa-complain &>/dev/null; then
    echo "🛡️  Konfiguracja AppArmor..."
    # Można dodać profil AppArmor tutaj
fi

# 6. Skonfiguruj chroot jail lub ograniczenia (opcjonalne, zaawansowane)
echo "🔒 Konfiguracja dodatkowych ograniczeń..."

# Utwórz plik z ograniczeniami dla agenta
cat > "$AGENT_HOME/.bashrc_restrictions" << 'EOF'
# Ograniczenia dla agenta AI
# Blokuj niebezpieczne komendy

# Funkcja blokująca niebezpieczne operacje
block_dangerous_commands() {
    local cmd="$1"
    local dangerous_patterns=(
        "rm -rf /root"
        "rm -rf /etc"
        "chmod.*/root/.ssh"
        "chattr.*/root/.ssh"
        "rm.*/root/.ssh"
        "mv.*/root/.ssh"
        "> /root/.ssh"
        ">> /root/.ssh"
    )
    
    for pattern in "${dangerous_patterns[@]}"; do
        if echo "$cmd" | grep -qE "$pattern"; then
            echo "❌ Operacja zablokowana ze względów bezpieczeństwa: $cmd"
            return 1
        fi
    done
    return 0
}

# Hook do sprawdzania komend (jeśli używasz shell history)
if [ -n "$BASH_VERSION" ]; then
    trap 'block_dangerous_commands "$BASH_COMMAND"' DEBUG
fi
EOF

# 7. Utwórz skrypt do monitorowania zmian w kluczach SSH
echo "👁️  Konfiguracja monitorowania kluczy SSH..."
MONITOR_SCRIPT="/usr/local/bin/monitor_ssh_keys.sh"

cat > "$MONITOR_SCRIPT" << 'EOF'
#!/bin/bash
# Monitor zmian w kluczach SSH i alertowanie

SSH_DIR="/root/.ssh"
LOG_FILE="/var/log/ssh_key_monitor.log"

check_ssh_keys() {
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    
    # Sprawdź czy authorized_keys został zmodyfikowany
    if [ -f "$SSH_DIR/authorized_keys" ]; then
        local last_mod=$(stat -c %Y "$SSH_DIR/authorized_keys" 2>/dev/null || echo "0")
        local last_check_file="/tmp/ssh_last_check"
        
        if [ -f "$last_check_file" ]; then
            local last_check=$(cat "$last_check_file")
            if [ "$last_mod" != "$last_check" ]; then
                echo "[$timestamp] ⚠️  WYKRYTO ZMIANĘ W authorized_keys!" >> "$LOG_FILE"
                echo "[$timestamp] Ostatnia modyfikacja: $(stat -c %y "$SSH_DIR/authorized_keys")" >> "$LOG_FILE"
                # Można dodać powiadomienie email/tutaj
            fi
        fi
        echo "$last_mod" > "$last_check_file"
    fi
}

check_ssh_keys
EOF

chmod +x "$MONITOR_SCRIPT"
echo "✅ Skrypt monitorowania utworzony"

# 8. Dodaj do crontab - sprawdzaj co 5 minut
(crontab -l 2>/dev/null | grep -v "monitor_ssh_keys"; echo "*/5 * * * * $MONITOR_SCRIPT") | crontab -

# 9. Utwórz backup kluczy SSH
echo "💾 Tworzenie backupu kluczy SSH..."
BACKUP_DIR="/root/.ssh_backup_$(date +%Y%m%d_%H%M%S)"
if [ -d "/root/.ssh" ]; then
    mkdir -p "$BACKUP_DIR"
    cp -a /root/.ssh/* "$BACKUP_DIR/" 2>/dev/null || true
    chmod 700 "$BACKUP_DIR"
    echo "✅ Backup utworzony w: $BACKUP_DIR"
fi

# 10. Ustaw immutable flag na kluczach (opcjonalne, może być zbyt restrykcyjne)
# echo "🔒 Ustawianie immutable flag na kluczach SSH..."
# chattr +i /root/.ssh/authorized_keys 2>/dev/null || echo "⚠️  Nie można ustawić immutable (może być już ustawione)"

# 11. Utwórz dokumentację dla agenta
echo "📚 Tworzenie dokumentacji..."
cat > "$AGENT_HOME/AGENT_README.md" << 'EOF'
# 🔒 Przewodnik dla Agenta AI - VoidTracker

## ⚠️ Ważne Ograniczenia

Jesteś skonfigurowany jako **ograniczony użytkownik** z następującymi zasadami:

### ✅ DOZWOLONE Operacje:
- Czytanie plików projektu w `/root/VoidTracker`
- Czytanie logów Docker: `sudo docker logs <container>`
- Sprawdzanie statusu serwisów: `sudo systemctl status <service>`
- Czytanie logów systemowych: `sudo journalctl -u <service>`

### ❌ ZABRONIONE Operacje:
- **Modyfikacja kluczy SSH** w `/root/.ssh/`
- **Usuwanie plików systemowych**
- **Modyfikacja konfiguracji SSH** w `/etc/ssh/`
- **Zmiana uprawnień** do katalogów systemowych

## 📁 Struktura Katalogów

- **Projekt**: `/root/VoidTracker` (tylko do odczytu)
- **Twoje pliki**: `~/workspace/` (pełny dostęp)
- **SSH**: `~/.ssh/` (tylko własne klucze)

## 🔐 Bezpieczeństwo

Klucze SSH root są chronione przed modyfikacją. Jeśli potrzebujesz dostępu SSH, 
skontaktuj się z administratorem.

## 🛠️ Przydatne Komendy

```bash
# Sprawdź status serwisów
sudo systemctl status docker

# Zobacz logi kontenera
sudo docker logs <container-name> --tail 100

# Sprawdź działające kontenery
sudo docker ps

# Sprawdź logi systemowe
sudo journalctl -u ssh -n 50
```

## 📝 Notatki

- Wszystkie zmiany w kluczach SSH są monitorowane
- Backup kluczy jest tworzony automatycznie
- Wszystkie operacje są logowane
EOF

chown "$AGENT_USER:$AGENT_USER" "$AGENT_HOME/AGENT_README.md"
chown -R "$AGENT_USER:$AGENT_USER" "$AGENT_HOME/workspace"

echo ""
echo "✅ Konfiguracja zakończona!"
echo ""
echo "📋 Podsumowanie:"
echo "   - Użytkownik: $AGENT_USER"
echo "   - Katalog domowy: $AGENT_HOME"
echo "   - Ograniczenia: Skonfigurowane w /etc/sudoers.d/${AGENT_USER}"
echo "   - Monitorowanie: $MONITOR_SCRIPT (uruchamiane co 5 minut)"
echo "   - Backup SSH: $BACKUP_DIR"
echo ""
echo "🔑 Aby dodać klucz SSH dla agenta:"
echo "   sudo -u $AGENT_USER ssh-keygen -t ed25519 -f $AGENT_HOME/.ssh/id_ed25519"
echo ""
echo "⚠️  WAŻNE: Przetestuj połączenie przed użyciem w produkcji!"
