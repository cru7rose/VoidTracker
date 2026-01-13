import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useConfigStore = defineStore('config', () => {
    // Default Configuration
    const config = ref({
        general: {
            systemName: 'VoidTracker',
            slaThresholdHours: 24,
            enableMaintenanceMode: false
        },
        workflow: {
            requireSignature: true,
            requirePhoto: true,
            requireSignature: true,
            requirePhoto: true,
            requireScan: true,
            maxPhotos: 3
        },
        billing: {
            defaultVatRate: 25,
            currency: 'DKK',
            paymentTerms: 'Net30',
            invoicePrefix: 'INV-',
            nextInvoiceNumber: 1001,
            companyAddress: '123 Logistics Way, Transport City',
            footerText: 'Thank you for your business!',
            surcharges: [
                { id: 'S001', name: 'Heavy Weight Fee', conditionField: 'weight', conditionOperator: 'gt', conditionValue: 50, amount: 25.00 },
                { id: 'S002', name: 'Express Delivery', conditionField: 'priority', conditionOperator: 'eq', conditionValue: 'HIGH', amount: 15.00 }
            ]
        },
        notifications: {
            magicLinkSmsTemplate: 'Your login link: {{link}}',
            enableEmailNotifications: true,
            outForDeliverySubject: 'Your package is on the way! 🚚',
            outForDeliveryBody: 'Hello {{name}},\n\nYour package {{orderId}} is out for delivery today.',
            deliveredSubject: 'Package Delivered! ✅',
            deliveredBody: 'Hello {{name}},\n\nYour package {{orderId}} has been delivered.'
        },
        communication: {
            // Email (SMTP)
            smtpHost: 'smtp.example.com',
            smtpPort: 587,
            smtpUsername: 'user@example.com',
            smtpPassword: '', // In real app, handle securely
            smtpFromEmail: 'noreply@voidtracker.com',

            // SMS
            smsProvider: 'TWILIO', // TWILIO, MESSAGEBIRD, MOCK
            smsApiKey: '',
            smsSenderId: 'DANXILS'
        },
        onboarding: {
            onboardingSteps: [
                { id: 'license', label: 'Upload Driver License', type: 'upload', required: true },
                { id: 'video', label: 'Watch Safety Video', type: 'video', required: true },
                { id: 'quiz', label: 'Complete Safety Quiz', type: 'quiz', required: true },
                { id: 'terms', label: 'Accept Terms & Conditions', type: 'checkbox', required: true }
            ]
        },
        dvir: {
            enabled: true,
            questions: [
                { id: 'tires', label: 'Check Tires (Pressure & Tread)', type: 'pass_fail' },
                { id: 'lights', label: 'Check Lights (Headlights, Brake, Turn)', type: 'pass_fail' },
                { id: 'brakes', label: 'Check Brakes', type: 'pass_fail' },
                { id: 'fluids', label: 'Check Fluids (Oil, Coolant)', type: 'pass_fail' },
                { id: 'body', label: 'Check Body for Damage', type: 'photo' }
            ]
        },
        automation: {
            n8nInstanceUrl: 'https://n8n.example.com',
            n8nApiKey: '',
            webhookSecret: 'whsec_sample_secret',
            webhooks: [
                { id: 'wh_001', event: 'ORDER_CREATED', url: 'https://n8n.example.com/webhook/order-created', active: true },
                { id: 'wh_002', event: 'DELIVERY_COMPLETED', url: 'https://n8n.example.com/webhook/delivery-completed', active: true },
                { id: 'wh_003', event: 'DRIVER_ASSIGNED', url: '', active: false }
            ]
        }
    });

    const updateConfig = (section, newConfig) => {
        config.value[section] = { ...config.value[section], ...newConfig };
    };

    return {
        config,
        updateConfig
    };
});
