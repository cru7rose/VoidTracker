<template>
  <div class="p-6 max-w-7xl mx-auto font-inter">
    <div class="flex justify-between items-center mb-6">
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Communication Center</h1>
        <div class="flex gap-2">
            <button @click="simulateReceive" class="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700 flex items-center gap-2">
                <span>📥</span> Simulate Receive
            </button>
            <button @click="refreshLogs" class="px-4 py-2 bg-gray-100 text-gray-700 rounded hover:bg-gray-200 dark:bg-gray-700 dark:text-gray-200">
                Refresh
            </button>
        </div>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div class="bg-white dark:bg-gray-800 p-6 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700">
            <div class="text-sm text-gray-500 mb-1">Total Sent (Today)</div>
            <div class="text-3xl font-bold text-gray-900 dark:text-white">1,248</div>
            <div class="text-xs text-green-600 mt-2">↑ 12% vs yesterday</div>
        </div>
        <div class="bg-white dark:bg-gray-800 p-6 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700">
            <div class="text-sm text-gray-500 mb-1">Delivery Rate</div>
            <div class="text-3xl font-bold text-gray-900 dark:text-white">99.8%</div>
            <div class="text-xs text-gray-400 mt-2">Target: 99.5%</div>
        </div>
        <div class="bg-white dark:bg-gray-800 p-6 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700">
            <div class="text-sm text-gray-500 mb-1">Failed</div>
            <div class="text-3xl font-bold text-red-600">2</div>
            <div class="text-xs text-red-400 mt-2">Invalid numbers</div>
        </div>
    </div>

    <!-- Tabs -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700 overflow-hidden">
        <div class="flex border-b border-gray-200 dark:border-gray-700">
            <button v-for="tab in tabs" :key="tab.id"
                    @click="activeTab = tab.id"
                    :class="['px-6 py-3 text-sm font-medium transition-colors', activeTab === tab.id ? 'border-b-2 border-blue-600 text-blue-600 dark:text-blue-400 bg-blue-50/50 dark:bg-blue-900/20' : 'text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200']">
                {{ tab.label }}
            </button>
        </div>

        <!-- Simulator Tab -->
        <div v-if="activeTab === 'simulator'" class="p-6">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">Event Simulator</h3>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Event Type</label>
                    <select v-model="selectedEvent" class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white">
                        <option value="ORDER_CREATED">ORDER_CREATED</option>
                        <option value="DELIVERY_COMPLETED">DELIVERY_COMPLETED</option>
                        <option value="DRIVER_ASSIGNED">DRIVER_ASSIGNED</option>
                        <option value="ONBOARDING_COMPLETED">ONBOARDING_COMPLETED</option>
                        <option value="INVOICE_GENERATED">INVOICE_GENERATED</option>
                    </select>
                    
                    <div class="mt-4">
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Payload Preview</label>
                        <pre class="bg-gray-50 dark:bg-gray-900 p-4 rounded-lg text-xs font-mono text-gray-600 dark:text-gray-300 overflow-auto max-h-60 border border-gray-200 dark:border-gray-700">{{ JSON.stringify(mockPayload, null, 2) }}</pre>
                    </div>
                </div>
                
                <div>
                    <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Target Webhooks</h4>
                    <div v-if="activeWebhooks.length > 0" class="space-y-2 mb-6">
                        <div v-for="wh in activeWebhooks" :key="wh.id" class="flex items-center gap-2 text-sm p-2 bg-gray-50 dark:bg-gray-700 rounded border border-gray-200 dark:border-gray-600">
                            <span class="w-2 h-2 rounded-full bg-green-500"></span>
                            <span class="font-mono text-gray-600 dark:text-gray-300 truncate flex-1">{{ wh.url }}</span>
                        </div>
                    </div>
                    <div v-else class="text-sm text-gray-500 italic mb-6">
                        No active webhooks configured for this event. Go to Settings -> Automation to configure.
                    </div>

                    <button @click="fireEvent" :disabled="activeWebhooks.length === 0"
                            class="w-full py-2 px-4 bg-purple-600 hover:bg-purple-700 text-white font-bold rounded-lg shadow-sm transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
                        🔥 Fire Event
                    </button>
                    
                    <div v-if="simulationResult" class="mt-4 p-3 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded text-sm text-green-700 dark:text-green-300">
                        {{ simulationResult }}
                    </div>
                </div>
            </div>
        </div>

        <!-- Logs Table -->
        <div v-else class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                <thead class="bg-gray-50 dark:bg-gray-900">
                    <tr>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Time</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Direction</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Recipient/Sender</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Content</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                    </tr>
                </thead>
                <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                    <tr v-for="log in filteredLogs" :key="log.id" class="hover:bg-gray-50 dark:hover:bg-gray-700/50">
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ new Date(log.timestamp).toLocaleString() }}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm">
                            <span :class="['px-2 py-1 rounded text-xs font-bold', log.channel === 'EMAIL' ? 'bg-blue-100 text-blue-800' : 'bg-purple-100 text-purple-800']">
                                {{ log.channel }}
                            </span>
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm">
                            <span v-if="log.direction === 'OUTBOUND' || !log.direction" class="text-gray-600">↗️ Outbound</span>
                            <span v-else class="text-green-600 font-medium">↙️ Inbound</span>
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">{{ log.recipient }}</td>
                        <td class="px-6 py-4 text-sm text-gray-500 max-w-xs truncate" :title="log.messageContent">{{ log.messageContent }}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm">
                            <span v-if="log.status === 'DELIVERED' || log.status === 'SENT'" class="text-green-600">✅ {{ log.status }}</span>
                            <span v-else-if="log.status === 'FAILED'" class="text-red-600">❌ Failed</span>
                            <span v-else class="text-yellow-600">⏳ {{ log.status }}</span>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useConfigStore } from '@/stores/configStore';
