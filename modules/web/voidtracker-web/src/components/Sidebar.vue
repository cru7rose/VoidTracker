<script setup>
import AppSwitcher from './AppSwitcher.vue';
import { LayoutDashboard, Package, Truck, Users, Settings, Map, FileText, Sliders, Building, MessageSquare } from 'lucide-vue-next';
import { useRoute } from 'vue-router';
import { computed } from 'vue';
import { useAuthStore } from '../stores/authStore';
import { useI18n } from 'vue-i18n';
import LanguageSwitcher from './LanguageSwitcher.vue';

const route = useRoute();
const authStore = useAuthStore();

const { t } = useI18n();

const navigation = computed(() => {
  const roles = authStore.user?.roles || [];
  const roleArray = Array.isArray(roles) ? roles : Array.from(roles);
  
  if (roleArray.includes('ROLE_ADMIN') || roleArray.includes('ROLE_SUPER_USER')) {
    return [
      { name: t('common.dashboard'), href: '/internal/dashboard', icon: LayoutDashboard },
      { name: t('common.orders'), href: '/internal/orders', icon: Package },
      { name: t('common.dispatch'), href: '/internal/dispatch', icon: Map },
      { name: 'Assignments', href: '/internal/dispatch/history', icon: FileText }, // History
      { name: 'Manifests', href: '/internal/manifests', icon: FileText }, // TODO: Add translation
      { name: 'Zone Management', href: '/internal/dispatch/zones', icon: Map }, // TODO: Add translation
      { name: 'Vehicle Profiles', href: '/internal/fleet/profiles', icon: Truck }, // TODO: Add translation
      { name: 'Carrier Compliance', href: '/internal/fleet/compliance', icon: FileText }, // TODO: Add translation
      { name: 'Report Builder', href: '/internal/analytics/reports', icon: FileText }, // TODO: Add translation
      { name: 'Communications', href: '/internal/admin/communications', icon: MessageSquare }, // TODO: Add translation
      { name: 'Audit Logs', href: '/internal/admin/audit-logs', icon: FileText }, // TODO: Add translation
      { name: 'Configuration', href: '/internal/config', icon: Sliders }, // TODO: Add translation
      { name: t('common.users'), href: '/internal/users', icon: Users },
      { name: 'Organizations', href: '/internal/organizations', icon: Building }, // TODO: Add translation
      { name: 'Leaderboard', href: '/internal/leaderboard', icon: Sliders }, // Using Sliders as placeholder icon, maybe Award would be better if available
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
    <div class="flex flex-col w-64 bg-gray-900 text-white">
      <div class="flex items-center justify-center h-16 bg-gray-900 border-b border-gray-800 px-4">
        <span class="text-xl font-bold tracking-wider">DANXILS OMS</span>
      </div>
      <div class="p-4 border-b border-gray-800">
        <AppSwitcher />
      </div>
      <div class="flex-1 flex flex-col overflow-y-auto">
        <nav class="flex-1 px-2 py-4 space-y-1">
          <router-link
            v-for="item in navigation"
            :key="item.name"
            :to="item.href"
            class="group flex items-center px-2 py-2 text-sm font-medium rounded-md transition-colors duration-150"
            :class="[
              isActive(item.href)
                ? 'bg-gray-800 text-white'
                : 'text-gray-300 hover:bg-gray-700 hover:text-white'
            ]"
          >
            <component
              :is="item.icon"
              class="mr-3 flex-shrink-0 h-6 w-6"
              :class="[
                isActive(item.href) ? 'text-white' : 'text-gray-400 group-hover:text-gray-300'
              ]"
              aria-hidden="true"
            />
            {{ item.name }}
          </router-link>
        </nav>
      </div>
      <div class="p-4 border-t border-gray-800">
        <LanguageSwitcher />
      </div>
    </div>
  </div>
</template>
