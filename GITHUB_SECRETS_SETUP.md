# 🔐 GitHub Secrets Setup - Szczegółowa Instrukcja

## 📋 Overview

Ten przewodnik krok po kroku pokazuje jak skonfigurować GitHub Secrets dla automatycznego deployu z GitHub Actions na serwer.

## 🎯 Wymagane Secrets

| Secret Name | Opis | Przykład |
|------------|------|----------|
| `DEPLOY_SSH_KEY` | Prywatny klucz SSH | Zawartość `~/.ssh/id_ed25519` |
| `DEPLOY_SSH_HOST` | Adres serwera | `your-server.com` lub IP |
| `DEPLOY_SSH_USER` | Użytkownik SSH | `root` |
| `DEPLOY_SSH_PORT` | Port SSH (opcjonalnie) | `22` |
| `DEPLOY_REMOTE_BASE` | Ścieżka na serwerze (opcjonalnie) | `/root/VoidTracker` |

---

## 📝 Krok 1: Wygeneruj Klucz SSH dla GitHub Actions

### 1.1. Na serwerze - wygeneruj nowy klucz

```bash
# Przejdź do katalogu projektu
cd /root/VoidTracker

# Wygeneruj klucz SSH dedykowany dla GitHub Actions
ssh-keygen -t ed25519 -C "github-actions-deploy" -f ~/.ssh/github_actions_deploy -N ""

# Sprawdź czy klucz został utworzony
ls -la ~/.ssh/github_actions_deploy*
```

**Oczekiwany output:**
```
Generating public/private ed25519 key pair.
Your identification has been saved in /root/.ssh/github_actions_deploy
Your public key has been saved in /root/.ssh/github_actions_deploy.pub
```

### 1.2. Dodaj klucz publiczny do authorized_keys (jeśli potrzebne)

```bash
# Sprawdź czy authorized_keys istnieje
cat ~/.ssh/authorized_keys

# Dodaj klucz publiczny (jeśli chcesz używać tego samego klucza do logowania)
cat ~/.ssh/github_actions_deploy.pub >> ~/.ssh/authorized_keys
```

**Uwaga:** Jeśli już masz dostęp SSH do serwera, ten krok jest opcjonalny.

### 1.3. Wyświetl klucz prywatny (będzie potrzebny w GitHub Secrets)

```bash
# Wyświetl klucz prywatny - SKOPIUJ CAŁĄ ZAWARTOŚĆ
cat ~/.ssh/github_actions_deploy
```

**Przykładowy output:**
```
-----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW
...
(może być dłuższy)
...
-----END OPENSSH PRIVATE KEY-----
```

⚠️ **WAŻNE:** 
- Skopiuj **CAŁĄ** zawartość od `-----BEGIN` do `-----END`
- To jest klucz **prywatny** - nie udostępniaj go publicznie!

---

## 📝 Krok 2: Sprawdź Informacje o Serwerze

### 2.1. Sprawdź adres serwera

```bash
# Sprawdź IP serwera
hostname -I

# Lub sprawdź hostname
hostname -f

# Lub sprawdź w konfiguracji
cat /etc/hostname
```

**Przykładowy output:**
```
192.168.1.100
```

### 2.2. Sprawdź użytkownika

```bash
whoami
```

**Przykładowy output:**
```
root
```

### 2.3. Sprawdź ścieżkę projektu

```bash
pwd
```

**Przykładowy output:**
```
/root/VoidTracker
```

### 2.4. Sprawdź port SSH

```bash
# Sprawdź na jakim porcie działa SSH
ss -tlnp | grep sshd

# Lub sprawdź w konfiguracji
grep Port /etc/ssh/sshd_config | grep -v "^#"
```

**Domyślnie:** `22`

---

## 📝 Krok 3: Dodaj Secrets do GitHub

### 3.1. Otwórz GitHub Repository Settings

1. Przejdź do repozytorium: https://github.com/cru7rose/VoidTracker
2. Kliknij **Settings** (w górnym menu)
3. W lewym menu kliknij **Secrets and variables**
4. Kliknij **Actions**

