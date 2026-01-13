# 🔧 Fix: SSH Key Error in GitHub Actions

## ❌ Problem

```
ssh-add - Error loading key "(stdin)": error in libcrypto
```

Ten błąd występuje gdy klucz SSH w GitHub Secrets jest:
- ❌ Niepoprawnie sformatowany
- ❌ Uszkodzony podczas kopiowania
- ❌ Ma dodatkowe znaki/spacje
- ❌ Nie jest w formacie OpenSSH

## ✅ Rozwiązanie

### Krok 1: Wygeneruj nowy klucz SSH

```bash
# Na serwerze
cd /root/VoidTracker

# Usuń stary klucz (jeśli istnieje)
rm -f ~/.ssh/github_actions_deploy ~/.ssh/github_actions_deploy.pub

# Wygeneruj nowy klucz
ssh-keygen -t ed25519 -C "github-actions-deploy" -f ~/.ssh/github_actions_deploy -N ""

# Sprawdź format
file ~/.ssh/github_actions_deploy
# Powinno pokazać: "OpenSSH private key"
```

### Krok 2: Skopiuj klucz prywatny (CAŁĄ zawartość)

```bash
# Wyświetl klucz prywatny
cat ~/.ssh/github_actions_deploy
```

**WAŻNE:**
- Skopiuj **CAŁĄ** zawartość od `-----BEGIN` do `-----END`
- **NIE** dodawaj spacji na początku ani końcu
- **NIE** modyfikuj klucza
- Skopiuj dokładnie jak jest

### Krok 3: Dodaj klucz publiczny do authorized_keys

```bash
# Dodaj klucz publiczny
cat ~/.ssh/github_actions_deploy.pub >> ~/.ssh/authorized_keys

# Ustaw poprawne uprawnienia
chmod 600 ~/.ssh/authorized_keys
chmod 700 ~/.ssh
```

### Krok 4: Zaktualizuj GitHub Secret

1. Otwórz: https://github.com/cru7rose/VoidTracker/settings/secrets/actions
2. Znajdź `DEPLOY_SSH_KEY`
3. Kliknij **Update** (lub usuń i dodaj nowy)
4. **Usuń całą starą zawartość**
5. Wklej **CAŁĄ** zawartość nowego klucza prywatnego
6. **NIE** dodawaj spacji/znaków
7. Kliknij **Update secret**

### Krok 5: Weryfikacja formatu

Klucz prywatny powinien wyglądać tak:

```
-----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW
...
(długi ciąg base64)
...
-----END OPENSSH PRIVATE KEY-----
```

**NIE powinien:**
- ❌ Mieć dodatkowych spacji na początku/końcu
- ❌ Mieć znaków nowej linii w środku (oprócz naturalnych)
- ❌ Być w formacie PEM (stary format)
- ❌ Mieć komentarzy

## 🔍 Troubleshooting

### Problem: "error in libcrypto" nadal występuje

**Sprawdź:**
1. Czy klucz jest w formacie OpenSSH (nie PEM):
   ```bash
   head -1 ~/.ssh/github_actions_deploy
   # Powinno być: -----BEGIN OPENSSH PRIVATE KEY-----
   # NIE: -----BEGIN RSA PRIVATE KEY-----
   ```

2. Czy klucz nie ma dodatkowych znaków:
   ```bash
   # Sprawdź pierwsze i ostatnie linie
   head -1 ~/.ssh/github_actions_deploy
   tail -1 ~/.ssh/github_actions_deploy
   ```

3. Wygeneruj nowy klucz w formacie OpenSSH:
   ```bash
   ssh-keygen -t ed25519 -f ~/.ssh/github_actions_deploy -N ""
   ```

### Problem: "Permission denied" po naprawie klucza

**Sprawdź:**
1. Czy klucz publiczny jest w `authorized_keys`:
   ```bash
   grep -f ~/.ssh/github_actions_deploy.pub ~/.ssh/authorized_keys
   ```

2. Czy uprawnienia są poprawne:
   ```bash
   chmod 600 ~/.ssh/authorized_keys
   chmod 700 ~/.ssh
   chmod 600 ~/.ssh/github_actions_deploy
   ```

### Problem: Klucz działa lokalnie ale nie w GitHub Actions

**Możliwe przyczyny:**
1. Klucz został źle skopiowany do GitHub Secrets
2. GitHub Secrets ma limit znaków (sprawdź czy klucz jest kompletny)
3. Format klucza nie jest kompatybilny z GitHub Actions

**Rozwiązanie:**
- Użyj `ssh-keygen -t ed25519` (nowszy format)
- Sprawdź czy klucz jest kompletny (wszystkie linie)
- Skopiuj klucz bezpośrednio z terminala (nie przez edytor)

## 📋 Quick Fix Script

```bash
#!/bin/bash
# Quick fix dla SSH key error

# 1. Wygeneruj nowy klucz
ssh-keygen -t ed25519 -C "github-actions-deploy" -f ~/.ssh/github_actions_deploy -N ""

# 2. Dodaj do authorized_keys
cat ~/.ssh/github_actions_deploy.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys

# 3. Wyświetl klucz do skopiowania
echo "╔════════════════════════════════════════╗"
echo "║   SKOPIUJ TEN KLUCZ DO GITHUB SECRETS ║"
echo "╚════════════════════════════════════════╝"
echo ""
cat ~/.ssh/github_actions_deploy
echo ""
echo "⚠️  Skopiuj CAŁĄ zawartość powyżej!"
```

## ✅ Weryfikacja

Po naprawie, sprawdź:

1. **Test lokalnie:**
   ```bash
   ssh-add ~/.ssh/github_actions_deploy
   ssh-add -l
   ```

2. **Test w GitHub Actions:**
   - Push zmian
   - Sprawdź czy "Setup SSH" step przechodzi
   - Sprawdź czy "Verify SSH Key" step przechodzi

3. **Test deploy:**
   - Jeśli SSH działa, deploy powinien działać
   - Sprawdź logi w GitHub Actions

## 🎯 Najczęstsze Błędy

1. **Kopiowanie tylko części klucza** - Skopiuj CAŁĄ zawartość
2. **Dodatkowe spacje** - Nie dodawaj spacji na początku/końcu
3. **Stary format (PEM)** - Użyj `ed25519` (OpenSSH format)
4. **Brak klucza publicznego w authorized_keys** - Dodaj klucz publiczny

## 📚 Related

- [GitHub Secrets Setup](GITHUB_SECRETS_SETUP.md) - Pełna instrukcja
- [SSH Key Setup](SSH_KEY_SETUP.md) - Konfiguracja SSH
