<template>
  <div class="min-h-screen flex items-center justify-center bg-brand-gradient p-4 relative overflow-hidden">
    <!-- Animated Background Orbs -->
    <div class="absolute top-0 left-0 w-96 h-96 bg-brand-500/20 rounded-full blur-3xl -translate-x-1/2 -translate-y-1/2 animate-pulse"></div>
    <div class="absolute bottom-0 right-0 w-96 h-96 bg-accent-neon/10 rounded-full blur-3xl translate-x-1/2 translate-y-1/2 animate-pulse" style="animation-delay: 2s"></div>

    <div class="glass-card p-8 w-full max-w-md text-center relative z-10">
      <div class="mb-8">
        <div class="w-16 h-16 bg-brand-500 rounded-2xl mx-auto mb-4 flex items-center justify-center shadow-lg shadow-brand-500/30">
           <!-- Logo or Icon -->
           <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
             <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
           </svg>
        </div>
        <h2 class="text-2xl font-bold text-white mb-2">Driver Login</h2>
        <p class="text-brand-100">Welcome back to DANXILS</p>
      </div>
      
      <div class="flex mb-6 border-b border-brand-500/30">
        <button 
          class="flex-1 py-2 text-center font-medium focus:outline-none transition-colors duration-200"
          :class="!isMagicLink ? 'text-accent-neon border-b-2 border-accent-neon' : 'text-brand-200 hover:text-white'"
          @click="isMagicLink = false"
        >
          Password
        </button>
        <button 
          class="flex-1 py-2 text-center font-medium focus:outline-none transition-colors duration-200"
          :class="isMagicLink ? 'text-accent-neon border-b-2 border-accent-neon' : 'text-brand-200 hover:text-white'"
          @click="isMagicLink = true"
        >
          Magic Link
        </button>
      </div>

      <form @submit.prevent="handleLogin" class="space-y-6 text-left">
        <template v-if="!isMagicLink">
          <div>
            <label for="username" class="block text-sm font-medium text-brand-100">Username</label>
            <input type="text" id="username" v-model="username" required
              class="mt-1 block w-full px-4 py-3 bg-brand-800/50 border border-brand-500/30 rounded-xl text-white placeholder-brand-400 focus:outline-none focus:ring-2 focus:ring-accent-neon focus:border-transparent backdrop-blur-sm transition-all duration-200">
          </div>
          <div>
            <label for="password" class="block text-sm font-medium text-brand-100">Password</label>
            <input type="password" id="password" v-model="password" required
              class="mt-1 block w-full px-4 py-3 bg-brand-800/50 border border-brand-500/30 rounded-xl text-white placeholder-brand-400 focus:outline-none focus:ring-2 focus:ring-accent-neon focus:border-transparent backdrop-blur-sm transition-all duration-200">
          </div>
        </template>

        <template v-else>
          <div v-if="!magicLinkSent">
            <label for="email" class="block text-sm font-medium text-brand-100">Email Address</label>
            <input type="email" id="email" v-model="email" required placeholder="driver@danxils.com"
              class="mt-1 block w-full px-4 py-3 bg-brand-800/50 border border-brand-500/30 rounded-xl text-white placeholder-brand-400 focus:outline-none focus:ring-2 focus:ring-accent-neon focus:border-transparent backdrop-blur-sm transition-all duration-200">
            <p class="mt-2 text-xs text-brand-300">We'll send a magic link to your email.</p>
          </div>
          <div v-else>
            <div class="bg-accent-neon/10 border-l-4 border-accent-neon p-4 mb-4 rounded-r">
              <div class="flex">
                <div class="flex-shrink-0">
                  <svg class="h-5 w-5 text-accent-neon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" />
                  </svg>
                </div>
                <div class="ml-3">
                  <p class="text-sm text-brand-100">
                    Token generated! Check your console/logs.
                  </p>
                </div>
              </div>
            </div>
            <label for="token" class="block text-sm font-medium text-brand-100">Magic Token</label>
            <input type="text" id="token" v-model="magicToken" required placeholder="Paste token here"
              class="mt-1 block w-full px-4 py-3 bg-brand-800/50 border border-brand-500/30 rounded-xl text-white placeholder-brand-400 focus:outline-none focus:ring-2 focus:ring-accent-neon focus:border-transparent backdrop-blur-sm transition-all duration-200">
          </div>
        </template>

        <div v-if="error" class="text-red-400 text-sm text-center bg-red-900/20 p-2 rounded">
          {{ error }}
        </div>

        <button type="submit" :disabled="loading"
          class="w-full flex justify-center py-3 px-4 border border-transparent rounded-xl shadow-lg shadow-accent-neon/20 text-sm font-bold text-brand-900 bg-accent-neon hover:bg-accent-neon/90 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-accent-neon disabled:opacity-50 disabled:cursor-not-allowed transform transition-all duration-200 hover:-translate-y-0.5">
          {{ loading ? 'Processing...' : (isMagicLink && !magicLinkSent ? 'Get Magic Link' : 'Sign In') }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import authStore from '../stores/authStore';

const router = useRouter();
const route = useRoute();

const isMagicLink = ref(false);
const magicLinkSent = ref(false);

const username = ref('');
const password = ref('');
const email = ref('');
const magicToken = ref('');

const loading = computed(() => authStore.state.loading);
const error = computed(() => authStore.state.error);

onMounted(async () => {
  const token = route.query.token;
  if (token) {
    isMagicLink.value = true;
    magicToken.value = token;
    await handleLogin();
  }
});

const handleLogin = async () => {
  if (isMagicLink.value) {
    if (!magicToken.value && !magicLinkSent.value) {
      // Request Link
      const success = await authStore.requestMagicLink(email.value);
      if (success) {
        magicLinkSent.value = true;
      }
    } else {
      // Login with Token
      const success = await authStore.loginWithMagicLink(magicToken.value);
      if (success) {
        router.push('/tasks'); // Redirect to Task Board
      }
    }
  } else {
    // Mock Password Login (keep existing logic or update later)
    // For now, let's just simulate success if they use password
    console.log('Password login not fully implemented in backend yet, using mock');
    localStorage.setItem('driverToken', 'mock-token');
    router.push('/tasks');
  }
};
</script>
