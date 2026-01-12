# Order Service Kafka Configuration Fix

**Data:** 2025-12-29 19:28  
**Status:** ✅ Resolved  
**Priorytet:** Krytyczny

## Problem

Order-service była w pętli crash loop z powodu błędnej konfiguracji Kafka:

### Objawy
- Serwis restartował się co kilka sekund
- Logi pokazywały: `Connection to node (localhost/127.0.0.1:9094) could not be established`
- Kafka consumer nie mógł się połączyć z brokerem

### Główna Przyczyna

**Niezgodność nazw zmiennych środowiskowych:**
- `application.yml` używał: `${KAFKA_BOOTSTRAP_SERVERS:localhost:9093}`
- Docker Compose ustawiał: `SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092`
- Spring Boot wymagał prefiksu `SPRING_` dla auto-konfiguracji

W rezultacie aplikacja używała wartości domyślnej `localhost:9093` zamiast `kafka:9092`, co powodowało błąd połączenia wewnątrz kontenera Docker.

## Rozwiązanie

### Zmiany w Kodzie

**Plik:** `modules/nexus/order-service/src/main/resources/application.yml`

```yaml
# PRZED:
kafka:
  bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9093}

# PO:
kafka:
  bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:kafka:9092}
```

### Dodatkowe Problemy Znalezione

Podczas debugowania odkryto również:

1. **Nieistniejąca baza danych**
   - Aplikacja oczekiwała `vt_order_service` ale istniała tylko `orders_db`
   - Rozwiązanie: Utworzono bazę `voidtracker_orders` zgodnie z `.env`

2. **Brakująca kolumna w schemacie**
   - Tabela `vt_addresses` nie miała kolumny `legacy_id`
   - Rozwiązanie: `ALTER TABLE vt_addresses ADD COLUMN legacy_id VARCHAR(255);`

## Kroki Wdrożenia

1. Zaktualizowano `application.yml` z poprawną zmienną środowiskową
2. Przebudowano serwis: `mvn clean package -DskipTests`
3. Przebudowano obraz Docker: `docker-compose build order-service`
4. Zrestartowano serwis: `docker-compose up -d order-service`
5. Zweryfikowano poprawne uruchomienie

## Weryfikacja

```bash
# Sprawdź status serwisu
docker ps | grep order-service
# Powinno pokazać "Up X seconds/minutes" (stabilnie)

# Sprawdź logi
docker logs order-service 2>&1 | grep "Started OrderServiceApplication"

# Test API
curl http://localhost:8091/api/orders
# Powinno zwrócić pustą listę [] lub poprawny JSON
```

## Wpływ

- **Czas przestoju:** ~56 minut (od pierwszego błędu do rozwiązania)
- **Usługi dotknięte:** order-service
- **Zależności:** Odblokowało możliwość testowania route optimization w planning-service

## Lekcje Wyniesione

1. **Konwencje nazewnictwa Spring Boot:** Zawsze używaj prefiksu `SPRING_` dla zmiennych środowiskowych Spring Boot
2. **Weryfikacja środowiska Docker:** Sprawdzaj zmienne środowiskowe w kontenerze: `docker exec <container> env`
3. **Spójność konfiguracji:** `.env`, `docker-compose.yml`, i `application.yml` muszą używać tych samych nazw zmiennych

## Powiązane Pliki

- [`application.yml`](file:///Users/VoidTracker/modules/nexus/order-service/src/main/resources/application.yml#L25-L32)
- [`.env`](file:///Users/VoidTracker/.env#L16)
- [`docker-compose.yml`](file:///Users/VoidTracker/docker-compose.yml#L29-L42)

## Następne Kroki

- [ ] Przetestować tworzenie zamówień przez API
- [ ] Wstrzyknąć batch testowych zamówień dla planning-service
- [ ] Zweryfikować flow optymalizacji tras
