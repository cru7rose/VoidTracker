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
}

const dbPromise = openDB<GhostDB>('titan-ghost-db', 1, {
    upgrade(db) {
        db.createObjectStore('orders', { keyPath: 'id' })
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
