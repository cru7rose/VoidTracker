<script setup>
import AppSwitcher from './AppSwitcher.vue';
import { LayoutDashboard, Package, Truck, Users, Settings, Map, FileText, Sliders, Building, MessageSquare } from 'lucide-vue-next';
import { useRoute } from 'vue-router';
import { computed } from 'vue';
import { useAuthStore } from '../stores/authStore';
import { useI18n } from 'vue-i18n';
import { useThemeStore } from '../stores/themeStore';
import LanguageSwitcher from './LanguageSwitcher.vue';

const route = useRoute();
const authStore = useAuthStore();
const themeStore = useThemeStore();

const { t } = useI18n();

const navigation = computed(() => {
  const roles = authStore.user?.roles || [];
  const roleArray = Array.isArray(roles) ? roles : Array.from(roles);
  
  if (roleArray.includes('ROLE_ADMIN') || roleArray.includes('ROLE_SUPER_USER')) {
    return [
      { name: t('common.dashboard'), href: '/internal/dashboard', icon: LayoutDashboard },
      { name: t('common.orders'), href: '/internal/orders', icon: Package },
      { name: t('common.dispatch'), href: '/internal/dispatch', icon: Map },
      { name: 'Assignments', href: '/internal/dispatch/history', icon: FileText },
      { name: 'Manifests', href: '/internal/manifests', icon: FileText },
      { name: 'Zone Management', href: '/internal/dispatch/zones', icon: Map },
      { name: 'Vehicle Profiles', href: '/internal/fleet/profiles', icon: Truck },
      { name: 'Carrier Compliance', href: '/internal/fleet/compliance', icon: FileText },
      { name: 'Report Builder', href: '/internal/analytics/reports', icon: FileText },
      { name: 'Communications', href: '/internal/admin/communications', icon: MessageSquare },
      { name: 'Audit Logs', href: '/internal/admin/audit-logs', icon: FileText },
      { name: 'Configuration', href: '/internal/config', icon: Sliders },
      { name: t('common.users'), href: '/internal/users', icon: Users },
      { name: 'Organizations', href: '/internal/organizations', icon: Building },
      { name: 'Leaderboard', href: '/internal/leaderboard', icon: Sliders },
    ];
  } else {
    return [
      { name: t('sidebar.dashboard'), href: '/customer/dashboard', icon: LayoutDashboard },
      { name: t('sidebar.my_orders'), href: '/customer/orders', icon: Package },
      { name: t('sidebar.new_order'), href: '/customer/orders/create', icon: FileText },
      { name: t('sidebar.bulk_upload'), href: '/customer/bulk-upload', icon: FileText },
      { name: t('sidebar.address_book'), href: '/customer/address-book', icon: Building },
      { name: t('sidebar.invoices'), href: '/customer/invoices', icon: FileText },
    ];
  }
});

const isActive = (path) => {
  return route.path === path || route.path.startsWith(path + '/');
};
</script>

<template>
  <div class="hidden md:flex md:flex-shrink-0">
    <div class="flex flex-col w-64 bg-white dark:bg-spotify-black text-gray-900 dark:text-white border-r border-gray-200 dark:border-spotify-gray-800 transition-colors">
      <!-- Logo / Brand Header -->
      <div class="flex items-center justify-center h-16 bg-white dark:bg-spotify-darker border-b border-gray-200 dark:border-spotify-gray-800 px-4 transition-colors">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-lg bg-gradient-to-br from-spotify-green-400 to-spotify-green-500 flex items-center justify-center shadow-lg shadow-spotify-green-400/30">
            <span class="text-white font-bold text-xl">V</span>
          </div>
          <span class="text-xl font-bold tracking-tight">VoidTracker</span>
        </div>
      </div>
      
      <!-- App Switcher -->
      <div class="p-4 border-b border-gray-200 dark:border-spotify-gray-800">
        <AppSwitcher />
      </div>
      
      <!-- Navigation -->
      <div class="flex-1 flex flex-col overflow-y-auto">
        <nav class="flex-1 px-3 py-4 space-y-1">
          <router-link
            v-for="item in navigation"
            :key="item.name"
            :to="item.href"
            class="group flex items-center px-3 py-2.5 text-sm font-medium rounded-lg transition-all duration-200"
            :class="[
              isActive(item.href)
                ? 'bg-spotify-green-400/10 dark:bg-spotify-green-400/20 text-spotify-green-600 dark:text-spotify-green-400'
                : 'text-gray-700 dark:text-spotify-gray-300 hover:bg-gray-100 dark:hover:bg-spotify-darker hover:text-gray-900 dark:hover:text-white'
            ]"
          >
            <component
              :is="item.icon"
              class="mr-3 flex-shrink-0 h-5 w-5 transition-colors"
              :class="[
                isActive(item.href) 
                  ? 'text-spotify-green-600 dark:text-spotify-green-400' 
                  : 'text-gray-500 dark:text-spotify-gray-400 group-hover:text-gray-700 dark:group-hover:text-white'
              ]"
              aria-hidden="true"
            />
            <span>{{ item.name }}</span>
          </router-link>
        </nav>
      </div>
      
      <!-- Footer -->
      <div class="p-4 border-t border-gray-200 dark:border-spotify-gray-800">
        <div class="flex items-center justify-between mb-3">
          <button
            @click="themeStore.toggleTheme()"
            class="flex items-center gap-2 px-3 py-2 text-sm text-gray-600 dark:text-spotify-gray-400 hover:text-gray-900 dark:hover:text-white hover:bg-gray-100 dark:hover:bg-spotify-darker rounded-lg transition-colors"
          >
            <span class="text-lg">{{ themeStore.isDark ? '☀️' : '🌙' }}</span>
            <span>{{ themeStore.isDark ? 'Light Mode' : 'Dark Mode' }}</span>
          </button>
        </div>
        <LanguageSwitcher />
      </div>
    </div>
  </div>
</template>