### 3.2. Dodaj Secret: DEPLOY_SSH_KEY

1. Kliknij **New repository secret** (zielony przycisk)
2. **Name:** `DEPLOY_SSH_KEY`
3. **Secret:** Wklej zawartość klucza prywatnego z kroku 1.3
   - Skopiuj **CAŁĄ** zawartość od `-----BEGIN` do `-----END`
   - Wklej w pole "Secret"
4. Kliknij **Add secret**

**Przykład:**
```
Name: DEPLOY_SSH_KEY
Secret: -----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW
...
-----END OPENSSH PRIVATE KEY-----
```

### 3.3. Dodaj Secret: DEPLOY_SSH_HOST

1. Kliknij **New repository secret**
2. **Name:** `DEPLOY_SSH_HOST`
3. **Secret:** Adres serwera (IP lub hostname)
   - Użyj wartości z kroku 2.1
   - Przykład: `192.168.1.100` lub `your-server.com`
4. Kliknij **Add secret**

**Przykład:**
```
Name: DEPLOY_SSH_HOST
Secret: 192.168.1.100
```

### 3.4. Dodaj Secret: DEPLOY_SSH_USER

1. Kliknij **New repository secret**
2. **Name:** `DEPLOY_SSH_USER`
3. **Secret:** Użytkownik SSH
   - Użyj wartości z kroku 2.2
   - Przykład: `root`
4. Kliknij **Add secret**

**Przykład:**
```
Name: DEPLOY_SSH_USER
Secret: root
```

### 3.5. Dodaj Secret: DEPLOY_SSH_PORT (Opcjonalnie)

1. Kliknij **New repository secret**
2. **Name:** `DEPLOY_SSH_PORT`
3. **Secret:** Port SSH
   - Użyj wartości z kroku 2.4
   - Domyślnie: `22`
   - Jeśli używasz domyślnego portu, możesz pominąć ten secret
4. Kliknij **Add secret**

**Przykład:**
```
Name: DEPLOY_SSH_PORT
Secret: 22
```

### 3.6. Dodaj Secret: DEPLOY_REMOTE_BASE (Opcjonalnie)

1. Kliknij **New repository secret**
2. **Name:** `DEPLOY_REMOTE_BASE`
3. **Secret:** Ścieżka do projektu na serwerze
   - Użyj wartości z kroku 2.3
   - Domyślnie: `/root/VoidTracker`
   - Jeśli używasz domyślnej ścieżki, możesz pominąć ten secret
4. Kliknij **Add secret**

**Przykład:**
```
Name: DEPLOY_REMOTE_BASE
Secret: /root/VoidTracker
```

---

## ✅ Krok 4: Weryfikacja

### 4.1. Sprawdź czy wszystkie secrets są dodane

W GitHub: **Settings → Secrets and variables → Actions**

Powinieneś zobaczyć:
- ✅ `DEPLOY_SSH_KEY`
- ✅ `DEPLOY_SSH_HOST`
- ✅ `DEPLOY_SSH_USER`
- ✅ `DEPLOY_SSH_PORT` (opcjonalnie)
- ✅ `DEPLOY_REMOTE_BASE` (opcjonalnie)

### 4.2. Test SSH Connection (Opcjonalnie)

Możesz przetestować czy klucz działa:

```bash
# Na serwerze - sprawdź czy klucz jest poprawny
ssh-keygen -l -f ~/.ssh/github_actions_deploy.pub

# Test połączenia (jeśli masz dostęp do GitHub Actions runner)
# (Ten test wykonasz po pierwszym uruchomieniu workflow)
```

### 4.3. Test Workflow

1. Push zmian do GitHub:
   ```bash
   cd /root/VoidTracker
   ./scripts/git-push-from-server.sh "test: Verify GitHub Secrets"
   ```

2. Sprawdź GitHub Actions:
   - Przejdź do: https://github.com/cru7rose/VoidTracker/actions
   - Kliknij na najnowszy workflow run
   - Sprawdź czy "Deploy to Server" job przechodzi

