# Nexus: Billing & Rates Engine Implementation Plan

## Goal
Implement a flexible Billing Logic Engine capable of calculating costs based on configurable Rate Cards.
Supports multiple metrics (Weight, Volume, Package Count) and billing cycles (Monthly, Semi-monthly, Ad-hoc).
Zero Hardcoding: logic driven by database configuration.

## User Review Required
> [!IMPORTANT]
> **Architecture**: New microservice `billing-service` in `modules/nexus`.
> **Pricing Logic**:
> *   **RateCard**: Defines currency, client, validity.
> *   **RateRule**: `Condition` (e.g. `weight > 10`) -> `Action` (e.g. `base: 5, per_unit: 0.5`).
> **Billing Cycle**:
> *   **Profiles**: `MONTHLY`, `SEMI_MONTHLY`, `INSTANT`.
> *   **Trigger**: Scheduler or API Event.

## Proposed Changes

### [NEW] [modules/nexus/billing-service](file:///modules/nexus/billing-service)

#### 1. Domain / Entities
*   **`RateCardEntity`**: `{ id, clientId, name, currency, validFrom, validTo, active }`
*   **`PricingRuleEntity`**:
    *   `metric`: `WEIGHT`, `VOLUME`, `COUNT`, `DISTANCE`.
    *   `rangeStart`, `rangeEnd`.
    *   `basePrice`, `unitPrice`.
*   **`BillingProfileEntity`**:
    *   `frequency`: `MONTHLY`, `WEEKLY`, `AD_HOC`.
    *   `lastRun`, `nextRun`.

#### 2. Service
*   **`PricingEngine`**:
    *   `calculatePrice(Order order)`: Finds applicable RateCard -> Rules -> Sums cost.
*   **`InvoiceGenerator`**:
    *   `generateInvoice(Client client, Cycle cycle)`: Aggregates orders -> Generates PDF/JSON.

#### 3. Controller
*   **`RateCardController`**: CRUD for Admin Panel.
*   **`BillingController`**: `GET /api/billing/preview`, `POST /api/billing/run`.

## Verification Plan

### Automated Tests
*   **Rule Logic**:
    *   Rule: 0-10kg = 5 EUR. 10kg+ = 5 EUR + 1 EUR/kg.
    *   Test 5kg -> 5 EUR.
    *   Test 12kg -> 7 EUR.
*   **Cycle Logic**:
    *   Profile: MONTHLY.
    *   Trigger -> Selects orders from last month -> Creates Invoice.
