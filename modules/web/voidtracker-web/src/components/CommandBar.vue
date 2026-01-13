<template>
  <Teleport to="body">
    <Transition name="command-bar">
      <div 
        v-if="isOpen" 
        class="fixed inset-0 z-[9999] flex items-start justify-center pt-[15vh] bg-black/80 backdrop-blur-md"
        @click.self="close"
      >
        <div class="w-full max-w-3xl mx-4 spotify-card rounded-2xl overflow-hidden border-2 border-gray-200 dark:border-spotify-gray-800 shadow-2xl">
          <!-- Search Input -->
          <div class="flex items-center px-6 py-4 border-b border-gray-200 dark:border-spotify-gray-800 bg-gray-50 dark:bg-spotify-darker">
            <span class="text-spotify-green-400 mr-4 text-2xl">⌘</span>
            <input 
              ref="inputRef"
              v-model="query"
              @keydown.down="navigateDown"
              @keydown.up="navigateUp"
              @keydown.enter="executeSelected"
              @keydown.esc="close"
              @input="filterCommands"
              type="text" 
              placeholder="Search commands, navigate, or ask AI..." 
              class="flex-1 bg-transparent border-none outline-none text-gray-900 dark:text-white text-lg placeholder-gray-500 dark:placeholder-spotify-gray-500"
            />
            <div v-if="loading" class="ml-4">
              <div class="w-5 h-5 border-2 border-spotify-green-400 border-t-transparent rounded-full animate-spin"></div>
            </div>
          </div>
          
          <!-- Results List -->
          <div class="max-h-96 overflow-y-auto bg-white dark:bg-spotify-black">
            <div v-if="filteredCommands.length === 0 && query" class="px-6 py-8 text-center text-gray-500 dark:text-spotify-gray-500">
              <div class="text-4xl mb-2">🔍</div>
              <p>No commands found</p>
            </div>
            
            <div v-else class="py-2">
              <div 
                v-for="(command, index) in filteredCommands" 
                :key="command.id"
                @click="executeCommand(command)"
                :class="[
                  'px-6 py-4 cursor-pointer transition-all duration-200 flex items-center justify-between border-l-2',
                  selectedIndex === index 
                    ? 'bg-spotify-green-400/10 dark:bg-spotify-green-400/20 border-spotify-green-400 text-gray-900 dark:text-white' 
                    : 'border-transparent hover:bg-gray-50 dark:hover:bg-spotify-darker hover:border-spotify-gray-700 text-gray-700 dark:text-spotify-gray-300'
                ]"
              >
                <div class="flex items-center gap-4 flex-1">
                  <span class="text-2xl">{{ command.icon }}</span>
                  <div class="flex-1">
                    <div class="font-bold text-sm">{{ command.label }}</div>
                    <div class="text-xs text-gray-500 dark:text-spotify-gray-500 mt-1">{{ command.description }}</div>
                  </div>
                </div>
                <div class="flex items-center gap-2">
                  <span v-if="command.shortcut" class="text-xs text-gray-500 dark:text-spotify-gray-500 px-2 py-1 bg-gray-100 dark:bg-spotify-darker rounded">
                    {{ command.shortcut }}
                  </span>
                  <span v-if="selectedIndex === index" class="text-spotify-green-400">↵</span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- Footer -->
          <div class="px-6 py-3 bg-gray-50 dark:bg-spotify-darker border-t border-gray-200 dark:border-spotify-gray-800 flex justify-between items-center text-xs text-gray-600 dark:text-spotify-gray-500">
            <div class="flex items-center gap-4">
              <span class="flex items-center gap-1">
                <kbd class="px-2 py-1 bg-white dark:bg-spotify-black border border-gray-300 dark:border-spotify-gray-700 rounded">↑↓</kbd>
                <span>Navigate</span>
              </span>
              <span class="flex items-center gap-1">
                <kbd class="px-2 py-1 bg-white dark:bg-spotify-black border border-gray-300 dark:border-spotify-gray-700 rounded">↵</kbd>
                <span>Select</span>
              </span>
            </div>
            <span class="flex items-center gap-1">
              <kbd class="px-2 py-1 bg-white dark:bg-spotify-black border border-gray-300 dark:border-spotify-gray-700 rounded">Esc</kbd>
              <span>Close</span>
            </span>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useThemeStore } from '@/stores/themeStore'
