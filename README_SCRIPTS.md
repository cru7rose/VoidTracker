# VoidTracker Startup Scripts

## 📋 Overview

Skrypty zostały podzielone na pojedyncze segmenty dla lepszej kontroli i uniknięcia problemów z SSH podczas builda.

## 🏗️ Infrastructure

### `start-sup.sh`
**Jedyny skrypt który pozostaje w całości** - uruchamia całą infrastrukturę:
- PostgreSQL (port 5434)
- Kafka (port 9094)
- Neo4j (port 7474, 7687)
- Redis (port 6379)
- MailHog (port 8025)
- n8n (port 5678)

**Użycie:**
```bash
./start-sup.sh
```

### `stop-sup.sh`
Zatrzymuje infrastrukturę.

**Użycie:**
```bash
./stop-sup.sh          # Stop (default)
./stop-sup.sh preserve # Stop but keep containers
./stop-sup.sh nuke     # Stop + delete all data (⚠️ destructive)
```

## 🚀 Application Services

### Individual Service Scripts

Każdy serwis ma własny skrypt start/stop:

#### IAM Service
```bash
./start-iam.sh    # Start IAM Service only
./stop-iam.sh     # Stop IAM Service only
```

#### Order Service
```bash
./start-order.sh    # Start Order Service only
./stop-order.sh     # Stop Order Service only
```

#### Planning Service
```bash
./start-planning.sh    # Start Planning Service only (SSH-safe build)
./stop-planning.sh     # Stop Planning Service only
```

**Uwaga:** `start-planning.sh` używa SSH-safe build settings (ograniczona równoległość Mavena) aby zapobiec zrywaniu połączenia SSH.

#### Frontend
```bash
./start-frontend.sh    # Start Frontend (Vite) only
./stop-frontend.sh     # Stop Frontend only
```

### Convenience Wrapper

#### `start-all.sh`
Opcjonalny wrapper który uruchamia wszystkie serwisy po kolei:
```bash
./start-all.sh
```

**Uwaga:** Ten skrypt wywołuje pojedyncze skrypty, więc każdy serwis buduje się osobno (bezpieczniejsze dla SSH).

#### `stop-all.sh`
Zatrzymuje wszystkie serwisy aplikacyjne:
```bash
./stop-all.sh
```

## 🔧 Environment Variables

### `SKIP_BUILD`
Pomija build (używa istniejącego JAR):
```bash
SKIP_BUILD=1 ./start-iam.sh
SKIP_BUILD=1 ./start-order.sh
SKIP_BUILD=1 ./start-planning.sh
```

## 📝 Workflow Examples

### Start Everything
```bash
# 1. Start infrastructure
./start-sup.sh

# 2. Start all services
./start-all.sh
```

### Start Individual Services
```bash
# 1. Start infrastructure
./start-sup.sh

# 2. Start services one by one
./start-iam.sh
./start-order.sh
./start-planning.sh
./start-frontend.sh
```

### Development Workflow (SSH-safe)
```bash
# 1. Start infrastructure once
./start-sup.sh

# 2. Start services individually (each builds separately)
./start-iam.sh      # Builds IAM Service
./start-order.sh    # Builds Order Service
./start-planning.sh # Builds Planning Service (SSH-safe)
./start-frontend.sh # Sets up Frontend
```

### Restart Single Service
```bash
# Stop
./stop-planning.sh

# Start (rebuilds if source changed)
./start-planning.sh
```

## 🐛 Troubleshooting

### SSH Connection Issues During Build
Jeśli masz problemy z SSH podczas builda `planning-service`:
1. Użyj `./start-planning.sh` (ma SSH-safe build settings)
2. Lub użyj `SKIP_BUILD=1 ./start-planning.sh` jeśli JAR już istnieje

### Port Already in Use
```bash
# Check what's using the port
lsof -i :8093

# Kill the process
kill -9 <PID>
```

