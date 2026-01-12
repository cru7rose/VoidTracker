PROJECT DEFINITION: DANXILS "OMNI-NEXUS"
Target System: Enterprise Logistics Operating System (Combines TMS, WMS, & Customer Portal) Role: Senior Systems Architect & Lead Developer UX Philosophy: "Liquid Interface" (UI adapts to configuration) Infrastructure Strategy: "The Antigravity Topology" (Edge, Brain, Vault)
￼
1. THE MANIFESTO (Rules of Engagement)
You are building the "Samsara + Aleet" killer. We do not hardcode business logic; we configure it.
1. No Native Apps: The driver experience is a PWA (Progressive Web App). Zero install friction.
2. Configuration Over Code: If a client needs a new field ("Is the pallet wet?"), we add it via Admin Panel JSON Schema, not by deploying new Java code.
3. Event-Driven Nervous System: The core app emits events; n8n handles the logic (Email, SMS, Alerts).
4. Strict Data Integrity: We separate Geocoding (Where is it?) from Normalization (Is it a legal address?).
5. Hierarchical Context: Users do not just "exist." They exist within a Profile -> Department -> Site hierarchy.

2. TECHNOLOGY STACK
Backend ("The Brain")
* Language: Java 21 (Spring Boot 3.3) 
* Database: PostgreSQL 16 (Hybrid Relational + JSONB for dynamic attributes)
* Messaging: Kafka (Internal Microservices) + Webhooks (External/n8n)
* Orchestration: n8n (Workflow Engine)
Frontend ("The Face")
* Framework: Vue.js 3 (Composition API) + Vite
* Styling: Tailwind CSS (Dark Mode native, "Slick" Enterprise UI)
* Components: Headless UI (for unstyled logic) + Heroicons
* Maps: Leaflet (OpenSource) + OSRM (Routing)
Driver Client ("The Ghost Interface")
* Type: PWA (Progressive Web App)
* Auth: Magic Link (One-Time-Token via SMS/Email)
* Scanner: html5-qrcode (Browser-based barcode/QR reading)

3. CORE DOMAIN ARCHITECTURE
3.1 The "Liquid" Asset Model (EAV via JSONB)
We do not have a static shipment table column structure. We use a Meta-Schema approach.
* asset_definition Table: Defines what we are shipping (e.g., "Scooter in Frame", "Euro Pallet", "Envelope").
    * Contains a schema_json column defining required fields (Weight, Serial Number, Photo Required).
* orders Table: Stores the core transactional data (From, To, Price).
* order_items Table: Stores the dynamic attributes in a properties JSONB column.
    * Example: {"vin_number": "XYZ", "frame_damage_check": false}
3.2 Identity & Access Management (IAM) - The "5-Profile" Model
We support complex B2B hierarchies.
1. Master Profile (HQ): Sees ALL shipments across all departments. Can allocate costs via dropdown.
2. Sub-Profiles (Departments 1-4): Siloed visibility. They only see/manage their specific vertical (e.g., "Spare Parts," "Whole Goods," "Returns").
3. Invoicing Logic:
    * One consolidated monthly invoice per Master Profile.
    * Invoice MUST provide a breakdown/sub-total by Department.
    * Data Export: CSV/PDF templates compatible with SAP/ERP imports.

4. THE DRIVER PWA ("The Ghost Interface")
User Journey:
1. Trigger: Driver receives SMS/Email with a secure "Magic Link" (Token valid for 12 hours).
2. Activation: Clicking link opens PWA in browser.
3. Navigation: "Start Route" deep-links to Google Maps/Waze.
4. Arrival (The Geofence Guard):
    * Driver clicks "I'm Here".
    * Logic: System compares Device GPS vs. Target Coordinates.
    * Rule: If distance > 300m -> Alert UI: "You are far away. Confirm?" -> Logs "Anomaly Event" to n8n.
