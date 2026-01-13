# 🔍 Diagnostyka Problemów z SSH podczas Builda

## 📊 Analiza Obecnej Sytuacji

### Zasoby Serwera
```
RAM:        3.7GB total
           2.7GB used (73%)
           308MB free (8%)
           0GB swap (BRAK!)

CPU:        2 cores
Load:       1.64 (82% obciążenia dla 2-core systemu)

Dysk:       14GB/38GB użyte (38% - OK)
```

### Główne Procesy Zużywające Zasoby
1. **Java Language Server (Cursor)**: ~1.9GB RAM (49.9%!) 🔴
2. **Cursor Server**: ~350MB RAM
3. **Docker/Containers**: ~200MB RAM
4. **System**: ~500MB RAM

### Problem: Dlaczego SSH się zrywa?

#### Przyczyna Główna: **Brak Pamięci (OOM - Out of Memory)**

Podczas budowania Maven:
1. **Maven kompilacja** wymaga:
   - ~500MB-1GB RAM na proces kompilacji
   - Przy `-T 1C` (1 core) = 1 proces
   - Przy `-T 2C` (2 cores) = 2 procesy = **1-2GB RAM**

2. **Spring Boot initialization** (przed naszymi poprawkami):
   - Próbuje połączyć się z bazą danych
   - Tworzy connection pooli (HikariCP, Neo4j, Kafka)
   - Inicjalizuje beany
   - **Dodatkowe ~200-500MB RAM**

3. **Java Language Server** (Cursor):
   - Już używa **1.9GB RAM**
   - Podczas build może próbować reindeksować
   - **Dodatkowe ~200-500MB RAM**

**RAZEM: 3.7GB RAM potrzebne, a mamy tylko 3.7GB total!**

Gdy system nie ma pamięci:
- Linux OOM Killer zabija procesy
- System staje się nieodpowiedzialny
- SSH timeout (nie może wysłać keepalive)
- Połączenie się zrywa

---

## ✅ Rozwiązania (od Najlepszych do Tymczasowych)

### 🥇 **ROZWIĄZANIE 1: Zwiększyć RAM Serwera** (NAJLEPSZE)

**Wymagane minimum:**
- **8GB RAM** (rekomendowane w `DEPLOYMENT.md`)
- **4GB RAM** (minimum dla developmentu)

**Dlaczego:**
- Maven build: ~1-2GB
- Java Language Server: ~2GB
- Docker/Containers: ~500MB
- System: ~500MB
- **Buffer**: ~1-2GB
- **RAZEM: 6-8GB**

**Jak to zrobić:**
- Jeśli to VM (AWS, Azure, GCP): zwiększ typ instancji
- Jeśli to fizyczny serwer: dodaj RAM
- Jeśli to VPS: sprawdź możliwość upgrade'u

---

### 🥈 **ROZWIĄZANIE 2: Dodać Swap** (TYMCZASOWE)

Swap to "wirtualna pamięć" na dysku - wolniejsza, ale zapobiega OOM.

```bash
# Sprawdź obecny swap
free -h

# Utwórz plik swap (4GB)
sudo fallocate -l 4G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile

# Zrób to permanentne
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# Sprawdź
free -h
```

**Uwaga:** Swap jest **wolniejszy** niż RAM (dysk vs pamięć), ale zapobiega zrywaniu SSH.

---

### 🥉 **ROZWIĄZANIE 3: Ograniczyć Zużycie przez Cursor** (TYMCZASOWE)

Java Language Server używa 1.9GB RAM. Można ograniczyć:

**Opcja A: Wyłączyć Java Language Server podczas build**
```bash
# W Cursor: Settings → Extensions → Java
# Wyłącz "Java: Language Server" przed buildem
```

**Opcja B: Ograniczyć pamięć Java Language Server**
```json
// .vscode/settings.json
{
  "java.jdt.ls.java.home": "/usr/lib/jvm/java-21",
  "java.jdt.ls.vmargs": "-Xmx1g"  // Zmniejsz z 4GB do 1GB
}
```

---

### 🏅 **ROZWIĄZANIE 4: Build Lokalnie, Deploy na Serwer** (ALTERNATYWNE)

Zamiast budować na serwerze, zbuduj lokalnie i przesłać JAR:

```bash
# Lokalnie (na Twoim komputerze)
cd VoidTracker
mvn clean package -DskipTests

# Prześlij JAR na serwer
scp modules/nexus/order-service/target/order-service-*.jar user@server:/path/

# Na serwerze - użyj SKIP_BUILD
SKIP_BUILD=1 ./start-order.sh
```

---

### 🎯 **ROZWIĄZANIE 5: CI/CD Pipeline** (PRODUKCYJNE)

Użyj GitHub Actions / GitLab CI do budowania:

```yaml
# .github/workflows/build.yml
name: Build Services
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
      - run: mvn clean package -DskipTests
      - run: |
          # Upload artifacts
          # Deploy to server
```

---

### ✅ **ROZWIĄZANIE 6: Nasze Poprawki** (JUŻ ZASTOSOWANE)

Te poprawki **zmniejszają** zużycie, ale nie rozwiązują problemu całkowicie:

1. ✅ **Lazy initialization** - opóźnia inicjalizację beanów
2. ✅ **Connection pool limits** - mniej połączeń = mniej RAM
3. ✅ **Maven parallelism** - `-T 1C` = mniej równoległych procesów
4. ✅ **CommandLineRunner checks** - nie łączy się z DB podczas build

**Efekt:** Zmniejszyliśmy zużycie z ~500MB do ~200MB podczas build, ale nadal potrzebujemy więcej RAM.

---

## 📋 Rekomendacja

### Dla Developmentu (Teraz):
1. ✅ **Dodaj 4GB swap** (Rozwiązanie 2) - szybkie, łatwe
2. ✅ **Użyj naszych poprawek** (już zrobione)
3. ⚠️ **Ogranicz Java Language Server** (Rozwiązanie 3) - jeśli nadal problemy

### Dla Produkcji (Długoterminowo):
1. 🎯 **Zwiększ RAM do 8GB** (Rozwiązanie 1) - najlepsze
2. 🎯 **Użyj CI/CD** (Rozwiązanie 5) - profesjonalne
3. 🎯 **Build lokalnie** (Rozwiązanie 4) - jeśli CI/CD nie możliwe

---

## 🔧 Szybka Diagnostyka

Jeśli problem nadal występuje, sprawdź:

```bash
# 1. Sprawdź czy jest OOM
dmesg | grep -i "out of memory"
journalctl -k | grep -i "oom"

# 2. Sprawdź użycie podczas build
watch -n 1 'free -h && echo "---" && uptime'

# 3. Sprawdź procesy Maven
ps aux | grep maven

# 4. Sprawdź logi SSH
journalctl -u ssh | tail -50
```

---

## 📝 Podsumowanie

**Problem:** Serwer ma za mało RAM (3.7GB) dla:
- Java Language Server (1.9GB)
- Maven build (1-2GB)
- System + Docker (500MB)

**Rozwiązanie:** 
- **Krótkoterminowe:** Dodaj swap (4GB)
- **Długoterminowe:** Zwiększ RAM do 8GB

**Nasze poprawki** zmniejszają zużycie, ale nie rozwiązują problemu całkowicie - potrzebujemy więcej zasobów.
