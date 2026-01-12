# Legacy Database Migration Assessment
**Date**: 2025-11-28  
**Project**: DANXILS TMS Modernization  
**Scope**: Analysis of legacy TMS database schema for migration planning

---

## Executive Summary

The legacy database contains **100+ tables** with extensive audit logging, multiple duplicate/variant tables, and legacy integrations (IBM, FIAT). This assessment categorizes each component and provides migration recommendations aligned with the modern DANXILS microservices architecture.

### Key Findings
- **Core entities** (~40% of tables): Should be migrated with modernization
- **Audit/Log tables** (~30% of tables): Selective migration, most deprecated
- **Legacy integrations** (~15% of tables): Deprecated, replaced by modern APIs
- **Duplicate variants** (~15% of tables): Consolidated during migration

---

## Table Categorization & Migration Strategy

### 🟢 MIGRATE & MODERNIZE (High Priority)

#### 1. Address Management Domain
**Maps to**: `order-service` Address aggregates

| Legacy Table | Modern Entity | Notes |
|--------------|---------------|-------|
| `Adr` | `Address` | Core address entity - migrate all active records |
| `Street` | Part of `Address` | Flatten into Address |
| `Postal` | Part of `Address` | Integrate into Address with Country |
| `Country` | `Country` lookup | Migrate to reference data service |
| `CountryCode` | `Country` enrichment | Merge with Country table |
| `Phone` | `ContactInfo` embedded | Move to JSON field in Address |

**Migration Strategy**:
- Transform hierarchical structure (Country → Postal → Street → Adr) into flat `Address` entity
- Migrate GPS coordinates (`GpsEast`, `GpsNorth`) to standard `Latitude`/`Longitude`
- Consolidate phone data into JSON contact info
- Preserve GDPR flags
- **Data Volume Estimate**: Likely 10K-500K addresses

#### 2. Customer/Alias Management
**Maps to**: `iam-service` Customer entities

| Legacy Table | Modern Entity | Notes |
|--------------|---------------|-------|
| `Alias` | `Customer` | Primary customer records |
| `Alias2` | Merge into `Customer` | Duplicate with different CustID length |
| `Alias_Depot` | `CustomerDepot` | Depot associations |
| `Alias_Phone` | `ContactInfo` | Embedded in Customer |
| `CustList` | Reference data | Customer lookup |
| `CustomerBridge` | Integration map | For legacy system sync only |

**Migration Strategy**:
- Merge `Alias` and `Alias2` into single `Customer` entity
- Normalize CustID to consistent length (nvarchar(40) recommended)
- Move contact data to structured JSON
- Preserve depot associations as relationships
- **Data Volume Estimate**: 1K-50K customers

#### 3. Shipment/Order Management (Core Business Logic)
**Maps to**: `order-service` Order/Shipment entities

| Legacy Table | Modern Entity | Notes |
|--------------|---------------|-------|
| `Sending` | `Order` | Core shipment records - CRITICAL |
| `Sending2` | Merge into `Order` | Variant table, consolidate |
| `SendingPending` | `Order` (status-based) | Use status field instead |
| `Sending_Addon` | `OrderLineItem` | Additional order details |
| `Sending_Note_Info` | `OrderNotes` | Embedded in Order |
| `SendingCons` | `Consolidation` | Consolidation tracking |
| `Barcode` | `OrderBarcode` | Barcode assignments |

**Migration Strategy**:
- Consolidate Sending, Sending2, SendingPending into unified `Order` entity
- Use status field (PENDING, NEW, CONFIRMED, etc.) instead of separate tables
- Migrate pickup/delivery addresses by reference (AdrID)
- Preserve route assignments for planning service
- Transform IBM-specific fields to generic references
- **Data Volume Estimate**: 100K-5M shipments (depending on retention)

#### 4. Status Tracking & Events
**Maps to**: `order-service` Status history

| Legacy Table | Modern Entity | Notes |
|--------------|---------------|-------|
| `StatusHistory` | `OrderStatusEvent` | Event sourcing approach |
| `StatusCodes` | `StatusCode` enum/lookup | Standardize codes |
| `StatusCodesDirection` | Part of `StatusCode` | Embedded direction field |
| `StatusHistory_Reason` | `StatusReason` | Enhanced reason tracking |
| `StatusHistoryExt` | `StatusMetadata` | GPS, signature, photo data |

**Migration Strategy**:
- Transform to event-sourcing pattern with immutable status events
- Standardize DANX codes to modern status enum
- Preserve GPS coordinates and delivery proof (signatures, photos)
- Maintain event chain for audit purposes
- **Data Volume Estimate**: 500K-50M status events

#### 5. Employee/Driver Management
**Maps to**: `iam-service` Employee entities

