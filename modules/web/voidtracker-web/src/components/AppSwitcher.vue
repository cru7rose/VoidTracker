<template>
  <div class="relative">
    <button 
      @click="isOpen = !isOpen"
      class="w-full flex items-center justify-between px-4 py-3 bg-gray-800 hover:bg-gray-700 transition-colors text-white rounded-lg mb-4"
    >
      <div class="flex items-center space-x-3">
        <div class="p-1.5 bg-indigo-500 rounded-md">
          <component :is="currentApp.icon" class="w-5 h-5 text-white" />
        </div>
        <div class="flex flex-col items-start">
          <span class="text-xs text-gray-400 font-medium uppercase tracking-wider">App Context</span>
          <span class="text-sm font-bold">{{ currentApp.name }}</span>
        </div>
      </div>
      <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
      </svg>
    </button>

    <div 
      v-if="isOpen"
      class="absolute left-0 right-0 top-full mt-1 bg-gray-800 rounded-lg shadow-xl border border-gray-700 z-50 overflow-hidden"
    >
      <div class="py-1">
        <button
          v-for="app in apps"
          :key="app.id"
          @click="switchApp(app)"
          class="w-full flex items-center space-x-3 px-4 py-3 hover:bg-gray-700 transition-colors text-left"
          :class="{ 'bg-gray-700': currentApp.id === app.id }"
        >
          <div class="p-1.5 rounded-md" :class="app.colorClass">
            <component :is="app.icon" class="w-4 h-4 text-white" />
          </div>
          <div class="flex flex-col">
            <span class="text-sm font-medium text-white">{{ app.name }}</span>
            <span class="text-xs text-gray-400">{{ app.description }}</span>
          </div>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { LayoutDashboard, Truck, ShoppingBag, Shield } from 'lucide-vue-next';

const router = useRouter();
const route = useRoute();
const isOpen = ref(false);

const apps = [
  { 
    id: 'admin', 
    name: 'VoidTracker Admin', 
    description: 'System Administration',
    icon: Shield,
    colorClass: 'bg-spotify-green-400',
    pathPrefix: '/internal'
  },
  { 
    id: 'driver', 
    name: 'Driver App', 
    description: 'Delivery Tasks',
    icon: Truck,
    colorClass: 'bg-green-500',
    pathPrefix: '/driver'
  },
  { 
    id: 'customer', 
    name: 'Customer OMS', 
    description: 'Order Management',
    icon: ShoppingBag,
    colorClass: 'bg-blue-500',
    pathPrefix: '/customer'
  }
];

const currentApp = computed(() => {
  const path = route.path;
  if (path.startsWith('/driver')) return apps.find(a => a.id === 'driver');
  if (path.startsWith('/customer')) return apps.find(a => a.id === 'customer');
  return apps.find(a => a.id === 'admin');
});

function switchApp(app) {
  isOpen.value = false;
  if (app.id === 'admin') router.push('/internal/dashboard');
  else if (app.id === 'driver') router.push('/driver/login'); // Or check auth and go to task
  else if (app.id === 'customer') router.push('/customer/dashboard');
}

// Close dropdown when clicking outside
function closeDropdown(e) {
  if (!e.target.closest('.relative')) {
    isOpen.value = false;
  }
}

onMounted(() => {
  document.addEventListener('click', closeDropdown);
});

onUnmounted(() => {
  document.removeEventListener('click', closeDropdown);
});
</script>
