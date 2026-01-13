# 🚨 Naprawa Błędu w Konfiguracji SSH Klienta

## ⚠️ Problem

Z logów widzimy błąd:
```
/Users/cruz/.ssh/config: line 23: Bad configuration option: controlautoreconnect
/Users/cruz/.ssh/config: terminating, 1 bad configuration options
```

**Przyczyna:** Błędna pisownia opcji SSH - `controlautoreconnect` powinno być `ControlAutoReconnect` (z wielkimi literami).

---

## ✅ Szybka Naprawa

### Krok 1: Otwórz Konfigurację SSH

Na swoim **lokalnym komputerze** (macOS), edytuj plik:
```bash
nano ~/.ssh/config
# lub
code ~/.ssh/config
```

### Krok 2: Znajdź i Napraw Błąd

Znajdź linię z `controlautoreconnect` (prawdopodobnie linia 23) i zmień na:

**PRZED (błędne):**
```ini
controlautoreconnect yes
```

**PO (poprawne):**
```ini
ControlAutoReconnect yes
```

### Krok 3: Sprawdź Całą Konfigurację

Upewnij się, że cała sekcja dla `voidtracker` wygląda tak:

```ini
Host voidtracker
    HostName TWOJ_IP_SERWERA
    User root
    IdentityFile ~/.ssh/voidtracker_ed25519
    
    # BARDZO AGRESYWNE Keep-Alive (NAJWAŻNIEJSZE dla dużego obciążenia!)
    ServerAliveInterval 10
    ServerAliveCountMax 20
    TCPKeepAlive yes
    
    # Automatyczne reconnect - POPRAWNA PISOWNIA!
    ControlMaster auto
    ControlPath ~/.ssh/control-%h-%p-%r
    ControlPersist 2h
    ControlAutoReconnect yes
    
    # Retry
    ConnectionAttempts 10
    ConnectTimeout 60
    
    # Ważne dla port forwarding
    ExitOnForwardFailure no
    
    # Kompresja (pomaga przy dużym obciążeniu)
    Compression yes
    CompressionLevel 6
```

**UWAGA:** Zamień `TWOJ_IP_SERWERA` na prawdziwy IP serwera!

### Krok 4: Sprawdź Składnię

Przetestuj konfigurację:
```bash
ssh -F ~/.ssh/config -T voidtracker echo "Test"
```

Jeśli nie ma błędów, powinno się połączyć.

### Krok 5: Wyczyść Stare Połączenia

```bash
# Usuń stare kontrolne sockety
rm -rf ~/.ssh/control-*

# Zrestartuj Cursor/VS Code
```

---

## 🔍 Weryfikacja

### Sprawdź czy Konfiguracja jest Poprawna

```bash
# Sprawdź składnię (nie powinno być błędów)
ssh -F ~/.ssh/config -G voidtracker

# Sprawdź czy połączenie działa
ssh -v voidtracker
```

### Sprawdź Ustawienia Cursor/VS Code

1. Otwórz Settings (`Cmd + ,` na macOS)
2. Szukaj i ustaw:
   - `remote.SSH.connectTimeout` → `60`
   - `remote.SSH.serverAliveInterval` → `10`
   - `remote.SSH.serverAliveCountMax` → `20`
   - `remote.SSH.keepAlive` → `true`

3. Zrestartuj Cursor/VS Code

---

## 📋 Lista Wszystkich Poprawnych Opcji SSH

Jeśli masz inne błędy, oto poprawne pisownie opcji SSH (wszystkie z wielkimi literami):

- ✅ `ControlMaster` (nie `controlmaster`)
- ✅ `ControlPath` (nie `controlpath`)
- ✅ `ControlPersist` (nie `controlpersist`)
- ✅ `ControlAutoReconnect` (nie `controlautoreconnect`)
- ✅ `ServerAliveInterval` (nie `serveraliveinterval`)
- ✅ `ServerAliveCountMax` (nie `serveralivecountmax`)
- ✅ `TCPKeepAlive` (nie `tcpkeepalive`)
- ✅ `ConnectionAttempts` (nie `connectionattempts`)
- ✅ `ConnectTimeout` (nie `connecttimeout`)
- ✅ `ExitOnForwardFailure` (nie `exitonforwardfailure`)

---

## 🚨 Jeśli Nadal Nie Działa

### Problem: Nadal widzę błąd "Bad configuration option"

**Rozwiązanie:**
1. Sprawdź czy nie ma innych błędów w pliku:
   ```bash
   ssh -F ~/.ssh/config -G voidtracker 2>&1 | grep -i error
   ```

2. Sprawdź czy wszystkie opcje są poprawnie napisane (wielkie litery)

3. Sprawdź czy nie ma duplikatów opcji

### Problem: Połączenie nadal się zrywa przy dużym obciążeniu

**Rozwiązanie:**
1. **Zwiększ Keep-Alive jeszcze bardziej:**
   ```ini
   ServerAliveInterval 5
   ServerAliveCountMax 30
   ```

2. **Sprawdź ustawienia na serwerze:**
   ```bash
   # Na serwerze
   sudo grep -E "ClientAliveInterval|ClientAliveCountMax|MaxSessions" /etc/ssh/sshd_config
   ```

3. **Zrestartuj SSH na serwerze (jeśli zmieniono konfigurację):**
   ```bash
   # Na serwerze
   sudo systemctl restart sshd
   ```

---

## ✅ Checklist

- [ ] Błąd `controlautoreconnect` naprawiony na `ControlAutoReconnect`
- [ ] Składnia konfiguracji sprawdzona (`ssh -F ~/.ssh/config -G voidtracker`)
- [ ] Stare połączenia wyczyszczone (`rm -rf ~/.ssh/control-*`)
- [ ] Ustawienia Cursor/VS Code zaktualizowane
- [ ] Cursor/VS Code zrestartowany
- [ ] Połączenie przetestowane
- [ ] Logi sprawdzone (brak błędów)

---

**Ostatnia aktualizacja:** 2026-01-12  
**Wersja:** 1.0 (Naprawa błędu konfiguracji)
