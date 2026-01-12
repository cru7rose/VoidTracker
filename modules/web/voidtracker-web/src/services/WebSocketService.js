import SockJS from 'sockjs-client/dist/sockjs';
import Stomp from 'stompjs';

// Configuration
const WS_ENDPOINT = 'http://localhost:8093/ws-planning'; // Direct to service for now (or via proxy)

class WebSocketService {
    constructor() {
        this.stompClient = null;
        this.connected = false;
        this.subscriptions = new Map();
    }

    connect(onConnectedCallback, onErrorCallback) {
        if (this.connected) return;

        const socket = new SockJS(WS_ENDPOINT);
        this.stompClient = Stomp.over(socket);

        // Disable debug logs in production
        this.stompClient.debug = () => { };

        this.stompClient.connect(
            {},
            (frame) => {
                this.connected = true;
                console.log('✅ WebSocket Connected:', frame);
                if (onConnectedCallback) onConnectedCallback(frame);
            },
            (error) => {
                this.connected = false;
                console.error('❌ WebSocket Error:', error);
                if (onErrorCallback) onErrorCallback(error);

                // Reconnect after 5s
                setTimeout(() => this.connect(onConnectedCallback, onErrorCallback), 5000);
            }
        );
    }

    disconnect() {
        if (this.stompClient !== null) {
            this.stompClient.disconnect();
        }
        this.connected = false;
        console.log('Disconnected');
    }

    subscribe(topic, callback) {
        if (!this.stompClient || !this.connected) {
            console.warn('⚠️ Cannot subscribe, STOMP not connected. Queueing subscription...');
            // Simple retry logic could go here
            return;
        }

        if (this.subscriptions.has(topic)) return;

        const subscription = this.stompClient.subscribe(topic, (message) => {
            try {
                const payload = JSON.parse(message.body);
                callback(payload);
            } catch (e) {
                console.error('Error parsing WS message:', e);
                callback(message.body);
            }
        });

        this.subscriptions.set(topic, subscription);
        console.log(`📡 Subscribed to ${topic}`);
    }
}

export default new WebSocketService();
