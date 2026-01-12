// Mock Notification Service
// In production, this would call a backend API (e.g., /api/notifications/send)

class NotificationService {
    constructor() {
        this.templates = {
            OUT_FOR_DELIVERY: {
                subject: 'Your package is on the way! 🚚',
                body: 'Hello {{name}},\n\nYour package {{orderId}} is out for delivery today. Track it here: {{trackingLink}}\n\nBest,\nDANXILS Logistics'
            },
            DELIVERED: {
                subject: 'Package Delivered! ✅',
                body: 'Hello {{name}},\n\nYour package {{orderId}} has been delivered at {{time}}.\n\nThank you for choosing DANXILS!'
            }
        };
    }

    getTemplates() {
        return this.templates;
    }

    updateTemplate(type, subject, body) {
        if (this.templates[type]) {
            this.templates[type] = { subject, body };
            console.log(`Template ${type} updated:`, this.templates[type]);
            return true;
        }
        return false;
    }

    async sendEmail(type, recipient, data) {
        console.log(`Sending ${type} email to ${recipient}...`);

        const template = this.templates[type];
        if (!template) {
            console.error(`Template ${type} not found`);
            return false;
        }

        // Simple variable substitution
        let subject = template.subject;
        let body = template.body;

        for (const [key, value] of Object.entries(data)) {
            const placeholder = `{{${key}}}`;
            subject = subject.replace(new RegExp(placeholder, 'g'), value);
            body = body.replace(new RegExp(placeholder, 'g'), value);
        }

        // Simulate API delay
        await new Promise(resolve => setTimeout(resolve, 800));

        console.log('---------------------------------------------------');
        console.log(`To: ${recipient}`);
        console.log(`Subject: ${subject}`);
        console.log(`Body:\n${body}`);
        console.log('---------------------------------------------------');

        return true;
    }
}

export const notificationService = new NotificationService();
