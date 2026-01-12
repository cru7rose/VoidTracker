/**
 * VoidTracker RxDB Database Singleton
 * Provides reactive, offline-first database with IndexedDB persistence
 */

import { createRxDatabase } from 'rxdb';
import { getRxStorageDexie } from 'rxdb/plugins/storage-dexie';
import { orderSchema, driverSchema, schemaMigrations } from './schema.js';

/** @type {import('rxdb').RxDatabase | null} */
let dbInstance = null;

/**
 * Initialize and return VoidTracker RxDB database (Singleton)
 * Lazy loads on first call, subsequent calls return cached instance
 * 
 * @returns {Promise<import('rxdb').RxDatabase>}
 */
export async function getVoidDb() {
    if (dbInstance) {
        return dbInstance;
    }

    try {
        console.log('[VoidDB] Initializing RxDB database...');

        // Create RxDB instance with Dexie (IndexedDB) storage
        dbInstance = await createRxDatabase({
            name: 'voidtracker_offline',
            storage: getRxStorageDexie(),
            multiInstance: true, // Allow multiple tabs
            eventReduce: true, // Performance optimization
            ignoreDuplicate: true
        });

        console.log('[VoidDB] Database created, adding collections...');

        // Add Orders collection
        await dbInstance.addCollections({
            orders: {
                schema: orderSchema,
                migrationStrategies: schemaMigrations.orders
            }
        });

        // Add Drivers collection
        await dbInstance.addCollections({
            drivers: {
                schema: driverSchema,
                migrationStrategies: schemaMigrations.drivers
            }
        });

        console.log('[VoidDB] Collections added successfully');

        // Setup cleanup on window unload
        window.addEventListener('beforeunload', async () => {
            if (dbInstance) {
                await dbInstance.destroy();
                dbInstance = null;
            }
        });

        return dbInstance;

    } catch (error) {
        console.error('[VoidDB] Failed to initialize database:', error);

        // Recovery: Try to destroy corrupted database
        if (error.message?.includes('already exists') || error.message?.includes('version')) {
            console.warn('[VoidDB] Attempting database recovery...');
            try {
                await destroyVoidDb();
                // Retry initialization
                return getVoidDb();
            } catch (recoveryError) {
                console.error('[VoidDB] Recovery failed:', recoveryError);
            }
        }

        throw new Error(`VoidDB initialization failed: ${error.message}`);
    }
}

/**
 * Destroy database instance (use for testing or recovery)
 * WARNING: Deletes all local data!
 * 
 * @returns {Promise<void>}
 */
export async function destroyVoidDb() {
    if (dbInstance) {
        await dbInstance.destroy();
        dbInstance = null;
    }

    // Clear IndexedDB manually if needed
    if (typeof indexedDB !== 'undefined') {
        await new Promise((resolve, reject) => {
            const req = indexedDB.deleteDatabase('voidtracker_offline');
            req.onsuccess = () => resolve();
            req.onerror = () => reject(req.error);
        });
    }

    console.log('[VoidDB] Database destroyed');
}

/**
 * Check database health
 * @returns {Promise<boolean>}
 */
export async function isVoidDbHealthy() {
    try {
        const db = await getVoidDb();
        // Simple health check: try to query orders
        await db.orders.find().limit(1).exec();
        return true;
    } catch (error) {
        console.error('[VoidDB] Health check failed:', error);
        return false;
    }
}

/**
 * Export statistics for monitoring
 * @returns {Promise<Object>}
 */
export async function getVoidDbStats() {
    const db = await getVoidDb();

    const orderCount = await db.orders.count().exec();
    const driverCount = await db.drivers.count().exec();

    return {
        orders: orderCount,
        drivers: driverCount,
        storage: 'Dexie (IndexedDB)',
        multiInstance: true
    };
}
