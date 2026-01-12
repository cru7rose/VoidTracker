# Kafka Topics Documentation - DANXILS System

## Overview
This document lists all Kafka topics used by the DANXILS system (DANXILS-API + TES services).

## Topic Naming Convention
- Legacy topics: `snake_case` (e.g., `danxils_orders`)
- New architecture topics: `dot.notation.v1` (e.g., `alias.check.request.v1`)

## Core Order Flow Topics

### `danxils_orders`
- **Producer**: DANXILS-API (OrderController)
- **Consumer**: TES (OrderRequestListener)
- **Purpose**: Main order ingestion topic
- **Message Type**: `OrderEvent`

### `danxils_status`
- **Producer**: TES (TesKafkaProducer)
- **Consumer**: DANXILS-API (StatusListener)
- **Purpose**: Order status updates from TES to DANXILS-API
- **Message Type**: Status update events

### `order_error_topic`
- **Producer**: TES
- **Consumer**: DANXILS-API
- **Purpose**: Order processing errors
- **Message Type**: Error events

### `order_response_topic`
- **Producer**: TES
- **Consumer**: DANXILS-API
- **Purpose**: Order processing responses
- **Message Type**: Response events

## New Architecture Topics (v1)

### `alias.check.request.v1`
- **Producer**: DANXILS-API
- **Consumer**: TES (AliasCheckRequestListener)
- **Purpose**: Request alias verification from TES
- **Message Type**: Alias check request

### `alias.check.result.v1`
- **Producer**: TES
- **Consumer**: DANXILS-API (AliasCheckResultListener)
- **Purpose**: Return alias verification results
- **Message Type**: Alias check result

### `order.pending.verification.v1`
- **Producer**: DANXILS-API
- **Consumer**: TES
- **Purpose**: Orders that need manual verification
- **Message Type**: Order verification request

### `persist.to.trackit.request.v1`
- **Producer**: DANXILS-API
- **Consumer**: TES (PersistToTrackitRequestListener)
- **Purpose**: Request to persist order to TrackIT database
- **Message Type**: Persistence request

### `order.persisted.v1`
- **Producer**: TES
- **Consumer**: DANXILS-API (OrderPersistedListener)
- **Purpose**: Confirmation that order was persisted to TrackIT
- **Message Type**: Persistence confirmation

### `algoplanner.sync.request.v1`
- **Producer**: TES
- **Consumer**: DANXILS-API (AlgoPlannerSyncListener)
- **Purpose**: Request to sync order with AlgoPlanner
- **Message Type**: Sync request

### `address.search.request.v1`
- **Producer**: DANXILS-API
- **Consumer**: TES
- **Purpose**: Address search/lookup requests
- **Message Type**: Address search request

### `danxils.reply.v1` ⚠️ **MISSING - CAUSING WARNINGS**
- **Producer**: TES
- **Consumer**: DANXILS-API
- **Purpose**: General reply topic for TES responses
- **Message Type**: Generic reply events
- **Status**: **NOT CREATED** - This is causing the `UNKNOWN_TOPIC_OR_PARTITION` warnings

## Address Management Topics

### `address_corrections`
- **Producer**: DANXILS-API
- **Consumer**: TES
- **Purpose**: Address correction requests
- **Message Type**: Address correction event

### `address_verification_requests`
- **Producer**: DANXILS-API
- **Consumer**: TES
- **Purpose**: Address verification requests
- **Message Type**: Verification request

### `address_verification_results`
- **Producer**: TES
- **Consumer**: DANXILS-API
- **Purpose**: Address verification results
- **Message Type**: Verification result

### `address_import_row_requests`
- **Producer**: DANXILS-API
- **Consumer**: TES (AddressRowConsumer)
- **Purpose**: Bulk address import rows
- **Message Type**: Address row data

### `danxils_address_save_with_alias_request`
- **Producer**: DANXILS-API
- **Consumer**: TES (AddressPersistenceConsumer)
- **Purpose**: Save address with alias to TrackIT
- **Message Type**: Address save request

### `address_upsert`
- **Producer**: TES
- **Consumer**: Internal
- **Purpose**: Address upsert operations
- **Message Type**: Address upsert event

### `db_address_cdc_topic`
- **Producer**: TES (CDC Extractor)
- **Consumer**: Internal
- **Purpose**: Change Data Capture for addresses
- **Message Type**: CDC event

## Admin & Management Topics

### `danxils_tes_admin_requests`
- **Producer**: DANXILS-API
- **Consumer**: TES (AdminRequestListener)
- **Purpose**: Admin commands to TES
- **Message Type**: Admin request

### `tes_danxils_admin_responses`
- **Producer**: TES
- **Consumer**: DANXILS-API (AdminResponseListener)
- **Purpose**: Admin command responses
- **Message Type**: Admin response

### `tes_processing_errors_events_topic`
- **Producer**: TES
- **Consumer**: DANXILS-API (TesProcessingErrorListener)
- **Purpose**: TES processing error feed
- **Message Type**: Processing error event

### `tes_danxils_upload_status`
- **Producer**: TES
- **Consumer**: DANXILS-API (AddressUploadStatusListener)
- **Purpose**: Address upload status updates
- **Message Type**: Upload status event

## WMS Integration Topics

### `wms_pinquark_orders_topic`
- **Producer**: External WMS / DANXILS-API
- **Consumer**: TES (WmsOrderRequestListener) / DANXILS-API (WmsOrderRequestListener)
- **Purpose**: Orders from WMS Pinquark system
- **Message Type**: WMS order event

## Other Topics

### `raw_db_orders_topic`
- **Producer**: TES (Order Extractor)
- **Consumer**: DANXILS-API (RawOrderEventConsumer)
- **Purpose**: Raw orders extracted from TrackIT database
- **Message Type**: Raw order event

### `danxils_orders_delete`
- **Producer**: DANXILS-API
- **Consumer**: TES / DANXILS-API (OrderDeletionListener)
- **Purpose**: Order deletion requests
- **Message Type**: Deletion request

### `algoplanner_status_updates_topic`
- **Producer**: External AlgoPlanner
- **Consumer**: TES (AlgoPlannerStatusUpdateListener)
- **Purpose**: Status updates from AlgoPlanner
- **Message Type**: Status update

## Zipkin Tracing Issue

The logs also show Zipkin connection errors:
```
java.net.ConnectException: Connection refused
at zipkin2.reporter.urlconnection.InternalURLConnectionSender.postSpans
```

**Solution**: Either configure Zipkin properly or disable it in `application.yml`:
```yaml
management:
  zipkin:
    tracing:
      endpoint: ${ZIPKIN_ENDPOINT:http://localhost:9411/api/v2/spans}
  tracing:
    sampling:
      probability: ${TRACING_SAMPLING_PROBABILITY:0.0}  # Set to 0.0 to disable
```

## Action Items

1. ✅ Run `create-kafka-topics.sh` script to create all topics
2. ✅ Verify `danxils.reply.v1` topic is created
3. ✅ Restart DANXILS-API and TES services
4. ⚠️ Configure or disable Zipkin tracing
5. ✅ Monitor logs for any remaining `UNKNOWN_TOPIC_OR_PARTITION` warnings