5. Execution (Configurable Tiles): UI renders based on DeliveryType:
    * Night Drop (Unattended):
        * Requirement: Scan "Wall QR Code" (Proof of presence).
        * Evidence: Take photo of goods in cage.
    * Day Delivery (Attended):
        * Requirement: Signature on glass.
        * Optional: COD (Cash on Delivery) collection confirmation.
    * Dealer-to-Dealer:
        * Requirement: Pick up at A, Drop at B.
6. Scan Tech: Integrated html5-qrcode. If camera fails, fallback to manual entry.

5. INTELLIGENT ADDRESS LOGIC
We reject the "lazy" Google Maps approach. We implement Two-Phase Validation:
1. Phase 1: Normalization (The Registry):
    * Input is checked against Official Postal Registry (e.g., GUS TERYT in Poland).
    * We confirm the address exists legally.
    * Hierarchy: Postal Code (Primary Filter) -> City -> Street -> House Number.
2. Phase 2: Geocoding (The Coordinates):
    * Only after normalization do we ask Nominatim/Google for Lat/Lon.
    * UI Guard Rail: We store "Confidence Score". If confidence is "Low" (Geometric Center), the PWA forces the driver to pin the exact location upon first successful delivery, "healing" the database for next time.

6. AUTOMATION (n8n Integration)
The backend acts as an Event Emitter. It does not send emails.
* Event: shipment.delivered_night
    * n8n Action: Generates PDF with "Wall QR Scan" + "Cage Photo" -> Emails Store Manager immediately ("Your parts are waiting").
* Event: driver.anomaly_detected (GPS > 300m)
    * n8n Action: Posts message to "Control Tower" Slack channel/Dashboard.
* Event: invoice.generated
    * n8n Action: Splits PDF by Department -> Sends to Master Profile.

7. DASHBOARD & UI SPECS
* Technology: Vue 3 + Tailwind CSS.
* Visual Language: "Slick Enterprise." High contrast, Skeleton loaders (no spinners), Dark Mode support.
* Master View:
    * Top: Financial KPIs (Current Month Spend).
    * Middle: Live Map (Leaflet) showing active transfers.
    * Bottom: "Billable Events" table (filterable by Profile 1-4).
* Invoicing Module:
    * Auto-generated monthly.
    * Template Engine: Uses HTML->PDF rendering to ensure the output matches the client's strict template requirements.

8. IMPLEMENTATION PLAN (Prompt for AI)
Phase 1: The Core (Weeks 1-2)
* Setup Spring Boot + Postgres.
* Implement AssetDefinition and Order entities (JSONB).
* Build IAM (Master + 4 Sub-profiles).
Phase 2: The Ghost PWA (Weeks 3-4)
* Build Vue 3 PWA with "Magic Link" auth.
* Integrate html5-qrcode.
* Implement "Wall QR" vs "Signature" logic toggle.
Phase 3: The Intelligence (Weeks 5-6)
* Connect n8n webhooks.
* Implement Geofence Logic (300m rule).
* Build Invoicing Engine (Departmental breakdown).
Phase 4: Polish (Week 7)
* Tailwind UI Refactor (Dark mode, Skeletons).
* Address Normalization integration.
PROJECT TITAN: THE OMNI-NEXUS LOGISTICS SINGULARITY
Mission: Fuse WMS (Hubs), TMS (Last Mile), and CX (Portal) into a single, event-driven organism.
Market Goal: Displace Samsara (Fleet) and Aleet (Optimization) by offering "Total Lifecycle Control."
Core Philosophy: "The Liquid Enterprise" — Code handles the physics; Configuration handles the business.

