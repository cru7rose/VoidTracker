# 🔧 Naprawa Problemu z Zrywaniem Połączenia SSH

## ⚠️ Problem
Za każdym razem jak zerwie się połączenie, musisz restartować serwer i nie możesz testować.

**UWAGA:** Jeśli widzisz błąd `Bad configuration option: controlautoreconnect` w logach, zobacz: [NAPRAW_BLED_KONFIGURACJI_SSH.md](./NAPRAW_BLED_KONFIGURACJI_SSH.md)

## ✅ Rozwiązanie - 3 Kroki (BEZ RESTARTU SERWERA!)

### Krok 1: Na Serwerze (Już Zrobione ✅)

Automatyczny health check jest już skonfigurowany i działa! Sprawdź:
```bash
systemctl status ssh-check.timer
```

Jeśli nie działa, uruchom:
```bash
sudo systemctl enable --now ssh-check.timer
```

### Krok 2: Na Lokalnym Komputerze (WAŻNE!)

**Edytuj `~/.ssh/config` na swoim lokalnym komputerze:**

```ini
Host voidtracker
    HostName TWOJ_IP_SERWERA
    User root
    IdentityFile ~/.ssh/voidtracker_ed25519
    
    # BARDZO AGRESYWNE Keep-Alive (NAJWAŻNIEJSZE dla dużego obciążenia!)
    ServerAliveInterval 10
    ServerAliveCountMax 20
    TCPKeepAlive yes
    
    # Automatyczne reconnect - UWAGA: Poprawna pisownia!
    ControlMaster auto
    ControlPath ~/.ssh/control-%h-%p-%r
    ControlPersist 2h
    ControlAutoReconnect yes
    
    # Retry - zwiększone dla dużego obciążenia
    ConnectionAttempts 10
    ConnectTimeout 60
    
    # Ważne dla port forwarding
    ExitOnForwardFailure no
    
    # Kompresja (pomaga przy dużym obciążeniu)
    Compression yes
    CompressionLevel 6
```

**🚨 WAŻNE:** Jeśli widzisz błąd `Bad configuration option: controlautoreconnect`, zobacz: [NAPRAW_BLED_KONFIGURACJI_SSH.md](./NAPRAW_BLED_KONFIGURACJI_SSH.md)

**Zamień `TWOJ_IP_SERWERA` na prawdziwy IP!**

### Krok 3: W Cursor/VS Code (WAŻNE!)

1. Otwórz Settings:
   - **macOS**: `Cmd + ,`
   - **Windows/Linux**: `Ctrl + ,`

2. Szukaj i ustaw:
   - `remote.SSH.connectTimeout` → `60`
   - `remote.SSH.serverAliveInterval` → `10`
   - `remote.SSH.serverAliveCountMax` → `20`
   - `remote.SSH.keepAlive` → `true`

3. Zrestartuj Cursor/VS Code

---

## 🚀 Alternatywne Rozwiązania

### Opcja A: Użyj autossh (NAJLEPSZE!)

**Na lokalnym komputerze:**
```bash
# Zainstaluj autossh
# macOS:
brew install autossh

# Linux:
sudo apt-get install autossh

# Użyj zamiast ssh:
autossh -M 20000 -f -N voidtracker
```

### Opcja B: Użyj Skryptu Auto-Reconnect

**Na lokalnym komputerze:**
```bash
# Skopiuj skrypt z serwera
scp root@SERVER:/root/VoidTracker/scripts/ssh-auto-reconnect-client.sh ~/

# Uruchom
chmod +x ~/ssh-auto-reconnect-client.sh
~/ssh-auto-reconnect-client.sh voidtracker
```

---

## 🔍 Weryfikacja

### Sprawdź czy Health Check działa:
```bash
# Na serwerze
systemctl status ssh-check.timer
journalctl -u ssh-check.service -n 20
```

### Sprawdź logi:
```bash
# Na serwerze
tail -f /var/log/ssh-health.log
```

### Test połączenia:
```bash
# Na lokalnym komputerze
ssh voidtracker
# Powinno połączyć się automatycznie, nawet po zrywaniu
```

---

## 🛠️ Jeśli Nadal Trzeba Restartować Serwer

### Włącz Watchdog (bardziej agresywny):
```bash
# Na serwerze
sudo systemctl enable --now ssh-watchdog.service
```

**UWAGA:** Watchdog może być zbyt agresywny. Użyj tylko jeśli health check nie wystarcza.

### Sprawdź co się dzieje:
```bash
# Na serwerze
# Sprawdź logi SSH
sudo journalctl -u ssh -f

# Sprawdź czy SSH działa
systemctl status ssh

# Sprawdź czy nasłuchuje na porcie 22
ss -tlnp | grep :22
```

---

## 📊 Monitorowanie

### Na Serwerze:
```bash
# Health check logi
tail -f /var/log/ssh-health.log

# Watchdog logi (jeśli włączony)
tail -f /var/log/ssh-watchdog.log

# Status serwisów
systemctl status ssh-check.timer
systemctl status ssh-watchdog.service
```

### Na Lokalnym Komputerze:
```bash
# Sprawdź aktywne połączenia
ssh -O check voidtracker

# Sprawdź kontrolne sockety
ls -la ~/.ssh/control-*
```

---

## ✅ Checklist

- [ ] Health check timer działa (`systemctl status ssh-check.timer`)
- [ ] `~/.ssh/config` zaktualizowany na lokalnym komputerze
- [ ] Ustawienia Cursor/VS Code zaktualizowane
- [ ] Cursor/VS Code zrestartowany
- [ ] Połączenie przetestowane
- [ ] Logi sprawdzone (brak błędów)

---

## 🚨 Szybka Naprawa (Jeśli Nic Nie Działa)

```bash
# Na serwerze - jednorazowa naprawa
sudo systemctl restart ssh

# Na lokalnym komputerze - wyczyść stare połączenia
rm -rf ~/.ssh/control-*

# Zrestartuj Cursor/VS Code
# Połącz się ponownie
```

---

## 📖 Więcej Informacji

- Pełna dokumentacja: `/root/SSH_AUTO_RECONNECT.md`
- Troubleshooting: `/root/VoidTracker/REMOTE_SSH_TROUBLESHOOTING.md`
- Best practices: `/root/VoidTracker/AGENT_BEST_PRACTICES.md`

---

**WAŻNE:** Po zastosowaniu tych zmian, **NIE BĘDZIESZ MUSIAŁ RESTARTOWAĆ SERWERA** przy zrywaniu połączenia. Health check automatycznie przywróci SSH, a klient automatycznie się połączy ponownie.
