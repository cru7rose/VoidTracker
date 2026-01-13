import { defineStore } from 'pinia';
import { ref } from 'vue';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export const useWebSocketStore = defineStore('websocket', () => {
    // State
    const connected = ref(false);
    const connecting = ref(false);
    const error = ref(null);
    const lastUpdate = ref(null);
    const optimizationUpdates = ref([]);

    // STOMP client instance
    let stompClient = null;

    /**
     * Connect to WebSocket endpoint
     */
    function connect() {
        if (stompClient && stompClient.connected) {
            console.log('WebSocket already connected');
            return;
        }

        connecting.value = true;
        error.value = null;

        // Create SockJS connection
        const socket = new SockJS('/ws-planning');
        
        // Create STOMP client
        stompClient = new Client({
            webSocketFactory: () => socket,
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            onConnect: (frame) => {
                console.log('✅ WebSocket connected:', frame);
                connected.value = true;
                connecting.value = false;
                error.value = null;

                // Subscribe to optimization updates
                subscribeToOptimizationUpdates();
            },
            onStompError: (frame) => {
                console.error('STOMP error:', frame);
                error.value = frame.headers['message'] || 'STOMP connection error';
                connected.value = false;
                connecting.value = false;
            },
            onWebSocketClose: () => {
                console.log('WebSocket closed');
                connected.value = false;
                connecting.value = false;
            },
            onWebSocketError: (event) => {
                console.error('WebSocket error:', event);
                error.value = 'WebSocket connection error';
                connected.value = false;
                connecting.value = false;
            }
        });

        // Activate connection
        stompClient.activate();
    }

    /**
     * Subscribe to optimization updates topic
     */
    function subscribeToOptimizationUpdates() {
        if (!stompClient || !stompClient.connected) {
            console.warn('Cannot subscribe: WebSocket not connected');
            return;
        }

        stompClient.subscribe('/topic/optimization-updates', (message) => {
            try {
                const update = JSON.parse(message.body);
                console.log('📊 Optimization update received:', update);
                
                lastUpdate.value = new Date();
                optimizationUpdates.value.push(update);
                
                // Keep only last 50 updates
                if (optimizationUpdates.value.length > 50) {
                    optimizationUpdates.value.shift();
                }

                // Emit custom event for components to listen
                window.dispatchEvent(new CustomEvent('optimization-update', { detail: update }));
            } catch (e) {
                console.error('Failed to parse optimization update:', e);
            }
        });
    }

    /**
     * Disconnect from WebSocket
     */
    function disconnect() {
        if (stompClient) {
            stompClient.deactivate();
            stompClient = null;
        }
        connected.value = false;
        connecting.value = false;
    }

    /**
     * Send message to server (if needed)
     */
    function sendMessage(destination, body) {
        if (!stompClient || !stompClient.connected) {
            console.warn('Cannot send message: WebSocket not connected');
            return;
        }

        stompClient.publish({
            destination,
            body: JSON.stringify(body)
        });
    }

    /**
     * Get latest optimization update
     */
    function getLatestUpdate() {
        return optimizationUpdates.value.length > 0 
            ? optimizationUpdates.value[optimizationUpdates.value.length - 1]
            : null;
    }

    return {
        connected,
        connecting,
        error,
        lastUpdate,
        optimizationUpdates,
        connect,
        disconnect,
        sendMessage,
        getLatestUpdate
    };
});
