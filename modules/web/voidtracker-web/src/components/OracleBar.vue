<template>
  <Teleport to="body">
    <!-- Modal Backdrop -->
    <Transition name="oracle-backdrop">
      <div 
        v-if="isOpen" 
        class="fixed inset-0 z-50 flex items-start justify-center pt-32 bg-black/60 backdrop-blur-sm"
        @click="closeOracle"
      >
        <!-- Oracle Panel -->
        <Transition name="oracle-panel">
          <div 
            v-if="isOpen"
            class="w-full max-w-2xl mx-4"
            @click.stop
          >
            <!-- Glassmorphism Container -->
            <div class="oracle-container bg-slate-900/85 backdrop-blur-xl border border-cyan-500/30 rounded-2xl shadow-2xl shadow-cyan-500/20 overflow-hidden">
              <!-- Header -->
              <div class="px-6 py-4 border-b border-slate-700/50 bg-gradient-to-r from-cyan-500/10 to-transparent">
                <h2 class="text-lg font-bold text-cyan-400 flex items-center">
                  <span class="text-2xl mr-3">🔮</span>
                  THE ORACLE
                  <span class="ml-auto text-xs text-slate-500 font-normal">Cmd+K</span>
                </h2>
              </div>

              <!-- Command Input -->
              <div class="p-6">
                <div class="relative">
                  <input 
                    ref="inputRef"
                    v-model="query"
                    type="text"
                    placeholder="Type your command... (e.g., 'show at-risk orders in Warsaw')"
                    class="w-full px-5 py-4 bg-slate-800/50 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-cyan-500 focus:border-transparent transition-all"
                    @keydown.enter="executeCommand"
                    @keydown.esc="closeOracle"
                  />
                  
                  <!-- Submit Button -->
                  <button 
                    v-if="query"
                    @click="executeCommand"
                    class="absolute right-2 top-1/2 -translate-y-1/2 px-4 py-2 bg-gradient-to-r from-cyan-500 to-cyan-600 text-white rounded-lg font-semibold hover:shadow-lg hover:shadow-cyan-500/50 transition-all"
                    :disabled="loading"
                  >
                    <span v-if="!loading">Execute</span>
                    <span v-else class="flex items-center">
                      <svg class="animate-spin h-4 w-4 mr-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                      Processing...
                    </span>
                  </button>
                </div>

                <!-- Status Display -->
                <Transition name="fade">
                  <div v-if="status" class="mt-4 p-4 rounded-lg" :class="statusClass">
                    <div class="flex items-start">
                      <span class="text-lg mr-2">{{ statusIcon }}</span>
                      <div class="flex-1">
                        <p class="font-semibold">{{ status.title }}</p>
                        <p v-if="status.message" class="text-sm mt-1 opacity-90">{{ status.message }}</p>
                      </div>
                    </div>
                  </div>
                </Transition>
              </div>

              <!-- Quick Commands -->
              <div class="px-6 pb-6">
                <p class="text-xs text-slate-500 mb-3">Quick Commands:</p>
                <div class="flex flex-wrap gap-2">
                  <button 
                    v-for="cmd in quickCommands" 
                    :key="cmd"
                    @click="query = cmd"
                    class="px-3 py-1.5 bg-slate-800/50 border border-slate-700 rounded-lg text-sm text-slate-300 hover:bg-slate-700 hover:border-cyan-500/50 hover:text-cyan-300 transition-all"
                  >
                    {{ cmd }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
import { onKeyStroke } from '@vueuse/core';
import axios from '@/api/axios';
import { usePlanningStore } from '@/stores/planningStore';
import { useToast } from '@/composables/useToast';

const isOpen = ref(false);
const query = ref('');
const loading = ref(false);
const status = ref(null);
const inputRef = ref(null);

const planningStore = usePlanningStore();
const { success: showSuccess, error: showError } = useToast();

const quickCommands = [
  'show at-risk orders',
  'pokaż wszystkie zamówienia',
  'switch to profitability',
  'clear filters',
  'wyczyść filtry'
];

/**
 * Open Oracle modal
 */
function openOracle() {
  isOpen.value = true;
  query.value = '';
  status.value = null;
  
  // Focus input after DOM update
  setTimeout(() => {
    inputRef.value?.focus();
  }, 100);
}

/**
 * Close Oracle modal
 */
function closeOracle() {
  isOpen.value = false;
  query.value = '';
  status.value = null;
}

/**
 * Execute natural language command
 */
async function executeCommand() {
  if (!query.value.trim()) return;

  loading.value = true;
  status.value = {
    title: '🧠 Parsing command...',
    message: null,
    type: 'loading'
  };

  try {
    // Call NLP parser backend
    const response = await axios.post('/nlp/command/parse', {
      query: query.value,
      locale: navigator.language.startsWith('pl') ? 'pl' : 'en'
    });

    const commandResponse = response.data;

    if (commandResponse.success && commandResponse.action !== 'UNKNOWN') {
      // Execute action via planning store
      await planningStore.executeOracleCommand(commandResponse);

      // Show success status
      status.value = {
        title: '✅ Command executed',
        message: commandResponse.interpretation,
        type: 'success'
      };

      showSuccess(commandResponse.interpretation);

      // Auto-close after 2 seconds
      setTimeout(() => {
        closeOracle();
      }, 2000);

    } else {
      // Unknown command
      status.value = {
        title: '⚠️ Command not recognized',
        message: 'Try: "show at-risk orders" or "pokaż zamówienia w Warszawie"',
        type: 'warning'
      };
    }

  } catch (error) {
    console.error('[Oracle] Command execution failed:', error);
    
    status.value = {
      title: '❌ Execution failed',
      message: error.response?.data?.message || error.message,
      type: 'error'
    };

    showError('Oracle connection disrupted: ' + (error.message || 'Unknown error'));
  } finally {
    loading.value = false;
  }
}

/**
 * Status styling
 */
const statusClass = computed(() => {
  if (!status.value) return '';
  
  switch (status.value.type) {
    case 'success':
      return 'bg-green-500/10 border border-green-500/30 text-green-400';
    case 'error':
      return 'bg-red-500/10 border border-red-500/30 text-red-400';
    case 'warning':
      return 'bg-amber-500/10 border border-amber-500/30 text-amber-400';
    case 'loading':
      return 'bg-cyan-500/10 border border-cyan-500/30 text-cyan-400';
    default:
      return 'bg-slate-700/50 border border-slate-600 text-slate-300';
  }
});

const statusIcon = computed(() => {
  if (!status.value) return '';
  
  switch (status.value.type) {
    case 'success': return '✅';
    case 'error': return '❌';
    case 'warning': return '⚠️';
    case 'loading': return '🧠';
    default: return 'ℹ️';
  }
});

// Keyboard shortcuts
onKeyStroke(['k', 'K'], (e) => {
  if (e.metaKey || e.ctrlKey) {
    e.preventDefault();
    if (isOpen.value) {
      closeOracle();
    } else {
      openOracle();
    }
  }
});

onKeyStroke('Escape', () => {
  if (isOpen.value) {
    closeOracle();
  }
});
</script>

<style scoped>
/* Glassmorphism */
.oracle-container {
  backdrop-filter: blur(16px);
}

/* Backdrop Transition */
.oracle-backdrop-enter-active,
.oracle-backdrop-leave-active {
  transition: opacity 0.2s ease;
}

.oracle-backdrop-enter-from,
.oracle-backdrop-leave-to {
  opacity: 0;
}

/* Panel Transition (Spring animation) */
.oracle-panel-enter-active {
  animation: slideDown 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.oracle-panel-leave-active {
  animation: slideUp 0.2s ease-out;
}

@keyframes slideDown {
  from {
    transform: translateY(-50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(0);
    opacity: 1;
  }
  to {
    transform: translateY(-30px);
    opacity: 0;
  }
}

/* Fade Transition */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