PART 1: THE ARCHITECTURAL TOPOLOGY ("Antigravity")
We do not build three separate systems. We build One Data Core with Three Lenses.
1.1 The Trinity of Lenses
1. Lens A: The Kinetic Lens (TMS/Driver): Focused on movement, GPS, Geofences, and Proof of Delivery.
2. Lens B: The Static Lens (WMS/Hub): Focused on custody, sorting, consolidation, and "TERM" (Terminal) scans.
3. Lens C: The Commercial Lens (Portal): Focused on visibility, ordering, hierarchical cost allocation, and invoicing.
1.2 Infrastructure (The "Air-Gapped" Cloud)
* The Brain (Cloud): Spring Boot 3.3 (Java 21) + PostgreSQL 16. Handles Logic, State, and API.
* The Edge (PWA): Vue 3 + Vite. Runs on Driver Phones and Warehouse Zebra Scanners. Offline-First.
* The Nervous System: Kafka (Internal high-volume events) + n8n (External business logic/alerts).
* The Map: OSRM (Self-hosted) for routing + GUS TERYT/Nominatim for Validation.

PART 2: THE "LIQUID" DATA CORE (OMS)
Imperative: Zero Schema Migrations for new Business Verticals.
2.1 The "Asset" Meta-Model
We replace Shipments with Assets. An Asset is defined by its AssetDefinition.
* Entity: AssetDefinition (The Blueprint)
    * id: "scooter_frame"
    * input_schema: JSONB defining fields (vin, frame_color, damage_check).
    * handling_rules: JSONB (requires_two_man_lift, cannot_stack, wms_zone: "OVERSIZE").
* Entity: AssetInstance (The Physical Item)
    * properties: JSONB payload matching the schema.
    * custody_chain: List of Events (Who has it right now?).
2.2 The "5-Profile" IAM Hierarchy
* Level 1: The Platform Super-Admin (You): God mode.
* Level 2: The Tenant Master (HQ): Sees ALL. Can allocate costs via dropdown. Receives the Consolidated Invoice.
* Level 3: The Department (Profile 1-4): Siloed visibility.
    * Example: "Warranty Returns" Dept only sees broken parts; "New Sales" Dept only sees new bikes.

PART 3: THE WMS (HUB VELOCITY ENGINE)
The Missing Piece. Warehouses in this network are not for storage; they are for Flow.
3.1 The "Hot Potato" Philosophy
We do not use complex "Put-away" logic. We use Cross-Dock Logic.
* Inbound Scan (Linehaul Arrival):
    * Truck arrives at Hub A.
    * Staff scans Master_Manifest_QR or individual Asset_Barcodes.
    * System Action: Status update to AT_HUB. Updates "Control Tower."
* The "Sortation" Ritual:
    * Staff scans an item.
    * PWA Screen flashes: "ZONE B - TRUCK 4" (Last Mile) OR "ZONE C - LINEHAUL" (Transfer to next Hub).
    * Innovation: Staff does not need to know the destination. The Screen tells them the Zone.
* The "TERM" Scan (Custody Transfer):
    * This is the critical "Terminal" status.
    * It signifies: "Item is safe, accounted for, and sitting on the floor of Hub X."
    * Audit: If an item stays in AT_HUB > 12 hours -> n8n Alert: "Stagnant Asset in Hub."
3.2 WMS Hardware Agnosticism
* We do NOT build a native Android app for PDAs yet.
* Strategy: The same Vue 3 PWA adapts its UI when logged in as "Warehouse Staff."
* Scanner: Uses html5-qrcode on Zebra devices (via Chrome) or smartphone cameras.

PART 4: THE TMS (KINETIC OPERATIONS)
Focus: Last Mile optimization and Driver execution.
4.1 Routing & Manifesting
* Optimization: We integrate Vroom or Jsprit (Java-based) or call out to OSRM.
* Constraints: Defined in AssetDefinition (e.g., "Scooter cannot go in Van Type A").
* Fleet Health: We do not track oil changes. We track Availability.
    * Driver PWA: "Vehicle Check" at start of shift. "Is the van broken?" [Yes/No].
    * If Yes: n8n Alert to Fleet Manager -> Remove vehicle from available pool -> Re-optimize routes.
4.2 The "Ghost" Driver Interface (PWA)
* Auth: Magic Link (SMS). No passwords.
* Geofence Guard: 300m tolerance.
    * Logic: (GPS_Device <-> GPS_Target) > 300m ? Trigger Anomaly_Event.
