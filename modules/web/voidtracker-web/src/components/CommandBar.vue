<template>
  <div v-if="isOpen" class="fixed inset-0 z-[9999] flex items-start justify-center pt-[20vh] bg-black/50 backdrop-blur-sm" @click.self="isOpen = false">
    <div class="w-full max-w-2xl bg-slate-900 border border-slate-700 rounded-xl shadow-2xl overflow-hidden animate-fade-in-down">
      <div class="flex items-center px-4 py-3 border-b border-slate-800">
        <span class="text-brand-400 mr-3 text-xl">⌘</span>
        <input 
          ref="inputRef"
          v-model="query"
          @keydown.enter="executeQuery"
          type="text" 
          placeholder="Ask VoidTracker... (e.g. 'Show risky drivers')" 
          class="flex-1 bg-transparent border-none outline-none text-white text-lg placeholder-slate-500"
        />
        <div v-if="loading" class="animate-spin h-5 w-5 border-2 border-brand-500 rounded-full border-t-transparent"></div>
      </div>
      
      <div v-if="message" class="px-4 py-3 bg-slate-800 text-slate-300 text-sm">
        {{ message }}
      </div>
      
      <div class="px-4 py-2 bg-slate-950/50 text-xs text-slate-600 flex justify-between">
        <span>Enter to execute</span>
        <span>Esc to close</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { sendCommand } from '@/api/commandApi'
import { useThemeStore } from '@/stores/themeStore'

const isOpen = ref(false)
const query = ref('')
const message = ref(null)
const loading = ref(false)
const inputRef = ref(null)
const router = useRouter()

function toggle() {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    nextTick(() => inputRef.value?.focus())
  } else {
    query.value = ''
    message.value = null
  }
}

async function executeQuery() {
  if (!query.value.trim()) return
  
  // Frontend-only commands
  const normalizedQuery = query.value.trim().toLowerCase();
  
  if (normalizedQuery === 'toggle theme' || normalizedQuery === 'switch theme') {
      const themeStore = useThemeStore();
      themeStore.toggleTheme();
      message.value = `Theme switched to ${themeStore.isDark ? 'Dark' : 'Light'} Mode`;
      isOpen.value = false; // Close on successful toggle
      return;
  }

  loading.value = true
  message.value = null
  
  try {
    const result = await sendCommand(query.value)
    
    if (result.action === 'NAVIGATE') {
        isOpen.value = false
        router.push(result.target)
    } else {
        message.value = result.message
    }
  } catch (error) {
    message.value = "Error executing command."
    console.error(error)
  } finally {
    loading.value = false
  }
}

function onKeydown(e) {
  if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
    e.preventDefault()
    toggle()
  }
  if (e.key === 'Escape' && isOpen.value) {
    toggle()
  }
}

onMounted(() => {
  window.addEventListener('keydown', onKeydown)
  window.addEventListener('open-command-bar', () => {
    isOpen.value = true
    nextTick(() => inputRef.value?.focus())
  })
})
onUnmounted(() => {
  window.removeEventListener('keydown', onKeydown)
  window.removeEventListener('open-command-bar', () => {})
})
</script>

<style scoped>
.animate-fade-in-down {
  animation: fadeInDown 0.2s ease-out;
}
@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
