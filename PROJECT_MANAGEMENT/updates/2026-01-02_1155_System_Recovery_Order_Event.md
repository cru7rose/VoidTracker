# System Recovery Update: Order & Event Services

**Date:** 2026-01-02 11:55
**Status:** ✅ RECOVERED

## Incident Summary
System analysis revealed two critical blockers preventing the platform from operating:
1.  **Order Service Failure**: `java.lang.NoClassDefFoundError: AdditionalServiceEntity` prevented application startup.
2.  **Event Emitter Offline**: Kafka connection refused due to misconfigured bootstrap server (`localhost` vs `kafka` internal network).

## Actions Taken
### 1. Event Emitter Configuration
- **Modified**: `docker-compose.yml`
- **Change**: Updated `SPRING_KAFKA_BOOTSTRAP_SERVERS` from `kafka:9092` (mapped to host) to `kafka:29092` (internal Docker listener).
- **Result**: Service successfully discovered group coordinator and joined consumer group `event-emitter-group`.

### 2. Order Service Recovery
- **Action**: Executed `mvn clean package` to force recompilation of MapStruct mappers.
- **Result**: Build success. Service successfully started and synced with Kafka consumer group `danxils-commons`.

## Verification
- **Logs**:
    - `event-emitter-service`: `Discovered group coordinator kafka:29092` ✅
    - `order-service`: `Successfully synced group ... assignments: [routes.planned-0]` ✅
