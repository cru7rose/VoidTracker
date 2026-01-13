# 🚀 CI/CD Setup Guide - GitHub Actions

## 📋 Overview

GitHub Actions automatycznie buduje i deployuje serwisy VoidTracker po push do `main` lub `develop`.

## 🔧 Setup (First Time)

### 1. Configure GitHub Secrets

W repozytorium GitHub: **Settings → Secrets and variables → Actions**

Dodaj następujące secrets:

| Secret Name | Description | Example |
|------------|-------------|---------|
| `DEPLOY_SSH_KEY` | Private SSH key do serwera | Zawartość `~/.ssh/id_rsa` |
| `DEPLOY_SSH_HOST` | Adres serwera | `your-server.com` |
| `DEPLOY_SSH_USER` | Użytkownik SSH | `root` |
| `DEPLOY_SSH_PORT` | Port SSH (opcjonalnie) | `22` |
| `DEPLOY_REMOTE_BASE` | Ścieżka na serwerze (opcjonalnie) | `/root/VoidTracker` |

### 2. Generate SSH Key (if needed)

```bash
# Na lokalnym komputerze
ssh-keygen -t ed25519 -C "github-actions-deploy" -f ~/.ssh/github_deploy

# Skopiuj publiczny klucz na serwer
ssh-copy-id -i ~/.ssh/github_deploy.pub user@your-server.com

# Skopiuj zawartość prywatnego klucza do GitHub Secret DEPLOY_SSH_KEY
cat ~/.ssh/github_deploy
```

### 3. Test SSH Connection

```bash
# Testuj połączenie
ssh -i ~/.ssh/github_deploy user@your-server.com "echo 'Connection OK'"
```

## 🎯 Workflows

### `build-and-deploy.yml`
**Kiedy się uruchamia:**
- Push do `main` lub `develop` (tylko zmiany w `modules/`)
- Manual trigger (workflow_dispatch)

**Co robi:**
1. ✅ Buduje wszystkie serwisy (IAM, Order, Planning)
2. ✅ Uploaduje JAR-y jako artifacts
3. ✅ Deployuje na serwer przez SSH/SCP
4. ✅ Tworzy backup starych JAR-ów

**Manual trigger:**
```bash
# W GitHub: Actions → Build and Deploy Services → Run workflow
# Możesz wybrać:
# - Services to deploy (iam,order,planning)
# - Skip tests (true/false)
# - Deploy to server (true/false)
```

### `build-only.yml`
**Kiedy się uruchamia:**
- Pull Request do `main` lub `develop`
- Manual trigger

**Co robi:**
1. ✅ Buduje wszystkie serwisy
2. ✅ Uploaduje JAR-y jako artifacts (do pobrania)
3. ❌ **NIE deployuje** (tylko build)

## 📦 Workflow Details

### Build Process

1. **Checkout code** - Pobiera kod z repozytorium
2. **Setup Java 21** - Instaluje JDK i konfiguruje Maven cache
3. **Build danxils-commons** - Buduje zależność (wymagane)
4. **Build services** - Buduje każdy serwis równolegle (matrix strategy)
5. **Upload artifacts** - Zapisuje JAR-y do pobrania

### Deploy Process

1. **Download artifacts** - Pobiera zbudowane JAR-y
2. **Setup SSH** - Konfiguruje SSH key
3. **Test connection** - Sprawdza połączenie z serwerem
4. **Create backup** - Tworzy backup starych JAR-ów
5. **Deploy JARs** - Przesyła nowe JAR-y przez SCP
6. **Summary** - Pokazuje podsumowanie i następne kroki

## 🔄 Usage

### Automatic Deploy (on push to main)

```bash
# Po prostu push do main
git push origin main

# GitHub Actions automatycznie:
# 1. Zbuduje serwisy
# 2. Zdeployuje na serwer
# 3. Wyśle notification
```

### Manual Deploy

