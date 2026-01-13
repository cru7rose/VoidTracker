# 🚨 Szybka Naprawa Błędu SSH - Krok po Kroku

## Problem
```
/Users/cruz/.ssh/config: line 22: Bad configuration option: controlautoreconnect
```

## ✅ Rozwiązanie (2 minuty)

### Krok 1: Otwórz plik konfiguracji
Na swoim lokalnym komputerze (macOS), uruchom:
```bash
nano ~/.ssh/config
```
LUB jeśli masz VS Code/Cursor:
```bash
code ~/.ssh/config
```

### Krok 2: Znajdź linię 22
Przewiń do linii 22 (lub użyj `Ctrl + W` w nano, aby wyszukać `controlautoreconnect`)

### Krok 3: Zmień pisownię
**PRZED (błędne - małe litery):**
```ini
controlautoreconnect yes
```

**PO (poprawne - wielkie litery):**
```ini
ControlAutoReconnect yes
```

### Krok 4: Zapisz i wyjdź
- **W nano:** `Ctrl + O` (zapisz), `Enter`, `Ctrl + X` (wyjdź)
- **W VS Code/Cursor:** `Cmd + S` (zapisz)

### Krok 5: Sprawdź składnię
```bash
ssh -F ~/.ssh/config -G voidtracker
```
**Powinno działać bez błędów!**

### Krok 6: Przetestuj połączenie
```bash
ssh -F ~/.ssh/config -T voidtracker echo "Test"
```
**Powinno wyświetlić:** `Test`

---

## 📋 Pełna Poprawna Konfiguracja (dla referencji)

Jeśli chcesz sprawdzić całą sekcję dla `voidtracker`, powinna wyglądać tak:

```ini
Host voidtracker
    HostName TWOJ_IP_SERWERA
    User root
    IdentityFile ~/.ssh/voidtracker_ed25519
    
    # BARDZO AGRESYWNE Keep-Alive
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
    
    # Kompresja
    Compression yes
    CompressionLevel 6
```

**UWAGA:** Zamień `TWOJ_IP_SERWERA` na prawdziwy IP serwera!

---

## 🔍 Jeśli Nadal Nie Działa

### Sprawdź czy nie ma innych błędów:
```bash
# Sprawdź wszystkie błędy
ssh -F ~/.ssh/config -G voidtracker 2>&1 | grep -i error

# Sprawdź składnię całego pliku
ssh -F ~/.ssh/config -T voidtracker 2>&1
```

### Sprawdź czy wszystkie opcje są poprawnie napisane:
Wszystkie opcje SSH muszą mieć **wielkie litery** na początku każdego słowa:
- ✅ `ControlAutoReconnect` (nie `controlautoreconnect`)
- ✅ `ServerAliveInterval` (nie `serveraliveinterval`)
- ✅ `ControlMaster` (nie `controlmaster`)
- ✅ `ControlPath` (nie `controlpath`)
- ✅ `ControlPersist` (nie `controlpersist`)

---

## ✅ Po Naprawie

1. **Wyczyść stare połączenia:**
   ```bash
   rm -rf ~/.ssh/control-*
   ```

2. **Zrestartuj Cursor/VS Code**

3. **Połącz się ponownie**

---

**Czas naprawy:** ~2 minuty  
**Trudność:** ⭐ (bardzo łatwe)
