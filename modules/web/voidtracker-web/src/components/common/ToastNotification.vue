<template>
  <Teleport to="body">
    <TransitionGroup
      name="toast"
      tag="div"
      class="fixed bottom-4 right-4 z-[10000] flex flex-col gap-3 pointer-events-none"
    >
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="pointer-events-auto hologram-panel rounded-xl p-4 border-2 min-w-[300px] max-w-md animate-slide-in"
        :class="getToastClass(toast.type)"
      >
        <div class="flex items-start gap-3">
          <span class="text-2xl">{{ getIcon(toast.type) }}</span>
          <div class="flex-1">
            <p class="font-bold text-sm font-mono text-void-cyan-300 mb-1">{{ toast.title }}</p>
            <p class="text-sm text-void-cyan-400 font-mono">{{ toast.message }}</p>
          </div>
          <button
            @click="removeToast(toast.id)"
            class="text-void-cyan-600 hover:text-void-cyan-400 transition-colors"
          >
            ✕
          </button>
        </div>
      </div>
    </TransitionGroup>
  </Teleport>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue';
import { Teleport, TransitionGroup } from 'vue';
import { useToast } from '@/composables/useToast';

const { toasts, removeToast, addToast } = useToast();

// Expose showToast globally for easy access
function showToast(title, message, type = 'info', duration = 5000) {
  return addToast(message, type, duration, title);
}

function getToastClass(type) {
  const classes = {
    success: 'border-green-600 bg-green-900/20',
    error: 'border-void-pink-600 bg-void-pink-900/20',
    warning: 'border-void-amber-600 bg-void-amber-900/20',
    info: 'border-void-cyan-600 bg-void-cyan-900/20'
  };
  return classes[type] || classes.info;
}

function getIcon(type) {
  const icons = {
    success: '✅',
    error: '❌',
    warning: '⚠️',
    info: 'ℹ️'
  };
  return icons[type] || icons.info;
}

// Expose to global window for easy access
onMounted(() => {
  window.showToast = showToast;
});

onUnmounted(() => {
  delete window.showToast;
});

// Expose for composable use
defineExpose({
  showToast,
  removeToast
});
</script>

<style scoped>
.toast-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.toast-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(100%) scale(0.9);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(100%) scale(0.9);
}

.toast-move {
  transition: transform 0.3s ease;
}

.animate-slide-in {
  animation: slideInRight 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}
</style>
