# 🧪 Instrukcje Testowania: Gatekeeper + WebSocket + CI/CD

## ⚡ Szybki Start (TL;DR)

1. **Utwórz `application.yml`** (już utworzony w `modules/flux/planning-service/src/main/resources/application.yml`)
2. **Push do GitHub:** `git add . && git commit -m "Add application.yml" && git push`
3. **Sprawdź GitHub Actions:** Otwórz https://github.com/[twoj-username]/VoidTracker/actions
4. **Po zakończeniu buildu:** Restart planning-service na serwerze
5. **Testuj:** Uruchom optymalizację i sprawdź logi Gatekeeper

---

## 📋 Spis Treści
1. [Budowanie planning-service przez CI/CD](#1-budowanie-planning-service-przez-cicd)
2. [Konfiguracja n8n Webhook URL](#2-konfiguracja-n8n-webhook-url)
3. [Testowanie Gatekeeper Approval Flow](#3-testowanie-gatekeeper-approval-flow)
4. [Testowanie WebSocket Live Updates](#4-testowanie-websocket-live-updates)

---

## 1. Budowanie planning-service przez CI/CD

### Krok 1.1: Sprawdź czy masz workflow GitHub Actions

```bash
# Sprawdź czy istnieje workflow
ls -la .github/workflows/
```

**Jeśli folder jest pusty lub nie ma pliku `build-and-deploy.yml`:**
- Musisz utworzyć workflow. Zobacz **Krok 1.2**.

**Jeśli workflow już istnieje:**
- Przejdź do **Kroku 1.3** (Push do GitHub).

### Krok 1.2: Utwórz workflow GitHub Actions (jeśli nie istnieje)

**UWAGA:** Jeśli workflow już istnieje, pomiń ten krok i przejdź do **Kroku 1.3**.

Utwórz plik `.github/workflows/build-and-deploy.yml` z następującą zawartością:

```yaml
name: Build and Deploy Services

on:
  push:
    branches: [ main ]
  workflow_dispatch:
    inputs:
      build_commons:
        description: 'Build danxils-commons'
        required: false
        default: 'true'
      build_iam:
        description: 'Build IAM Service'
        required: false
        default: 'true'
      build_order:
        description: 'Build Order Service'
        required: false
        default: 'true'
      build_planning:
        description: 'Build Planning Service'
        required: false
        default: 'true'

jobs:
  build-commons:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Install Parent POM
        run: |
          cd modules/nexus
          mvn install -N -DskipTests
      - name: Build danxils-commons
        run: |
          mvn clean install -DskipTests -pl modules/nexus/danxils-commons -am
      - name: Upload build logs
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: commons-build-logs
          path: modules/nexus/danxils-commons/target/*.log

  build-planning:
    runs-on: ubuntu-latest
    needs: build-commons
    if: github.event.inputs.build_planning != 'false' || github.event_name == 'push'
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Download commons artifact
        uses: actions/download-artifact@v3
        with:
          name: commons-build-logs
          path: /tmp/commons
      - name: Install Parent POM
        run: |
          cd modules/nexus
          mvn install -N -DskipTests
      - name: Install danxils-commons
        run: |
          mvn install -DskipTests -pl modules/nexus/danxils-commons -am
      - name: Build Planning Service
        run: |
          mvn clean package -DskipTests -pl modules/flux/planning-service -am
      - name: Upload JAR artifact
        uses: actions/upload-artifact@v3
        with:
          name: planning-service-jar
          path: modules/flux/planning-service/target/*.jar
      - name: Upload build logs
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: planning-build-logs
          path: modules/flux/planning-service/target/*.log

  deploy-planning:
    runs-on: ubuntu-latest
    needs: build-planning
    steps:
      - uses: actions/checkout@v3
      - name: Download JAR
        uses: actions/download-artifact@v3
        with:
          name: planning-service-jar
          path: /tmp/planning-jar
      - name: Deploy to Server
        uses: appleboy/scp-action@master
        with:
          host: ${{ secrets.DEPLOY_SSH_HOST }}
          username: ${{ secrets.DEPLOY_SSH_USER }}
          key: ${{ secrets.DEPLOY_SSH_KEY }}
          port: ${{ secrets.DEPLOY_SSH_PORT || 22 }}
          source: "/tmp/planning-jar/*.jar"
          target: "${{ secrets.DEPLOY_REMOTE_BASE || '/root/VoidTracker' }}/modules/flux/planning-service/target/"
      - name: Restart Planning Service
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.DEPLOY_SSH_HOST }}
          username: ${{ secrets.DEPLOY_SSH_USER }}
          key: ${{ secrets.DEPLOY_SSH_KEY }}
          port: ${{ secrets.DEPLOY_SSH_PORT || 22 }}
          script: |
            cd ${{ secrets.DEPLOY_REMOTE_BASE || '/root/VoidTracker' }}
            # Znajdź i zatrzymaj istniejący proces planning-service
            pkill -f "planning-service" || true
            sleep 2
            # Uruchom planning-service
            cd modules/flux/planning-service
            nohup java -jar target/planning-service-*.jar > ../../logs/planning-service.log 2>&1 &
            echo "Planning Service restarted"
```

### Krok 1.3: Push do GitHub

```bash
# Sprawdź status
git status

# Dodaj zmiany (application.yml już został utworzony)
git add modules/flux/planning-service/src/main/resources/application.yml

# Jeśli tworzyłeś workflow, dodaj też:
# git add .github/workflows/build-and-deploy.yml

# Commit
git commit -m "Add application.yml with Gatekeeper n8n webhook config"

# Push
git push origin main
```

**UWAGA:** Jeśli nie masz jeszcze workflow GitHub Actions, musisz go najpierw utworzyć (Krok 1.2) przed pushem.

### Krok 1.4: Monitoruj build w GitHub Actions

1. Otwórz https://github.com/[twoj-username]/VoidTracker/actions
2. Kliknij na najnowszy workflow run
3. Sprawdź czy build `build-planning` zakończył się sukcesem
4. Sprawdź czy `deploy-planning` wdrożył JAR na serwer

### Krok 1.5: Sprawdź czy JAR został wdrożony

```bash
# Na serwerze
ls -lh modules/flux/planning-service/target/*.jar
```

Powinieneś zobaczyć plik `planning-service-1.0.0-SNAPSHOT.jar`.

---

## 2. Konfiguracja n8n Webhook URL

### Krok 2.1: Sprawdź czy n8n działa

```bash
# Sprawdź czy n8n jest uruchomione
curl http://localhost:5678/healthz || echo "n8n nie działa"
```

Jeśli n8n nie działa, uruchom infrastrukturę:

```bash
./start-sup.sh
```

### Krok 2.2: Utwórz webhook w n8n (opcjonalnie - dla pełnego testu)

1. Otwórz http://localhost:5678 w przeglądarce
2. Utwórz nowy workflow
3. Dodaj node "Webhook" (trigger)
4. Skonfiguruj:
   - **Path:** `/webhook/gatekeeper`
   - **Method:** POST
5. Dodaj node "Code" (JavaScript) z przykładowym kodem:

```javascript
// Przykładowa odpowiedź z AI justification
const payload = $input.all()[0].json;

// Symulacja AI Agent (w produkcji użyj OpenAI/Anthropic)
const justification = `System wykrył zmianę w optymalizacji tras:
- Zmiana wyniku: ${payload.scoreChangePercent}%
- Liczba tras: ${payload.routeCount}
- Liczba przystanków: ${payload.stopCount}

Uzasadnienie: Optymalizacja została zaktualizowana w celu poprawy efektywności tras. 
Zmiany są zgodne z polityką firmy i nie wymagają dodatkowej interwencji.`;

return {
  justification: justification,
  approved: true
};
```

6. Aktywuj workflow

### Krok 2.3: Sprawdź konfigurację w application.yml

Plik `modules/flux/planning-service/src/main/resources/application.yml` powinien zawierać:

```yaml
gatekeeper:
  enabled: true
  score-threshold-percent: 20.0
  n8n:
    webhook:
      url: http://localhost:5678/webhook/gatekeeper
```

### Krok 2.4: Restart planning-service (jeśli już działa)

```bash
# Znajdź PID procesu planning-service
ps aux | grep planning-service

# Zatrzymaj proces (zastąp PID rzeczywistym PID)
kill <PID>

# Lub użyj pkill
pkill -f planning-service

# Poczekaj 2 sekundy
sleep 2

# Uruchom ponownie
cd modules/flux/planning-service
nohup java -jar target/planning-service-*.jar > ../../logs/planning-service.log 2>&1 &

# Sprawdź czy działa
tail -f ../../logs/planning-service.log
```

---

## 3. Testowanie Gatekeeper Approval Flow

### Krok 3.1: Sprawdź czy planning-service działa

```bash
# Sprawdź health endpoint
curl http://localhost:8093/actuator/health

# Sprawdź logi
tail -f logs/planning-service.log | grep -i gatekeeper
```

### Krok 3.2: Utwórz testowe zamówienia

```bash
# Użyj istniejącego skryptu lub utwórz nowe zamówienia przez API
curl -X POST http://localhost:8091/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "test-customer",
    "deliveryAddress": {
      "street": "Test Street 1",
      "city": "Warsaw",
      "postalCode": "00-001",
      "latitude": 52.23,
      "longitude": 21.01
    },
    "delivery": {
      "sla": "2026-01-14T07:00:00"
    }
  }'
```

### Krok 3.3: Uruchom optymalizację

```bash
# Wywołaj endpoint optymalizacji
curl -X POST http://localhost:8093/api/planning/optimization/optimize \
  -H "Content-Type: application/json" \
  -d '{
    "orderIds": ["order-id-1", "order-id-2"],
    "profileId": null
  }'
```

### Krok 3.4: Sprawdź logi Gatekeeper

```bash
# Monitoruj logi w czasie rzeczywistym
tail -f logs/planning-service.log | grep -i "gatekeeper\|n8n\|approval"
```

Powinieneś zobaczyć:
- `⚠️ Gatekeeper: Score change X% exceeds threshold 20%`
- `📡 Triggering n8n webhook for Gatekeeper approval: http://localhost:5678/webhook/gatekeeper`
- `✅ Received AI justification from n8n: ...`

### Krok 3.5: Testuj approval przez API

```bash
# Sprawdź czy endpoint approval działa
curl -X POST http://localhost:8093/api/planning/gatekeeper/approve \
  -H "Content-Type: application/json" \
  -d '{
    "solutionId": "test-solution-id",
    "approved": true,
    "justification": "Test approval"
  }'
```

### Krok 3.6: Testuj w UI (Vue)

1. Otwórz http://91.107.224.0:5173 (lub localhost:5173)
2. Zaloguj się jako admin
3. Przejdź do **Dispatch Board** → **Routes**
4. Uruchom optymalizację
5. Jeśli Gatekeeper wykryje znaczącą zmianę, powinien pojawić się modal **GatekeeperApprovalModal**
6. Kliknij **Zatwierdź** lub **Odrzuć**

---

## 4. Testowanie WebSocket Live Updates

### Krok 4.1: Sprawdź konfigurację WebSocket

Plik `modules/flux/planning-service/src/main/java/com/example/planning_service/config/WebSocketConfig.java` powinien zawierać:

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-planning")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
```

### Krok 4.2: Sprawdź czy frontend używa proxy

Plik `modules/web/voidtracker-web/vite.config.js` powinien zawierać:

```javascript
proxy: {
  '/api/planning/ws-planning': {
    target: 'ws://localhost:8093',
    ws: true,
    changeOrigin: true,
    secure: false
  }
}
```

### Krok 4.3: Sprawdź połączenie WebSocket w przeglądarce

1. Otwórz **DevTools** (F12)
2. Przejdź do zakładki **Network** → **WS** (WebSocket)
3. Otwórz **Dispatch Board** → **Routes**
4. Powinieneś zobaczyć połączenie WebSocket do `/api/planning/ws-planning`

### Krok 4.4: Monitoruj wiadomości WebSocket

W **DevTools Console** powinieneś zobaczyć:

```javascript
// Sprawdź czy OptimizationService łączy się
console.log("WebSocket connected");

// Sprawdź czy otrzymujesz aktualizacje
// (powinny pojawiać się podczas optymalizacji)
```

### Krok 4.5: Uruchom optymalizację i obserwuj live updates

1. Uruchom optymalizację (jak w **Kroku 3.3**)
2. W **DevTools Console** powinieneś zobaczyć:
   ```
   Received optimization update: {score: "...", solverStatus: "SOLVING", ...}
   ```
3. Na mapie (jeśli jest zaimplementowana) powinny pojawiać się **live updates tras** podczas rozwiązywania

### Krok 4.6: Sprawdź logi backendu

```bash
# Monitoruj logi WebSocket
tail -f logs/planning-service.log | grep -i "websocket\|stomp\|topic/optimization"
```

Powinieneś zobaczyć:
- `Broadcasting solution update to /topic/optimization-updates`
- `WebSocket connection established`

---

## 🔍 Troubleshooting

### Problem: Build nie działa w GitHub Actions

**Rozwiązanie:**
1. Sprawdź logi w GitHub Actions → Artifacts
2. Sprawdź czy `danxils-commons` został zbudowany przed `planning-service`
3. Sprawdź czy wszystkie zależności są w `pom.xml`

### Problem: n8n webhook nie odpowiada

**Rozwiązanie:**
1. Sprawdź czy n8n działa: `curl http://localhost:5678/healthz`
2. Sprawdź czy webhook jest aktywny w n8n UI
3. Sprawdź logi n8n: `docker logs n8n` (jeśli w Dockerze)

### Problem: WebSocket nie łączy się

**Rozwiązanie:**
1. Sprawdź czy Vite proxy jest skonfigurowane poprawnie
2. Sprawdź czy frontend używa `/api/planning/ws-planning` (nie `http://localhost:8093`)
3. Sprawdź CORS w backendzie (powinno być `setAllowedOriginPatterns("*")`)

### Problem: Gatekeeper nie wykrywa zmian

**Rozwiązanie:**
1. Sprawdź czy `gatekeeper.enabled=true` w `application.yml`
2. Sprawdź czy `score-threshold-percent` jest ustawione na rozsądną wartość (20.0)
3. Sprawdź logi: `tail -f logs/planning-service.log | grep gatekeeper`

---

## ✅ Checklist Testowania

- [ ] Planning-service zbudowany przez CI/CD
- [ ] JAR wdrożony na serwerze
- [ ] `application.yml` skonfigurowany z n8n webhook URL
- [ ] n8n działa i webhook jest aktywny
- [ ] Gatekeeper wykrywa zmiany w score
- [ ] n8n webhook jest wywoływany
- [ ] Modal approval pojawia się w UI
- [ ] WebSocket łączy się z frontendem
- [ ] Live updates są widoczne podczas optymalizacji
- [ ] Logi pokazują wszystkie kroki

---

**Data utworzenia:** 2026-01-13  
**Ostatnia aktualizacja:** 2026-01-13
