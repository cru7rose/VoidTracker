# Enterprise Entity Documentation: IAM & Topology

## 1. Architectural Overview
This system is designed as a Multi-Tenant B2B Platform. It supports complex organizational hierarchies where a single B2B customer (e.g., "Sony Poland") can manage multiple logistic hubs, retail points, and users with distinct permissions.

**Core Philosophy: "Configuration over Code"**
Nothing is hardcoded. Business rules, label formats, pickup windows, and carrier selections are defined in cascading JSON configurations stored at the Organization, Site, or User level.

### 1.1 The Hierarchy
*   **Platform (Root)**: The System Owner (You).
*   **Organization (Tenant)**: The B2B Customer (e.g., A Retailer).
*   **Site (Node)**: A physical location (Warehouse, Store, Hub) connected to an Address.
*   **User (Agent)**: An entity executing actions within the scope of an Org or Site.

## 2. Core Entities

### 2.1 The Organization Entity (Organization)
Represents the B2B Client. This is the root of the "OMS Hub" for that client.

*   `orgId` (UUID): Unique Tenant ID.
*   `legalName` (String): Official business name.
*   `vatId` (String): Tax identification for billing.
*   `status` (Enum): ACTIVE, SUSPENDED, ONBOARDING.
*   `configuration` (JSONB): The Dynamic Engine. Stores global settings (Default carriers, SLA rules, Label formats).

### 2.2 The Site Entity (Site)
Represents the "Set of Addresses". A B2B customer has many sites (Warehouses, Pickup points).

*   `siteId` (UUID): Unique Site ID.
*   `orgId` (UUID): FK to Organization.
*   `siteType` (Enum): WAREHOUSE, STORE, HEADQUARTERS, DROP_OFF_POINT.
*   `address` (JSONB): Structured physical address (Geo-coordinates, Street, Zip).
*   `pickupConfig` (JSONB): Specific pickup windows (e.g., "Trucks only 8am-10am").
*   `omsHubConfig` (JSONB): API Webhooks and integration settings specific to this physical location.

### 2.3 The User Entity (UserEntity)
The user is now lightweight. They are merely an identity. Their power comes from their relationships.

*   `userId` (UUID): PK Global User ID.
*   `email` (String): Unique Login credential.
*   `passwordHash` (String): Not Null BCrypt/Argon2 hash.
*   `globalStatus` (Enum): ACTIVE, LOCKED, MFA_REQUIRED.
*   `preferences` (JSONB): UI settings (Dark mode, Language, Notification density).

## 3. The Relationship Model (The "Glue")
We do not store role directly on the user anymore. We store `UserContext`. This allows a single user to be an "Admin" at "Warehouse A" but a read-only "Observer" at "Warehouse B".

### 3.1 Entity: UserOrganizationAccess
Maps a user to a B2B Organization.

```java
@Entity
@Table(name = "iam_user_org_access")
public class UserOrganizationAccess {
    
    @EmbeddedId
    private UserOrgId id; // Composite Key (userId, orgId)

    // Example: "REGIONAL_MANAGER"
    // This role is dynamic, defined in a separate Permission Table, not an Enum.
    @Column(name = "role_definition_id")
    private String roleDefinitionId; 
    
    // Configuration overrides for this specific user in this specific company
    @Column(name = "user_config_overrides", columnDefinition = "jsonb")
    private String configOverrides;
}
```

### 3.2 Entity: UserSiteAccess
Maps a user to specific Addresses (Sites) within that Organization.

```java
@Entity
@Table(name = "iam_user_site_access")
public class UserSiteAccess {
    
    @EmbeddedId
    private UserSiteId id; // Composite Key (userId, siteId)
    
    // Specific operational permissions for this site
    // e.g., CAN_PRINT_LABELS, CAN_APPROVE_DISPATCH
    @ElementCollection
    private Set<String> fineGrainedPermissions;
}
```

## 4. The Configuration Engine (No Hardcoding)
This is the most critical part for a DHL/DPD level system. We use a **Cascading Configuration Pattern**.

