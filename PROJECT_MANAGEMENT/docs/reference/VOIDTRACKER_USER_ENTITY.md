# User Entity Documentation

## 1. Overview
The **User Entity** (`UserEntity`) is a core component of the Identity and Access Management (IAM) service in the VoidTracker (DANXILS) system. It represents the system users, including dispatchers, drivers, and administrators, and manages their authentication credentials and access rights.

## 2. Data Structure
The entity is mapped to the `iam_users` table in the database.

### 2.1 Fields

| Field Name | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `userId` | `UUID` | Primary Key, Generated | Unique identifier for the user. |
| `username` | `String` | Unique, Not Null | Public username for login and display. |
| `password` | `String` | Not Null | Hashed password for authentication. |
| `email` | `String` | Unique, Not Null | User's email address for communication and recovery. |
| `enabled` | `boolean` | Default: `true` | Flag indicating if the account is active. |
| `roles` | `Set<UserRole>` | ElementCollection, Eager Fetch | Set of roles assigned to the user (e.g., ADMIN, DRIVER). |

### 2.2 Database Mapping
- **Table**: `iam_users`
- **Roles Table**: `iam_user_roles` (Join column: `user_id`)

## 3. Data Transfer Objects (DTOs)

### 3.1 UserResponseDto
Used for returning user details in API responses. Excludes sensitive information like passwords.

```java
public class UserResponseDto {
    private String userId;
    private String username;
    private String email;
    private boolean enabled;
    private Set<UserRole> roles;
}
```

### 3.2 UpdateUserRequestDto
Used for updating user information.

```java
public class UpdateUserRequestDto {
    private String email;
    private boolean enabled;
    private Set<UserRole> roles;
    // Password updates are handled via a separate secure endpoint
}
```

