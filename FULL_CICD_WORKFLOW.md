# 🚀 Pełny CI/CD Workflow - Server ↔ GitHub

## 📋 Overview

Kompletny automatyczny workflow: **Server → GitHub → Build → Deploy → Server**

Wszystko działa na serwerze, bez użycia lokalnego PC.

## 🔄 Workflow Diagram

```
┌─────────┐         ┌──────────┐         ┌─────────┐
│ Server  │  push   │  GitHub  │  build  │ Server  │
│         │────────>│          │────────>│         │
│  code   │         │ Actions  │         │  JARs   │
│         │         │          │         │         │
│         │         │  deploy  │         │ restart │
│         │<────────│          │<────────│         │
└─────────┘         └──────────┘         └─────────┘
```

## 🎯 Pełny Cykl

### 1. Server → GitHub (Push)

```bash
# Na serwerze
cd /root/VoidTracker

# Push zmian
./scripts/git-push-from-server.sh "feat: Add new feature"

# Lub ręcznie
git add .
git commit -m "feat: Add new feature"
git push origin main
```

**Co się dzieje:**
- ✅ Zmiany commitowane
- ✅ Push do GitHub
- ✅ GitHub Actions automatycznie uruchamia workflow

### 2. GitHub Actions (Build)

**Automatycznie po push:**
1. ✅ Checkout code
2. ✅ Setup Java 21
3. ✅ Build danxils-commons (dependency)
4. ✅ Build wszystkich serwisów (IAM, Order, Planning)
5. ✅ Upload JAR artifacts

**Logi dostępne w:**
- https://github.com/cru7rose/VoidTracker/actions

### 3. GitHub → Server (Deploy)

**Automatycznie po build:**
1. ✅ Download JAR artifacts
2. ✅ Backup starych JAR-ów
3. ✅ Deploy nowych JAR-ów przez SCP
4. ✅ Restart serwisów automatycznie
5. ✅ Verify health endpoints

**Co się dzieje:**
- ✅ JAR-y przesyłane na serwer
- ✅ Serwisy automatycznie restartowane
- ✅ Health checks weryfikują działanie

### 4. Server (Running)

**Serwisy działają z nowymi JAR-ami:**
- ✅ IAM Service (port 8090)
- ✅ Order Service (port 8091)
- ✅ Planning Service (port 8093)

## 🔧 Configuration

### GitHub Secrets (Wymagane)

W repozytorium: **Settings → Secrets and variables → Actions**

| Secret | Opis | Przykład |
|--------|------|----------|
| `DEPLOY_SSH_KEY` | Prywatny klucz SSH do serwera | Zawartość `~/.ssh/id_ed25519` |
| `DEPLOY_SSH_HOST` | Adres serwera | `your-server.com` |
| `DEPLOY_SSH_USER` | Użytkownik SSH | `root` |
| `DEPLOY_SSH_PORT` | Port SSH (opcjonalnie) | `22` |
| `DEPLOY_REMOTE_BASE` | Ścieżka na serwerze (opcjonalnie) | `/root/VoidTracker` |

### Setup SSH Key dla GitHub Actions

```bash
# Na serwerze - wygeneruj klucz dla GitHub Actions
ssh-keygen -t ed25519 -C "github-actions-deploy" -f ~/.ssh/github_actions_deploy

# Skopiuj publiczny klucz do authorized_keys (jeśli potrzebne)
cat ~/.ssh/github_actions_deploy.pub >> ~/.ssh/authorized_keys

# Skopiuj zawartość prywatnego klucza do GitHub Secret DEPLOY_SSH_KEY
cat ~/.ssh/github_actions_deploy
```

## 📊 Workflow Files

### `.github/workflows/build-and-deploy.yml`

**Główny workflow:**
- Build serwisów w GitHub Actions
- Deploy JAR-ów na serwer
- Automatyczny restart serwisów
- Health checks

**Kiedy się uruchamia:**
- Push do `main` lub `develop`
- Manual trigger (workflow_dispatch)

### Skrypty na serwerze

**`scripts/git-push-from-server.sh`**
- Push zmian z serwera do GitHub

**`scripts/git-sync-and-build.sh`**
- Pull zmian z GitHub
- Build serwisów lokalnie (opcjonalnie)

