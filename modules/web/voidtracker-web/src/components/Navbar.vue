<template>
  <div class="bg-white dark:bg-spotify-darker border-b border-gray-200 dark:border-spotify-gray-800 px-6 py-4 transition-colors">
    <div class="flex items-center justify-between">
      <!-- Left: Search -->
      <div class="flex items-center gap-4 flex-1 max-w-2xl">
        <div class="relative flex-1">
          <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <svg class="h-5 w-5 text-gray-400 dark:text-spotify-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
          <input
            type="text"
            placeholder="K Search..."
            class="spotify-input w-full pl-10 pr-4 py-2 text-sm"
          />
        </div>
      </div>

      <!-- Right: Actions -->
      <div class="flex items-center gap-4">
        <!-- Notifications -->
        <button class="relative p-2 text-gray-600 dark:text-spotify-gray-400 hover:text-gray-900 dark:hover:text-white hover:bg-gray-100 dark:hover:bg-spotify-darker rounded-lg transition-colors">
          <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
          </svg>
          <span v-if="notificationCount > 0" class="absolute top-1 right-1 w-2 h-2 bg-spotify-green-400 rounded-full"></span>
        </button>

        <!-- Organization Switcher -->
        <OrganizationSwitcher />

        <!-- Language Switcher -->
        <LanguageSwitcher />

        <!-- User Menu -->
        <div class="flex items-center gap-3">
          <div class="flex items-center gap-2">
            <div class="w-8 h-8 rounded-full bg-spotify-green-400 flex items-center justify-center">
              <span class="text-white font-semibold text-sm">{{ userInitial }}</span>
            </div>
            <span class="text-sm font-medium text-gray-900 dark:text-white">{{ authStore.user?.username || 'User' }}</span>
          </div>
          <button
            @click="handleLogout"
            class="text-sm text-gray-600 dark:text-spotify-gray-400 hover:text-gray-900 dark:hover:text-white transition-colors"
          >
            [→ Wyloguj]
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/authStore';
import OrganizationSwitcher from './OrganizationSwitcher.vue';
import LanguageSwitcher from './LanguageSwitcher.vue';

const router = useRouter();
const authStore = useAuthStore();

const notificationCount = computed(() => 0); // TODO: Implement notification count

const userInitial = computed(() => {
  const username = authStore.user?.username || 'U';
  return username.charAt(0).toUpperCase();
});

function handleLogout() {
  authStore.logout();
  router.push('/internal/login');
}
</script>
