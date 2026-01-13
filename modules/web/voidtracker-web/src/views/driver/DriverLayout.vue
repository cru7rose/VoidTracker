<template>
  <div class="min-h-screen bg-gray-100 flex flex-col">
    <!-- Mobile Header -->
    <header class="bg-blue-600 text-white p-4 flex justify-between items-center shadow-md">
      <div class="font-bold text-lg">VoidTracker Driver</div>
      <div v-if="driverName" class="text-sm">{{ driverName }}</div>
    </header>

    <!-- Main Content -->
    <main class="flex-1 p-4 overflow-y-auto">
      <router-view></router-view>
    </main>

    <!-- Mobile Footer / Nav (Optional) -->
    <footer class="bg-white border-t p-3 text-center text-xs text-gray-500">
      &copy; 2025 VoidTracker
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useAuthStore } from '@/stores/authStore';

const authStore = useAuthStore();
const driverName = ref('');

onMounted(() => {
    // Assuming the user object has a name or email
    if (authStore.user) {
        driverName.value = authStore.user.name || authStore.user.email || 'Driver';
    }
});
</script>

<style scoped>
/* Mobile optimizations */
main {
  -webkit-overflow-scrolling: touch;
}
</style>
