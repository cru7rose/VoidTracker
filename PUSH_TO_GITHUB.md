# 📤 Push do GitHub - Instrukcja

## ✅ Commit gotowy!

Commit został utworzony:
```
d757811 feat: Add GitHub Actions CI/CD workflows for automated build and deploy
```

## 🚀 Opcje Push

### Opcja 1: HTTPS (z tokenem)

```bash
# Push do GitHub
git push origin main
```

**Jeśli wymaga autoryzacji:**
1. Utwórz Personal Access Token w GitHub:
   - Settings → Developer settings → Personal access tokens → Tokens (classic)
   - Generate new token (classic)
   - Scope: `repo` (full control)
   - Skopiuj token

2. Użyj tokenu jako hasła:
   ```bash
   git push origin main
   # Username: cru7rose
   # Password: <wklej token>
   ```

### Opcja 2: SSH (zalecane)

```bash
# Zmień remote na SSH
git remote set-url origin git@github.com:cru7rose/VoidTracker.git

# Sprawdź czy masz klucz SSH
ls -la ~/.ssh/id_rsa.pub

# Jeśli nie masz, wygeneruj:
ssh-keygen -t ed25519 -C "cru7rose@github"
# Skopiuj zawartość ~/.ssh/id_ed25519.pub
# Dodaj do GitHub: Settings → SSH and GPG keys → New SSH key

# Push
git push origin main
```

### Opcja 3: GitHub CLI (najłatwiejsze)

```bash
# Zainstaluj GitHub CLI (jeśli nie masz)
# Ubuntu/Debian:
curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null
sudo apt update && sudo apt install gh

# Zaloguj się
gh auth login

# Push
git push origin main
```

## 🔐 Szybki Push (jeśli masz już skonfigurowane)

```bash
git push origin main
```

## ⚠️ Jeśli push się nie powiedzie

**Problem: "Permission denied"**
- Użyj Personal Access Token (Opcja 1)
- Lub skonfiguruj SSH (Opcja 2)

**Problem: "Repository not found"**
- Sprawdź czy masz dostęp do repozytorium
- Sprawdź czy repozytorium istnieje: https://github.com/cru7rose/VoidTracker

**Problem: "Updates were rejected"**
- Ktoś inny pushował zmiany
- Zrób pull najpierw:
  ```bash
  git pull origin main --rebase
  git push origin main
  ```

## ✅ Po udanym push

GitHub Actions automatycznie:
1. Zbuduje serwisy
2. Zdeployuje na serwer (jeśli skonfigurowane secrets)
3. Wyśle notification

Sprawdź status: https://github.com/cru7rose/VoidTracker/actions
