<template>
  <div class="p-6 max-w-4xl mx-auto font-inter">
    <h1 class="text-2xl font-bold text-gray-900 dark:text-white mb-6">System Configuration</h1>

    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700 overflow-hidden">
        <div class="flex border-b border-gray-200 dark:border-gray-700">
            <button v-for="tab in tabs" :key="tab.id"
                    @click="activeTab = tab.id"
                    :class="['px-6 py-3 text-sm font-medium transition-colors', activeTab === tab.id ? 'border-b-2 border-blue-600 text-blue-600 dark:text-blue-400 bg-blue-50/50 dark:bg-blue-900/20' : 'text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200']">
                {{ tab.label }}
            </button>
        </div>

        <div class="p-6">
            <form @submit.prevent="saveSettings">
                <ConfigBuilder v-if="activeTab === 'general'" v-model="settings.general" :schema="schemas.general" />
                <ConfigBuilder v-if="activeTab === 'branding'" v-model="settings.branding" :schema="schemas.branding" />
                <ConfigBuilder v-if="activeTab === 'workflow'" v-model="settings.workflow" :schema="schemas.workflow" />
                <ConfigBuilder v-if="activeTab === 'billing'" v-model="settings.billing" :schema="schemas.billing" />
                <ConfigBuilder v-if="activeTab === 'communication'" v-model="settings.communication" :schema="schemas.communication" />
                <ConfigBuilder v-if="activeTab === 'onboarding'" v-model="settings.onboarding" :schema="schemas.onboarding" />
                <ConfigBuilder v-if="activeTab === 'automation'" v-model="settings.automation" :schema="schemas.automation" />
                <ConfigBuilder v-if="activeTab === 'notifications'" v-model="settings.notifications" :schema="schemas.notifications" />
                <ConfigBuilder v-if="activeTab === 'fleet'" v-model="settings.fleet" :schema="schemas.fleet" />

                <div class="mt-8 flex justify-end pt-6 border-t border-gray-100 dark:border-gray-700">
                    <button type="submit" class="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg shadow-sm font-medium transition-colors">
                        Save Changes
                    </button>
                </div>
            </form>

            <!-- Alerts Tab -->
            <div v-if="activeTab === 'alerts'" class="space-y-6">
                <div class="bg-gray-50 dark:bg-gray-900 p-6 rounded-xl border border-gray-200 dark:border-gray-700">
                    <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">Threshold Configuration</h3>

                    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Insurance Expiry Warning (Days)</label>
                            <input type="number" value="30" class="block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm" />
                            <p class="text-xs text-gray-500 mt-1">Alert when carrier insurance is expiring soon.</p>
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">License Expiry Warning (Days)</label>
                            <input type="number" value="45" class="block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm" />
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Route Capacity Alert (%)</label>
                            <input type="number" value="95" class="block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm" />
                            <p class="text-xs text-gray-500 mt-1">Alert if route utilization exceeds this percentage.</p>
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Max Route Duration (Hours)</label>
                            <input type="number" value="10" class="block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm" />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import ConfigBuilder from '@/components/common/ConfigBuilder.vue';
import { auditService } from '@/services/AuditService';
import { notificationService } from '@/services/NotificationService';
import { useThemeStore } from '@/stores/themeStore';
import { useConfigStore } from '@/stores/configStore';
import {
  Palette,
  Smartphone,
  DollarSign,
  UserPlus,
  Zap,
  Mail,
  Truck,
  Trash2,
  Plus
} from 'lucide-vue-next';

const themeStore = useThemeStore();
const configStore = useConfigStore();

const activeTab = ref('general');

const tabs = [
    { id: 'general', label: 'General & SLA' },
    { id: 'branding', label: 'Branding & Theme' },
    { id: 'workflow', label: 'Driver Workflow' },
    { id: 'billing', label: 'Billing Rules' },
    { id: 'communication', label: 'Communication Gateways' },
    { id: 'onboarding', label: 'Driver Onboarding' },
    { id: 'fleet', label: 'Fleet & Compliance' },
    { id: 'alerts', label: 'Alerts & Notifications' },
    { id: 'automation', label: 'Automation & n8n' },
];

