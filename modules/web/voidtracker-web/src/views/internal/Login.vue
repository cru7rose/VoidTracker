<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-spotify-black via-spotify-darker to-spotify-black">
    <!-- Animated Background Gradient -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div class="absolute top-0 left-0 w-1/2 h-1/2 bg-spotify-green-400/5 rounded-full blur-3xl"></div>
      <div class="absolute bottom-0 right-0 w-1/2 h-1/2 bg-spotify-green-500/5 rounded-full blur-3xl"></div>
    </div>

    <!-- Login Card -->
    <div class="relative z-10 spotify-card max-w-md w-full mx-4 p-10 border border-spotify-gray-800">
      <Transition name="fade-in" appear>
        <div>
          <!-- Logo & Header -->
          <div class="text-center mb-8">
            <div class="inline-block mb-6">
              <div class="w-20 h-20 rounded-full bg-gradient-to-br from-spotify-green-400 to-spotify-green-500 flex items-center justify-center shadow-lg shadow-spotify-green-400/30">
                <span class="text-spotify-black font-bold text-4xl">V</span>
              </div>
            </div>
            <h1 class="text-4xl font-bold mb-2">
              VOID-FLOW
            </h1>
            <p class="text-spotify-gray-400 text-sm">Internal Portal</p>
          </div>

          <!-- Login Form -->
          <form @submit.prevent="handleLogin" class="space-y-6">
            <TransitionGroup name="slide-up" tag="div">
              <div key="username" class="space-y-2">
                <label for="username" class="block text-sm font-semibold text-white">
                  Username
                </label>
                <input
                  id="username"
                  v-model="username"
                  type="text"
                  autocomplete="username"
                  required
                  class="spotify-input w-full"
                  placeholder="Enter your username"
                />
              </div>

              <div key="password" class="space-y-2">
                <label for="password" class="block text-sm font-semibold text-white">
                  Password
                </label>
                <input
                  id="password"
                  v-model="password"
                  type="password"
                  autocomplete="current-password"
                  required
                  class="spotify-input w-full"
                  placeholder="Enter your password"
                />
              </div>
            </TransitionGroup>
            
            <div class="text-right">
              <router-link 
                to="/forgot-password" 
                class="text-sm text-spotify-gray-400 hover:text-white transition-colors"
              >
                Forgot password?
              </router-link>
            </div>

            <Transition name="fade">
              <div v-if="error" class="p-4 bg-red-500/10 border border-red-500/30 rounded-lg text-red-400 text-sm">
                {{ error }}
              </div>
            </Transition>

            <button
              type="submit"
              :disabled="loading"
              class="spotify-button w-full disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span v-if="loading" class="flex items-center justify-center gap-3">
                <div class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                <span>Authenticating...</span>
              </span>
              <span v-else>Login</span>
            </button>
          </form>
        </div>
      </Transition>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../../stores/authStore';
import { Transition, TransitionGroup } from 'vue';

const router = useRouter();
const authStore = useAuthStore();

const username = ref('');
const password = ref('');
const loading = ref(false);
const error = ref('');

async function handleLogin() {
  loading.value = true;
  error.value = '';
  
  try {
    const result = await authStore.login(username.value, password.value);
    
    if (result.success) {
      router.push('/internal/dashboard');
    } else {
      error.value = result.error || 'Login failed. Please check your credentials.';
    }
  } catch (err) {
    error.value = 'Login failed. Please check your credentials.';
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.fade-in-enter-active,
.fade-in-leave-active {
  transition: opacity 0.5s ease;
}

.fade-in-enter-from,
.fade-in-leave-to {
  opacity: 0;
}

.slide-up-enter-active {
  transition: all 0.4s ease;
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
