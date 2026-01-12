#!/bin/bash
# ==============================================================================
# Kafka Topic Creation Script for DANXILS System
# ==============================================================================
# This script creates all required Kafka topics for DANXILS-API and TES services
# Run this on the Kafka broker or from a machine with kafka-topics.sh access
#
# Usage: ./create-kafka-topics.sh
# ==============================================================================

KAFKA_BIN=""  # Rely on PATH inside container
BOOTSTRAP_SERVER="localhost:9093"  # Adjust to your Kafka broker address
PARTITIONS=3
REPLICATION_FACTOR=1  # Adjust based on your cluster size

echo "Creating Kafka topics for DANXILS system..."
echo "Bootstrap server: $BOOTSTRAP_SERVER"
echo "Partitions: $PARTITIONS, Replication Factor: $REPLICATION_FACTOR"
echo ""

# ==============================================================================
# DANXILS-API Topics
# ==============================================================================

echo "=== Creating DANXILS-API Topics ==="

# Core order flow
kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic danxils_orders --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic danxils_status --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic order_error_topic --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic order_response_topic --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

# WMS Integration
kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic wms_pinquark_orders_topic --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

# Address management
kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic address_corrections --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic address_verification_requests --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic address_verification_results --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic address_import_row_requests --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic danxils_address_save_with_alias_request --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

# Raw DB orders
kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic raw_db_orders_topic --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

# Order deletion
kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic danxils_orders_delete --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

# Admin communication
kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic danxils_tes_admin_requests --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic tes_danxils_admin_responses --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

# TES processing errors feed
kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic tes_processing_errors_events_topic --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

# Address upload status
kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic tes_danxils_upload_status --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

# ==============================================================================
# New Architecture Topics (v1)
# ==============================================================================

echo "=== Creating New Architecture Topics ==="

# Alias check flow
kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic alias.check.request.v1 --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic alias.check.result.v1 --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

# Pending verification
kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic order.pending.verification.v1 --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

# TrackIT persistence
kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic persist.to.trackit.request.v1 --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic order.persisted.v1 --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

# AlgoPlanner sync
kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic algoplanner.sync.request.v1 --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic algoplanner_status_updates_topic --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

# TES Integration Topics
kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic address.search.request.v1 --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

# *** MISSING TOPIC - This is causing the warnings ***
kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic danxils.reply.v1 --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

# ==============================================================================
# TES-Specific Topics
# ==============================================================================

echo "=== Creating TES-Specific Topics ==="

# Address CDC
kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic db_address_cdc_topic --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

# Address upsert
kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --create --if-not-exists \
  --topic address_upsert --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR

# ==============================================================================
# Verification
# ==============================================================================

echo ""
echo "=== Verifying Topic Creation ==="
echo "Listing all topics:"
kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --list

echo ""
echo "=== Topic Creation Complete ==="
echo "Total topics created: 30+"
echo ""
echo "IMPORTANT: Restart DANXILS-API and TES services after topic creation"
echo "to clear the UNKNOWN_TOPIC_OR_PARTITION warnings."
