<template>
  <div class="flex flex-col items-center justify-center h-full space-y-6">
    <div class="text-center">
      <h1 class="text-2xl font-bold text-gray-800">Welcome Driver</h1>
      <p class="text-gray-600 mt-2">Enter your access token or use the link from your email.</p>
    </div>

    <div class="w-full max-w-sm bg-white p-6 rounded-lg shadow-md">
      <div class="mb-4">
        <label class="block text-gray-700 text-sm font-bold mb-2" for="token">
          Access Token
        </label>
        <input
          v-model="token"
          class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
          id="token"
          type="text"
          placeholder="e.g. 123e4567-e89b..."
        >
      </div>
      <button
        @click="login"
        class="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
        :disabled="loading"
      >
        {{ loading ? 'Verifying...' : 'Start Route' }}
      </button>
      <p v-if="error" class="text-red-500 text-xs italic mt-2">{{ error }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import axios from 'axios';
import { useAuthStore } from '@/stores/authStore';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const token = ref('');
const loading = ref(false);
const error = ref('');

const login = async () => {
  if (!token.value) return;
  
  loading.value = true;
  error.value = '';

  try {
    // Call backend to validate token
    const response = await axios.get(`http://localhost:8092/api/driver/auth/validate?token=${token.value}`);
    const session = response.data;
    
    // Update Auth Store
    // We construct a user object based on the session data
    // In a real app, we might fetch full user details here
    authStore.setAuth({
        accessToken: session.token,
        refreshToken: null, // Magic links might not have refresh tokens or it's same
        user: {
            id: session.driverId,
            name: 'Driver', // Placeholder, layout fetches real name if available or we can fetch it here
            email: 'driver@example.com', // Placeholder
            role: 'DRIVER',
            roles: ['DRIVER']
        }
    });
    
    router.push('/driver/tasks');
  } catch (err) {
    console.error(err);
    error.value = 'Invalid or expired token.';
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  // Check if token is in URL query param
  if (route.query.token) {
    token.value = route.query.token;
    login();
  }
});
</script>
