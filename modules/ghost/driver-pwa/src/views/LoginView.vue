<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const identifier = ref('')
const loading = ref(false)
const sent = ref(false)
const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

onMounted(async () => {
    const token = route.params.token as string
    if (token) {
        loading.value = true
        try {
            await authStore.loginWithToken(token)
            router.push('/route') // Redirect to Route View
        } catch (e) {
            alert('Invalid Magic Link')
            loading.value = false
        }
    }
})

const handleSubmit = async () => {
  if (!identifier.value) return
  loading.value = true
  try {
    // For DEV mostly: if user enters a valid token directly, assume login
    // In prod, this flow is handled via URL link /auth/magic?token=...
    if (identifier.value.length > 20) {
        await authStore.loginWithToken(identifier.value)
        router.push('/scan')
        return
    }

    await authStore.requestMagicLink(identifier.value)
    sent.value = true
  } catch (e) {
    console.error(e)
    alert('Błąd logowania. Spróbuj ponownie.')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-screen p-4">
    <div class="w-full max-w-sm">
      <h1 class="text-3xl font-bold mb-2 tracking-tight text-center">THE GHOST</h1>
      <p class="text-gray-400 text-center mb-8">Driver Interface v2.0</p>

      <div v-if="!sent" class="bg-gray-800 p-6 rounded-2xl shadow-xl border border-gray-700">
        <label class="block text-sm font-medium text-gray-300 mb-2">Numer telefonu / Email</label>
        <input 
          v-model="identifier" 
          type="text" 
          placeholder="np. +48 123 456 789"
          class="w-full bg-gray-900 border border-gray-600 rounded-lg p-3 text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all"
        />
        <button 
          @click="handleSubmit" 
          :disabled="loading"
          class="mt-6 w-full bg-blue-600 hover:bg-blue-500 text-white font-semibold py-3 rounded-lg transition-colors flex items-center justify-center"
        >
          <span v-if="loading">Wysyłanie...</span>
          <span v-else>Zaloguj się</span>
        </button>
      </div>

      <div v-else class="text-center bg-gray-800 p-6 rounded-2xl border border-gray-700">
        <div class="text-5xl mb-4">📩</div>
        <h2 class="text-xl font-semibold mb-2">Sprawdź wiadomość</h2>
        <p class="text-gray-400">Wysłaliśmy link do logowania na adres:</p>
        <p class="font-mono text-blue-400 mt-2">{{ identifier }}</p>
        <button @click="sent = false" class="mt-6 text-sm text-gray-500 hover:text-white underline">
          Wróć
        </button>
      </div>
    </div>
  </div>
</template>
