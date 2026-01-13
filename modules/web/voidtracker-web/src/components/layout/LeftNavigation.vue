<template>
  <div class="fixed left-0 top-0 h-full w-20 glass-panel border-r border-void-cyan-900/50 backdrop-blur-2xl z-50">
    <div class="flex flex-col items-center py-6 space-y-8 h-full">
      <!-- Logo / Brand -->
      <div class="relative group">
        <div class="w-14 h-14 rounded-xl bg-gradient-to-br from-void-cyan-400 to-void-pink-400 flex items-center justify-center shadow-2xl shadow-void-cyan-500/50 animate-neon-flicker border-2 border-void-cyan-400/50">
          <span class="text-void-black font-bold text-3xl font-mono">V</span>
        </div>
        <div class="absolute left-full ml-4 top-1/2 -translate-y-1/2 opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none z-50">
          <div class="glass-panel px-4 py-2 rounded-lg border border-void-cyan-600/50">
            <span class="neon-text text-sm font-bold font-mono tracking-wider">VOID-FLOW</span>
          </div>
        </div>
      </div>

      <!-- Divider -->
      <div class="w-12 h-px bg-gradient-to-r from-transparent via-void-cyan-600 to-transparent"></div>

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
