<template>
  <div class="overflow-auto h-full">
    <table class="min-w-full divide-y divide-gray-200">
      <thead class="bg-gray-50 sticky top-0">
        <tr>
          <th scope="col" class="px-3 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
          <th scope="col" class="px-3 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
          <th scope="col" class="px-3 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ETA</th>
        </tr>
      </thead>
      <tbody class="bg-white divide-y divide-gray-200">
        <tr v-for="order in orders" :key="order.id">
          <td class="px-3 py-2 whitespace-nowrap text-xs font-medium text-gray-900">{{ order.id }}</td>
          <td class="px-3 py-2 whitespace-nowrap text-xs">
            <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
              {{ order.status }}
            </span>
          </td>
          <td class="px-3 py-2 whitespace-nowrap text-xs text-gray-500">{{ order.eta }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
const orders = [
  { id: '#ORD-001', status: 'In Transit', eta: '14:30' },
  { id: '#ORD-002', status: 'Pending', eta: '15:00' },
  { id: '#ORD-003', status: 'Delivered', eta: '12:15' },
  { id: '#ORD-004', status: 'In Transit', eta: '16:45' },
  { id: '#ORD-005', status: 'Pending', eta: 'Tomorrow' }
]
</script>
