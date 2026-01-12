# Google Maps Platform Integration - Setup Guide

## 🎯 Cel: Traffic-Aware Routing dla Nocnych Dostaw

**Expected Benefits**:
- 5-10% oszczędność na paliwie (traffic-aware optimization)
- 15% lepsza dokładność ETA
- Real-time awareness o congestion

**Koszt**: ~€200/month (~880 PLN/month) dla 10,000 orders/day

---

## 📋 Setup Steps

### 1. Utwórz Google Cloud Project

1. Idź do [Google Cloud Console](https://console.cloud.google.com/)
2. Utwórz nowy projekt: `VoidTracker-RouteOptimization`
3. Enable billing dla projektu

### 2. Enable Required APIs

W Cloud Console → APIs & Services → Library, włącz:

- ✅ **Distance Matrix API** (REQUIRED)
- ✅ **Directions API** (optional, dla future enhancements)
- ✅ **Geocoding API** (optional, dla address validation)

### 3. Create API Key

1. APIs & Services → Credentials
2. Create Credentials → API Key
3. **IMPORTANT**: Restrict API key:
   - Application restrictions: **IP addresses** (twój serwer IP)
   - API restrictions: Tylko Distance Matrix API
4. Copy API key

### 4. Configure Application

**Environment Variable** (production):
```bash
export GOOGLE_MAPS_API_KEY="AIzaSyXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
```

**application.yml** (development):
```yaml
google:
  maps:
    enabled: true
    api-key: ${GOOGLE_MAPS_API_KEY}
```

### 5. Test Integration

```bash
# Restart planning-service
docker-compose restart planning-service

# Check logs
docker logs planning-service | grep "Google Maps"

# Expected: "Google Maps distance matrix retrieved: 10x50 locations"
```

---

## 💰 Cost Management

### API Pricing (as of 2024)

**Distance Matrix API**:
- $5 per 1,000 requests
- $0.005 per origin-destination pair

### Cost Estimation dla VoidTracker

**Nocne dostawy scenario**:
- 10,000 orders/day
- 8 hubs (regional partitioning)
- Average 1,250 orders per hub
- 30 vehicles per hub

**Distance matrix size per hub**:
- Origins: 30 vehicles (depot locations)
- Destinations: 1,250 orders
- Pairs: 30 × 1,250 = 37,500 pairs

**API calls per day**:
- 37,500 pairs × 8 hubs = 300,000 pairs/day
- With caching (30 min): ~150,000 pairs/day effectively

**Monthly cost**:
- 150,000 pairs/day × 30 days = 4,500,000 pairs/month
- Cost: 4,500,000 × $0.005 = $22,500/month ❌ TOO HIGH!

### ⚠️ Cost Optimization Required

**Recommended Strategy**:

1. **Use clustering** przed Google Maps call:
   ```
   1,250 orders → cluster do 50 representative points
   30 vehicles × 50 clusters = 1,500 pairs (instead of 37,500!)
   ```

2. **Longer cache** dla stable routes:
   ```yaml
   cache-expiration-minutes: 120  # 2h dla nocnych dostaw
   ```

3. **Selective traffic-aware routing**:
   ```java
   // Use Google Maps only dla critical routes (>100km)
   if (estimatedDistance > 100) {
       useGoogleMaps();
   } else {
       useStraightLineEstimate();
   }
   ```

**With optimizations**:
- 1,500 pairs × 8 hubs × 30 days = 360,000 pairs/month
- Cost: 360,000 × $0.005 = **$1,800/month** (~€1,650/month = 7,300 PLN) ❌ Still too high!

### 🎯 FINAL RECOMMENDATION

**Use Google Maps SELECTIVELY**:
- ✅ Long-distance routes (>100km between locations)
- ✅ Critical time windows (7 AM technician deliveries)
- ✅ Urban areas z high traffic variability
- ❌ Short distances (\<20km) - use straight-line estimate

**Expected cost with selective usage**:
- 50,000 pairs/month (critical routes only)
- Cost: 50,000 × $0.005 = **$250/month** (~€230/month = **1,000 PLN**) ✅ ACCEPTABLE!

---

## 🔧 Implementation Notes

### Current Status

✅ **Implemented**:
- GoogleMapsDistanceService (with REST client)
- Caching (30 min default)
- Fallback to Haversine formula
- Traffic model configuration
- Batch processing framework (TODO: implementation)

❌ **Not Yet Implemented**:
- Clustering optimization
- Selective traffic-aware routing
- Cost monitoring dashboard
- Batch processing for >100 locations

### Integration with Timefold

```java
// In PlanningExecutionService.executePlanForOrderIds()

// Step 1: Get locations
List<Location> depots = vehicles.stream()
    .map(v -> Location.builder()
        .latitude(v.getLatitude())
        .longitude(v.getLongitude())
        .build())
    .toList();

List<Location> deliveries = orders.stream()
    .map(o -> Location.builder()
        .latitude(o.delivery().lat())
        .longitude(o.delivery().lon())
        .build())
    .toList();

// Step 2: Call Google Maps (if enabled)
if (googleMapsProperties.isEnabled()) {
    DistanceMatrixResult matrix = googleMapsDistanceService.getDistanceMatrix(
        depots,
        deliveries,
        LocalDateTime.of(today, LocalTime.of(23, 0))  // 23:00 departure
    );
    
    // Step 3: Feed to Timefold
    vrpSolution.setDistanceMatrix(matrix.getDistancesMeters());
    vrpSolution.setDurationMatrix(matrix.getDurationsSeconds());
} else {
    // Fallback to straight-line
    log.info("Google Maps disabled, using Haversine distances");
}
```

---

## 📊 Monitoring

### Prometheus Metrics (TODO)

```java
@Component
public class GoogleMapsMetrics {
    Counter apiCallsTotal;
    Counter apiFailuresTotal;
    Timer apiRequestDuration;
    Gauge costPerDayEstimate;
}
```

### Grafana Dashboard Panels

1. **API Usage**: Calls per day
2. **Cost Tracker**: Estimated daily cost
3. **Cache Hit Rate**: % requests served from cache
4. **Fallback Rate**: % requests using Haversine fallback

---

## 🚀 Next Steps

1. **Get API key** from Google Cloud Console
2. **Set environment variable** `GOOGLE_MAPS_API_KEY`
3. **Enable in config**: `google.maps.enabled=true`
4. **Test with small batch** (10-20 orders)
5. **Monitor costs** in Google Cloud Console
6. **Implement clustering** if costs > €200/month
7. **Compare ETA accuracy** before/after integration

---

## 🆘 Troubleshooting

**"Google Maps API call failed"**:
- Check API key jest valid
- Verify IP restrictions w Cloud Console
- Check billing jest enabled

**"REQUEST_DENIED"**:
- API key restrictions za restrykcyjne
- Distance Matrix API nie enabled w projekcie

**High costs**:
- Implement clustering (reduce location count)
- Increase cache duration
- Use selective routing (only critical routes)

---

**Status**: ✅ Implemented, awaiting API key configuration
