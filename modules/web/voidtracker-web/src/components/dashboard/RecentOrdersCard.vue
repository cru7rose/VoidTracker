<template>
  <div class="spotify-card">
    <h2 class="text-lg font-bold mb-4">
      Recent Orders
    </h2>
    <div class="space-y-2">
      <div 
        v-for="order in orders" 
        :key="order.id"
        class="playlist-item"
        @click="$emit('order-click', order.id)"
      >
        <div class="flex-1 min-w-0">
          <p class="text-sm font-semibold text-white truncate">#{{ order.id.substring(0, 8) }}</p>
          <p class="text-xs text-spotify-gray-400 truncate">{{ order.customerName }}</p>
        </div>
        <span 
          class="px-3 py-1 text-xs font-semibold rounded-full"
          :style="getStatusStyle(order.status)"
        >
          {{ order.status }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  orders: {
    type: Array,
    required: true
  }
});

defineEmits(['order-click']);

function getStatusStyle(status) {
  const styles = {
    'INGESTED': 'background: rgba(115, 115, 115, 0.2); color: rgb(163, 163, 163); border: 1px solid rgba(115, 115, 115, 0.3);',
    'PLANNED': 'background: rgba(29, 185, 84, 0.2); color: rgb(29, 185, 84); border: 1px solid rgba(29, 185, 84, 0.3);',
    'OUT_FOR_DELIVERY': 'background: rgba(250, 204, 21, 0.2); color: rgb(250, 204, 21); border: 1px solid rgba(250, 204, 21, 0.3);',
    'DELIVERED': 'background: rgba(34, 197, 94, 0.2); color: rgb(34, 197, 94); border: 1px solid rgba(34, 197, 94, 0.3);',
    'EXCEPTION': 'background: rgba(239, 68, 68, 0.2); color: rgb(239, 68, 68); border: 1px solid rgba(239, 68, 68, 0.3);'
  };
  return styles[status] || 'background: rgba(115, 115, 115, 0.2); color: rgb(163, 163, 163); border: 1px solid rgba(115, 115, 115, 0.3);';
}
</script>
