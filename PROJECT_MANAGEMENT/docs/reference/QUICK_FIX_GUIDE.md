# Quick Fix Guide - Kafka & Zipkin Issues

## Problem Summary
Your NEXUS-TEST server is showing two types of warnings:
1. **Kafka**: `UNKNOWN_TOPIC_OR_PARTITION` for `danxils.reply.v1`
2. **Zipkin**: Connection refused errors

## Solution 1: Create Missing Kafka Topics

### On NEXUS-TEST Server:

```bash
# 1. Copy the script to the server
scp /Users/cruz/DISYSTEMS/create-kafka-topics.sh user@nexus-test:/tmp/

# 2. SSH to the server
ssh user@nexus-test

# 3. Edit the script to match your Kafka setup
nano /tmp/create-kafka-topics.sh
# Update these variables:
# - KAFKA_BIN (path to kafka-topics.sh)
# - BOOTSTRAP_SERVER (your Kafka broker address)
# - REPLICATION_FACTOR (1 for single broker, 3 for production cluster)

# 4. Run the script
chmod +x /tmp/create-kafka-topics.sh
/tmp/create-kafka-topics.sh

# 5. Verify topics were created
/opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list | grep danxils.reply
```

### Quick Manual Fix (if script doesn't work):

```bash
# Just create the missing topic
/opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 \
  --create --if-not-exists \
  --topic danxils.reply.v1 \
  --partitions 3 \
  --replication-factor 1
```

## Solution 2: Fix Zipkin Tracing

### Option A: Disable Zipkin (Recommended for now)

Add to `/path/to/danxils-api/application.yml` and `/path/to/tes/application.yml`:

```yaml
management:
  tracing:
    sampling:
      probability: 0.0  # Disable tracing
```

### Option B: Configure Zipkin Properly

If you want to use Zipkin:

```yaml
management:
  zipkin:
    tracing:
      endpoint: http://your-zipkin-server:9411/api/v2/spans
  tracing:
    sampling:
      probability: 0.1  # Sample 10% of requests
```

## After Fixes

1. **Restart services** on NEXUS-TEST:
   ```bash
   sudo systemctl restart danxils-api
   sudo systemctl restart tes-service
   ```

2. **Monitor logs**:
   ```bash
   # Check for warnings
   tail -f /var/log/danxils-api.log | grep -i "unknown_topic\|zipkin"
   tail -f /var/log/tes-service.log | grep -i "unknown_topic\|zipkin"
   ```

3. **Verify** - You should no longer see:
   - ❌ `UNKNOWN_TOPIC_OR_PARTITION` warnings
   - ❌ Zipkin connection errors

## Complete Topic List

See `KAFKA_TOPICS.md` for the full list of 30+ topics and their purposes.

## Need Help?

If issues persist:
1. Check Kafka broker is running: `systemctl status kafka`
2. Verify connectivity: `telnet kafka-broker 9092`
3. Check topic exists: `kafka-topics.sh --list --bootstrap-server localhost:9092`
