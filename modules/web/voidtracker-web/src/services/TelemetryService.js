// Mock Telemetry Service to simulate real-time driver tracking
// In production, this would connect to a WebSocket (e.g., /ws/telemetry)

import { ref } from 'vue';

class TelemetryService {
    constructor() {
        this.subscribers = [];
        this.intervalId = null;
        this.mockDrivers = new Map();
    }

    // Simulate connecting to a WebSocket
    connect() {
        console.log('TelemetryService: Connecting...');

        // Initialize some mock drivers around Warsaw
        this.mockDrivers.set('D-001', { id: 'D-001', name: 'John Doe', lat: 52.2297, lng: 21.0122, heading: 0, speed: 45, status: 'MOVING' });
        this.mockDrivers.set('D-002', { id: 'D-002', name: 'Jane Smith', lat: 52.2350, lng: 21.0200, heading: 90, speed: 30, status: 'MOVING' });
        this.mockDrivers.set('D-003', { id: 'D-003', name: 'Bob Wilson', lat: 52.2200, lng: 21.0100, heading: 180, speed: 0, status: 'STOPPED' });

        this.startSimulation();
    }

    disconnect() {
        if (this.intervalId) {
            clearInterval(this.intervalId);
            this.intervalId = null;
        }
        console.log('TelemetryService: Disconnected');
    }

    subscribe(callback) {
        this.subscribers.push(callback);
        // Send initial state
        callback(Array.from(this.mockDrivers.values()));
        return () => {
            this.subscribers = this.subscribers.filter(cb => cb !== callback);
        };
    }

    startSimulation() {
        this.intervalId = setInterval(() => {
            this.updatePositions();
            this.notifySubscribers();
        }, 2000); // Update every 2 seconds
    }

    updatePositions() {
        this.mockDrivers.forEach(driver => {
            if (driver.status === 'MOVING') {
                // Simple random movement logic
                const moveLat = (Math.random() - 0.5) * 0.002;
                const moveLng = (Math.random() - 0.5) * 0.002;

                driver.lat += moveLat;
                driver.lng += moveLng;

                // Calculate heading (rough approximation)
                driver.heading = (Math.atan2(moveLng, moveLat) * 180 / Math.PI + 360) % 360;

                // Randomly stop sometimes
                if (Math.random() > 0.95) driver.status = 'STOPPED';
            } else {
                // Randomly start moving
                if (Math.random() > 0.8) {
                    driver.status = 'MOVING';
                    driver.speed = 30 + Math.random() * 30;
                }
            }
        });
    }

    notifySubscribers() {
        const drivers = Array.from(this.mockDrivers.values());
        this.subscribers.forEach(cb => cb(drivers));
    }
}

export const telemetryService = new TelemetryService();
