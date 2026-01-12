# Order Service State Machine Documentation

## ⭐ EXCELLENT NEWS: State Machine Logic Already Exists!

The `OrderStatus` enum in `danxils-commons` **ALREADY HAS** built-in state machine validation!

### Actual OrderStatus States

```java
public enum OrderStatus {
    PENDING,  // Awaiting verification (address validation)
    NEW,      // Ready for driver assignment  

    PICKUP,   // Assigned for pickup
    PSIP,     // Pickup in progress (transfer/return)
    LOAD,     // Loaded on vehicle (scanned by driver)
    TERM,     // Scanned at terminal/hub
    POD       // Delivered with ePOD (TERMINAL STATE)
}
```

### ✅ Existing State Machine Methods

**1. `canTransitionTo(OrderStatus target)`** - Validates if transition is allowed
**2. `getAllowedNextStatuses()`** - Returns Set of valid next states
**3. `isTerminal()`** - Checks if state is final (POD)
**4. `isActive()`** - Checks if order is in transit
**5. `requiresDriverAction()`** - Checks if driver action needed

### Valid State Transitions (FROM SOURCE CODE)

```
PENDING
  ↓
NEW (must verify first)
  ↓
PICKUP (assigned for pickup)
  ├→ PSIP (pickup in progress) → LOAD → TERM → POD
  ├→ LOAD (direct load) → TERM → POD
  └→ POD (direct delivery)

PSIP (pickup in progress)
  ├→ LOAD → TERM → POD
  └→ POD (skip hub)

LOAD (on vehicle)
  ├→ TERM (hub scan) → POD
  └→ POD (direct delivery)

TERM (at terminal)
  ↓
POD (delivered - TERMINAL)
```

### Transition Rules Matrix

| From \ To | PENDING | NEW | PICKUP | PSIP | LOAD | TERM | POD |
|-----------|---------|-----|--------|------|------|------|-----|
| **PENDING** | - | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **NEW** | ❌ | - | ✅ | ❌ | ❌ | ❌ | ❌ |
| **PICKUP** | ❌ | ❌ | - | ✅ | ✅ | ❌ | ✅ |
| **PSIP** | ❌ | ❌ | ❌ | - | ✅ | ❌ | ✅ |
| **LOAD** | ❌ | ❌ | ❌ | ❌ | - | ✅ | ✅ |
| **TERM** | ❌ | ❌ | ❌ | ❌ | ❌ | - | ✅ |
| **POD** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | - |

✅ = Allowed transition
❌ = Blocked transition

### Examples of Blocked Transitions

❌ **Backwards:**
- POD → any (terminal state)
- TERM → LOAD
- LOAD → PICKUP

❌ **Skip States:**
- PENDING → PICKUP (must go through NEW)
- NEW → PSIP (must pickup first)
- PENDING → POD (must follow workflow)

### Kafka Events Per Transition

| Transition | Event Topic | Notes |
|------------|-------------|-------|
| → PENDING | `orders.created` | Initial creation |
| → PICKUP | `orders.assigned` | Driver assigned |
| Any status change | `orders.status.changed` | Generic status update |

### Usage in Order Service

**GOOD NEWS:** The validation logic exists, but we need to:

1. ✅ **Leverage existing `canTransitionTo()` method**
2. ⚠️ **Ensure Order Service actually CALLS this validation**
3. ⚠️ **Throw proper exceptions on invalid transitions**
4. ✅ **Use `getAllowedNextStatuses()` for API responses**

### Recommended Integration

```java
// In OrderService.updateStatus()
public void updateOrderStatus(UUID orderId, OrderStatus newStatus) {
    OrderEntity order = findById(orderId);
    OrderStatus currentStatus = order.getStatus();
    
    // USE EXISTING VALIDATION
    if (!currentStatus.canTransitionTo(newStatus)) {
        throw new InvalidStateTransitionException(
            currentStatus, newStatus,
            currentStatus.getAllowedNextStatuses()
        );
    }
    
    order.setStatus(newStatus);
    order.setLastStatusChange(Instant.now());
    repository.save(order);
    
    // Publish event
    publishStatusChangeEvent(order, currentStatus, newStatus);
}
```

### Phase 2 Simplified!

Since state machine logic exists, Phase 2 becomes:

1. ✅ Create exception classes (InvalidStateTransitionException, etc.)
2. ✅ Create GlobalExceptionHandler
3. ✅ **Integrate existing `canTransitionTo()` into OrderService**
4. ✅ Add proper error responses
5. ✅ **MUCH LESS WORK THAN EXPECTED!** 🎉

### Terminal State Protection

```java
public boolean isTerminal() {
    return this == POD;
}
```

POD (Proof of Delivery) is the ONLY terminal state. No transitions allowed from POD.

### Driver Action States

```java
public boolean requiresDriverAction() {
    return this == PICKUP || this == LOAD || this == TERM;
}
```

These states require driver to scan/complete action.

### Active States

```java
public boolean isActive() {
    return this == PICKUP || this == PSIP || this == LOAD || this == TERM;
}
```

Orders in these states are "in transit" and should be tracked actively.

---

## Summary

**🎉 EXCELLENT FOUNDATION:**
- ✅ State machine already implemented
- ✅ Validation logic exists
- ✅ Helper methods for UI/API
- ⚠️ Just need to INTEGRATE into OrderService
- ⚠️ Add proper exception handling

**This significantly reduces Phase 2 complexity!**
