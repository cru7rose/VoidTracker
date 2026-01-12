import { defineStore } from 'pinia'
import { ref } from 'vue'
import { saveOrder, getOrders } from '../db'

export const useOrderStore = defineStore('order', () => {
    const orders = ref<any[]>([])
    const loading = ref(false)

    async function loadOrders() {
        loading.value = true
        try {
            // In a real app, try to fetch from API first
            // const apiOrders = await fetch('/api/orders').then(r => r.json())
            // For now, load from local DB
            const localOrders = await getOrders()
            orders.value = localOrders
        } catch (e) {
            console.error('Failed to load orders', e)
        } finally {
            loading.value = false
        }
    }

    async function addMockOrder() {
        const newOrder = {
            id: crypto.randomUUID(),
            customerName: 'Test Customer ' + Math.floor(Math.random() * 100),
            status: 'PENDING',
            address: '123 Fake St',
            synced: false,
            updatedAt: Date.now()
        }

        // Optimistic UI update
        orders.value.push(newOrder)

        // Save to IDB
        await saveOrder(newOrder)
    }

    return { orders, loading, loadOrders, addMockOrder }
})