## 4. JSON Schema
The following JSON Schema defines the contract for the User Profile, compliant with the project's technical specifications (`urn:projekt:user:profile:v1`).

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "$id": "urn:projekt:user:profile:v1",
  "title": "User Profile",
  "description": "Schema for a user's public profile information.",
  "type": "object",
  "properties": {
    "userId": {
      "description": "Unique identifier for the user.",
      "type": "string",
      "format": "uuid"
    },
    "username": {
      "description": "Public username.",
      "type": "string",
      "minLength": 3,
      "maxLength": 50,
      "pattern": "^[a-zA-Z0-9_]+$"
    },
    "fullName": {
      "description": "Full name of the user.",
      "type": "string",
      "maxLength": 100
    },
    "avatarUrl": {
      "description": "URL of the user's avatar.",
      "type": "string",
      "format": "uri"
    },
    "bio": {
      "description": "A short biography of the user.",
      "type": "string",
      "maxLength": 500
    }
  },
  "required": [
    "userId",
    "username"
  ],
  "additionalProperties": false
}
```

## 5. Security Considerations
- **Password Storage**: Passwords must never be stored in plain text. They are hashed using BCrypt before persistence.
- **DTO Exposure**: The `UserEntity` is never exposed directly via APIs. `UserResponseDto` is used to ensure the password field is never serialized to the client.
- **Role-Based Access**: Access to user management endpoints is restricted based on the `roles` assigned to the authenticated user.

## 6. API Endpoints

### 6.1 Authentication & Registration
- **Public Registration**: `POST /api/auth/register`
    - Body: `{ "username": "...", "email": "...", "password": "..." }`
    - Returns: `LoginResponseDto` (JWT Tokens + User Info)
- **Login**: `POST /api/auth/login`
    - Body: `{ "username": "...", "password": "..." }`
- **Magic Link**: `POST /api/auth/magic-link/request`

### 6.2 User Management (Admin Panel)
These endpoints are secured and require `ROLE_ADMIN`.

- **List Users**: `GET /api/users`
- **Get User**: `GET /api/users/{userId}`
- **Update User**: `PUT /api/users/{userId}`
- **Delete User**: `DELETE /api/users/{userId}`
- **Change Password**: `PUT /api/users/{userId}/password`
- **Initiate Invite**: `POST /api/users/initiate-registration`

## 7. Frontend Integration
- **Registration Page**: `/customer/register`
- **Admin User List**: `/internal/users` (Accessible via Sidebar > Users)


Enterprise Entity Documentation: IAM & Topology1. Architectural OverviewThis system is designed as a Multi-Tenant B2B Platform. It supports complex organizational hierarchies where a single B2B customer (e.g., "Sony Poland") can manage multiple logistic hubs, retail points, and users with distinct permissions.Core Philosophy: "Configuration over Code"Nothing is hardcoded. Business rules, label formats, pickup windows, and carrier selections are defined in cascading JSON configurations stored at the Organization, Site, or User level.1.1 The HierarchyPlatform (Root): The System Owner (You).Organization (Tenant): The B2B Customer (e.g., A Retailer).Site (Node): A physical location (Warehouse, Store, Hub) connected to an Address.User (Agent): An entity executing actions within the scope of an Org or Site.2. Core Entities2.1 The Organization Entity (Organization)Represents the B2B Client. This is the root of the "OMS Hub" for that client.Field NameTypeDescriptionorgIdUUIDUnique Tenant ID.legalNameStringOfficial business name.vatIdStringTax identification for billing.statusEnumACTIVE, SUSPENDED, ONBOARDING.configurationJSONBThe Dynamic Engine. Stores global settings (Default carriers, SLA rules, Label formats).2.2 The Site Entity (Site)Represents the "Set of Addresses" you mentioned. A B2B customer has many sites (Warehouses, Pickup points).Field NameTypeDescriptionsiteIdUUIDUnique Site ID.orgIdUUIDFK to Organization.siteTypeEnumWAREHOUSE, STORE, HEADQUARTERS, DROP_OFF_POINT.addressJSONBStructured physical address (Geo-coordinates, Street, Zip).pickupConfigJSONBSpecific pickup windows (e.g., "Trucks only 8am-10am").omsHubConfigJSONBAPI Webhooks and integration settings specific to this physical location.2.3 The User Entity (UserEntity)The user is now lightweight. They are merely an identity. Their power comes from their relationships.Field NameTypeConstraintsDescriptionuserIdUUIDPKGlobal User ID.emailStringUniqueLogin credential.passwordHashStringNot NullBCrypt/Argon2 hash.globalStatusEnumACTIVE, LOCKED, MFA_REQUIRED.preferencesJSONBUI settings (Dark mode, Language, Notification density).3. The Relationship Model (The "Glue")We do not store role directly on the user anymore. We store UserContext. This allows a single user to be an "Admin" at "Warehouse A" but a read-only "Observer" at "Warehouse B".3.1 Entity: UserOrganizationAccessMaps a user to a B2B Organization.Java@Entity
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
3.2 Entity: UserSiteAccessMaps a user to specific Addresses (Sites) within that Organization.Java@Entity
@Table(name = "iam_user_site_access")
public class UserSiteAccess {
    
    @EmbeddedId
    private UserSiteId id; // Composite Key (userId, siteId)
    
    // Specific operational permissions for this site
    // e.g., CAN_PRINT_LABELS, CAN_APPROVE_DISPATCH
    @ElementCollection
    private Set<String> fineGrainedPermissions;
}
4. The Configuration Engine (No Hardcoding)This is the most critical part for a DHL/DPD level system. We use a Cascading Configuration Pattern.When the system asks: "Can this user print a label for UPS?" it checks the JSON configurations in this order:User Level (Does this user have a specific block/override?)Site Level (Does this Warehouse support UPS?)Organization Level (Does the B2B Contract include UPS?)System Level (Is UPS globally active?)4.1 Example JSON Configuration (Stored in Organization.configuration)This defines the "OMS Hub" behavior for the client without writing code.JSON{
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
5. Address Management (The "Set of Addresses")B2B logistics requires strict address validation and management. We don't just store strings; we store verified locations.5.1 JSON Schema for Address/SiteJSON{
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
6. API Interaction (Enterprise Level)Enterprise APIs must handle context. The client doesn't just "Login." They "Login into an Organization."6.1 Authentication Response (JWT)The JWT contains the sub (User ID), but the Access Token allows switching contexts.Header Required for Operations:X-Tenant-ID: {orgId}X-Site-ID: {siteId} (Optional, if operation is site-specific)6.2 Example: Create Shipment (Context Aware)POST /api/v1/shipmentsRequest:JSON{
  "recipient": { ... },
  "parcels": [ ... ]
  // No sender address needed here! 
  // The system infers the sender address from the "X-Site-ID" header 
  // and the specific configuration of that Site.
}
7. Dynamic Role Definitions (Admin Panel)Since we cannot hardcode roles, we provide an endpoint to define them.Entity: RoleDefinitionid: logistics_managerpermissions: ["SHIPMENT_CREATE", "SHIPMENT_CANCEL", "REPORT_VIEW_FINANCIAL"]Admin UI: Allows the Super Admin to create a new Role "Summer Intern" and check boxes for permissions. This saves the new Role Definition to the database, which can immediately be assigned to users.Key Takeaways for "Configurable Enterprise":Separation of Concerns: Users are people. Organizations are contracts. Sites are physical places.Cascading Config: Settings flow down from Org -> Site -> User.JSONB Columns: Use Postgres JSONB for config columns to allow schema-less flexibility for changing business rules (like label formats or carrier logic) without database migrations.Context Headers: API requests must explicitly state which "Site" or "Organization" context is being acted upon.