### Service Won't Start
```bash
# Check logs
tail -f logs/iam-service.log
tail -f logs/order-service.log
tail -f logs/planning-service.log
tail -f logs/frontend.log
```

## 📂 File Structure

```
.
├── start-sup.sh          # Infrastructure (stays as-is)
├── stop-sup.sh           # Stop infrastructure
├── start-iam.sh          # IAM Service only
├── stop-iam.sh           # Stop IAM Service
├── start-order.sh        # Order Service only
├── stop-order.sh          # Stop Order Service
├── start-planning.sh      # Planning Service only (SSH-safe)
├── stop-planning.sh       # Stop Planning Service
├── start-frontend.sh      # Frontend only
├── stop-frontend.sh       # Stop Frontend
├── start-all.sh           # Wrapper (calls all individual scripts)
└── stop-all.sh            # Stop all services
```

## 🚀 Local Build & Deploy (Recommended for SSH Issues)

Jeśli masz problemy z SSH podczas builda na serwerze, możesz budować lokalnie i deployować na serwer:

### Setup (First Time)

1. **Skonfiguruj deploy.conf:**
```bash
cp deploy.conf.example deploy.conf
# Edytuj deploy.conf i ustaw:
# - DEPLOY_SERVER_HOST (adres serwera)
# - DEPLOY_SERVER_USER (użytkownik SSH)
# - DEPLOY_SERVER_PORT (port SSH, domyślnie 22)
# - DEPLOY_SSH_KEY (ścieżka do klucza SSH, opcjonalnie)
```

2. **Upewnij się, że masz dostęp SSH:**
```bash
ssh user@your-server.com
# Powinno działać bez hasła (użyj SSH key)
```

### Usage

#### Option 1: Build & Deploy in One Command (Recommended)
```bash
./scripts/build-and-deploy.sh
```
Buduje wszystkie serwisy lokalnie i przesyła na serwer.

#### Option 2: Build Only
```bash
./scripts/build-local.sh
```
Buduje serwisy lokalnie (nie przesyła na serwer).

#### Option 3: Deploy Only
```bash
./scripts/deploy.sh
```
Przesyła już zbudowane JAR-y na serwer.

### After Deploy

Na serwerze, zrestartuj serwisy używając `SKIP_BUILD=1`:

```bash
# SSH to server
ssh user@your-server.com
cd /root/VoidTracker

# Restart services (używa już przesłanych JAR-ów)
SKIP_BUILD=1 ./stop-iam.sh && SKIP_BUILD=1 ./start-iam.sh
SKIP_BUILD=1 ./stop-order.sh && SKIP_BUILD=1 ./start-order.sh
SKIP_BUILD=1 ./stop-planning.sh && SKIP_BUILD=1 ./start-planning.sh
```

### Benefits

- ✅ **No SSH Issues** - Build na lokalnym komputerze (więcej RAM/CPU)
- ✅ **Faster Builds** - Lokalny komputer zazwyczaj szybszy niż serwer
- ✅ **Better Control** - Możesz testować lokalnie przed deployem
- ✅ **Backup** - Stare JAR-y są backupowane przed deployem

### Configuration

W `deploy.conf` możesz:
- Wybrać które serwisy deployować (`DEPLOY_IAM_SERVICE=1`)
- Ustawić równoległość builda (`BUILD_PARALLEL_THREADS="2C"`)
- Włączyć/wyłączyć backup (`DEPLOY_BACKUP_OLD_JARS=1`)

## ✅ Benefits

1. **Granular Control** - Start/stop tylko tego co potrzebujesz
2. **SSH-Safe** - Każdy serwis buduje się osobno, mniejsze obciążenie SSH
3. **Faster Iteration** - Nie musisz restartować wszystkiego, tylko zmieniony serwis
4. **Better Debugging** - Łatwiej zidentyfikować problemy w konkretnym serwisie
5. **Local Build Option** - Buduj lokalnie, deployuj na serwer (unikaj problemów SSH)