// Use standard axios instance or create one. Using planningApi from existing patterns is better if imported.
// Importing planningApi
import { planningApi } from '../../../api/axios';

const configStore = useConfigStore();

const activeTab = ref('all');
const tabs = [
    { id: 'all', label: 'All Messages' },
    { id: 'email', label: 'Emails' },
    { id: 'sms', label: 'SMS' },
    { id: 'simulator', label: 'Event Simulator' }
];

// Simulator State
const selectedEvent = ref('ORDER_CREATED');
const simulationResult = ref('');

const activeWebhooks = computed(() => {
    const hooks = configStore.config.automation?.webhooks || [];
    return hooks.filter(h => h.active && h.event === selectedEvent.value);
});

const mockPayload = computed(() => {
    const base = {
        eventId: 'evt_' + Date.now(),
        timestamp: new Date().toISOString(),
        environment: 'production'
    };
    
    switch (selectedEvent.value) {
        case 'ORDER_CREATED':
            return { ...base, type: 'ORDER_CREATED', data: { orderId: 'ORD-123', customer: 'Acme Corp', items: 5, totalWeight: 150 } };
        case 'DELIVERY_COMPLETED':
            return { ...base, type: 'DELIVERY_COMPLETED', data: { orderId: 'ORD-123', deliveredAt: new Date().toISOString(), signedBy: 'John Doe' } };
        case 'DRIVER_ASSIGNED':
            return { ...base, type: 'DRIVER_ASSIGNED', data: { orderId: 'ORD-123', driverId: 'DRV-001', vehicleId: 'WX 12345' } };
        case 'ONBOARDING_COMPLETED':
            return { ...base, type: 'ONBOARDING_COMPLETED', data: { driverId: 'DRV-001', name: 'Alice Smith', licenseVerified: true } };
        case 'INVOICE_GENERATED':
            return { ...base, type: 'INVOICE_GENERATED', data: { invoiceId: 'INV-1001', amount: 1250.00, currency: 'DKK', dueDate: '2025-01-01' } };
        default:
            return base;
    }
});

const fireEvent = () => {
    simulationResult.value = `Event ${selectedEvent.value} fired to ${activeWebhooks.value.length} webhooks!`;
    
    // Add to logs
    activeWebhooks.value.forEach(wh => {
        logs.value.unshift({
            id: Date.now() + Math.random(),
            timestamp: new Date().toISOString(),
            channel: 'WEBHOOK',
            direction: 'OUTBOUND',
            recipient: wh.url,
            messageContent: `Event: ${selectedEvent.value}`,
            status: 'DELIVERED'
        });
    });
    
    setTimeout(() => simulationResult.value = '', 3000);
};

// Logs
const logs = ref([]);
const loading = ref(false);

const fetchLogs = async () => {
    loading.value = true;
    try {
        const response = await planningApi.get('/communications');
        logs.value = response.data;
    } catch (error) {
        console.error('Failed to fetch communication logs:', error);
    } finally {
        loading.value = false;
    }
};

const filteredLogs = computed(() => {
    if (activeTab.value === 'all') return logs.value;
    return logs.value.filter(log => log.channel === activeTab.value.toUpperCase());
});

const refreshLogs = () => {
    fetchLogs();
};

const simulateReceive = () => {
    const newLog = {
        id: Date.now(),
        timestamp: new Date().toISOString(),
        type: 'SMS',
        direction: 'INBOUND',
        contact: '+48 555 000 111',
        content: 'Driver arrived at location.',
        status: 'RECEIVED'
    };
    logs.value.unshift(newLog);
};

onMounted(() => {
    fetchLogs();
});
</script>
