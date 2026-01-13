<template>
  <div class="min-h-screen flex items-center justify-center bg-void-black cyber-grid relative overflow-hidden">
    <!-- Animated Background -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div class="absolute top-1/4 left-1/4 w-96 h-96 bg-void-cyan-500/10 rounded-full blur-3xl animate-float"></div>
      <div class="absolute bottom-1/4 right-1/4 w-96 h-96 bg-void-pink-500/10 rounded-full blur-3xl animate-float" style="animation-delay: 2s;"></div>
    </div>

    <!-- Login Card -->
    <div class="relative z-10 hologram-panel p-10 rounded-2xl max-w-md w-full mx-4 border-2 border-void-cyan-600/50">
      <Transition name="fade-in" appear>
        <div>
          <!-- Logo & Header -->
          <div class="text-center mb-8">
            <div class="inline-block mb-4">
              <div class="w-16 h-16 rounded-xl bg-gradient-to-br from-void-cyan-400 to-void-pink-400 flex items-center justify-center shadow-2xl shadow-void-cyan-500/50 animate-neon-flicker border-2 border-void-cyan-400/50">
                <span class="text-void-black font-bold text-3xl font-mono">V</span>
              </div>
            </div>
            <h1 class="text-3xl font-bold font-mono mb-2 neon-glow text-void-cyan-400 tracking-wider">
              VOID-FLOW
            </h1>
            <p class="text-void-cyan-500 text-sm font-mono uppercase tracking-widest">Customer Portal</p>
          </div>

          <!-- Login Form -->
          <form @submit.prevent="handleLogin" class="space-y-6">
            <TransitionGroup name="slide-up" tag="div">
              <div key="username" class="space-y-2">
                <label for="username" class="block text-sm font-bold text-void-cyan-400 uppercase tracking-wider font-mono">
                  Username
                </label>
                <input
                  id="username"
                  v-model="username"
                  type="text"
                  autocomplete="username"
                  required
                  class="w-full px-4 py-3 bg-void-black border-2 border-void-cyan-800 rounded-lg text-void-cyan-300 placeholder-void-cyan-600 focus:outline-none focus:border-void-cyan-400 focus:ring-2 focus:ring-void-cyan-400/50 transition-all font-mono"
                  placeholder="Enter your username"
                />
              </div>

              <div key="password" class="space-y-2">
                <label for="password" class="block text-sm font-bold text-void-cyan-400 uppercase tracking-wider font-mono">
                  Password
                </label>
                <input
                  id="password"
                  v-model="password"
                  type="password"
                  autocomplete="current-password"
                  required
                  class="w-full px-4 py-3 bg-void-black border-2 border-void-cyan-800 rounded-lg text-void-cyan-300 placeholder-void-cyan-600 focus:outline-none focus:border-void-cyan-400 focus:ring-2 focus:ring-void-cyan-400/50 transition-all font-mono"
                  placeholder="Enter your password"
                />
              </div>
            </TransitionGroup>
            
            <div class="text-right">
              <router-link 
                to="/forgot-password" 
                class="text-sm font-mono text-void-cyan-500 hover:text-void-cyan-400 transition-colors"
              >
                Forgot password?
              </router-link>
            </div>

            <Transition name="fade">
              <div v-if="error" class="p-3 bg-void-pink-900/30 border border-void-pink-600 rounded-lg text-void-pink-300 text-sm font-mono">
                {{ error }}
              </div>
            </Transition>

            <button
              type="submit"
              :disabled="loading"
              class="w-full neon-button py-4 px-6 rounded-lg text-center transition-all duration-300 font-mono text-lg uppercase tracking-wider disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span v-if="loading" class="flex items-center justify-center gap-3">
                <div class="w-5 h-5 border-2 border-void-cyan-400 border-t-transparent rounded-full animate-spin"></div>
                <span>Authenticating...</span>
              </span>
              <span v-else class="flex items-center justify-center gap-3">
                <span>🌐</span>
                <span>Login</span>
              </span>
            </button>
          </form>
          
          <div class="mt-6 text-center">
            <p class="text-sm text-void-cyan-600 font-mono">
              Don't have an account?
              <router-link 
                to="/customer/register" 
                class="font-bold text-void-cyan-400 hover:text-void-cyan-300 transition-colors ml-1"
              >
                Sign up
              </router-link>
            </p>
          </div>
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
      router.push('/customer/dashboard');
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