* Workflow Tiles:
    * Night Drop: Scan Wall QR + Photo.
    * Day Drop: Signature + Name Check.
    * Transfer: Pick up from Dealer A -> Scan -> Route updates to Dealer B.

PART 5: THE CUSTOMER PORTAL & INVOICING
5.1 The "Consolidated" Invoice Engine
This is a complex B2B requirement.
* Trigger: End of Month (Cron Job).
* Process:
    1. Query all DELIVERED assets for Tenant_ID.
    2. Group by Department_ID.
    3. Calculate Subtotals based on Rate_Card (Matrix Pricing).
    4. Generate Single PDF with 5 Sections (One per department).
    5. Export CSV data matching the client's SAP import template.
5.2 The "Liquid" Dashboard
* Master Profile View: Map of Europe showing Hubs (WMS) and active trucks (TMS).
* Department View: List of specific SKUs (e.g., "Broken Scooters") currently in transit.

PART 6: THE "NERVOUS SYSTEM" (n8n & EVENTS)
We stop writing Java code for notifications. We emit Events.
6.1 The Event Registry
* asset.scanned_at_hub (Payload: Hub ID, User ID, Timestamp)
* asset.loaded_on_linehaul
* delivery.success_night
* delivery.failed_access
* vehicle.breakdown_reported
6.2 n8n Workflows (The Logic)
1. The "Morning Report":
    * Trigger: 6:00 AM.
    * Action: Aggregate all "Night Drop" photos for Client X.
    * Output: Send summary Email to Client Manager.
2. The "Linehaul Alert":
    * Trigger: Truck departs Hub A but does not arrive at Hub B within Expected_Time + 2 hours.
    * Action: Slack alert to "Control Tower."

PART 7: ADVANCED INTELLIGENCE ("MAD AI")
7.1 Address "Self-Healing"
* Problem: Client enters "Industrial Zone, Gate 4." (Not a valid address).
* Solution:
    1. Driver finds the location manually.
    2. Driver completes Delivery + Scan.
    3. System: Captures High_Confidence_GPS from the Scan event.
    4. AI: Updates the Site_Master_Record with these coordinates for all future deliveries.
7.2 Visual Verification (Computer Vision)
* Context: Hub/WMS.
* Action: Staff takes photo of "Damaged Frame."
* AI: Analyzes photo -> Classifies damage (Scratch vs. Bent) -> Updates Asset_Condition automatically.

PART 8: EXECUTION PROMPTS (How to build this)
Use these prompts to instruct your AI Architect (Google Antigravity) to generate the specific code modules.
Prompt 1 (The Core & WMS Entities):
"Generate the Java Spring Boot Entity structure for a Logistics System. I need an Asset entity that uses a JSONB properties column for dynamic fields (EAV pattern). I need a Hub entity and a scan_event entity to track 'TERM' scans and custody transfers. Ensure scan_event captures gps_lat, gps_lon, hub_id, and user_id. Use JPA and Lombok."
Prompt 2 (The Invoicing Engine):
"Create a Java Service named InvoiceGeneratorService. It must accept a TenantId and a DateRange. It should query all completed deliveries, group them by DepartmentProfile (Master/Sub-profile logic), calculate costs using a Strategy Pattern for RateCards, and generate a structured DTO ready for PDF rendering. Include a method to export this as CSV."
Prompt 3 (The Driver/Hub PWA Scanner):
"Build a Vue 3 Composition API component named UniversalScanner.vue. It must use html5-qrcode. It should have two modes: 'Driver' (validates against Stop Geofence) and 'Hub' (validates against Hub Manifest). Include a fallback for manual barcode entry and a toggle for rear/front camera."
Prompt 4 (The n8n Webhook Controller):
"Write a Spring Boot @RestController named EventEmitterController. It should capture internal system events (Delivery, Scan, Exception) and asynchronously push them to a configurable n8n Webhook URL. It must include a retry mechanism if n8n is down."