1. Przejdź do **Actions** w GitHub
2. Wybierz **Build and Deploy Services**
3. Kliknij **Run workflow**
4. Wybierz:
   - Branch: `main` lub `develop`
   - Services: `iam,order,planning` (lub wybrane)
   - Skip tests: `true`
   - Deploy: `true`
5. Kliknij **Run workflow**

### After Deploy

Po deployu, na serwerze zrestartuj serwisy:

```bash
ssh user@your-server.com
cd /root/VoidTracker

# Restart services (używa już przesłanych JAR-ów)
SKIP_BUILD=1 ./stop-iam.sh && SKIP_BUILD=1 ./start-iam.sh
SKIP_BUILD=1 ./stop-order.sh && SKIP_BUILD=1 ./start-order.sh
SKIP_BUILD=1 ./stop-planning.sh && SKIP_BUILD=1 ./start-planning.sh
```

## 🔐 Security

### Best Practices

1. **Never commit secrets** - Używaj tylko GitHub Secrets
2. **Use SSH keys** - Nie używaj haseł
3. **Limit access** - SSH key powinien mieć minimalne uprawnienia
4. **Rotate keys** - Regularnie zmieniaj klucze SSH

### SSH Key Permissions

Na serwerze, upewnij się że klucz ma odpowiednie uprawnienia:

```bash
# Na serwerze
chmod 700 ~/.ssh
chmod 600 ~/.ssh/authorized_keys
```

## 🐛 Troubleshooting

### Build Fails

**Problem:** Build nie przechodzi
```bash
# Sprawdź logi w GitHub Actions
# Częste przyczyny:
# - Brak zależności (danxils-commons)
# - Błędy kompilacji
# - Testy nie przechodzą
```

**Rozwiązanie:**
- Sprawdź logi w Actions → Job → Step
- Uruchom build lokalnie: `mvn clean package -DskipTests`

### Deploy Fails

**Problem:** SSH connection failed
```bash
# Sprawdź:
# 1. DEPLOY_SSH_KEY jest poprawny
# 2. DEPLOY_SSH_HOST jest dostępny
# 3. Firewall pozwala na SSH
```

**Rozwiązanie:**
- Testuj SSH lokalnie: `ssh -i key user@host`
- Sprawdź GitHub Secrets
- Sprawdź logi w Actions

### JAR Not Found

**Problem:** JAR nie został znaleziony po deployu
```bash
# Sprawdź:
# 1. Build zakończył się sukcesem
# 2. Artifact został uploadowany
# 3. SCP zakończył się sukcesem
```

**Rozwiązanie:**
- Sprawdź logi w Actions → Deploy step
- Sprawdź czy JAR istnieje na serwerze: `ls -lh /root/VoidTracker/modules/*/target/*.jar`

## 📊 Monitoring

### GitHub Actions Status

- **Green checkmark** ✅ - Build i deploy zakończone sukcesem
- **Red X** ❌ - Build lub deploy nieudany
- **Yellow circle** ⏳ - W trakcie

### Notifications

GitHub wyśle email gdy:
- Workflow się zakończy (sukces lub błąd)
- Workflow się nie powiedzie

Możesz też skonfigurować webhooks dla innych systemów.

## 🎉 Benefits

1. ✅ **Automatyzacja** - Zero manualnych kroków
2. ✅ **Consistency** - Zawsze ten sam proces
3. ✅ **History** - Pełna historia buildów i deployów
4. ✅ **Rollback** - Łatwy rollback (backup JAR-ów)
5. ✅ **No SSH Issues** - Build na GitHub runners (więcej zasobów)

## 📝 Next Steps

1. ✅ Skonfiguruj GitHub Secrets
2. ✅ Przetestuj workflow (manual trigger)
3. ✅ Push do main (automatic deploy)
4. ✅ Skonfiguruj restart serwisów (opcjonalnie przez SSH w workflow)
