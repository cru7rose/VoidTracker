<template>
  <div 
    :class="[
      'glass-panel rounded-xl',
      hoverEffect && 'glass-panel-hover cursor-pointer',
      paddingClass,
      className
    ]"
    :style="customStyle"
  >
    <slot />
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  hoverEffect: {
    type: Boolean,
    default: false
  },
  padding: {
    type: String,
    default: 'md',
    validator: (value) => ['none', 'sm', 'md', 'lg', 'xl'].includes(value)
  },
  blur: {
    type: String,
    default: 'xl'
  },
  className: {
    type: String,
    default: ''
  }
});

const paddingClass = computed(() => {
  const paddings = {
    'none': 'p-0',
    'sm': 'p-2',
    'md': 'p-4',
    'lg': 'p-6',
    'xl': 'p-8'
  };
  return paddings[props.padding];
});

const customStyle = computed(() => {
  if (props.blur && props.blur !== 'xl') {
    return {
      backdropFilter: `blur(${props.blur})`
    };
  }
  return {};
});
</script>

<style scoped>
/* Reduced motion support */
@media (prefers-reduced-motion: reduce) {
  .glass-panel {
    backdrop-filter: none !important;
    background: rgba(10, 10, 15, 0.95) !important;
  }
}



/* High contrast mode support */
@media (prefers-contrast: high) {
  .glass-panel {
    border-width: 2px;
    background: rgba(10, 10, 15, 0.9) !important;
  }
}
</style>
