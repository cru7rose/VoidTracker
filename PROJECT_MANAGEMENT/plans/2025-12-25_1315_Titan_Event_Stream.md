# Titan: Event Stream (EventEmitter Service) Implementation Plan

## Goal
Decouple `OrderService` from synchronous n8n webhook calls.
Implement a dedicated `event-emitter-service` that consumes Kafka events (e.g., `ORDER_STATUS_CHANGED`, `DELIVERY_CONFIRMED`) and forwards them to external endpoints (n8n/Webhooks) asynchronously.

## User Review Required
> [!IMPORTANT]
> **Decoupling Strategy**: We are moving logic from `WebhookService` inside `order-service` to a standalone listener.
> **Topology**: `OrderService` -> (db transaction) -> `Outbox` -> (Debezium/Poller) -> `Kafka` -> **`EventEmitter`** -> `n8n`.
> *Note: For MVP, we are using the existing "Transactional Outbox" which writes to `outbox_events` table, and we assume an existing mechanism (or we implement a simple poller) pushes to Kafka. The new service consumes from Kafka.*

## Proposed Changes

### [NEW] [event-emitter-service](file:///modules/titan/event-emitter-service)
A Spring Boot application in `/modules/titan/event-emitter-service`.

#### 1. Configuration
*   `application.yml`: Kafka Broker URL, n8n Base URL.

#### 2. Kafka Consumer
*   **Listener**: Listens to `danxils-orders-events` (or specific topics like `orders.status-changed`).
*   **EventPayload**: Generic JSON handling to be forwarded.

#### 3. Webhook Logic
*   **Workflow Routing**: Logic to determine *which* n8n workflow to trigger based on event type or payload content (e.g., `AssetDefinition.workflowId` present in the event).

### [Refactor] Nexus / Order Service
*   **Deprecate**: `WebhookService.triggerWorkflow` usage in `OrderService`.
*   **Ensure**: All necessary data (like `workflowId`) is included in the Kafka Event payload.

## Verification Plan

### Automated Tests
*   **Integration Test**: Publish dummy event to embedded Kafka -> Verify `MockWebServer` receives POST request matching the event.