PART 9: THE MARKET DOMINATION SUMMARY
1. For the Driver: "It just works. No install. Tells me the gate code. Scans fast."
2. For the Warehouse: "Tells me exactly which truck to throw the box into. No thinking required."
3. For the Client: "I see everything. I get one clean invoice. My night deliveries are waiting for me at 8 AM."
4. For You (The Owner): "I never have to deploy code to add a new product type. I just edit the JSON schema."
Next Step: Shall we generate the Database Schema (SQL) for the WMS/Hub tracking layer first, or the Vue 3 PWA Scanner code?This document is the Strategic Handover Package for the Google Antigravity team. It defines the behavior, logic, and flow of the TITAN system without providing raw code.
It bridges the gap between the existing source code (Core Entities) and the required functionality (WMS/TMS/Portal).

PROJECT TITAN: ARCHITECTURAL HANDOVER DEFINITION
Version: 1.0 (Master Definition) To: Engineering Team (Antigravity) From: Chief Architect Subject: WMS Integration, Logic Flows (BPMN), and Data Contracts (UML)

1. SCOPE DEFINITION & GAP ANALYSIS
You have received danxils-system_part1.txt. This contains the Skeleton. You must now build the Muscles and Brains.
A. What Exists (The Foundation)
1. Liquid Data Core: AssetDefinition and AssetInstance (EAV/JSONB) are implemented. We can store "anything" without schema changes.
2. IAM Hierarchy: UserProfile supports the Master (HQ) vs. Department (Profile 1-4) logic.
3. Event Emitter Stub: The mechanism to talk to n8n exists (EventEmitterController).
4. Scanner Stub: A basic Vue 3 component using html5-qrcode exists.
B. What is Missing (The Work Ahead)
1. WMS "Sortation Logic": The system knows what an item is, but not where inside a Hub it should go. We need a "Zone Resolution Engine."
2. TMS "Geofence Guard" State Machine: The specific logic for the >300m anomaly check needs a rigid state machine definition.
3. Consolidated Invoicing Logic: The service stub exists, but the logic to group AssetInstances by Department and apply RateCards is missing.
4. "Ghost" PWA State Management: Handling offline state synchronization (IndexedDB -> Cloud) for the driver app.

2. LOGIC DEFINITION (BPMN 2.0)
Use these flows to configure the Business Logic Layer.
Diagram A: WMS High-Velocity Cross-Docking
Target System: HubService + ScannerComponent Goal: Staff scans an item; System instantly tells them the Outbound Zone (e.g., "TRUCK 4").
Fragment kodu

sequenceDiagram
    participant Driver as Inbound Driver
    participant Staff as Hub Staff (PWA)
    participant System as Titan Core (WMS)
    participant Logic as Zone Resolver
    participant N8N as n8n (Alerts)

    Driver->>Staff: Unloads Pallet/Asset
    Staff->>System: Scans Barcode (AssetInstance)
    
    rect rgb(240, 248, 255)
    note right of System: "The Brain" Logic
    System->>System: Lookup Order.DestinationAddress
    System->>Logic: Resolve Outbound Zone (Zip -> Route)
    Logic-->>System: Returns "ZONE: B-12" (Hamburg Linehaul)
    end

    System-->>Staff: PWA Flash: "ZONE B-12" (Big Text)
    Staff->>System: Scans "TERM" (Terminal) Location Code
    
    alt Time > 12 Hours in Hub
        System->>N8N: Trigger "Stagnant_Asset_Alert"
    end

    System->>System: Update AssetStatus = AT_HUB
    System->>System: Append ScanEvent (Lat/Lon, HubID)
Diagram B: Consolidated Invoicing (5-Profile Logic)
Target System: InvoiceGeneratorService Goal: One PDF, split by Department, tailored for SAP import.
Fragment kodu