import { Teleport, Transition } from 'vue'

const router = useRouter()
const themeStore = useThemeStore()

const isOpen = ref(false)
const query = ref('')
const loading = ref(false)
const inputRef = ref(null)
const selectedIndex = ref(0)

const commands = ref([
  { id: 'dashboard', label: 'Go to Dashboard', icon: '📊', description: 'Navigate to main dashboard', action: 'navigate', target: '/internal/dashboard', shortcut: '' },
  { id: 'orders', label: 'View Orders', icon: '📦', description: 'Open orders list', action: 'navigate', target: '/internal/orders', shortcut: '' },
  { id: 'dispatch', label: 'Dispatch Board', icon: '🗺️', description: 'Open dispatch control tower', action: 'navigate', target: '/internal/dispatch', shortcut: '' },
  { id: 'optimize', label: 'Run Optimizer', icon: '⚡', description: 'Trigger route optimization', action: 'command', target: 'optimize', shortcut: '' },
  { id: 'settings', label: 'Settings', icon: '⚙️', description: 'Open system settings', action: 'navigate', target: '/internal/settings', shortcut: '' },
  { id: 'theme', label: 'Toggle Theme', icon: '🌓', description: 'Switch between light/dark mode', action: 'command', target: 'toggle-theme', shortcut: '' },
  { id: 'help', label: 'Help & Documentation', icon: '❓', description: 'View help and docs', action: 'navigate', target: '/help', shortcut: '' },
])

const filteredCommands = computed(() => {
  if (!query.value.trim()) return commands.value.slice(0, 5)
  
  const q = query.value.toLowerCase()
  return commands.value.filter(cmd => 
    cmd.label.toLowerCase().includes(q) || 
    cmd.description.toLowerCase().includes(q)
  )
})

watch(query, () => {
  selectedIndex.value = 0
})

function filterCommands() {
  // Already handled by computed
}

function navigateDown() {
  if (selectedIndex.value < filteredCommands.value.length - 1) {
    selectedIndex.value++
  }
}

function navigateUp() {
  if (selectedIndex.value > 0) {
    selectedIndex.value--
  }
}

function executeSelected() {
  if (filteredCommands.value[selectedIndex.value]) {
    executeCommand(filteredCommands.value[selectedIndex.value])
  }
}

function executeCommand(command) {
  if (command.action === 'navigate') {
    close()
    router.push(command.target)
  } else if (command.action === 'command') {
    if (command.target === 'toggle-theme') {
      themeStore.toggleTheme()
      close()
    } else if (command.target === 'optimize') {
      // Trigger optimization
      close()
    }
  }
}

function close() {
  isOpen.value = false
  query.value = ''
  selectedIndex.value = 0
}

function toggle() {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    nextTick(() => {
      inputRef.value?.focus()
    })
  } else {
    close()
  }
}

function onKeydown(e) {
  if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
    e.preventDefault()
    toggle()
  }
  if (e.key === 'Escape' && isOpen.value) {
    close()
  }
}

onMounted(() => {
  window.addEventListener('keydown', onKeydown)
  window.addEventListener('open-command-bar', () => {
    toggle()
  })
})

onUnmounted(() => {
  window.removeEventListener('keydown', onKeydown)
})
</script>

<style scoped>
.command-bar-enter-active,
.command-bar-leave-active {
  transition: opacity 0.2s ease;
}

.command-bar-enter-from,
.command-bar-leave-to {
  opacity: 0;
}

.command-bar-enter-active > div,
.command-bar-leave-active > div {
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.command-bar-enter-from > div,
.command-bar-leave-to > div {
  transform: translateY(-20px) scale(0.95);
  opacity: 0;
}
</style>
