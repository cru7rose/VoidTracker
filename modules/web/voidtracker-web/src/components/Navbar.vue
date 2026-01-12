<script setup>
import { LogOut, Bell, User } from 'lucide-vue-next';
import { useAuthStore } from '../stores/authStore';
import { useRouter } from 'vue-router';
import LanguageSwitcher from './LanguageSwitcher.vue';
import OrganizationSwitcher from './OrganizationSwitcher.vue';

const authStore = useAuthStore();
const router = useRouter();

const handleLogout = () => {
  authStore.logout();
  router.push('/login');
};
</script>

<template>
  <header class="bg-white shadow-sm z-10">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between h-16">
        <div class="flex">
          <!-- Mobile menu button could go here -->
        </div>
        <div class="flex items-center space-x-4">
          <!-- Command Bar Trigger -->
          <button 
            @click="$window.dispatchEvent(new CustomEvent('open-command-bar'))"
            class="flex items-center px-3 py-1.5 bg-gray-100 hover:bg-gray-200 rounded-md text-gray-500 text-sm transition-colors mr-2 border border-gray-200"
          >
            <span class="mr-2">⌘ K</span>
            <span class="hidden sm:inline">Search...</span>
          </button>

          <button class="p-1 rounded-full text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
            <span class="sr-only">{{ $t('view_notifications') }}</span>
            <Bell class="h-6 w-6" />
          </button>

          <div class="relative ml-3 flex items-center">
            <div class="mr-4">
              <OrganizationSwitcher />
            </div>
            <div class="mr-4">
              <LanguageSwitcher />
            </div>
            <div class="flex items-center space-x-3">
              <div class="bg-gray-200 rounded-full p-2">
                <User class="h-5 w-5 text-gray-600" />
              </div>
              <span class="text-sm font-medium text-gray-700">{{ authStore.user?.username || $t('user') }}</span>
            </div>
            
            <button 
              @click="handleLogout"
              class="ml-4 px-3 py-2 text-sm text-red-600 hover:text-red-800 font-medium flex items-center"
            >
              <LogOut class="h-4 w-4 mr-1" />
              {{ $t('logout') }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>