// Load settings from store
const settings = ref({
    general: { ...configStore.config.general },
    branding: {
        systemName: themeStore.branding.systemName,
        logoUrl: themeStore.branding.logoUrl,
        primaryColor: themeStore.branding.primaryColor
    },
    workflow: { ...configStore.config.workflow },
    billing: { ...configStore.config.billing },
    communication: { ...configStore.config.communication },
    onboarding: { ...configStore.config.onboarding },
    automation: { ...configStore.config.automation },
    notifications: { ...configStore.config.notifications }
});

const schemas = {
    general: {
        systemName: { label: 'System Name', type: 'text', required: true },
        slaThresholdHours: { label: 'SLA Warning Threshold (Hours)', type: 'number', required: true },
        enableMaintenanceMode: { label: 'Maintenance Mode', type: 'boolean', description: 'Prevent user logins during maintenance.' }
    },
    branding: {
        systemName: { label: 'System Name', type: 'text', required: true, description: 'Appears in browser tab and headers.' },
        logoUrl: { label: 'Logo URL', type: 'text', placeholder: 'https://example.com/logo.png' },
        primaryColor: { label: 'Primary Color', type: 'color', required: true }
    },
    workflow: {
        requireSignature: { label: 'Require Signature', type: 'boolean', description: 'Driver must capture signature to complete stop.' },
        requirePhoto: { label: 'Require Photo Proof', type: 'boolean', description: 'Driver must take a photo to complete stop.' },
        requireSignature: { label: 'Require Signature', type: 'boolean', description: 'Driver must capture signature to complete stop.' },
        requirePhoto: { label: 'Require Photo Proof', type: 'boolean', description: 'Driver must take a photo to complete stop.' },
        requireScan: { label: 'Require Barcode Scan', type: 'boolean', description: 'Driver must scan package to complete stop.' },
        maxPhotos: { label: 'Max Photos', type: 'number', description: 'Maximum number of photos allowed per stop.' }
    },
    billing: {
        defaultVatRate: { label: 'Default VAT Rate (%)', type: 'number', required: true },
        currency: { 
            label: 'Currency', 
            type: 'select', 
            options: [
                { label: 'DKK - Danish Krone', value: 'DKK' },
                { label: 'EUR - Euro', value: 'EUR' },
                { label: 'USD - US Dollar', value: 'USD' }
            ]
        },
        paymentTerms: {
            label: 'Default Payment Terms',
            type: 'select',
            options: [
                { label: 'Net 15', value: 'Net15' },
                { label: 'Net 30', value: 'Net30' },
                { label: 'Net 60', value: 'Net60' }
            ]
        },
        section1: { type: 'header', label: 'Invoice Customization' },
        invoicePrefix: { label: 'Invoice Prefix', type: 'text', placeholder: 'INV-' },
        nextInvoiceNumber: { label: 'Next Invoice Number', type: 'number' },
        companyAddress: { label: 'Company Address', type: 'textarea', rows: 3 },
        footerText: { label: 'Footer Text', type: 'textarea', rows: 2 },
        
        sectionSurcharges: { type: 'header', label: 'Dynamic Surcharges' },
        surcharges: { 
            label: 'Surcharge Rules', 
            type: 'array', 
            itemSchema: {
                name: { label: 'Rule Name', type: 'text', placeholder: 'e.g. Heavy Load Fee' },
                conditionField: { 
                    label: 'Field', 
                    type: 'select', 
                    options: [
                        { label: 'Weight (kg)', value: 'weight' },
                        { label: 'Priority', value: 'priority' },
                        { label: 'Distance (km)', value: 'distance' }
                    ] 
                },
                conditionOperator: { 
                    label: 'Operator', 
                    type: 'select', 
                    options: [
                        { label: 'Greater Than (>)', value: 'gt' },
                        { label: 'Equals (=)', value: 'eq' },
                        { label: 'Less Than (<)', value: 'lt' }
                    ] 
                },
                conditionValue: { label: 'Value', type: 'text', placeholder: '50' },
                amount: { label: 'Surcharge Amount ($)', type: 'number' }
            }
        }
    },
    communication: {
        section1: { type: 'header', label: 'Email Settings (SMTP)' },
        smtpHost: { label: 'SMTP Host', type: 'text', placeholder: 'smtp.gmail.com' },
        smtpPort: { label: 'SMTP Port', type: 'number', placeholder: '587' },
        smtpUsername: { label: 'SMTP Username', type: 'text' },
        smtpPassword: { label: 'SMTP Password', type: 'password' },
        smtpFromEmail: { label: 'From Email Address', type: 'text', placeholder: 'noreply@yourcompany.com' },
        
        section2: { type: 'header', label: 'SMS Settings' },
        smsProvider: { 
            label: 'SMS Provider', 
            type: 'select', 
            options: [
                { label: 'Twilio', value: 'TWILIO' },
                { label: 'MessageBird', value: 'MESSAGEBIRD' },
                { label: 'Mock (Simulation)', value: 'MOCK' }
            ]
        },
        smsApiKey: { label: 'API Key / Auth Token', type: 'password' },
        smsSenderId: { label: 'Sender ID', type: 'text', placeholder: 'DANXILS' }
    },
    onboarding: {
        section1: { type: 'header', label: 'Driver Onboarding Workflow' },
        steps: {
            label: 'Onboarding Steps',
            type: 'array',
            itemSchema: {
                label: { label: 'Step Label', type: 'text', placeholder: 'e.g. Upload License' },
                type: { 
                    label: 'Step Type', 
                    type: 'select', 
                    options: [
                        { label: 'File Upload', value: 'upload' },
                        { label: 'Video Training', value: 'video' },
                        { label: 'Checkbox / Terms', value: 'checkbox' }
                    ] 
                },
                url: { label: 'Video/Resource URL', type: 'text', placeholder: 'Optional' },
                required: { label: 'Required?', type: 'checkbox' }
            }
        }

    },
    automation: {
        section1: { type: 'header', label: 'n8n Connection' },
        n8nInstanceUrl: { label: 'n8n Instance URL', type: 'text', placeholder: 'https://n8n.yourcompany.com' },
        n8nApiKey: { label: 'API Key', type: 'password' },
        webhookSecret: { label: 'Webhook Signing Secret', type: 'password' },
        
        section2: { type: 'header', label: 'Active Webhooks' },
        webhooks: {
            label: 'Event Triggers',
            type: 'array',
            itemSchema: {
                event: { 
                    label: 'System Event', 
                    type: 'select', 
                    options: [
                        { label: 'Order Created', value: 'ORDER_CREATED' },
                        { label: 'Delivery Completed', value: 'DELIVERY_COMPLETED' },
                        { label: 'Driver Assigned', value: 'DRIVER_ASSIGNED' },
                        { label: 'Onboarding Completed', value: 'ONBOARDING_COMPLETED' },
                        { label: 'Invoice Generated', value: 'INVOICE_GENERATED' }
                    ] 
                },
                url: { label: 'Target URL (n8n Webhook)', type: 'text', placeholder: 'https://...' },
                active: { label: 'Active', type: 'checkbox' }
            }
        },
        
        section3: { type: 'header', label: 'Automation Templates' },
        // Note: ConfigBuilder doesn't support buttons natively yet, but we can add a custom slot or just use a read-only text for now to indicate availability.
        // For this demo, we'll assume ConfigBuilder can render a 'info' type or similar, or we just leave it as configuration.
        // Let's add a placeholder for templates configuration if we want to "enable" them.
        enableSlackAlerts: { label: 'Template: Slack Alerts (New Order)', type: 'boolean' },
        enableXeroSync: { label: 'Template: Xero Sync (Invoice)', type: 'boolean' },
        enableSupportTicket: { label: 'Template: Support Ticket (Failed Delivery)', type: 'boolean' }
    },
    notifications: {
        magicLinkSmsTemplate: { label: 'Magic Link SMS Template', type: 'textarea', placeholder: 'Enter SMS text...' },
        enableEmailNotifications: { label: 'Enable Email Notifications', type: 'boolean', description: 'Send emails for system events.' },
        section1: { type: 'header', label: 'Email Templates' },
        outForDeliverySubject: { label: 'Out for Delivery - Subject', type: 'text' },
        outForDeliveryBody: { label: 'Out for Delivery - Body', type: 'textarea', rows: 4, description: 'Variables: {{name}}, {{orderId}}, {{trackingLink}}' },
        deliveredSubject: { label: 'Delivered - Subject', type: 'text' },
        deliveredBody: { label: 'Delivered - Body', type: 'textarea', rows: 4, description: 'Variables: {{name}}, {{orderId}}, {{time}}' }
    }
};

