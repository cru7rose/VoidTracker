<template>
  <div class="fixed left-0 top-0 h-full w-16 glass-panel border-r border-white/10">
    <div class="flex flex-col items-center py-6 space-y-8">
      <!-- Logo / Brand -->
      <div class="relative group">
        <div class="w-10 h-10 rounded-lg bg-gradient-to-br from-void-cyan-500 to-void-pink-500 flex items-center justify-center shadow-lg">
          <span class="text-white font-bold text-2xl font-mono">V</span>
        </div>
        <div class="absolute left-full ml-3 top-1/2 -translate-y-1/2 opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none">
          <div class="glass-panel px-3 py-1 whitespace-nowrap">
            <span class="neon-text text-sm font-bold">VOID-FLOW</span>
          </div>
        </div>
      </div>

      <!-- Divider -->
      <div class="w-8 h-px bg-white/10"></div>

      <!-- Navigation Icons -->
      <NavIcon 
        icon="🗺️" 
        to="/map" 
        label="Control Tower"
        :active="isActive('/map')"
      />
      
      <NavIcon 
        icon="🚛" 
        to="/fleet" 
        label="Fleet"
        :active="isActive('/fleet')"
      />
      
      <NavIcon 
        icon="📊" 
        to="/analytics" 
        label="Analytics"
        :active="isActive('/analytics')"
      />
      
      <!-- Spacer -->
      <div class="flex-1"></div>
      
      <!-- Bottom Icons -->
      <NavIcon 
        icon="⚙️" 
        to="/settings" 
        label="Settings"
        :active="isActive('/settings')"
      />
    </div>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router';
import NavIcon from './NavIcon.vue';

const route = useRoute();

function isActive(path) {
  return route.path.startsWith(path);
}
</script>
