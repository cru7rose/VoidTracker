# Flux: Zone & District Mapping (Postal Codes) Implementation Plan

## Goal
Implement a Zone/District resolution engine based on Postal Codes.
Primary focus: **Poland (PL)** (e.g., "00-XXX" -> Warszawa Centrum), but designed for global support.
Used by the Routing Engine to assign orders to specific districts/teams before optimization.

## User Review Required
> [!TIP]
> **Resolution Strategy**:
> 1.  **Exact Match**: Check if full code (e.g., "00-950") is defined.
> 2.  **Range/Prefix**: Check if code falls within a defined range (e.g., "00-000" to "00-999").
> 3.  **Fallback**: Default zone for the Country/City.

## Proposed Changes

### [MODIFY] [modules/flux/planning-service](file:///modules/flux/planning-service)

#### 1. Domain / Entities
*   **[NEW] `ZoneDefinitionEntity`**:
    *   `code`: "WAW-Z1"
    *   `name`: "Warszawa Centrum"
    *   `countryCode`: "PL"
*   **[NEW] `PostalCodeRuleEntity`**:
    *   `zoneId`: Link to Zone.
    *   `postalCodeStart`: "00-000"
    *   `postalCodeEnd`: "00-999" (Inclusive range).
    *   `priority`: Integer (to handle overlapping rules).

#### 2. Service
*   **[NEW] `ZoneResolutionService.java`**:
    *   `resolveZone(String country, String postalCode)`: Returns `ZoneDefinitionEntity`.
    *   Cacheable results (Postal codes rarely change).

#### 3. Controller
*   **[NEW] `ZoneController.java`**:
    *   CRUD for Zones and Rules (for Admins).
    *   `GET /api/zones/resolve?country=PL&code=00-950` (Testing).

## Verification Plan

### Automated Tests
*   **PL Logic**:
    *   Rule: "00-000" - "00-999" -> Zone A.
    *   Test "00-500" -> Returns Zone A.
    *   Test "01-000" -> Returns Null or Default.
*   **Prefix Logic**: Verify string comparison works (Lexicographical order is standard for postal codes).