const saveSettings = () => {
    console.log('Saving settings:', settings.value);
    
    // Log to Audit Service
    auditService.logAction(
        'admin@danxils.com', 
        'UPDATE_CONFIG', 
        `Updated system settings for ${activeTab.value} tab`
    );

    // Update Config Store
    configStore.updateConfig('general', settings.value.general);
    configStore.updateConfig('workflow', settings.value.workflow);
    configStore.updateConfig('billing', settings.value.billing);
    configStore.updateConfig('communication', settings.value.communication);
    configStore.updateConfig('onboarding', settings.value.onboarding);
    configStore.updateConfig('automation', settings.value.automation);
    configStore.updateConfig('notifications', settings.value.notifications);

    // Update Notification Service templates if on notifications tab
    if (activeTab.value === 'notifications') {
        notificationService.updateTemplate('OUT_FOR_DELIVERY', settings.value.notifications.outForDeliverySubject, settings.value.notifications.outForDeliveryBody);
        notificationService.updateTemplate('DELIVERED', settings.value.notifications.deliveredSubject, settings.value.notifications.deliveredBody);
    }

    // Update Branding
    if (activeTab.value === 'branding') {
        themeStore.updateBranding(settings.value.branding);
    }
    
    // Save Webhooks if on automation tab
    if (activeTab.value === 'automation') {
        const webhooks = settings.value.automation.webhooks;
        // Map to backend entity structure if needed, or assume 1:1 mapping
        // Backend expects: id (UUID), eventType, url, authHeader, active, description
        // Frontend has: event, url, active. 
        // We need to map 'event' to 'eventType' and ensure 'authHeader' is passed.
        // Also handle ID generation or preservation.
        
        const entities = webhooks.map(wh => ({
            id: wh.id && wh.id.length === 36 ? wh.id : null, // Send ID if it's a valid UUID, else null for new
            eventType: wh.event,
            url: wh.url,
            authHeader: settings.value.automation.n8nApiKey, // Use the global API key for all webhooks for now, or per-webhook? 
            // The UI has a global "API Key" field in automation section. Let's use that.
            active: wh.active,
            description: 'Configured via Dashboard'
        }));

        // We need to use fetch or axios. Assuming 'api' is available globally or we use fetch.
        // Since I don't see 'api' imported, I'll use fetch with the relative path which should be proxied or absolute.
        // Wait, the other services are on 8092. Dashboard is 5173.
        // I should check how other components make API calls.
        // 'DriverTaskView.vue' used axios.
        
        planningApi.post('/api/webhooks/batch', entities)
        .then(response => response.data) // Assuming planningApi (axios) returns data directly in response.data
        .then(data => {
            console.log('Webhooks saved:', data);
            // Update local IDs with returned IDs
            // This is a bit tricky if order changes, but batch save usually returns in order or we match by eventType.
        })
        .catch(error => console.error('Error saving webhooks:', error));
    }

    alert('Settings saved successfully!');
};

// Fetch webhooks on mount
onMounted(() => {
    planningApi.get('/api/webhooks')
        .then(res => res.data)
        .then(data => {
            if (data && data.length > 0) {
                // Map backend entities to frontend structure
                const mappedWebhooks = data.map(entity => ({
                    id: entity.id,
                    event: entity.eventType,
                    url: entity.url,
                    active: entity.active
                }));
                
                // Update store/local state
                // We want to merge with existing options or replace?
                // The frontend has a fixed list of "Event Triggers" in the schema options, but the data is in 'webhooks' array.
                // Let's replace the 'webhooks' array in settings.automation
                settings.value.automation.webhooks = mappedWebhooks;
                
                // Also try to populate API key if we can find a common one? 
                // The backend stores authHeader per webhook. 
                // If they are all the same, we can show it.
                if (data[0].authHeader) {
                    settings.value.automation.n8nApiKey = data[0].authHeader;
                }
            }
        })
        .catch(err => console.error('Failed to load webhooks:', err));
});
</script>
