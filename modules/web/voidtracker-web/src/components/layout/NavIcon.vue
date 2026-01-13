<template>
  <router-link 
    :to="to"
    class="relative group block"
  >
    <!-- Icon Button -->
    <div 
      :class="[
        'w-12 h-12 rounded-xl flex items-center justify-center transition-all duration-300 relative overflow-hidden',
        active 
          ? 'bg-void-cyan-900/50 border-2 border-void-cyan-400 shadow-lg shadow-void-cyan-500/50 neon-border' 
          : 'bg-void-cyan-950/30 border border-void-cyan-800 hover:bg-void-cyan-900/50 hover:border-void-cyan-600 hover:shadow-lg hover:shadow-void-cyan-500/30'
      ]"
    >
      <!-- Hologram Scan Effect -->
      <div 
        v-if="active"
        class="absolute inset-0 bg-gradient-to-b from-transparent via-void-cyan-400/20 to-transparent animate-scan-line"
        style="animation-duration: 3s;"
      ></div>
      
      <span :class="[
        'text-2xl relative z-10 transition-transform duration-300',
        active ? 'animate-neon-flicker scale-110' : 'group-hover:scale-110'
      ]">
        {{ icon }}
      </span>
    </div>

    <!-- Tooltip -->
    <div class="absolute left-full ml-4 top-1/2 -translate-y-1/2 opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none whitespace-nowrap z-50">
      <div class="glass-panel px-4 py-2 rounded-lg border border-void-cyan-600/50">
        <span :class="[
          'text-sm font-bold font-mono tracking-wider',
          active ? 'neon-text' : 'text-void-cyan-300'
        ]">
          {{ label }}
        </span>
      </div>
    </div>

    <!-- Active Indicator -->
    <div 
      v-if="active"
      class="absolute left-0 top-1/2 -translate-y-1/2 w-1 h-8 bg-void-cyan-400 rounded-r shadow-lg shadow-void-cyan-500/70 animate-glow-pulse"
    ></div>
  </router-link>
</template>

<script setup>
defineProps({
  icon: {
    type: String,
    required: true
  },
  to: {
    type: String,
    required: true
  },
  label: {
    type: String,
    required: true
  },
  active: {
    type: Boolean,
    default: false
  }
});
</script>
