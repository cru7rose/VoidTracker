# Naprawa Problemu z Zrywaniem Połączenia SSH przy Starcie Planning-Service

## Problem
Za każdym razem, gdy uruchamiany jest `planning-service`, połączenie SSH w Cursor się zrywa. Z logów widać:
- Problemy z port forwarding dla PostgreSQL (5434) i Kafka (9094)
- Błędy "This operation was aborted" podczas próby rekonfiguracji SSH
- Wielokrotne próby ponownego połączenia

## Przyczyna
`planning-service` nie miał skonfigurowanego connection pool, co powodowało:
1. **Nadmierne połączenia do bazy danych** - domyślne ustawienia HikariCP mogą otwierać zbyt wiele połączeń jednocześnie
2. **Brak limitów dla Kafka** - nieograniczone połączenia do brokera Kafka
3. **Brak limitów dla Neo4j** - nieograniczony connection pool dla Neo4j
4. **Zbyt duży thread pool Tomcat** - domyślne ustawienia mogą przeciążać port forwarding SSH

## Rozwiązanie

### 1. Dodano konfigurację HikariCP (PostgreSQL Connection Pool)
```yaml
spring:
  datasource:
    hikari:
      connection-timeout: 30000
      maximum-pool-size: 10       # Ograniczone z domyślnych 20
      minimum-idle: 2
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
      connection-test-query: SELECT 1
      validation-timeout: 5000
```

### 2. Dodano limity dla Kafka
```yaml
spring:
  kafka:
    consumer:
      session-timeout-ms: 30000
      heartbeat-interval-ms: 10000
      max-poll-interval-ms: 300000
      max-poll-records: 50  # Limit batch size
    producer:
      request-timeout-ms: 30000
      delivery-timeout-ms: 120000
      max-in-flight-requests-per-connection: 5
```

### 3. Dodano limity dla Neo4j
```yaml
spring:
  neo4j:
    pool:
      max-connection-lifetime: 1800000
      max-connection-pool-size: 10
      connection-acquisition-timeout: 30000
```

### 4. Ograniczono Thread Pool Tomcat
```yaml
server:
  connection-timeout: 60000
  tomcat:
    threads:
      max: 50  # Zmniejszone z domyślnych 200
      min-spare: 5
    accept-count: 50
    max-connections: 200  # Limit równoczesnych połączeń
    connection-timeout: 60000
```

## Pliki Zmodyfikowane
- `modules/flux/planning-service/src/main/resources/application.yml`

## Testowanie
1. Zrestartuj planning-service
2. Sprawdź, czy połączenie SSH pozostaje stabilne
3. Monitoruj logi SSH - nie powinny pojawiać się błędy "connection closed"

## Dodatkowe Uwagi
- Jeśli problem nadal występuje, można dalej zmniejszyć `maximum-pool-size` do 5
- W przypadku problemów z Neo4j, sprawdź czy konfiguracja pool jest poprawnie aplikowana
- Monitoruj liczbę otwartych połączeń w bazie danych: `SELECT count(*) FROM pg_stat_activity;`

## Aktualizacja 2: Problem podczas Builda

### Problem
Nawet po dodaniu limitów connection pool, problem nadal występował **podczas budowy** (build) planning-service, nie podczas uruchamiania.

### Dodatkowe Rozwiązania

#### 1. Lazy Initialization Spring Boot
```yaml
spring:
  main:
    lazy-initialization: true  # Opóźnia inicjalizację beanów, w tym połączeń do bazy
```

#### 2. Dalsze Ograniczenie Connection Pool
- PostgreSQL: `maximum-pool-size: 5` (zmniejszone z 10)
- Neo4j: `max-connection-pool-size: 5` (zmniejszone z 10)
- Dodano `initialization-fail-timeout: -1` - nie failuje podczas builda jeśli baza niedostępna

#### 3. Ograniczenie Równoległości Mavena
Utworzono `.mvn/maven.config`:
```
-T 1C  # Limit do 1 wątku na rdzeń CPU
```

#### 4. Konfiguracja Spring Boot Maven Plugin
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <excludeDevtools>true</excludeDevtools>
    </configuration>
</plugin>
```

### Pliki Zmodyfikowane (Aktualizacja 2)
- `modules/flux/planning-service/src/main/resources/application.yml` - lazy initialization, dalsze limity
- `modules/flux/planning-service/pom.xml` - konfiguracja Spring Boot plugin
- `modules/flux/planning-service/src/main/java/com/example/planning_service/config/GraphConfig.java` - zmniejszony Neo4j pool
- `.mvn/maven.config` - limit równoległości Mavena

## Data
2026-01-12 (Aktualizacja 2)
