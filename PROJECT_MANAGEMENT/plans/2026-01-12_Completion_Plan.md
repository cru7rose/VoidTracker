# 🎯 VoidTracker Completion Plan
**Based on Walkthrough Verification Report**

**Date:** 2026-01-12  
**Status:** In Progress  
**Priority:** High

---

## 📊 Current Status Summary

| Category | Count | Priority |
|----------|-------|----------|
| ✅ Fully Implemented | 6 | - |
| ⚠️ Partially Implemented | 2 | HIGH |
| ❌ Missing | 2 | HIGH |

---

## 🎯 Implementation Plan

### Phase 1: Address Issue Logging System (HIGH PRIORITY)

**Objective:** Implement missing `AddressIssueLoggerService` mentioned in walkthrough

**Tasks:**
1. ✅ Create `AddressIssueEntity` - Entity for storing address issues
2. ✅ Create `AddressIssueRepository` - JPA repository
3. ✅ Create `AddressIssueLoggerService` - Service for logging issues
4. ✅ Create `AddressMediatorService` - Mediator between verification and logging
5. ✅ Integrate with `OrderVerificationService` for TES timeouts
6. ✅ Add REST API endpoints for issue management

**Files to Create:**
- `modules/nexus/order-service/src/main/java/com/example/order_service/entity/AddressIssueEntity.java`
- `modules/nexus/order-service/src/main/java/com/example/order_service/repository/AddressIssueRepository.java`
- `modules/nexus/order-service/src/main/java/com/example/order_service/service/AddressIssueLoggerService.java`
- `modules/nexus/order-service/src/main/java/com/example/order_service/service/AddressMediatorService.java`
- `modules/nexus/order-service/src/main/java/com/example/order_service/controller/AddressIssueController.java`

**Integration Points:**
- `OrderVerificationService` - Log TES timeouts
- `AddressVerificationService` - Log normalization failures
- Address conflict detection

---

### Phase 2: Gatekeeper Agent Logic Completion (HIGH PRIORITY)

**Current Status:** 50% (Backend monitoring ✅, n8n webhook ⚠️, LLM ❌, UI ❌)

**Tasks:**
1. ✅ Verify n8n webhook code (currently active in GatekeeperService line 83)
2. ✅ Add LLM integration for justification generation (Polish language)
3. ✅ Create Vue component for approval flow (`GatekeeperApprovalModal.vue`)
4. ✅ Add API endpoint for approval/rejection
5. ✅ Update route publishing to require approval if flagged

**Files to Modify:**
- `modules/flux/planning-service/src/main/java/com/example/planning_service/service/GatekeeperService.java` - Add LLM call
- `modules/flux/planning-service/src/main/java/com/example/planning_service/controller/GatekeeperController.java` - New controller

**Files to Create:**
- `modules/web/voidtracker-web/src/components/GatekeeperApprovalModal.vue`
- `modules/web/voidtracker-web/src/stores/gatekeeperStore.js`

**Integration:**
- Connect to LLM API (OpenAI/Anthropic) for Polish justification
- n8n webhook already active (line 83 in GatekeeperService)

---

### Phase 3: High-Fidelity Dashboard WebSocket (MEDIUM PRIORITY)

**Current Status:** 40% (Backend config ✅, Frontend client ❌)

**Tasks:**
1. ✅ Install SockJS and STOMP.js in frontend
2. ✅ Create WebSocket service/store (`websocketStore.js`)
3. ✅ Connect to `/ws-planning` endpoint
4. ✅ Subscribe to `/topic/optimization-updates`
5. ✅ Update map in real-time on `BestSolutionChangedEvent`
6. ✅ Progressive UI updates during Timefold solving

**Files to Create:**
- `modules/web/voidtracker-web/src/stores/websocketStore.js`
- `modules/web/voidtracker-web/src/composables/useWebSocket.js`

**Files to Modify:**
- `modules/web/voidtracker-web/src/views/internal/DispatchView.vue` - Add WebSocket integration
- `modules/flux/planning-service/src/main/java/com/example/planning_service/optimization/impl/TimefoldOptimizer.java` - Ensure broadcasting

**Dependencies:**
- `sockjs-client` npm package
- `@stomp/stompjs` npm package

---

### Phase 4: Elastic Shell Integration (LOW PRIORITY - Future)

**Status:** Not implemented (mentioned in walkthrough but not verified)

**Note:** This is a complex feature requiring Timefold shadow variables. Defer to future sprint.

**Tasks (Future):**
- [ ] Create `FixedRouteShell` domain class
- [ ] Implement shadow variables (`remainingCapacity`, `availableTimeWindow`)
- [ ] Add constraint for detour distance
- [ ] UI for configuring max detour in Vue

---

## 📅 Implementation Timeline

### Week 1 (Jan 12-18)
- [x] Day 1: Create completion plan
- [ ] Day 2-3: Phase 1 - Address Issue Logging System
- [ ] Day 4-5: Phase 2 - Gatekeeper completion (backend + LLM)

### Week 2 (Jan 19-25)
- [ ] Day 1-2: Phase 2 - Gatekeeper UI approval flow
- [ ] Day 3-4: Phase 3 - WebSocket frontend client
- [ ] Day 5: Testing & integration

---

## 🔧 Technical Decisions

### Address Issue Logging
- **Storage:** PostgreSQL table `address_issue`
- **Issue Types:** `TES_TIMEOUT`, `NORMALIZATION_FAILURE`, `CONFLICT_DETECTED`
- **Severity Levels:** `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`

### Gatekeeper LLM Integration
- **Provider:** OpenAI GPT-4 or Anthropic Claude (via API)
- **Language:** Polish (pl-PL)
- **Prompt Template:** "Wyjaśnij dlaczego ta optymalizacja wymaga zatwierdzenia: {warnings}, {score_change}%"
- **Caching:** Cache justifications for 1 hour

### WebSocket Architecture
- **Protocol:** STOMP over SockJS
- **Reconnection:** Exponential backoff (1s, 2s, 4s, 8s, max 30s)
- **Message Format:** JSON with `OptimizationUpdateDto`

---

## ✅ Success Criteria

### Phase 1 Complete When:
- [ ] Address issues are logged for TES timeouts
- [ ] Address issues are logged for normalization failures
- [ ] REST API returns issue list
- [ ] Integration tested with OrderVerificationService

### Phase 2 Complete When:
- [ ] n8n webhook triggers on significant score changes
- [ ] LLM generates Polish justification
- [ ] Vue modal shows approval request
- [ ] Routes require approval before publishing

### Phase 3 Complete When:
- [ ] WebSocket connects successfully
- [ ] Map updates in real-time during optimization
- [ ] No performance degradation
- [ ] Reconnection works after network issues

---

## 📝 Notes

- All implementations should follow existing code patterns
- Use existing error handling and logging patterns
- Maintain backward compatibility
- Add unit tests for new services
- Update documentation after each phase

---

**Last Updated:** 2026-01-12  
**Next Review:** After Phase 1 completion
