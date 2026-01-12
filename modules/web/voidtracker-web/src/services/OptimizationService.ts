import { Client, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export interface OptimizationUpdateDto {
    score: string;
    solverStatus: string;
    requiresApproval: boolean;
    warnings: string[];
    routes: RouteDto[];
}

export interface RouteDto {
    vehicleId: string;
    totalDistanceMeters: number;
    path: PointDto[];
    color: string;
}

export interface PointDto {
    lat: number;
    lon: number;
    type: string;
    orderId: string;
}

/**
 * Service to handle real-time optimization updates via WebSocket.
 */
class OptimizationService {
    private client: Client;
    private subscription: StompSubscription | null = null;
    private onUpdateCallback: ((update: OptimizationUpdateDto) => void) | null = null;

    constructor() {
        this.client = new Client({
            // Endpoint matches PlanningWebSocketConfig: /ws-planning
            brokerURL: 'ws://localhost:8093/api/planning/ws-planning', // Direct WS is better if possible, but SockJS fallback often used

            // If using SockJS (which is common with Spring), we need a factory.
            // Spring 'registerStompEndpoints' usually maps to http.
            // Adjust based on typical Spring Boot + SockJS setup.
            webSocketFactory: () => {
                // Note: 'ws' vs 'http' depends on if we use pure WS or SockJS.
                // If the backend has .withSockJS(), we MUST use SockJS.
                // Assuming standard setup:
                return new SockJS('http://localhost:8093/api/planning/ws-planning');
            },

            debug: (str) => {
                console.debug('[Timefold WS]:', str);
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        this.client.onConnect = (frame) => {
            console.log('Connected to Timefold WebSocket');
            if (this.onUpdateCallback) {
                this.subscribeInternal();
            }
        };

        this.client.onStompError = (frame) => {
            console.error('Broker reported error: ' + frame.headers['message']);
            console.error('Additional details: ' + frame.body);
        };
    }

    public connect(onUpdate: (update: OptimizationUpdateDto) => void) {
        this.onUpdateCallback = onUpdate;
        try {
            this.client.activate();
        } catch (e) {
            console.error("Failed to activate WS client", e);
        }
    }

    public disconnect() {
        if (this.subscription) {
            this.subscription.unsubscribe();
            this.subscription = null;
        }
        this.client.deactivate();
        console.log("Disconnected from Timefold WebSocket");
    }

    private subscribeInternal() {
        if (this.client.connected) {
            this.subscription = this.client.subscribe('/topic/optimization-updates', (message) => {
                if (message.body) {
                    const update: OptimizationUpdateDto = JSON.parse(message.body);
                    if (this.onUpdateCallback) {
                        this.onUpdateCallback(update);
                    }
                }
            });
            console.log("Subscribed to /topic/optimization-updates");
        }
    }
}

export default new OptimizationService();
