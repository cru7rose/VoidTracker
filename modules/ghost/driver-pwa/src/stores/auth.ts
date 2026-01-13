import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { saveSession, getSession, clearSession } from '../db'
import type { Router } from 'vue-router'

interface DriverSession {
    driverId: string
    routeId: string | null
    token: string
    expiresAt: number
}

export const useAuthStore = defineStore('auth', () => {
    const token = ref<string | null>(localStorage.getItem('token'))
    const driverId = ref<string | null>(null)
    const routeId = ref<string | null>(null)
    const isAuthenticated = computed(() => !!driverId.value && !!token.value)

    /**
     * Validate magic link token with Planning Service
     * Endpoint: GET /api/planning/driver/auth/validate?token={uuid}
     */
    const loginWithToken = async (magicToken: string) => {
        try {
            const response = await fetch(`/api/planning/driver/auth/validate?token=${encodeURIComponent(magicToken)}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            })

            if (!response.ok) {
                if (response.status === 401) {
                    throw new Error('Invalid or expired token')
                }
                throw new Error(`Authentication failed: ${response.statusText}`)
            }

            const data = await response.json()
            
            if (!data.valid || !data.driverId) {
                throw new Error('Invalid token response')
            }

            // Store session data
            driverId.value = data.driverId
            routeId.value = data.routeId || null
            token.value = magicToken // Use magic token as session token
            
            // Save to localStorage for persistence
            localStorage.setItem('token', magicToken)
            localStorage.setItem('driverId', data.driverId)
            if (data.routeId) {
                localStorage.setItem('routeId', data.routeId)
            }

            // Save to IndexedDB for offline access
            const expiresAt = Date.now() + (24 * 60 * 60 * 1000) // 24 hours
            await saveSession({
                driverId: data.driverId,
                routeId: data.routeId || null,
                token: magicToken,
                expiresAt
            })

            return { driverId: data.driverId, routeId: data.routeId }
        } catch (error) {
            console.error('Login error:', error)
            throw error
        }
    }

    /**
     * Load session from IndexedDB (for offline/refresh scenarios)
     */
    const loadSession = async () => {
        try {
            const session = await getSession()
            if (session && session.expiresAt > Date.now()) {
                driverId.value = session.driverId
                routeId.value = session.routeId
                token.value = session.token
                localStorage.setItem('token', session.token)
                localStorage.setItem('driverId', session.driverId)
                if (session.routeId) {
                    localStorage.setItem('routeId', session.routeId)
                }
                return true
            } else if (session) {
                // Session expired, clear it
                await clearSession()
            }
        } catch (error) {
            console.error('Load session error:', error)
        }
        return false
    }

    /**
     * Logout - clear all session data
     */
    const logout = async () => {
        driverId.value = null
        routeId.value = null
        token.value = null
        localStorage.removeItem('token')
        localStorage.removeItem('driverId')
        localStorage.removeItem('routeId')
        await clearSession()
    }

    /**
     * Request magic link (legacy support - may use IAM service)
     * This is for requesting a new magic link, not for validation
     */
    const requestMagicLink = async (identifier: string) => {
        // This endpoint might be in IAM service or Planning Service
        // For now, keeping legacy endpoint
        const response = await fetch('/api/auth/magic-link/driver/generate?identifier=' + encodeURIComponent(identifier), {
            method: 'POST'
        })

        if (!response.ok) {
            throw new Error('Failed to request link')
        }

        const data = await response.json()
        console.log('Magic Link (Debug):', data.link) // For testing in browser console
        return data
    }

    // Initialize: try to load session from localStorage/IndexedDB
    const storedDriverId = localStorage.getItem('driverId')
    const storedRouteId = localStorage.getItem('routeId')
    if (storedDriverId && token.value) {
        driverId.value = storedDriverId
        routeId.value = storedRouteId
    }

    return { 
        token, 
        driverId, 
        routeId,
        isAuthenticated,
        requestMagicLink, 
        loginWithToken,
        loadSession,
        logout
    }
})
