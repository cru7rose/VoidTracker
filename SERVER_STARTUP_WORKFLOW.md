# 🚀 Server Startup Workflow

## Po restarcie serwera

Uruchom **tylko** te dwa skrypty:

```bash
./start-sup.sh        # Infrastruktura Docker (PostgreSQL, Kafka, Neo4j, etc.)
./start-frontend.sh   # Frontend dev server (Vite)
```

## Backend Services (IAM, Order, Planning)

**NIE uruchamiaj ręcznie!** Są deployowane i restartowane **automatycznie** przez CI/CD:

1. **Push do GitHub** → GitHub Actions buduje moduły
2. **Deploy** → Zbudowane JAR-y są deployowane na serwer przez SCP
3. **Restart** → Serwisy są automatycznie restartowane (stop + start)

**GitHub Actions automatycznie:**
- Buduje JAR-y (danxils-commons, iam, order, planning)
- Deployuje JAR-y na serwer
- Restartuje serwisy (`./stop-*.sh && ./start-*.sh`)
- Restartuje frontend

**Jeśli chcesz uruchomić ręcznie** (np. po restarcie serwera, przed pierwszym deploy):

```bash
./start-iam.sh       # Sprawdza czy JAR istnieje, uruchamia
./start-order.sh     # Sprawdza czy JAR istnieje, uruchamia
./start-planning.sh  # Sprawdza czy JAR istnieje, uruchamia
```

**UWAGA:** Skrypty `start-*.sh` **NIE budują** - tylko sprawdzają czy JAR istnieje i uruchamiają. Jeśli JAR nie istnieje, pokażą błąd z linkiem do CI/CD.

## Zatrzymywanie

```bash
./stop-frontend.sh
./stop-iam.sh
./stop-order.sh
./stop-planning.sh
./stop-sup.sh        # Zatrzymuje infrastrukturę Docker
```

## Pełny restart

```bash
# 1. Zatrzymaj wszystko
./stop-all.sh

# 2. Uruchom infrastrukturę i frontend
./start-sup.sh
./start-frontend.sh

# 3. Backend services będą deployowane przez CI/CD automatycznie
#    lub uruchom ręcznie po deploy:
./start-iam.sh
./start-order.sh
./start-planning.sh
```

## Sprawdzanie statusu

```bash
# Sprawdź czy serwisy działają
ps aux | grep java
ps aux | grep node

# Sprawdź porty
netstat -tlnp | grep -E "8081|8091|8093|5173"
```

## Logi

```bash
tail -f logs/iam-service.log
tail -f logs/order-service.log
tail -f logs/planning-service.log
tail -f logs/frontend.log
```
