<template>
  <div class="antialiased text-slate-100 bg-slate-900 min-h-screen">
    <ToastNotification />
    <CommandBar />
    <router-view></router-view>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useThemeStore } from '@/stores/themeStore';
import CommandBar from '@/components/CommandBar.vue';
import ToastNotification from '@/components/common/ToastNotification.vue';

const themeStore = useThemeStore();

onMounted(() => {
    themeStore.applyTheme();
});
</script>