**`scripts/restart-services.sh`**
- Restart serwisów po deployu

## 🚀 Usage

### Pełny Cykl (Automatyczny)

```bash
# 1. Na serwerze - push zmian
./scripts/git-push-from-server.sh "feat: My changes"

# 2. GitHub Actions automatycznie:
#    - Buduje serwisy
#    - Deployuje JAR-y
#    - Restartuje serwisy
#    - Weryfikuje health

# 3. Gotowe! Serwisy działają z nowymi zmianami
```

### Manual Deploy

```bash
# W GitHub: Actions → Build and Deploy Services → Run workflow
# Wybierz:
# - Services: iam,order,planning
# - Deploy: true
# - Run workflow
```

### Restart Serwisów (Ręcznie)

```bash
# Na serwerze
./scripts/restart-services.sh iam,order,planning

# Lub pojedynczo
SKIP_BUILD=1 ./stop-iam.sh && SKIP_BUILD=1 ./start-iam.sh
```

## 📈 Monitoring

### GitHub Actions

**Status buildów:**
- https://github.com/cru7rose/VoidTracker/actions

**Logi:**
- Kliknij na workflow run
- Zobacz logi dla każdego serwisu
- Sprawdź deploy status

### Server Logs

```bash
# Logi serwisów
tail -f logs/iam-service.log
tail -f logs/order-service.log
tail -f logs/planning-service.log

# Logi deployu
tail -f logs/server-sync.log
```

### Health Checks

```bash
# Sprawdź health endpoints
curl http://localhost:8090/actuator/health  # IAM
curl http://localhost:8091/actuator/health  # Order
curl http://localhost:8093/actuator/health  # Planning
```

## 🔍 Troubleshooting

### Build Fails w GitHub Actions

**Problem:** Build nie przechodzi

**Rozwiązanie:**
1. Sprawdź logi w GitHub Actions
2. Sprawdź czy danxils-commons jest zbudowany
3. Sprawdź błędy kompilacji

### Deploy Fails

**Problem:** JAR-y nie są deployowane

**Rozwiązanie:**
1. Sprawdź GitHub Secrets (DEPLOY_SSH_KEY, DEPLOY_SSH_HOST)
2. Sprawdź SSH connection w logach
3. Sprawdź czy ścieżki są poprawne

### Services Don't Restart

**Problem:** Serwisy nie restartują się po deployu

**Rozwiązanie:**
1. Sprawdź logi restart w GitHub Actions
2. Sprawdź czy skrypty start/stop działają
3. Restart ręcznie: `./scripts/restart-services.sh`

### Health Checks Fail

**Problem:** Health checks nie przechodzą

**Rozwiązanie:**
1. Sprawdź czy serwisy się uruchomiły
2. Sprawdź logi serwisów
3. Sprawdź czy porty są dostępne

## ✅ Benefits

### Dla Developmentu

1. ✅ **Pełna automatyzacja** - Zero manualnych kroków
2. ✅ **Weryfikacja** - Build w GitHub przed deployem
3. ✅ **Rollback** - Backup starych JAR-ów
4. ✅ **Historia** - Pełna historia w GitHub Actions

### Dla Agenta

1. ✅ **Wszystkie logi w GitHub** - Łatwa analiza
2. ✅ **Strukturalne dane** - Łatwe parsowanie
3. ✅ **Historia buildów** - Porównywanie
4. ✅ **Status monitoring** - Automatyczne alerty

## 🎉 Quick Start

```bash
# 1. Skonfiguruj GitHub Secrets (jednorazowo)
#    Settings → Secrets → Add secrets

# 2. Push z serwera
./scripts/git-push-from-server.sh "feat: Initial deploy"

# 3. Czekaj na GitHub Actions (sprawdź w GitHub)

# 4. Gotowe! Serwisy działają z nowymi JAR-ami
```

## 📚 Related Documentation

- [CI/CD Setup](.github/CICD_SETUP.md) - Szczegółowa konfiguracja
- [Server Git Workflow](SERVER_GIT_WORKFLOW.md) - Git workflow
- [Agent Log Analysis](AGENT_LOG_ANALYSIS.md) - Analiza logów
