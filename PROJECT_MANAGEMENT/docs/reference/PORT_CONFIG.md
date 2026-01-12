# DANXILS Port Configuration

## Port Conflict with Antigravity IDE

**Issue**: Antigravity IDE uses port 9092, which conflicts with Kafka's default port.

**Solution**: Changed Kafka's external port to **9094**

---

## Updated Port Mapping

| Service | Port | Notes |
|---------|------|-------|
| **IAM Service** | 8081 | ✅ Available |
| **Order Service** | 8091 | ✅ Available |
| **Planning Service** | 8092 | ✅ Available |
| **Web Dashboard** | 5173 | ✅ Available |
| **Kafka (External)** | 9094 | ✅ Changed from 9092 |
| **Kafka (Internal)** | 29092 | ✅ Docker network only |
| **PostgreSQL** | 5432 | ✅ Available |

---

## What Changed

### TES/docker-compose.yml
- Kafka external port: `9092` → `9094`
- Kafka advertised listeners updated
- Services inside Docker still use internal port `29092`

### Application Configuration

**No changes needed** - Services use Kafka's internal Docker network (`kafka:29092`), not the external port.

---

## Starting DANXILS

Now you can start without conflicts:

```bash
./start-danxils.sh
```

Kafka will be accessible at:
- **External**: `localhost:9094`
- **Internal** (Docker): `kafka:29092`

---

## If You Still Have Issues

Run the port checker:
```bash
./fix-ports.sh
```

This will show all port conflicts and offer to resolve them.
