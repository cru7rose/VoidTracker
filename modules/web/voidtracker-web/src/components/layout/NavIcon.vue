<template>
  <router-link 
    :to="to"
    class="relative group block"
  >
    <!-- Icon Button -->
    <div 
      :class="[
        'w-10 h-10 rounded-lg flex items-center justify-center transition-all duration-200',
        active 
          ? 'bg-void-cyan-500/20 border border-void-cyan-500 shadow-lg shadow-void-cyan-500/30' 
          : 'bg-white/5 border border-white/10 hover:bg-white/10 hover:border-void-cyan-500/50'
      ]"
    >
      <span :class="['text-xl', active ? 'animate-glow-pulse' : '']">{{ icon }}</span>
    </div>

    <!-- Tooltip -->
    <div class="absolute left-full ml-3 top-1/2 -translate-y-1/2 opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none whitespace-nowrap">
      <div class="glass-panel px-3 py-1.5">
        <span :class="['text-sm font-medium', active ? 'neon-text' : 'text-white/80']">
          {{ label }}
        </span>
      </div>
    </div>

    <!-- Active Indicator -->
    <div 
      v-if="active"
      class="absolute left-0 top-1/2 -translate-y-1/2 w-1 h-6 bg-void-cyan-400 rounded-r shadow-lg shadow-void-cyan-500/50"
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
