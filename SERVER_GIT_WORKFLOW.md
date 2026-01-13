# 🔄 Server ↔ GitHub Workflow

## 📋 Overview

Pełny cykl: **Server → GitHub → Server**

1. **Server → GitHub**: Push zmian z serwera
2. **GitHub Actions**: Build i weryfikacja (logi dostępne dla agenta)
3. **Server ← GitHub**: Pull zmian i build lokalnie

## 🔄 Workflow Diagram

```
┌─────────┐         ┌──────────┐         ┌─────────┐
│ Server  │  push   │  GitHub  │  pull   │ Server  │
│         │────────>│          │<────────│         │
│         │         │ Actions  │         │         │
│         │         │  build   │         │  build  │
└─────────┘         └──────────┘         └─────────┘
     │                   │                    │
     │                   │                    │
     └───────────────────┴────────────────────┘
              Logi dostępne w GitHub
              (analiza przez agenta)
```

## 🚀 Usage

### 1. Push z serwera do GitHub

```bash
# Szybki push
./scripts/git-push-from-server.sh "feat: Add new feature"

# Lub ręcznie
git add .
git commit -m "feat: Add new feature"
git push origin main
```

**Co się dzieje:**
- ✅ Zmiany są commitowane
- ✅ Push do GitHub
- ✅ GitHub Actions automatycznie buduje serwisy
- ✅ Logi buildów są dostępne w GitHub Actions

### 2. Pull i build na serwerze

**Automatycznie (po GitHub Actions build):**
- Workflow `server-sync.yml` automatycznie triggeruje pull & build na serwerze

**Ręcznie:**
```bash
# Pull i build wszystkich serwisów
./scripts/git-sync-and-build.sh

# Pull i build wybranych serwisów
./scripts/git-sync-and-build.sh iam,order

# Pull bez builda
SKIP_BUILD=1 ./scripts/git-sync-and-build.sh
```

**Co się dzieje:**
- ✅ Pull najnowszych zmian z GitHub
- ✅ Build serwisów lokalnie na serwerze
- ✅ Logi zapisywane w `logs/server-sync.log`

## 📊 Logi i Analiza

### GitHub Actions Logs

Wszystkie logi buildów są dostępne w GitHub:
- **URL**: https://github.com/cru7rose/VoidTracker/actions
- **Dostępne dla agenta**: ✅ Tak (przez GitHub API)

**Co zawierają:**
- ✅ Logi kompilacji Maven
- ✅ Błędy buildów
- ✅ Czas buildów
- ✅ Status każdego serwisu

### Server Logs

Logi lokalnych buildów na serwerze:
```bash
# Logi sync & build
tail -f logs/server-sync.log

# Logi webhook (jeśli używasz)
tail -f logs/webhook-pull-build.log

# Logi serwisów
tail -f logs/iam-service.log
tail -f logs/order-service.log
tail -f logs/planning-service.log
```

## 🔧 Configuration

### GitHub Secrets

Dodaj do GitHub Secrets (jeśli jeszcze nie masz):
- `DEPLOY_SSH_KEY` - Klucz SSH do serwera
- `DEPLOY_SSH_HOST` - Adres serwera
- `DEPLOY_SSH_USER` - Użytkownik SSH
- `DEPLOY_SSH_PORT` - Port SSH (opcjonalnie)
- `DEPLOY_REMOTE_BASE` - Ścieżka na serwerze (opcjonalnie)

### Webhook (Opcjonalnie)

Jeśli chcesz używać webhook zamiast SSH:

1. **Skonfiguruj webhook endpoint** (np. przez nginx reverse proxy):
   ```nginx
   location /webhook/pull-build {
       proxy_pass http://localhost:8080/webhook/pull-build;
   }
   ```

2. **Dodaj secret w GitHub**:
   - `DEPLOY_WEBHOOK_URL` = `https://your-server.com/webhook/pull-build`

3. **Workflow automatycznie użyje webhook** (fallback do SSH)

## 📝 Workflow Files

### `.github/workflows/build-and-deploy.yml`
- Build serwisów w GitHub Actions
- Upload artifacts
- Deploy JAR-ów na serwer (opcjonalnie)
- Trigger server sync

### `.github/workflows/server-sync.yml`
- Automatyczny pull & build na serwerze
- Uruchamia się po sukcesie `build-and-deploy.yml`
- Można też uruchomić ręcznie

### `scripts/git-push-from-server.sh`
- Push zmian z serwera do GitHub
- Automatyczny commit wszystkich zmian

### `scripts/git-sync-and-build.sh`
- Pull zmian z GitHub
- Build serwisów lokalnie
- Logowanie do pliku

## 🎯 Benefits

### Dla Agenta (Analiza Logów)

1. ✅ **Wszystkie logi w jednym miejscu** (GitHub Actions)
2. ✅ **Dostęp przez GitHub API** (łatwa analiza)
3. ✅ **Historia buildów** (porównywanie)
4. ✅ **Strukturalne logi** (łatwe parsowanie)

### Dla Developmentu

1. ✅ **Automatyzacja** - Zero manualnych kroków
2. ✅ **Weryfikacja** - Build w GitHub przed deployem
3. ✅ **Rollback** - Łatwy powrót do poprzedniej wersji
4. ✅ **Historia** - Pełna historia zmian w Git

## 🔍 Troubleshooting

### Push nie działa

```bash
# Sprawdź autoryzację
./scripts/setup-git-push.sh token

# Lub sprawdź remote
git remote -v
```

### Pull nie działa

```bash
# Sprawdź połączenie
git fetch origin main

# Sprawdź konflikty
git status
```

### Build nie działa

```bash
# Sprawdź logi
tail -f logs/server-sync.log

# Sprawdź Maven
mvn --version

# Sprawdź zależności
cd modules/nexus/danxils-commons
mvn clean install
```

### GitHub Actions nie triggeruje server sync

1. Sprawdź czy `server-sync.yml` workflow istnieje
2. Sprawdź GitHub Secrets
3. Sprawdź logi w GitHub Actions

## 🚀 Quick Start

```bash
# 1. Push zmiany
./scripts/git-push-from-server.sh "feat: My changes"

# 2. Czekaj na GitHub Actions (sprawdź w GitHub)

# 3. Pull i build na serwerze (automatycznie lub ręcznie)
./scripts/git-sync-and-build.sh

# 4. Restart serwisów
SKIP_BUILD=1 ./stop-iam.sh && SKIP_BUILD=1 ./start-iam.sh
SKIP_BUILD=1 ./stop-order.sh && SKIP_BUILD=1 ./start-order.sh
SKIP_BUILD=1 ./stop-planning.sh && SKIP_BUILD=1 ./start-planning.sh
```

## 📚 Related Documentation

- [CI/CD Setup](.github/CICD_SETUP.md) - Szczegółowa konfiguracja CI/CD
- [Push to GitHub](PUSH_TO_GITHUB.md) - Instrukcja push
- [Scripts README](README_SCRIPTS.md) - Dokumentacja skryptów
