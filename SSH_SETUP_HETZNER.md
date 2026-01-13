# 🔐 Konfiguracja SSH dla VoidTracker - Instrukcje dla Hetzner Console

## 📋 Wygenerowane Klucze SSH

Poniżej znajdują się **dwa nowe klucze publiczne**, które należy dodać w Hetzner Console:

### 1. Klucz ED25519 (Rekomendowany - nowszy, bezpieczniejszy)
```
ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIK7lVrwN26abP8Jn8jIXxOQ/cqDI+otJFzQ4tERxsCps voidtracker-ed25519-20260112
```

### 2. Klucz RSA 4096 (Kompatybilność wsteczna)
```
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQDamfeAuWyDBdCQheWwvDUaU9fJbcj26wIJB7gg4nTla2iN4OjiAWPYMSpFuS5WBRi9Q6ULtNNOD9BunevXy78/YrcGC9/cvr7dQ9g0C/AAIIvMy0MNfJyN7vXVfVAtpahzU85g8L3pIJ43VVHb78UvwxHnlnowraMV0ughXfJLbOUZKwOoyfn+trrXFh9vPuiu7EDudEh7q+mLBelwok+cvISxd12zfcSLTnVsa6qKoYmBmwboeff6lxgxcniAVUe+MYjosHafvnEoTpvOCAsR5Lwkl6sDc/Afg5vsnwNvbSo3aXi5OcsRFouaTcU0IJwIK6Iz/ESCGRlIlfLBV7ap/GhlXlqbA+47cOYaJnquLSBuLozf3irtLJihaHbnGMxbel9EEu8J/bRq2kwBGnOuolZpG8qSgYTYqqhHTwAI+d4IDs9/nMsybDTtCsZ5+Tyl8HgmwI+iKAK8TXh0G4BSGSHRWnDw/pBhQKK19MJd0Kf27MwCayq5RxVKN9lsRbz6fUErskfvjhWM3znAE+hXs+HJJCjquaIJ3cUapLdS9JCq0bgJL2+TrslN2oINbEYZkSbvqKmjnaaXkFw6uhfAKnjhwLf9M2yUxySwPkA8pa2dKpV5o4XVi0RkhLTWBKuoOI8KqEaNZbkl94EbyfkZb5PpkLwneExxB4Oy5KiowQ== voidtracker-rsa-20260112
```

---

## 🚀 Instrukcje Dodawania Kluczy w Hetzner Console

### Krok 1: Zaloguj się do Hetzner Console
1. Przejdź do [Hetzner Cloud Console](https://console.hetzner.cloud/)
2. Zaloguj się na swoje konto

### Krok 2: Dodaj Klucze SSH
1. W menu po lewej stronie kliknij **"Security"** → **"SSH Keys"**
2. Kliknij przycisk **"Add SSH Key"** lub **"New SSH Key"**
3. Wypełnij formularz:
   - **Name**: `VoidTracker ED25519` (dla pierwszego klucza)
   - **Public Key**: Wklej pierwszy klucz (ED25519) z sekcji powyżej
4. Kliknij **"Add SSH Key"**
5. Powtórz proces dla drugiego klucza:
   - **Name**: `VoidTracker RSA 4096`
   - **Public Key**: Wklej drugi klucz (RSA) z sekcji powyżej

### Krok 3: Przypisz Klucze do Serwera
1. Przejdź do **"Servers"** w menu
2. Wybierz swój serwer VoidTracker
3. Kliknij zakładkę **"SSH Keys"** lub **"Security"**
4. Upewnij się, że oba nowe klucze są zaznaczone/aktywne dla tego serwera

---

## ✅ Weryfikacja Konfiguracji

Po dodaniu kluczy w Hetzner Console, możesz zweryfikować połączenie:

```bash
# Test połączenia SSH (z lokalnego komputera)
ssh -i ~/.ssh/voidtracker_ed25519 root@YOUR_SERVER_IP

# Lub z kluczem RSA
ssh -i ~/.ssh/voidtracker_rsa root@YOUR_SERVER_IP
```

---

## 🔧 Konfiguracja VS Code Remote SSH

Jeśli używasz VS Code Remote SSH, dodaj do pliku `~/.ssh/config` na swoim lokalnym komputerze:

```ssh-config
Host voidtracker
    HostName YOUR_SERVER_IP
    User root
    IdentityFile ~/.ssh/voidtracker_ed25519
    ServerAliveInterval 60
    ServerAliveCountMax 3
    TCPKeepAlive yes
    ConnectTimeout 30
```

Następnie w VS Code:
1. Otwórz Command Palette (Ctrl+Shift+P / Cmd+Shift+P)
2. Wybierz **"Remote-SSH: Connect to Host"**
3. Wybierz **"voidtracker"** z listy

---

## 🛠️ Rozwiązywanie Problemów

### Problem: "Permission denied (publickey)"
**Rozwiązanie:**
- Sprawdź, czy klucze są poprawnie dodane w Hetzner Console
- Upewnij się, że klucze są przypisane do serwera
- Sprawdź uprawnienia: `chmod 600 ~/.ssh/authorized_keys`

### Problem: "Connection timeout"
**Rozwiązanie:**
- Sprawdź firewall w Hetzner Console (port 22 powinien być otwarty)
- Zweryfikuj, czy serwer jest uruchomiony
- Sprawdź logi: `journalctl -u ssh` na serwerze

### Problem: VS Code nie może się połączyć
**Rozwiązanie:**
- Upewnij się, że używasz poprawnej ścieżki do klucza prywatnego
- Sprawdź logi VS Code: View → Output → "Remote-SSH"
- Spróbuj połączyć się przez terminal najpierw, aby zweryfikować klucze

---

## 📝 Lokalizacja Plików na Serwerze

- **Klucze prywatne**: `/root/.ssh/voidtracker_ed25519`, `/root/.ssh/voidtracker_rsa`
- **Klucze publiczne**: `/root/.ssh/voidtracker_ed25519.pub`, `/root/.ssh/voidtracker_rsa.pub`
- **Konfiguracja SSH**: `/root/.ssh/config`
- **Authorized keys**: `/root/.ssh/authorized_keys`

---

## 🔒 Bezpieczeństwo

⚠️ **WAŻNE:**
- **NIGDY** nie udostępniaj kluczy prywatnych (pliki bez `.pub`)
- Klucze prywatne mają uprawnienia `600` (tylko właściciel może czytać)
- Regularnie rotuj klucze (co 6-12 miesięcy)
- Używaj klucza ED25519 jako głównego (nowszy, bezpieczniejszy)

---

**Data utworzenia**: 2026-01-12  
**Status**: ✅ Klucze wygenerowane i gotowe do użycia
