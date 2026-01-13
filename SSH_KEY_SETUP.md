# 🔐 SSH Key Setup dla GitHub

## 📋 Problem: "Key is invalid. You must supply a key in OpenSSH public key format"

Ten błąd występuje gdy:
- ❌ Skopiowałeś tylko część klucza
- ❌ Dodałeś dodatkowe spacje/znaki
- ❌ Skopiowałeś klucz prywatny zamiast publicznego

## ✅ Rozwiązanie

### Krok 1: Wyświetl klucz publiczny

```bash
cat ~/.ssh/id_ed25519.pub
```

**Przykładowy output:**
```
ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIH6DDUijfthL7TtDbPp/IZpiD1JRxZI8S156Oi6QIVEH cru7rose@github
```

### Krok 2: Skopiuj CAŁĄ linię

⚠️ **WAŻNE:**
- Skopiuj **CAŁĄ** linię od `ssh-ed25519` do końca
- **NIE** dodawaj spacji na początku ani końcu
- **NIE** kopiuj klucza prywatnego (`id_ed25519` bez `.pub`)

### Krok 3: Dodaj do GitHub

1. Otwórz: https://github.com/settings/keys
2. Kliknij: **New SSH key** (zielony przycisk)
3. **Title**: `void-tracker-dev` (lub dowolna nazwa)
4. **Key type**: `Authentication Key`
5. **Key**: Wklej skopiowany klucz
   - ⚠️ Wklej TYLKO linię z kluczem
   - ⚠️ Bez dodatkowych spacji/znaków
6. Kliknij: **Add SSH key**

### Krok 4: Test połączenia

```bash
ssh -T git@github.com
```

**Oczekiwana odpowiedź:**
```
Hi cru7rose! You've successfully authenticated, but GitHub does not provide shell access.
```

Jeśli widzisz tę wiadomość - ✅ **Sukces!**

### Krok 5: Konfiguracja Git

```bash
# Zmień remote na SSH
git remote set-url origin git@github.com:cru7rose/VoidTracker.git

# Sprawdź
git remote -v

# Teraz push będzie przez SSH (bez tokenu)
git push origin main
```

## 🔍 Troubleshooting

### Problem: "Permission denied (publickey)"

**Rozwiązanie:**
1. Sprawdź czy klucz jest dodany do GitHub
2. Sprawdź czy używasz poprawnego klucza:
   ```bash
   ssh-add -l  # Lista załadowanych kluczy
   ssh-add ~/.ssh/id_ed25519  # Załaduj klucz
   ```

### Problem: "Key is invalid" nadal występuje

**Sprawdź:**
1. Czy kopiujesz klucz **publiczny** (`.pub`), nie prywatny
2. Czy skopiowałeś **całą** linię
3. Czy nie ma dodatkowych spacji/znaków

**Test formatu:**
```bash
# Klucz powinien zaczynać się od:
ssh-ed25519  # dla ed25519
ssh-rsa      # dla RSA

# I mieć format:
ssh-ed25519 [długi_klucz_base64] [email]
```

### Problem: Klucz nie działa po dodaniu

**Rozwiązanie:**
1. Usuń stary klucz z GitHub
2. Wygeneruj nowy:
   ```bash
   ssh-keygen -t ed25519 -C "cru7rose@github" -f ~/.ssh/id_ed25519
   ```
3. Dodaj nowy klucz do GitHub

## 📚 Alternatywne Klucze

Jeśli masz klucz w niestandardowej lokalizacji:

```bash
# Wyświetl klucz
cat /root/VoidTracker/git.pub

# Dodaj do ssh-agent
ssh-add /root/VoidTracker/git

# Lub użyj w SSH config
cat >> ~/.ssh/config << EOF
Host github.com
    IdentityFile /root/VoidTracker/git
EOF
```

## ✅ Weryfikacja

Po skonfigurowaniu, sprawdź:

```bash
# 1. Test SSH
ssh -T git@github.com

# 2. Sprawdź remote
git remote -v
# Powinno pokazać: git@github.com:cru7rose/VoidTracker.git

# 3. Test push
git push origin main
# Powinno działać bez pytania o hasło/token
```

## 🎉 Gotowe!

Teraz możesz używać:
- ✅ `git push` bez tokenu
- ✅ `git pull` bez tokenu
- ✅ Automatyczne workflow przez SSH
