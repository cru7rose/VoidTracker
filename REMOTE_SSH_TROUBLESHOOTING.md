# 🔧 Rozwiązywanie Problemów z Remote-SSH/Cursor

## ⚠️ Objawy Problemu

Z logów widzimy błędy:
```
error while creating socks forwarding Socket closed
channel X: open failed: connect failed: Connection refused
```

To wskazuje na problemy z **port forwarding** w Remote-SSH, co może powodować zrywanie połączenia.

---

## ✅ Kompletne Rozwiązanie

### Krok 1: Uruchom Skrypt Naprawy
```bash
sudo bash /root/VoidTracker/scripts/fix_remote_ssh_connection.sh
```

**Co to robi:**
- Zwiększa limity systemowe dla port forwarding
- Optymalizuje konfigurację SSH dla Remote-SSH
- Konfiguruje sysctl dla lepszego tunelowania
- Dodaje monitorowanie port forwarding

### Krok 2: Zaktualizuj Konfigurację SSH (Jeśli Nie Uruchomiono Poprzednio)
```bash
sudo bash /root/VoidTracker/scripts/improve_ssh_stability.sh
sudo systemctl restart sshd
```

### Krok 3: Zrestartuj Cursor/VS Code
Zamknij i otwórz ponownie Cursor/VS Code, aby zastosować nowe ustawienia.

---

## 🔧 Konfiguracja Klienta (Lokalny Komputer)

**WAŻNE:** Edytuj `~/.ssh/config` na swoim lokalnym komputerze (nie na serwerze):

```ini
Host voidtracker
    HostName TWOJ_IP_SERWERA
    User root
    IdentityFile ~/.ssh/voidtracker_ed25519
    
    # BARDZO AGRESYWNE Keep-Alive dla Remote-SSH
    ServerAliveInterval 15
    ServerAliveCountMax 10
    TCPKeepAlive yes
    
    # Timeouty - zwiększone
    ConnectTimeout 60
    ConnectionAttempts 5
    
    # Kompresja
    Compression yes
    CompressionLevel 6
    
    # Automatyczne ponowne połączenie - ZWIĘKSZONE
    ControlMaster auto
    ControlPath ~/.ssh/control-%h-%p-%r
    ControlPersist 1h
    ControlAutoReconnect yes
    
    # Ważne dla port forwarding
    ExitOnForwardFailure no
    StrictHostKeyChecking accept-new
    
    # Retry
    ReconnectLimit 10
```

**Zamień `TWOJ_IP_SERWERA` na prawdziwy IP serwera!**

---

## 📊 Weryfikacja

### Sprawdź Limity Systemowe
```bash
# Na serwerze
ulimit -n
# Powinno pokazać: 65536

# Sprawdź konfigurację SSH
sudo grep -E "MaxSessions|ClientAliveInterval|MaxStartups" /etc/ssh/sshd_config
# Powinno być:
# MaxSessions 50
# ClientAliveInterval 30
# MaxStartups 50:30:200
```

### Sprawdź Aktywne Tunele
```bash
# Na serwerze
ss -tn | grep -E "9094|5434|7687|7474"
```

### Sprawdź Logi
```bash
# Logi port forwarding
tail -f /var/log/port_forwarding.log

# Logi SSH (szukaj błędów)
sudo journalctl -u ssh -f | grep -i "forward\|channel\|refused"
```

---

## 🔍 Diagnostyka

### Problem: Nadal są błędy "Connection refused"

**Przyczyna:** Porty mogą być zajęte lub serwisy nie działają.

**Rozwiązanie:**
```bash
# Sprawdź czy porty są otwarte
sudo netstat -tulpn | grep -E "9094|5434|7687"

# Sprawdź czy serwisy działają
sudo docker ps | grep -E "kafka|postgres|neo4j"

# Jeśli porty są zajęte, sprawdź co je używa
sudo lsof -i :9094
```

### Problem: Połączenie nadal się zrywa

