# 🚀 Szybki Start - Konfiguracja Agenta AI

## ⚠️ Problem
- Połączenie SSH się zrywa
- Agent może modyfikować klucze SSH
- Ryzyko utraty plików

## ✅ Rozwiązanie - 3 Kroki

### Krok 1: Utwórz Ograniczonego Użytkownika dla Agenta
```bash
sudo bash /root/VoidTracker/scripts/setup_restricted_agent_user.sh
```

**Co to robi:**
- Tworzy użytkownika `voidtracker-agent` z ograniczonymi uprawnieniami
- Agent **NIE MOŻE** modyfikować kluczy SSH w `/root/.ssh/`
- Agent **NIE MOŻE** usuwać plików systemowych
- Agent **MOŻE** czytać projekt i logi

### Krok 2: Popraw Stabilność Połączenia SSH
```bash
sudo bash /root/VoidTracker/scripts/improve_ssh_stability.sh
```

**Co to robi:**
- Konfiguruje Keep-Alive (zapobiega zrywaniu połączenia)
- Ustawia optymalne timeouty
- Konfiguruje automatyczne ponowne połączenie
- Dodaje monitorowanie połączeń

**WAŻNE:** Po uruchomieniu zrestartuj SSH:
```bash
sudo systemctl restart sshd
```

### Krok 3: Zabezpiecz Klucze SSH
```bash
sudo bash /root/VoidTracker/scripts/protect_ssh_keys.sh
```

**Co to robi:**
- Ustawia immutable flag na `authorized_keys` (nie można usunąć/modyfikować)
- Tworzy backup kluczy SSH
- Konfiguruje monitorowanie zmian
- Tworzy bezpieczny skrypt do zarządzania kluczami

---

## 🔍 Weryfikacja

### Sprawdź Użytkownika
```bash
id voidtracker-agent
sudo -l -U voidtracker-agent
```

### Sprawdź Ochronę Kluczy
```bash
# Sprawdź czy immutable flag jest ustawiony
lsattr /root/.ssh/authorized_keys
# Powinno pokazać: ----i--------e-- (litera 'i' oznacza immutable)

# Sprawdź status
sudo /usr/local/bin/manage_ssh_keys.sh status
```

### Sprawdź Konfigurację SSH
```bash
# Test konfiguracji
sudo sshd -t

# Sprawdź ustawienia Keep-Alive
sudo grep -E "ClientAlive|TCPKeepAlive" /etc/ssh/sshd_config
```

### Test Połączenia
```bash
# Z lokalnego komputera (po edycji ~/.ssh/config)
ssh voidtracker
```

---

## 📋 Konfiguracja Klienta SSH (VS Code Remote-SSH)

Edytuj `~/.ssh/config` na swoim lokalnym komputerze:

```ini
Host voidtracker
    HostName TWOJ_IP_SERWERA
    User root
    IdentityFile ~/.ssh/voidtracker_ed25519
    ServerAliveInterval 60
    ServerAliveCountMax 3
    TCPKeepAlive yes
    ConnectTimeout 30
    Compression yes
    # Automatyczne ponowne połączenie
    ControlMaster auto
    ControlPath ~/.ssh/control-%h-%p-%r
    ControlPersist 10m
    ConnectionAttempts 3
```

**WAŻNE:** Zamień `TWOJ_IP_SERWERA` na prawdziwy IP serwera!

---

## 🛠️ Zarządzanie Kluczami SSH (Bezpieczne)

### Dodaj Nowy Klucz
```bash
sudo /usr/local/bin/manage_ssh_keys.sh add "ssh-ed25519 AAAAC3... nazwa_klucza"
```

### Lista Kluczy
```bash
sudo /usr/local/bin/manage_ssh_keys.sh list
```

### Usuń Klucz
```bash
sudo /usr/local/bin/manage_ssh_keys.sh remove "nazwa_klucza"
```

### Tymczasowo Odblokuj (do edycji)
```bash
# Odblokuj
sudo /usr/local/bin/manage_ssh_keys.sh unlock

# ... wykonaj edycje ...

# Zablokuj ponownie
sudo /usr/local/bin/manage_ssh_keys.sh lock
```

---

## 📊 Monitorowanie

### Logi Zmian Kluczy SSH
```bash
tail -f /var/log/ssh_key_changes.log
```

### Logi Połączeń SSH
```bash
sudo journalctl -u ssh -f
```

### Aktywne Połączenia
```bash
who
w
```

---

## 🔧 Rozwiązywanie Problemów

### Problem z Remote-SSH/Cursor (błędy port forwarding)

Jeśli widzisz błędy typu:
- "error while creating socks forwarding Socket closed"
- "channel X: open failed: connect failed: Connection refused"

**Uruchom specjalny skrypt naprawy:**
```bash
sudo bash /root/VoidTracker/scripts/fix_remote_ssh_connection.sh
sudo systemctl restart sshd
```

**Następnie zaktualizuj `~/.ssh/config` na lokalnym komputerze:**
```ini
Host voidtracker
    ServerAliveInterval 15
    ServerAliveCountMax 10
    ControlPersist 1h
    ControlAutoReconnect yes
```

Szczegółowa dokumentacja: `/root/VoidTracker/REMOTE_SSH_TROUBLESHOOTING.md`

### Połączenie nadal się zrywa

1. **Sprawdź Keep-Alive:**
```bash
sudo grep ClientAlive /etc/ssh/sshd_config
# Powinno być: ClientAliveInterval 30 (zaktualizowane!)
```

2. **Sprawdź Firewall:**
```bash
sudo ufw status
sudo iptables -L -n | grep 22
```

3. **Zwiększ Timeout w ~/.ssh/config:**
```ini
ConnectTimeout 60
ServerAliveInterval 15
ServerAliveCountMax 10
```

### Agent nadal może modyfikować klucze

1. **Sprawdź immutable flag:**
```bash
lsattr /root/.ssh/authorized_keys
# Powinno pokazać 'i'
```

2. **Sprawdź sudoers:**
```bash
sudo cat /etc/sudoers.d/voidtracker-agent
```

3. **Ustaw ponownie ochronę:**
```bash
sudo bash /root/VoidTracker/scripts/protect_ssh_keys.sh
```

### Przywróć z Backupu

```bash
# Lista backupów
ls -la /root/.ssh_backup_*/

# Przywróć
sudo cp /root/.ssh_backup_YYYYMMDD_HHMMSS/* /root/.ssh/
sudo chmod 600 /root/.ssh/*
```

---

## 📚 Pełna Dokumentacja

Szczegółowa dokumentacja: `/root/VoidTracker/AGENT_BEST_PRACTICES.md`

---

## ✅ Checklist

- [ ] Użytkownik `voidtracker-agent` utworzony
- [ ] Konfiguracja SSH dla stabilności zastosowana
- [ ] SSH zrestartowany (`sudo systemctl restart sshd`)
- [ ] Klucze SSH zabezpieczone (immutable flag)
- [ ] Backup kluczy utworzony
- [ ] Monitorowanie skonfigurowane
- [ ] `~/.ssh/config` na lokalnym komputerze zaktualizowany
- [ ] Połączenie przetestowane

---

**Gotowe!** 🎉

Teraz agent będzie działał bezpiecznie, a połączenie SSH będzie stabilne.