graph TD
    A[Start: EOM Cron Job] --> B{Iterate Master Tenants}
    B -->|Tenant X| C[Fetch DELIVERED Assets]
    C --> D[Group by Sub-Profile (Department)]
    
    D --> E{For Each Dept}
    E --> F[Apply Rate Card Logic]
    F --> G[Calculate Base + Surcharges]
    G --> H[Generate Dept Sub-Invoice]
    
    H --> I[Aggregate into Master PDF]
    I --> J[Generate CSV Export (SAP Format)]
    J --> K[Trigger Event: invoice.generated]
    K --> L[n8n: Email to HQ Finance]

3. DATA CONTRACTS & UML
A. The WMS / Hub Domain (Class Diagram)
We need to extend the existing AssetInstance to support the WMS lifecycle. The ScanEvent is the "Source of Truth" for where an item was last seen.
Fragment kodu

classDiagram
    class AssetInstance {
        +UUID id
        +String barcode
        +JsonNode properties
        +AssetStatus status
        +List~ScanEvent~ custodyChain
    }

    class Hub {
        +UUID id
        +String name
        +String locationCode
        +Address address
        +JsonNode sortingRules
    }

    class ScanEvent {
        +UUID id
        +UUID assetId
        +UUID actorId
        +UUID hubId
        +ScanType type
        +GeoLocation gps
        +OffsetDateTime timestamp
    }

    class ScanType {
        <<enumeration>>
        PICKUP
        HUB_INBOUND
        HUB_SORTED
        HUB_OUTBOUND
        DELIVERY_ATTEMPT
        DELIVERY_SUCCESS
    }

    AssetInstance "1" *-- "many" ScanEvent : history
    ScanEvent "1" --> "1" Hub : occurs_at
B. The Driver PWA "Ghost" State Machine
This defines how the PWA behaves on the driver's phone. It must be Event-Driven to support the "Mad AI" features.
Fragment kodu

stateDiagram-v2
    [*] --> Idle
    Idle --> RouteActive : Magic Link Clicked
    
    state RouteActive {
        [*] --> Navigating
        Navigating --> Arrived : "I'm Here" Clicked
        
        state Arrived {
            [*] --> GPS_Check
            GPS_Check --> Distance_OK : < 300m
            GPS_Check --> Anomaly_Detected : > 300m
            
            Anomaly_Detected --> Warning_UI : Flash "Are you sure?"
            Warning_UI --> Evidence_Capture : Confirm
            Distance_OK --> Evidence_Capture
            
            state Evidence_Capture {
                [*] --> Check_Asset_Type
                Check_Asset_Type --> Scan_Wall_QR : Type = NIGHT_DROP
                Check_Asset_Type --> Signature : Type = DAY_DROP
                
                Scan_Wall_QR --> Photo_Proof
                Signature --> Photo_Proof
            }
        }
        
        Evidence_Capture --> Uploading : Submit
        Uploading --> Success : 200 OK
        Uploading --> Offline_Queue : Network Fail
    }

4. EXECUTION PLAN (For Antigravity)
Directives for the AI Implementation Team:
Phase 1: WMS & "The Zone Resolver"
* Action: Create the Hub entity.
* Logic: Implement a ZoneResolutionService. It should take an Order address and map it to a Hub outbound lane (e.g., "Lane 4 - Berlin").
* Constraint: This mapping must be stored in Hub.sortingRules (JSONB), NOT hardcoded.
Phase 2: The "Ghost" PWA Core
* Action: Expand UniversalScanner.vue.
* Logic: Implement the Geofence Guard logic client-side.
* Requirement: If GPS > 300m, the PWA must attach a warning_flag: true to the payload sent to the backend. The backend EventEmitter must then route this to n8n.
Phase 3: The "5-Profile" Invoicing
* Action: Flesh out InvoiceGeneratorService.
* Logic: Use the UserProfile.parentProfile relationship.
* Output: The service must return a complex DTO: MasterInvoiceDTO containing a list of DepartmentInvoiceDTOs.
Phase 4: Integration
* Action: Connect the PWA ScanEvent to the WMS AssetStatus.
* Flow: When a driver scans "DELIVERED", the WMS must automatically release custody.