| Legacy Table | Modern Entity | Notes |
|--------------|---------------|-------|
| `Emp` | `Employee` / `Driver` | User identity |
| `Emp_Company` | Company lookup | Multi-tenant support |
| `Emp_Department` | Department assignment | Organizational structure |
| `Emp_Area` | Service area | Geographic coverage |
| `Department` | Reference data | Organizational units |

**Migration Strategy**:
- Split into `Employee` (IAM) and `Driver` (planning-service) entities
- Migrate authentication to OAuth2/JWT (deprecate WinLogin)
- Preserve organizational hierarchy
- Link to modern role-based access control (RBAC)
- **Data Volume Estimate**: 100-10K employees

---

### 🟡 SELECTIVE MIGRATION (Conditional)

#### 6. Validation & Address Verification
**Maps to**: `order-service` Validation workflows

| Legacy Table | Decision | Rationale |
|--------------|----------|-----------|
| `Validation` | **Keep concept** | But use modern Nominatim integration |
| `Validation_Address` | **Archive** | Replace with real-time validation |
| `Validation_Address_Manual_Match` | **Migrate active** | Manual overrides still needed |
| `Validation_StatusHistory_Reason` | **Keep pattern** | Validation triggers status events |

**Migration Strategy**:
- Replace batch validation with real-time address verification
- Migrate active manual matches for continuity
- Archive historical validation records (read-only access)

#### 7. Email & SMS Notifications
**Maps to**: `notification-service` (if implemented)

| Legacy Table | Decision | Rationale |
|--------------|----------|-----------|
| `MailService` | **Archive** | Replace with modern notification service |
| `SMTPAddress` | **Deprecated** | Use environment config |

**Migration Strategy**:
- Extract email templates and rules, but rebuild notification infrastructure
- Archive sent mail log for compliance (6-12 months retention)

#### 8. Configuration & Setup
| Legacy Table | Decision | Rationale |
|--------------|----------|-----------|
| `SendingSetupI` | **Migrate templates** | Order templates functionality |
| `SendingSetupLocks` | **Deprecated** | Use RBAC permissions instead |
| `ServiceTypes` | **Migrate** | Service type reference data |
| `Holidays` | **Migrate** | Business calendar |

---

### 🔴 DEPRECATED - DO NOT MIGRATE

#### 9. Legacy Integration Tables (IBM, FIAT, etc.)
**Reason**: Specific to old systems, replaced by modern APIs

| Legacy Table | Reason for Deprecation |
|--------------|------------------------|
| `SendingAddonIBM` | IBM-specific integration |
| `FiatTmpTable` | FIAT file import staging |
| All `IBM*` fields in Sending | Legacy system codes |

**Strategy**: Archive for historical reference only

#### 10. Duplicate Log/Audit Tables
**Reason**: Excessive audit logging, use event sourcing instead

| Legacy Table | Modern Approach |
|--------------|----------------|
| `*_Log` tables (50+ tables) | Event sourcing in Order domain |
| `*_Log_Change` tables | Change data capture in DB layer |
| `TrackIT2015_SendingSetupI_Log` | Application-level audit log |

**Strategy**: 
- Archive recent logs (last 12-24 months)
- Older logs to cold storage

#### 11. Application-Specific Tables
**Reason**: TrackIT desktop app specific, replaced by web/mobile apps

| Legacy Table | Reason for Deprecation |
|--------------|------------------------|
| `TrackIT2015_*` (30+ tables) | Desktop app metadata |
| `TrackIT2015_AccessControl` | Old RBAC, use Keycloak |
| `TrackIT2015_ErrorLog` | Old error tracking, use modern APM |
| `TrackIT2015_Version` | Desktop version control |
| `TrackIT2015_FeatureLog` | Desktop usage analytics |

**Strategy**: No migration needed

#### 12. Obsolete/Technical Debt
| Legacy Table | Reason for Deprecation |
|--------------|------------------------|
| `APVLog` | Unclear purpose, 48 suggestion columns (!?) |
| `SendingNotImported` | Import failures, use dead letter queue |
| `SendingWebExt` | Web-specific fields, merge into Order |
| `SendingIDPrefix` | Auto-numbering, use DB sequences |
| `MaxID` | ID generation, use DB identity/sequences |
| `sysdiagrams` | SQL Server diagrams metadata |
| `SS_create` | Unclear technical artifact |

---

## Migration Priorities & Phases

### Phase 1: Core Master Data (Week 1-2)
1. ✅ Country & CountryCode → Reference data
2. ✅ Postal, Street, Adr → Address aggregate
3. ✅ Alias, Alias2 → Customer entity
4. ✅ Emp → Employee/Driver entities

**Goal**: Establish foundational reference data

### Phase 2: Transactional Data (Week 3-4)
1. ✅ Sending, Sending2, SendingPending → Order entity
2. ✅ StatusHistory, StatusCodes → OrderStatusEvent
3. ✅ Barcode, SendingCons → Order relationships
4. ✅ Sending_Addon → OrderLineItem

