import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
    const token = ref<string | null>(localStorage.getItem('token'))
    const user = ref<any>(null)

    const requestMagicLink = async (identifier: string) => {
        // Call backend API (Titan Magic Link endpoint)
        // PROXY is needed in vite.config.ts or full URL
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

    const loginWithToken = async (magicToken: string) => {
        const response = await fetch('/api/auth/magic-link/exchange?token=' + magicToken, {
            method: 'POST'
        })
        if (!response.ok) throw new Error('Invalid token')

        const data = await response.json()
        token.value = data.accessToken
        localStorage.setItem('token', data.accessToken)
    }

    return { token, user, requestMagicLink, loginWithToken }
})
