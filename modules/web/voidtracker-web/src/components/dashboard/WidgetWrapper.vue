<template>
  <div class="h-full w-full bg-white/80 backdrop-blur-md rounded-xl shadow-sm border border-gray-100 flex flex-col overflow-hidden transition-all duration-200 hover:shadow-md">
    <div class="drag-handle h-8 bg-gray-50/50 border-b border-gray-100 flex items-center justify-between px-3 cursor-move select-none group">
      <h3 class="text-xs font-semibold text-gray-600 uppercase tracking-wider">{{ title }}</h3>
      <div class="opacity-0 group-hover:opacity-100 transition-opacity">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
        </svg>
      </div>
    </div>
    <div class="flex-1 p-4 overflow-auto relative">
      <slot></slot>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  }
})
</script>

<style scoped>
.drag-handle:active {
  cursor: grabbing;
}
</style>
