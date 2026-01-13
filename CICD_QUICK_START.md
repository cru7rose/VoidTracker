# ⚡ CI/CD Quick Start

## 🚀 W 3 Krokach

### Krok 1: Skonfiguruj GitHub Secrets (jednorazowo)

W repozytorium: **Settings → Secrets and variables → Actions**

Dodaj:
```
DEPLOY_SSH_KEY      = <zawartość ~/.ssh/id_ed25519>
DEPLOY_SSH_HOST     = your-server.com
DEPLOY_SSH_USER     = root
DEPLOY_SSH_PORT     = 22 (opcjonalnie)
DEPLOY_REMOTE_BASE  = /root/VoidTracker (opcjonalnie)
```

### Krok 2: Push z serwera

```bash
cd /root/VoidTracker
./scripts/git-push-from-server.sh "feat: Initial CI/CD setup"
```

### Krok 3: Gotowe!

GitHub Actions automatycznie:
- ✅ Zbuduje serwisy
- ✅ Zdeployuje JAR-y na serwer
- ✅ Zrestartuje serwisy
- ✅ Zweryfikuje health

**Sprawdź status:**
- https://github.com/cru7rose/VoidTracker/actions

## 📊 Workflow

```
Server → GitHub → Build → Deploy → Server → Restart → ✅
```

## 🔍 Troubleshooting

**Problem:** Deploy nie działa
- Sprawdź GitHub Secrets
- Sprawdź SSH connection w logach

**Problem:** Serwisy nie restartują się
- Sprawdź logi restart w GitHub Actions
- Restart ręcznie: `./scripts/restart-services.sh`

## 📖 Pełna dokumentacja

- [FULL_CICD_WORKFLOW.md](FULL_CICD_WORKFLOW.md) - Szczegółowy opis
- [.github/CICD_SETUP.md](.github/CICD_SETUP.md) - Konfiguracja