**Goal**: Migrate active orders and shipments

### Phase 3: Supporting Data (Week 5-6)
1. ✅ Phone, Alias_Phone → ContactInfo
2. ✅ Validation_Address_Manual_Match → Manual overrides
3. ✅ ServiceTypes, Holidays → Reference data
4. ✅ SendingSetupI → Order templates

**Goal**: Complete supporting data migration

### Phase 4: Selective History (Week 7-8)
1. 🔄 Recent StatusHistory records (last 6-12 months)
2. 🔄 Important audit logs for compliance
3. 🔄 Active validation records

**Goal**: Preserve essential audit trail

---

## Data Transformation Examples

### Example 1: Address Flattening
```sql
-- Legacy hierarchical structure
SELECT 
    a.AdrID,
    a.Name,
    a.HouseNo,
    a.Flat,
    s.Street,
    p.Postal,
    p.City,
    c.Country,
    a.Latitude,
    a.Longitude
FROM Adr a
JOIN Street s ON a.StreetID = s.StreetID
JOIN Postal p ON s.PostalID = p.PostalID
JOIN Country c ON p.CountryID = c.CountryID;

-- Modern flat structure (JSON example)
{
  "addressId": "uuid",
  "name": "Company Name",
  "street": "Main street",
  "houseNumber": "123",
  "apartment": "4B",
  "postalCode": "12345",
  "city": "Copenhagen",
  "country": "DK",
  "coordinates": {
    "latitude": 55.6761,
    "longitude": 12.5683
  },
  "gdprConsent": true
}
```

### Example 2: Customer Consolidation
```sql
-- Merge Alias and Alias2
INSERT INTO modern.customer (
    customer_id, 
    customer_name, 
    address_id, 
    contact_info
)
SELECT 
    COALESCE(a.CustID, a2.CustID) as customer_id,
    COALESCE(a.Alias, a2.Alias) as customer_name,
    COALESCE(a.AdrID, a2.AdrID) as address_id,
    JSON_BUILD_OBJECT(
        'email', COALESCE(a.Email, a2.Email),
        'phone', COALESCE(a.PhoneNr, a2.PhoneNr),
        'mobile', COALESCE(a.Mobile, a2.Mobile)
    ) as contact_info
FROM Alias a
FULL OUTER JOIN Alias2 a2 ON a.id = a2.id;
```

### Example 3: Order Status Event Transformation
```sql
-- Transform status history to event sourcing
INSERT INTO modern.order_status_event (
    event_id,
    order_id,
    status_code,
    event_time,
    metadata
)
SELECT 
    gen_random_uuid(),
    sh.sendingID,
    sh.DanxCode,
    sh.LogTime,
    JSON_BUILD_OBJECT(
        'comment', sh.Comment,
        'recipientName', sh.RecpName,
        'location', JSON_BUILD_OBJECT(
            'latitude', she.Latitude,
            'longitude', she.Longitude,
            'site', she.Site
        ),
        'signature', sh.ReferenceNo,
        'userName', sh.UserNa
    )
FROM StatusHistory sh
LEFT JOIN StatusHistoryExt she ON sh.ID = she.ID
WHERE sh.LogTime > DATEADD(month, -12, GETDATE());
```

---

## Data Quality Concerns & Remediation

### Identified Issues

1. **Inconsistent CustID lengths** (Alias: nvarchar(40), Alias2: nvarchar(20))
   - **Fix**: Standardize to nvarchar(40), verify no truncation

2. **Multiple duplicate tables** (Sending/Sending2/SendingPending)
   - **Fix**: Identify distinguishing logic, consolidate with status field

3. **Excessive NULL columns** (APVLog with 48 Improve/Suggestion pairs)
   - **Fix**: Do not migrate, archive only

4. **Mixed data types** (SessionIDs sometimes int, sometimes varchar)
   - **Fix**: Standardize to BIGINT for modern session tracking

5. **GDPR flag inconsistency** (only in Adr, not in Alias)
   - **Fix**: Add GDPR consent tracking to Customer entity

### Data Cleansing Rules

```sql
-- Rule 1: Deduplicate addresses
WITH address_dupes AS (
    SELECT 
        MIN(AdrID) as keep_id,
        Name, Street, HouseNo, Postal, City
    FROM vw_flat_addresses
    GROUP BY Name, Street, HouseNo, Postal, City
    HAVING COUNT(*) > 1
)
-- Merge duplicate address references before migration

-- Rule 2: Standardize phone numbers
UPDATE Phone 
SET Phone = REPLACE(REPLACE(REPLACE(Phone, ' ', ''), '-', ''), '+', '')
WHERE Phone LIKE '%[^0-9]%';

-- Rule 3: Validate coordinates
UPDATE Adr 
SET Latitude = NULL, Longitude = NULL
WHERE 
    (Latitude NOT BETWEEN -90 AND 90)
    OR (Longitude NOT BETWEEN -180 AND 180);
```