When the system asks: "Can this user print a label for UPS?" it checks the JSON configurations in this order:
1.  **User Level** (Does this user have a specific block/override?)
2.  **Site Level** (Does this Warehouse support UPS?)
3.  **Organization Level** (Does the B2B Contract include UPS?)
4.  **System Level** (Is UPS globally active?)

### 4.1 Example JSON Configuration (Stored in Organization.configuration)
```json
{
  "logistics": {
    "carriers": {
      "allowed": ["DHL", "INPOST", "FEDEX"],
      "default": "INPOST",
      "rules": [
        { "if_weight_gt": 30.0, "use": "DHL_FREIGHT" },
        { "if_dest_country": "DE", "use": "DHL_DE" }
      ]
    },
    "labeling": {
      "format": "ZPL_203DPI",
      "include_return_label": true,
      "custom_logo_url": "https://cdn.voidtracker.com/clients/sony/logo.png"
    }
  },
  "oms_integration": {
    "webhook_url": "https://erp.sony.com/listeners/logistics",
    "retry_policy": "EXPONENTIAL_BACKOFF",
    "events": ["SHIPMENT_CREATED", "DELIVERY_ATTEMPTED", "EXCEPTION"]
  },
  "billing": {
    "cost_center_code": "LOG_WARSAW_01",
    "currency": "PLN"
  }
}
```

## 5. Address Management (The "Set of Addresses")
B2B logistics requires strict address validation and management. We don't just store strings; we store verified locations.

### 5.1 JSON Schema for Address/Site
```json
{
  "$schema": "http://voidtracker.com/schemas/site-v1",
  "type": "object",
  "properties": {
    "siteName": { "type": "string", "example": "North Distribution Hub" },
    "contact": {
      "name": "Warehouse Manager",
      "phone": "+48 123 456 789",
      "email": "warehouse.n@client.com"
    },
    "location": {
      "street": "Logistics Ave 5",
      "city": "Warsaw",
      "zip": "00-001",
      "country": "PL",
      "geo": { "lat": 52.2297, "lon": 21.0122 }
    },
    "capabilities": {
      "has_loading_dock": true,
      "requires_tail_lift": false,
      "max_vehicle_height_meters": 4.5
    },
    "operating_hours": {
      "mon_fri": "08:00-18:00",
      "sat": "09:00-14:00"
    }
  }
}
```

## 6. API Interaction (Enterprise Level)
Enterprise APIs must handle context. The client doesn't just "Login." They "Login into an Organization."

### 6.1 Authentication Response (JWT)
The JWT contains the sub (User ID), but the Access Token allows switching contexts.

**Header Required for Operations:**
*   `X-Tenant-ID`: {orgId}
*   `X-Site-ID`: {siteId} (Optional, if operation is site-specific)

### 6.2 Example: Create Shipment (Context Aware)
`POST /api/v1/shipments`

Request:
```json
{
  "recipient": { ... },
  "parcels": [ ... ]
  // No sender address needed here! 
  // The system infers the sender address from the "X-Site-ID" header 
  // and the specific configuration of that Site.
}
```

## 7. Dynamic Role Definitions (Admin Panel)
Since we cannot hardcode roles, we provide an endpoint to define them.

**Entity: RoleDefinition**
*   `id`: logistics_manager
*   `permissions`: ["SHIPMENT_CREATE", "SHIPMENT_CANCEL", "REPORT_VIEW_FINANCIAL"]

**Admin UI**: Allows the Super Admin to create a new Role "Summer Intern" and check boxes for permissions. This saves the new Role Definition to the database, which can immediately be assigned to users.

## Key Takeaways for "Configurable Enterprise":
*   **Separation of Concerns**: Users are people. Organizations are contracts. Sites are physical places.
*   **Cascading Config**: Settings flow down from Org -> Site -> User.
*   **JSONB Columns**: Use Postgres JSONB for config columns to allow schema-less flexibility.
*   **Context Headers**: API requests must explicitly state which "Site" or "Organization" context is being acted upon.
