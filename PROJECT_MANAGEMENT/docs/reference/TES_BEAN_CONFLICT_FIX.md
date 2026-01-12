# TES Bean Conflict Fix - Deployment Guide

## ✅ Fixed Bean Conflicts

### Problem
TES was failing to start with bean conflicts:
1. `AddressExtractor` in `extractor` vs `etl` packages
2. `OrderExtractor` in `extractor` vs `etl` packages

### Solution
Renamed CDC extractors to avoid conflicts:
- `extractor/AddressExtractor` → `extractor/AddressCdcExtractor`
- `extractor/OrderExtractor` → `extractor/OrderCdcExtractor`

The `etl` package versions remain unchanged.

## 🚀 Deployment Steps (NEXUS-TEST Server)

### 1. Pull Latest Code
```bash
cd ~/TES
git pull origin from7again
```

### 2. Rebuild Docker Image
```bash
# Stop current container
docker-compose down

# Rebuild with no cache to ensure fresh build
docker-compose build --no-cache tes-service

# Start services
docker-compose up -d
```

### 3. Verify TES Started Successfully
```bash
# Check container status
docker ps | grep tes-service

# Check logs for successful startup
docker logs -f tes-service

# Look for: "Started TesServiceApplication"
# Should NOT see: "ConflictingBeanDefinitionException"
```

### 4. Test Nominatim Connection
```bash
# From host
curl "http://localhost:8083/search?q=Warsaw&format=json&limit=1"

# From TES container
docker exec tes-service curl "http://localhost:8083/search?q=Warsaw&format=json&limit=1"
```

## 📝 Changes Summary

### Commits Pushed
1. `aaa48ca` - Rename AddressExtractor to AddressCdcExtractor
2. `c729297` - Rename OrderExtractor to OrderCdcExtractor

### Files Modified
- `src/main/java/com/example/tes/extractor/AddressCdcExtractor.java` (renamed)
- `src/main/java/com/example/tes/extractor/OrderCdcExtractor.java` (renamed)
- `src/main/resources/application.yml` (Nominatim URL updated)

## ⚠️ Important Notes

1. **Rebuild Required**: The old JAR in the Docker image still has the old class names. You MUST rebuild the image.

2. **No Code Changes Needed**: The renamed classes are Spring components - Spring will auto-detect them with their new names.

3. **CDC Functionality**: Both CDC extractors will continue to work normally with their new names.

## 🔍 Troubleshooting

### If TES Still Fails to Start

**Check for other bean conflicts:**
```bash
docker logs tes-service 2>&1 | grep "ConflictingBeanDefinitionException"
```

**Verify build used latest code:**
```bash
docker exec tes-service ls -la /opt/tes/app.jar
# Check timestamp - should be recent
```

**Force complete rebuild:**
```bash
docker-compose down
docker system prune -f
docker-compose build --no-cache
docker-compose up -d
```

### If Nominatim Not Working

**Check Nominatim is running:**
```bash
docker ps | grep nominatim
curl http://localhost:8083/search?q=test&format=json
```

**Check TES can reach Nominatim:**
```bash
docker exec tes-service ping -c 3 localhost
docker exec tes-service curl http://localhost:8083/search?q=test&format=json
```

## ✅ Success Criteria

- [ ] TES container shows status "Up" (not "Restarting")
- [ ] Logs show "Started TesServiceApplication"
- [ ] No "ConflictingBeanDefinitionException" errors
- [ ] Nominatim accessible from TES container
- [ ] Frontend can access Nominatim for address suggestions