3. Jeśli widzisz błędy SSH:
   - Sprawdź czy `DEPLOY_SSH_KEY` jest poprawnie skopiowany
   - Sprawdź czy `DEPLOY_SSH_HOST` jest poprawny
   - Sprawdź czy klucz publiczny jest w `authorized_keys` (jeśli potrzebne)

---

## 🔍 Troubleshooting

### Problem: "Permission denied (publickey)"

**Przyczyny:**
- Klucz prywatny niepoprawnie skopiowany
- Klucz publiczny nie dodany do `authorized_keys`
- Zły użytkownik (`DEPLOY_SSH_USER`)

**Rozwiązanie:**
1. Sprawdź czy klucz prywatny w GitHub Secrets jest kompletny
2. Dodaj klucz publiczny do `authorized_keys`:
   ```bash
   cat ~/.ssh/github_actions_deploy.pub >> ~/.ssh/authorized_keys
   ```
3. Sprawdź uprawnienia:
   ```bash
   chmod 600 ~/.ssh/authorized_keys
   chmod 700 ~/.ssh
   ```

### Problem: "Connection refused"

**Przyczyny:**
- Zły adres serwera (`DEPLOY_SSH_HOST`)
- Zły port (`DEPLOY_SSH_PORT`)
- Firewall blokuje połączenie

**Rozwiązanie:**
1. Sprawdź czy serwer jest dostępny:
   ```bash
   # Z innego komputera
   ssh -p 22 root@your-server-ip
   ```
2. Sprawdź firewall:
   ```bash
   # Na serwerze
   sudo ufw status
   sudo ufw allow 22/tcp
   ```

### Problem: "Host key verification failed"

**Rozwiązanie:**
- GitHub Actions używa `StrictHostKeyChecking=no` w workflow
- To powinno być automatycznie obsłużone

### Problem: "No such file or directory" podczas deployu

**Przyczyny:**
- Zła ścieżka (`DEPLOY_REMOTE_BASE`)
- Katalogi nie istnieją

**Rozwiązanie:**
1. Sprawdź ścieżkę:
   ```bash
   ls -la /root/VoidTracker/modules/nexus/iam-service/target
   ```
2. Utwórz brakujące katalogi:
   ```bash
   mkdir -p /root/VoidTracker/modules/nexus/iam-service/target
   mkdir -p /root/VoidTracker/modules/nexus/order-service/target
   mkdir -p /root/VoidTracker/modules/flux/planning-service/target
   ```

---

## 📋 Checklist

Przed pierwszym deployem upewnij się że:

- [ ] Klucz SSH wygenerowany (`~/.ssh/github_actions_deploy`)
- [ ] Klucz publiczny dodany do `authorized_keys` (jeśli potrzebne)
- [ ] `DEPLOY_SSH_KEY` dodany do GitHub Secrets
- [ ] `DEPLOY_SSH_HOST` dodany do GitHub Secrets
- [ ] `DEPLOY_SSH_USER` dodany do GitHub Secrets
- [ ] `DEPLOY_SSH_PORT` dodany (opcjonalnie)
- [ ] `DEPLOY_REMOTE_BASE` dodany (opcjonalnie)
- [ ] Test workflow wykonany
- [ ] Deploy działa poprawnie

---

## 🎉 Gotowe!

Po skonfigurowaniu wszystkich secrets, workflow będzie automatycznie:
1. ✅ Budować serwisy w GitHub Actions
2. ✅ Deployować JAR-y na serwer
3. ✅ Restartować serwisy
4. ✅ Weryfikować health

**Następny krok:**
```bash
./scripts/git-push-from-server.sh "feat: First automated deploy"
```

---

## 📚 Related Documentation

- [FULL_CICD_WORKFLOW.md](FULL_CICD_WORKFLOW.md) - Pełny workflow
- [CICD_QUICK_START.md](CICD_QUICK_START.md) - Szybki start
- [.github/CICD_SETUP.md](.github/CICD_SETUP.md) - Konfiguracja CI/CD
