# 🧪 Test Results - Ghost PWA & Planning Service

**Data testów:** 2026-01-13  
**Tester:** Automated Test Script

---

## ✅ DZIAŁAJĄCE KOMPONENTY

### Infrastructure
- ✅ **Kafka** - Port 9094 - Działa
- ✅ **MailHog** - Port 8025 - Działa (0 wiadomości)
- ✅ **Ghost PWA** - Port 5173 - Działa
- ✅ **IAM Service** - Port 8081 - Działa (wymaga autoryzacji - 403 OK)
- ✅ **Planning Service Process** - PID 14241 - Działa
- ✅ **IAM Service Process** - PID 14043 - Działa

---

## ⚠️ PROBLEMY WYKRYTE

### 1. PostgreSQL Connection
- ❌ **Status:** Nie odpowiada na porcie 5434
- **Możliwa przyczyna:** PostgreSQL działa w Dockerze na innym porcie
- **Rozwiązanie:** Sprawdź `docker ps | grep postgres` lub użyj portu z docker-compose

### 2. Planning Service Endpoints - ClassNotFoundException
- ❌ **Route Assignments** - HTTP 401/500
- ❌ **Driver Auth Validate** - HTTP 500 (ClassNotFoundException)
- ❌ **Media Upload** - HTTP 500

**Błąd w logach:**
```
java.lang.ClassNotFoundException: org.springframework.web.servlet.handler.AbstractUrlHandlerMapping$PathExposingHandlerInterceptor
java.lang.NoClassDefFoundError: org/apache/catalina/core/ApplicationContext$DispatchData
```

**Możliwe przyczyny:**
1. Problem z zależnościami Spring Boot (wersja mismatch)
2. Problem z classpath w JAR
3. Brakujące zależności w `pom.xml`

**Rozwiązanie:**
```bash
# 1. Rebuild planning-service
cd modules/flux/planning-service
mvn clean package -DskipTests

# 2. Restart service
pkill -f planning-service
java -jar target/planning-service-*.jar --server.port=8093 > ../../logs/planning-service.log 2>&1 &
```

---

## 📊 STATYSTYKI TESTÓW

| Kategoria | Passed | Failed | Total |
|-----------|--------|--------|-------|
| Infrastructure | 2 | 1 | 3 |
| Planning Service | 0 | 3 | 3 |
| IAM Service | 1 | 1 | 2 |
| Ghost PWA | 1 | 0 | 1 |
| Processes | 2 | 0 | 2 |
| **TOTAL** | **6** | **5** | **11** |

**Success Rate:** 54.5%

---

## 🔧 REKOMENDACJE

### Priorytet 1: Napraw Planning Service
1. Sprawdź `pom.xml` - czy wszystkie zależności są poprawne
2. Rebuild JAR z `mvn clean package`
3. Sprawdź czy nie ma konfliktów wersji Spring Boot

### Priorytet 2: Sprawdź PostgreSQL
1. Sprawdź port PostgreSQL: `docker ps | grep postgres`
2. Sprawdź connection string w `application.yml`
3. Upewnij się że baza danych jest zainicjalizowana

### Priorytet 3: Testy End-to-End
Po naprawieniu problemów:
1. Utwórz testowego kierowcę w IAM
2. Utwórz testową trasę
3. Opublikuj trasę (magic link)
4. Przetestuj Ghost PWA flow end-to-end

---

## 📝 NASTĘPNE KROKI

1. **Napraw ClassNotFoundException w planning-service**
   ```bash
   cd modules/flux/planning-service
   mvn dependency:tree | grep -i "spring-web"
   mvn clean package -DskipTests
   ```

2. **Sprawdź PostgreSQL**
   ```bash
   docker ps | grep postgres
   psql -h localhost -p 5434 -U postgres -d vt_planning_service -c "SELECT 1"
   ```

3. **Uruchom ponownie testy**
   ```bash
   ./test-ghost-pwa.sh
   ```

---

## ✅ CO DZIAŁA

Pomimo problemów, następujące komponenty działają poprawnie:
- ✅ Ghost PWA uruchomione i dostępne
- ✅ IAM Service odpowiada (wymaga autoryzacji - to jest OK)
- ✅ MailHog gotowy do odbierania emaili
- ✅ Kafka gotowy do eventów
- ✅ Procesy serwisów działają

**System jest częściowo funkcjonalny - wymaga naprawy planning-service.**

---

**Ostatnia aktualizacja:** 2026-01-13
