import { ref } from 'vue';

// Mock Audit Service
class AuditService {
    constructor() {
        this.logs = ref([
            { id: 1, timestamp: new Date(Date.now() - 1000000).toISOString(), user: 'admin@voidtracker.com', action: 'UPDATE_CONFIG', details: 'Changed VAT Rate to 25%' },
            { id: 2, timestamp: new Date(Date.now() - 5000000).toISOString(), user: 'system', action: 'AUTO_SYNC', details: 'Synced 50 orders from ERP' },
            { id: 3, timestamp: new Date(Date.now() - 86400000).toISOString(), user: 'manager@voidtracker.com', action: 'USER_LOGIN', details: 'Login successful' }
        ]);
    }

    getLogs() {
        return this.logs;
    }

    logAction(user, action, details) {
        const newLog = {
            id: Date.now(),
            timestamp: new Date().toISOString(),
            user,
            action,
            details
        };
        this.logs.value.unshift(newLog);
        console.log('Audit Log:', newLog);
    }
}

export const auditService = new AuditService();