---

## Migration Risks & Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Data volume exceeds estimate (5M+ orders) | High | Medium | Implement batch migration with chunking |
| Active orders during migration window | Critical | High | Blue-green deployment, dual-write period |
| Address deduplication causes ref breaks | High | Medium | Conservative merge, keep legacy IDs |
| Status code mapping inconsistencies | Medium | High | Create comprehensive mapping table |
| Performance degradation on old hardware | Medium | Low | Use read replicas for migration queries |
| Lost data due to transformation errors | Critical | Low | Extensive validation, rollback plan |

### Mitigation Strategies

1. **Dual-Write Period**: Run old and new systems in parallel for 2-4 weeks
2. **Data Validation**: Compare record counts and checksums after each phase
3. **Incremental Migration**: Migrate by date ranges (oldest first)
4. **Rollback Plan**: Keep legacy DB read-only for 90 days post-migration
5. **Smoke Testing**: Verify critical workflows after each phase

---

## Technical Implementation

### Recommended Tools

1. **Apache NiFi** or **AWS DMS**: For large-scale data movement
2. **Flyway/Liquibase**: Version-controlled migration scripts
3. **DBT (Data Build Tool)**: Data transformation and validation
4. **Great Expectations**: Data quality testing

### Migration Script Structure

```
migrations/
├── phase1_master_data/
│   ├── V001__create_country_reference.sql
│   ├── V002__migrate_countries.sql
│   ├── V003__create_address_aggregate.sql
│   ├── V004__migrate_addresses.sql
│   └── V005__validate_addresses.sql
├── phase2_transactional/
│   ├── V010__create_order_entity.sql
│   ├── V011__migrate_sending_current_year.sql
│   ├── V012__migrate_sending_last_year.sql
│   ├── V013__migrate_status_history.sql
│   └── V014__validate_orders.sql
├── phase3_supporting/
│   └── ...
└── phase4_history/
    └── ...
```

---

## Estimated Timeline & Resources

### Timeline
- **Analysis & Planning**: 1 week (CURRENT)
- **Infrastructure Setup**: 1 week
- **Phase 1 Migration**: 2 weeks
- **Phase 2 Migration**: 2 weeks  
- **Phase 3 Migration**: 2 weeks
- **Phase 4 Migration**: 2 weeks
- **Validation & Testing**: 2 weeks
- **Dual-Run Period**: 4 weeks
- **Total**: ~16 weeks (4 months)

### Resource Requirements
- **1 Data Engineer** (lead migration)
- **1 Backend Developer** (entity mapping, API updates)
- **1 DBA** (performance tuning, query optimization)
- **1 QA Engineer** (validation, testing)
- **Part-time DevOps** (infrastructure, monitoring)

### Infrastructure Needs
- **Staging database** (replica of legacy DB)
- **Migration server** (16GB RAM minimum)
- **Monitoring tools** (Prometheus, Grafana for progress tracking)
- **Backup storage** (2-3x database size for safety)

---

## Success Criteria

✅ **Functional**:
- All active customers migrated with contact info
- All orders from last 24 months migrated
- All active routes and planning data migrated
- Status history for active orders preserved

✅ **Performance**:
- Migration completes within 16-week timeline
- No more than 4 hours downtime for cutover
- New system query performance ≥ legacy system

✅ **Data Quality**:
- <0.1% data loss or corruption
- 100% of critical entities validated
- Address deduplication reduces storage by 15-25%

✅ **Business Continuity**:
- Zero order processing interruption
- Driver app continues functioning
- Customer portal remains accessible

---

## Next Steps

1. **Review & Approve** this assessment with stakeholders
2. **Set up staging environment** for migration testing
3. **Create detailed mapping** for Sending → Order transformation
4. **Develop Phase 1 migration scripts** (Country, Address, Customer)
5. **Establish validation framework** (Great Expectations rules)
6. **Plan cutover weekend** (minimal business impact)

---

## Appendix: Complete Table Inventory

### Tables by Category (Summary)

| Category | Table Count | Migration Action |
|----------|-------------|------------------|
| Core Entities | 15 | Migrate & Modernize |
| Audit/Log Tables | 32 | Selective Archive |
| Legacy Integration | 8 | Deprecated |
| Duplicate Variants | 12 | Consolidate |
| TrackIT Application | 25 | Deprecated |
| Reference Data | 10 | Migrate |
| Technical/Unknown | 8 | Evaluate individually |
| **TOTAL** | **~110** | - |

---

**Document Version**: 1.0  
**Last Updated**: 2025-11-28  
**Author**: DANXILS Migration Team  
**Status**: Draft for Review
