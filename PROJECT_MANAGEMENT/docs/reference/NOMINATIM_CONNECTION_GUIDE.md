# Nominatim Connection Configuration Guide

## Current Setup (NEXUS-TEST Server)

### Services Running:
- **Frontend**: `localhost:81` (apidanxils-frontend)
- **TES**: `localhost:8081` (tes-service)
- **Nominatim**: `localhost:8083` (nominatim-service)
- **Kafka**: `localhost:9092`
- **OSRM**: `localhost:5000`

## Configuration Changes

### 1. Frontend Configuration

**File**: `/Users/cruz/DISYSTEMS/api-frontend/.env`

**Current**:
```env
VITE_NOMINATIM_URL=/nominatim
```

**Change to**:
```env
VITE_NOMINATIM_URL=http://localhost:8083
```

**Why**: Frontend needs direct access to Nominatim service for address autocomplete.

### 2. TES Configuration

**File**: `/Users/cruz/DISYSTEMS/TES/TES/src/main/resources/application.yml`

**Updated** (line 191):
```yaml
url: "${APP_NOMINATIM_API_URL:http://localhost:8083/search}"
```

**Why**: TES uses Nominatim for address verification fallback.

### 3. Frontend Nginx Configuration (If using Nginx proxy)

If frontend uses Nginx, add this to nginx.conf:

```nginx
location /nominatim/ {
    proxy_pass http://localhost:8083/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

## Testing Nominatim Connection

### Test 1: Direct API Call
```bash
curl "http://localhost:8083/search?q=Marszałkowska+1+Warszawa&format=json&addressdetails=1&limit=5"
```

Expected: JSON response with address suggestions

### Test 2: From Frontend Container
```bash
docker exec apidanxils-frontend curl "http://localhost:8083/search?q=Warsaw&format=json&limit=1"
```

### Test 3: From TES Container
```bash
docker exec tes-service curl "http://localhost:8083/search?q=Krakow&format=json&limit=1"
```

## Frontend Integration

### Update Address Autocomplete Component

The frontend already has Nominatim integration in:
- `src/stores/useGeoStore.js` (line 81)
- `src/controllers/RealtimeVerificationOrchestrator.js` (line 72)

After updating `.env`, the frontend will automatically use local Nominatim.

### Usage in Components

```vue
<script setup>
import { useGeoStore } from '@/stores/useGeoStore';

const geoStore = useGeoStore();

const searchAddress = async (query) => {
  const results = await geoStore.searchAddress(query);
  console.log('Suggestions:', results);
};
</script>
```

## Deployment Steps

### On NEXUS-TEST Server:

1. **Update Frontend .env**:
```bash
cd ~/api-frontend
nano .env
# Change VITE_NOMINATIM_URL=http://localhost:8083
```

2. **Rebuild Frontend**:
```bash
docker-compose down
docker-compose up -d --build
```

3. **Update TES**:
```bash
cd ~/TES
# application.yml already updated in repo
docker-compose restart tes-service
```

4. **Verify Nominatim is Running**:
```bash
docker ps | grep nominatim
curl "http://localhost:8083/search?q=Warsaw&format=json&limit=1"
```

## Troubleshooting

### Issue: "Connection refused" from frontend

**Solution**: Check if Nominatim is accessible from frontend container:
```bash
docker exec apidanxils-frontend ping nominatim-service
docker exec apidanxils-frontend curl http://nominatim-service:8080/search?q=test&format=json
```

If using docker network, use service name instead:
```env
VITE_NOMINATIM_URL=http://nominatim-service:8080
```

### Issue: CORS errors in browser

**Solution**: Add CORS headers to Nominatim or use proxy.

For proxy approach, keep:
```env
VITE_NOMINATIM_URL=/nominatim
```

And configure Nginx/Apache to proxy `/nominatim` to `http://localhost:8083`.

### Issue: No suggestions appearing

**Check**:
1. Browser console for errors
2. Network tab for API calls
3. Nominatim logs: `docker logs nominatim-service`

## Expected Behavior

After configuration:

1. **Address Autocomplete**: Type in address field → see suggestions from Nominatim
2. **Address Verification**: TES verifies addresses using local Nominatim
3. **No External Calls**: All geocoding stays on local network

## Performance Notes

- **Nominatim Response Time**: ~100-500ms (local)
- **Cache**: Consider adding Redis cache for frequent queries
- **Rate Limiting**: Not needed for local instance
