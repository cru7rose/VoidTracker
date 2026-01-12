# LEGACY CONCEPTS ARCHIVE
> **Source:** Extracted from `projectOMS` before deletion (Dec 25, 2025).
> **Purpose:** Preserve valuable logic patterns for the new Modular Architecture.

## 1. N8N Auto-Manifest Workflow (Logic)
*Ref: N8N_WORKFLOW_GUIDE.md*
1.  **Trigger**: Daily Cron (18:00).
2.  **Order Fetch**: GET `order-service` (Status: `INGESTED`).
3.  **Matrix Calc**: POST `planning-service` with lat/lon list.
4.  **VRP Solver**: JavaScript/External optimization.
5.  **Commit**: POST manifests to `planning-service` (schema: driverId, date, vehicleId, routes[]).

## 2. Order Status Lifecycle
*Ref: OMS_ARCHITEKTURA.md*
A strict state machine for the TMS/Driver flow:
*   `PENDING` (Verification/Geocoding)
*   `NEW` (Ready for assignment)
*   `PICKUP` (Driver assigned & en-route)
*   `LOAD` (Scanned onto truck)
*   `TERM` (Scanned at Terminal/Hub - optional X-Dock step)
*   `POD` (Final Delivery - Photo/Signature)

## 3. High-Level Architecture (Gateway Pattern)
*Ref: OMS_ARCHITEKTURA.md*
```
[ERP/WMS] <---> [API Gateway] <---> [Microservices: Order, Route, Fleet, User]
                                      |--> Kafka (Internal Events)
```
*Note: New architecture favors direct Event-Driven N8N integration over a rigid Gateway for business logic.*

## 4. Tech Spec Standards
*Ref: Tech_Spec.txt*
*   **Security**: Strict HTTPS, JWT with 12h expiry (aligned with Magic Link).
*   **Validation**: Heavy emphasis on JSON Schema for API contracts (prevent "Mass Assignment" vulnerability).
