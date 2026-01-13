import { cacheRoute, getCachedRoute } from '../db'

const API_BASE = '/api/planning'

export interface DriverRoute {
    routeId: string
    routeName: string
    driverId: string
    vehicleId: string | null
    carrierId: string | null
    status: string
    routeData: {
        activities?: any[]
        stops?: any[]
        totalDistance?: number
        totalDuration?: number
        [key: string]: any
    }
    stopCount?: number
    totalDistanceKm?: number
    estimatedDurationMinutes?: number
    createdAt: string
    updatedAt: string
}

/**
 * Fetch active route for driver from Planning Service
 * Endpoint: GET /api/planning/driver/{driverId}/route
 */
export async function fetchDriverRoute(driverId: string): Promise<DriverRoute | null> {
    try {
        const response = await fetch(`${API_BASE}/driver/${driverId}/route`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        })

        if (!response.ok) {
            if (response.status === 404) {
                // No active route found
                return null
            }
            throw new Error(`Failed to fetch route: ${response.statusText}`)
        }

        const route: DriverRoute = await response.json()
        
        // Cache route in IndexedDB for offline access
        if (route) {
            await cacheRoute(route.routeId, route)
        }

        return route
    } catch (error) {
        console.error('Fetch route error:', error)
        
        // Try to get cached route if online fetch failed
        // Note: We need routeId to get from cache, but we only have driverId
        // This is a limitation - we'd need to store driverId -> routeId mapping
        throw error
    }
}

/**
 * Get cached route from IndexedDB (for offline access)
 */
export async function getCachedDriverRoute(routeId: string): Promise<DriverRoute | null> {
    try {
        const cached = await getCachedRoute(routeId)
        if (cached && cached.routeData) {
            return cached.routeData as DriverRoute
        }
    } catch (error) {
        console.error('Get cached route error:', error)
    }
    return null
}

/**
 * Update stop status
 * Endpoint: POST /api/planning/driver/status
 */
export async function updateStopStatus(data: {
    stopId: string
    status: string
    location?: { lat: number; lon: number }
    timestamp?: string
}): Promise<void> {
    const response = await fetch(`${API_BASE}/driver/status`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            ...data,
            timestamp: data.timestamp || new Date().toISOString()
        })
    })

    if (!response.ok) {
        throw new Error(`Failed to update status: ${response.statusText}`)
    }
}
