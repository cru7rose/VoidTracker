import { openDB, type DBSchema } from 'idb'

interface GhostDB extends DBSchema {
    orders: {
        key: string
        value: {
            id: string
            customerName: string
            status: string
            address: string
            synced: boolean
            updatedAt: number
        }
    }
    session: {
        key: string
        value: {
            key: string
            driverId: string
            routeId: string | null
            token: string
            expiresAt: number
            createdAt: number
        }
    }
    routes: {
        key: string
        value: {
            routeId: string
            routeData: any
            cachedAt: number
        }
    }
}

const dbPromise = openDB<GhostDB>('titan-ghost-db', 2, {
    upgrade(db, oldVersion) {
        if (oldVersion < 1) {
            db.createObjectStore('orders', { keyPath: 'id' })
        }
        if (oldVersion < 2) {
            db.createObjectStore('session', { keyPath: 'key' })
            db.createObjectStore('routes', { keyPath: 'routeId' })
        }
    },
})

export async function saveOrder(order: any) {
    return (await dbPromise).put('orders', order)
}

export async function getOrders() {
    return (await dbPromise).getAll('orders')
}

export async function clearOrders() {
    return (await dbPromise).clear('orders')
}

// Session storage functions
export async function saveSession(session: { driverId: string; routeId: string | null; token: string; expiresAt: number }) {
    const db = await dbPromise
    return db.put('session', {
        key: 'current',
        ...session,
        createdAt: Date.now()
    })
}

export async function getSession() {
    const db = await dbPromise
    return db.get('session', 'current')
}

export async function clearSession() {
    const db = await dbPromise
    return db.delete('session', 'current')
}

// Route cache functions
export async function cacheRoute(routeId: string, routeData: any) {
    const db = await dbPromise
    return db.put('routes', {
        routeId,
        routeData,
        cachedAt: Date.now()
    })
}

export async function getCachedRoute(routeId: string) {
    const db = await dbPromise
    return db.get('routes', routeId)
}
