# 🚀 CI/CD Automation - Quick Start

## TL;DR

GitHub Actions automatycznie buduje i deployuje serwisy po push do `main`.

## ⚡ Quick Setup (5 minutes)

### 1. Dodaj GitHub Secrets

W repozytorium: **Settings → Secrets and variables → Actions**

```
DEPLOY_SSH_KEY      = <zawartość ~/.ssh/id_rsa>
DEPLOY_SSH_HOST     = your-server.com
DEPLOY_SSH_USER     = root
DEPLOY_SSH_PORT     = 22 (opcjonalnie)
DEPLOY_REMOTE_BASE  = /root/VoidTracker (opcjonalnie)
```

### 2. Push do main

```bash
git push origin main
```

GitHub Actions automatycznie:
- ✅ Zbuduje wszystkie serwisy
- ✅ Zdeployuje JAR-y na serwer
- ✅ Wyśle notification

### 3. Restart serwisów na serwerze

```bash
ssh user@server
cd /root/VoidTracker
SKIP_BUILD=1 ./stop-iam.sh && SKIP_BUILD=1 ./start-iam.sh
SKIP_BUILD=1 ./stop-order.sh && SKIP_BUILD=1 ./start-order.sh
SKIP_BUILD=1 ./stop-planning.sh && SKIP_BUILD=1 ./start-planning.sh
```

## 📚 Full Documentation

Szczegółowa dokumentacja: [`.github/CICD_SETUP.md`](.github/CICD_SETUP.md)

## 🎯 Workflows

- **`build-and-deploy.yml`** - Build + Deploy (main/develop)
- **`build-only.yml`** - Build only (PRs)

## ✅ Benefits

- ✅ **No SSH issues** - Build na GitHub runners
- ✅ **Automatic** - Zero manual steps
- ✅ **History** - Pełna historia w GitHub Actions
- ✅ **Rollback** - Backup starych JAR-ów
