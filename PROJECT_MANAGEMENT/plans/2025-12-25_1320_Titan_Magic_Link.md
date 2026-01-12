# Titan: One-Time Driver Identity (Magic Link) Implementation Plan

## Goal
Implement a secure, passwordless authentication mechanism for drivers ("Magic Link").
The system should generate a unique link which, when clicked, authenticates the driver and issues a 12-hour JWT.

## User Review Required
> [!NOTE]
> **Exchange Pattern**: We are using a secure 2-step process.
> 1.  **Link Generation**: A short-lived (15 min) one-time token is generated and sent to the driver (SMS/Email).
> 2.  **Exchange**: The driver clicks the link, the backend validates the one-time token, invalidates it, and returns a **12-hour JWT**.
>
> This prevents the long-lived credential (JWT) from potentially lingering in SMS history or URL logs, while ensuring the driver app works offline/statelessly for the whole shift.

## Proposed Changes

### [Component] Nexus / IAM Service

#### [MODIFY] [MagicLinkService.java](file:///modules/nexus/iam-service/src/main/java/com/example/iam/service/MagicLinkService.java)
*   **Method**: `generateDriverLink(String identifier, String contextId)`
    *   `identifier`: Phone number or Email.
    *   `contextId`: Optional Route ID or Shift ID.
*   **Logic**:
    *   Upserts a `UserEntity` with role `TEMP_DRIVER` if not exists.
    *   Generates `MagicLinkTokenEntity` (15 min TTL).
    *   Returns full URL (e.g., `https://voidtracker.com/driver/auth?token=XYZ`).

#### [MODIFY] [TokenService.java](file:///modules/nexus/iam-service/src/main/java/com/example/iam/service/TokenService.java)
*   **Update**: Allow specifying custom expiration time (12h for Drivers vs standard 1h for Admin).

#### [NEW] [AuthController.java](file:///modules/nexus/iam-service/src/main/java/com/example/iam/controller/AuthController.java)
*   `POST /auth/magic-link/generate` (Admin/System only)
*   `POST /auth/magic-link/exchange` (Public) -> Returns `LoginResponseDto` (JWT).

## Verification Plan

### Automated Tests
*   **Unit Test**: Generate Token -> Exchange Token -> Verify JWT Claims & Expiry.
*   **Security Test**: Verify token cannot be used twice.
