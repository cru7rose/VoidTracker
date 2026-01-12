<template>
  <div class="p-6">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Audit Logs</h1>
      <p class="text-gray-500 dark:text-gray-400">Track system activity and compliance events.</p>
    </div>

    <!-- Filters -->
    <div class="bg-white dark:bg-gray-800 p-4 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 mb-6 flex gap-4">
      <div class="flex-1">
        <label class="block text-xs font-medium text-gray-500 mb-1">Search</label>
        <input v-model="filters.search" type="text" placeholder="Search logs..." class="block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm" />
      </div>
      <div class="w-48">
        <label class="block text-xs font-medium text-gray-500 mb-1">Action Type</label>
        <select v-model="filters.type" class="block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm">
          <option value="">All Types</option>
          <option value="CREATE">Create</option>
          <option value="UPDATE">Update</option>
          <option value="DELETE">Delete</option>
          <option value="LOGIN">Login</option>
        </select>
      </div>
      <div class="w-48">
        <label class="block text-xs font-medium text-gray-500 mb-1">User</label>
        <select v-model="filters.user" class="block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm">
          <option value="">All Users</option>
          <option value="admin">Admin User</option>
          <option value="system">System</option>
        </select>
      </div>
    </div>

    <!-- Logs Table -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
        <thead class="bg-gray-50 dark:bg-gray-900">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Timestamp</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">User</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Action</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Details</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">IP Address</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200 dark:divide-gray-700">
          <tr v-for="log in filteredLogs" :key="log.id" class="hover:bg-gray-50 dark:hover:bg-gray-700/50">
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ log.timestamp }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-white">
              <div class="flex items-center gap-2">
                <div class="w-6 h-6 rounded-full bg-gray-200 dark:bg-gray-600 flex items-center justify-center text-xs">
                  {{ log.user.charAt(0).toUpperCase() }}
                </div>
                {{ log.user }}
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">
              <span :class="['px-2 py-1 rounded-full text-xs font-medium', getTypeColor(log.type)]">
                {{ log.type }}
              </span>
            </td>
            <td class="px-6 py-4 text-sm text-gray-600 dark:text-gray-300">{{ log.details }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 font-mono">{{ log.ip }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const filters = ref({ search: '', type: '', user: '' });

const logs = ref([
  { id: 1, timestamp: '2023-10-27 14:30:05', user: 'admin', type: 'UPDATE', details: 'Updated System Settings (Branding)', ip: '192.168.1.1' },
  { id: 2, timestamp: '2023-10-27 14:15:22', user: 'system', type: 'CREATE', details: 'Auto-generated Invoice #INV-2023-001', ip: '127.0.0.1' },
  { id: 3, timestamp: '2023-10-27 13:45:10', user: 'admin', type: 'DELETE', details: 'Deleted User "john.doe"', ip: '192.168.1.1' },
  { id: 4, timestamp: '2023-10-27 09:00:00', user: 'admin', type: 'LOGIN', details: 'Successful login', ip: '192.168.1.1' },
  { id: 5, timestamp: '2023-10-26 16:20:00', user: 'dispatcher', type: 'CREATE', details: 'Created Order #ORD-5521', ip: '10.0.0.5' },
]);

const filteredLogs = computed(() => {
  return logs.value.filter(log => {
    const matchesSearch = log.details.toLowerCase().includes(filters.value.search.toLowerCase());
    const matchesType = !filters.value.type || log.type === filters.value.type;
    const matchesUser = !filters.value.user || log.user === filters.value.user;
    return matchesSearch && matchesType && matchesUser;
  });
});

const getTypeColor = (type) => {
  switch (type) {
    case 'CREATE': return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300';
    case 'UPDATE': return 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300';
    case 'DELETE': return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300';
    case 'LOGIN': return 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300';
    default: return 'bg-gray-100 text-gray-800';
  }
};
</script>
