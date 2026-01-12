/**
 * RxDB Schema Definitions for VoidTracker
 * Implements EAV (Entity-Attribute-Value) architecture with JSONB properties
 */

/**
 * Order Collection Schema
 * @type {import('rxdb').RxJsonSchema}
 */
export const orderSchema = {
    version: 0,
    primaryKey: 'orderId',
    type: 'object',
    properties: {
        orderId: {
            type: 'string',
            maxLength: 100
        },
        status: {
            type: 'string',
            maxLength: 50,
            description: 'Order status (NEW, PICKUP, PSIP, LOAD, TERM, POD)'
        },
        customerId: {
            type: 'string',
            maxLength: 100,
            description: 'Customer/Client identifier'
        },
        customerName: {
            type: 'string',
            maxLength: 255
        },
        // Pickup Location
        pickupAddress: {
            type: 'object',
            properties: {
                street: { type: 'string' },
                city: { type: 'string' },
                postalCode: { type: 'string' },
                country: { type: 'string' },
                lat: { type: 'number' },
                lon: { type: 'number' }
            }
        },
        // Delivery Location
        deliveryAddress: {
            type: 'object',
            properties: {
                street: { type: 'string' },
                city: { type: 'string' },
                postalCode: { type: 'string' },
                country: { type: 'string' },
                lat: { type: 'number' },
                lon: { type: 'number' },
                sla: { type: 'string', description: 'ISO 8601 datetime' }
            }
        },
        // Assigned Driver
        assignedDriver: {
            type: 'string',
            description: 'Driver UUID'
        },
        // EAV Core: Dynamic properties (JSONB equivalent)
        properties: {
            type: 'object',
            description: 'Dynamic fields specific to cargo type (weight, volume, hazmat, etc.)'
        },
        // Metadata
        createdAt: {
            type: 'string',
            format: 'date-time',
            maxLength: 30
        },
        updatedAt: {
            type: 'string',
            format: 'date-time',
            maxLength: 30
        },
        // Sync status for offline-first
        _syncStatus: {
            type: 'string',
            enum: ['synced', 'pending', 'conflict'],
            default: 'pending'
        }
    },
    required: ['orderId', 'status', 'createdAt'],
    indexes: [
        'status',
        'customerId',
        'assignedDriver',
        ['status', 'customerId'], // Compound index for common query
        'createdAt'
    ]
};

/**
 * Driver Collection Schema
 * For future offline driver tracking
 */
export const driverSchema = {
    version: 0,
    primaryKey: 'driverId',
    type: 'object',
    properties: {
        driverId: {
            type: 'string',
            maxLength: 100
        },
        name: {
            type: 'string',
            maxLength: 255
        },
        email: {
            type: 'string',
            maxLength: 255
        },
        phone: {
            type: 'string',
            maxLength: 50
        },
        status: {
            type: 'string',
            enum: ['active', 'inactive', 'on_route'],
            default: 'active'
        },
        currentLocation: {
            type: 'object',
            properties: {
                lat: { type: 'number' },
                lon: { type: 'number' },
                timestamp: { type: 'string' }
            }
        },
        vehicleId: {
            type: 'string'
        },
        createdAt: {
            type: 'string',
            format: 'date-time'
        }
    },
    required: ['driverId', 'name'],
    indexes: ['status', 'vehicleId']
};

/**
 * Schema version migrations
 * When schema changes, increment version and add migration logic
 */
export const schemaMigrations = {
    orders: {
        // Example migration from v0 to v1
        // 1: (oldDoc) => {
        //     oldDoc.newField = 'default';
        //     return oldDoc;
        // }
    },
    drivers: {}
};
