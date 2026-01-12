<template>
  <div class="relative" v-if="organizations.length > 0">
    <button 
      @click="isOpen = !isOpen"
      class="flex items-center space-x-2 px-3 py-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
    >
      <div class="flex flex-col items-start">
        <span class="text-xs text-gray-500 dark:text-gray-400">Organization</span>
        <span class="text-sm font-medium text-gray-900 dark:text-white">{{ currentOrg?.legalName || 'Select Org' }}</span>
      </div>
      <svg class="w-4 h-4 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
      </svg>
    </button>

    <div 
      v-if="isOpen"
      class="absolute right-0 mt-2 w-56 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 z-50"
    >
      <div class="py-1">
        <button
          v-for="org in organizations"
          :key="org.orgId"
          @click="selectOrg(org)"
          class="w-full text-left px-4 py-2 text-sm hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
          :class="{ 'bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400': currentOrg?.orgId === org.orgId }"
        >
          <div class="font-medium">{{ org.legalName }}</div>
          <div class="text-xs text-gray-500">{{ org.role }}</div>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useAuthStore } from '../stores/authStore';

const authStore = useAuthStore();
const isOpen = ref(false);

const organizations = computed(() => authStore.organizations);
const currentOrg = computed(() => authStore.currentOrg);

function selectOrg(org) {
  authStore.switchOrganization(org.orgId);
  isOpen.value = false;
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