**Przyczyna:** Keep-Alive może być za słabe lub firewall blokuje.

**Rozwiązanie:**
1. **Zwiększ Keep-Alive w ~/.ssh/config:**
```ini
ServerAliveInterval 10
ServerAliveCountMax 20
```

2. **Sprawdź firewall:**
```bash
sudo ufw status
sudo iptables -L -n | grep 22
```

3. **Sprawdź timeouty na routerze/ISP** (może być problem z NAT)

### Problem: Wiele błędów port forwarding

**Przyczyna:** Zbyt wiele równoczesnych prób tunelowania.

**Rozwiązanie:**
1. **Zamknij wszystkie okna Cursor/VS Code**
2. **Wyczyść stare połączenia:**
```bash
# Na lokalnym komputerze
rm -rf ~/.ssh/control-*
```

3. **Zrestartuj Cursor/VS Code**
4. **Połącz się ponownie**

---

## 🛠️ Zaawansowane Rozwiązywanie Problemów

### 1. Zwiększ Limity Ręcznie
```bash
# Na serwerze
echo "* soft nofile 65536" | sudo tee -a /etc/security/limits.conf
echo "* hard nofile 65536" | sudo tee -a /etc/security/limits.conf

# Wyloguj się i zaloguj ponownie
```

### 2. Sprawdź Konfigurację sysctl
```bash
# Na serwerze
sudo sysctl net.core.somaxconn
# Powinno być: 4096

sudo sysctl net.ipv4.ip_local_port_range
# Powinno być: 10000 65535
```

### 3. Test Połączenia z Debugowaniem
```bash
# Na lokalnym komputerze
ssh -v voidtracker

# Lub bardziej szczegółowo
ssh -vvv voidtracker
```

### 4. Sprawdź Procesy SSH
```bash
# Na serwerze
ps aux | grep sshd
# Sprawdź czy nie ma zbyt wielu procesów

# Sprawdź zużycie zasobów
top -p $(pgrep sshd | tr '\n' ',' | sed 's/,$//')
```

---

## 📈 Monitorowanie

### Automatyczne Monitorowanie
```bash
# Logi port forwarding (co 5 minut)
tail -f /var/log/port_forwarding.log

# Logi SSH
sudo journalctl -u ssh -f
```

### Ręczne Sprawdzanie
```bash
# Aktywne sesje SSH
who
w

# Aktywne tunele
ss -tn | grep ESTAB

# Statystyki SSH
sudo netstat -an | grep :22 | wc -l
```

---

## ✅ Checklist Naprawy

- [ ] Uruchomiono `fix_remote_ssh_connection.sh`
- [ ] Uruchomiono `improve_ssh_stability.sh`
- [ ] SSH zrestartowany (`sudo systemctl restart sshd`)
- [ ] `~/.ssh/config` zaktualizowany na lokalnym komputerze
- [ ] Cursor/VS Code zrestartowany
- [ ] Limity systemowe sprawdzone (`ulimit -n`)
- [ ] Konfiguracja SSH sprawdzona
- [ ] Połączenie przetestowane
- [ ] Logi sprawdzone (brak błędów)

---

## 🚨 Jeśli Nic Nie Pomaga

1. **Sprawdź logi Cursor/VS Code:**
   - Otwórz Output → "Remote-SSH"
   - Szukaj błędów

2. **Spróbuj połączenia bez Remote-SSH:**
```bash
ssh voidtracker
# Jeśli działa, problem jest w Remote-SSH, nie w SSH
```

3. **Sprawdź wersję Remote-SSH:**
   - Zaktualizuj rozszerzenie Remote-SSH w Cursor/VS Code

4. **Spróbuj alternatywnego klienta:**
   - VS Code zamiast Cursor (lub odwrotnie)
   - Sprawdź czy problem występuje w obu

---

**Ostatnia aktualizacja:** 2026-01-12  
**Wersja:** 2.0 (Zoptymalizowane dla Remote-SSH)
