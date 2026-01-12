<template>
  <div class="grid grid-cols-2 gap-4 h-full">
    <div v-for="(stat, index) in stats" :key="index" class="flex flex-col justify-center items-center bg-gray-50 rounded-lg p-2">
      <span class="text-2xl font-bold text-gray-800">{{ stat.value }}</span>
      <span class="text-xs text-gray-500 uppercase tracking-wide text-center">{{ stat.label }}</span>
    </div>
  </div>
</template>

<script setup>
const stats = [
  { label: 'Active Drivers', value: '12' },
  { label: 'Pending Orders', value: '45' },
  { label: 'Completed', value: '128' },
  { label: 'Issues', value: '3' }
]
</script>
