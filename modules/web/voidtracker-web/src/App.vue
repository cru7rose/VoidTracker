<template>
  <div class="antialiased bg-white dark:bg-spotify-black text-gray-900 dark:text-white min-h-screen transition-colors">
    <ToastNotification />
    <CommandBar />
    
    <!-- Router View with Smooth Transitions -->
    <router-view v-slot="{ Component, route }">
      <Transition 
        :name="getTransitionName(route)" 
        mode="out-in"
        appear
      >
        <component :is="Component" :key="route.path" />
      </Transition>
    </router-view>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useThemeStore } from '@/stores/themeStore';
import CommandBar from '@/components/CommandBar.vue';
import ToastNotification from '@/components/common/ToastNotification.vue';
import { Transition } from 'vue';

const router = useRouter();
const themeStore = useThemeStore();

function getTransitionName(route) {
  // Different transitions for different route types
  if (route.meta?.transition) {
    return route.meta.transition;
  }
  
  // Default smooth fade
  return 'fade';
}

onMounted(() => {
  themeStore.applyTheme();
  
  // Add smooth scroll behavior
  document.documentElement.style.scrollBehavior = 'smooth';
});
</script>

<style>
/* Router Transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.slide-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

.scale-enter-active,
.scale-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.scale-enter-from,
.scale-leave-to {
  opacity: 0;
  transform: scale(0.95);
}
</style>
