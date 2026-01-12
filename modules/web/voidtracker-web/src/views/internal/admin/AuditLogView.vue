<template>
  <div class="p-6 max-w-6xl mx-auto font-inter">
    <div class="flex justify-between items-center mb-6">
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Audit & Compliance Log</h1>
        <div class="flex gap-2">
            <input v-model="searchQuery" type="text" placeholder="Search logs..." class="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg shadow-sm text-sm dark:bg-gray-700 dark:text-white" />
            <button class="px-3 py-2 bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg shadow-sm text-sm hover:bg-gray-50 dark:hover:bg-gray-600">
                Export CSV
            </button>
        </div>
    </div>

    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700 overflow-hidden">
        <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                <thead class="bg-gray-50 dark:bg-gray-900/50">
                    <tr>
                        <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Timestamp</th>
                        <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">User</th>
                        <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Action</th>
                        <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Details</th>
                    </tr>
                </thead>
                <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                    <tr v-for="log in filteredLogs" :key="log.id" class="hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400 font-mono">
                            {{ new Date(log.timestamp).toLocaleString() }}
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-white">
                            {{ log.user }}
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap">
                            <span :class="['px-2 inline-flex text-xs leading-5 font-semibold rounded-full', getActionColor(log.action)]">
                                {{ log.action }}
                            </span>
                        </td>
                        <td class="px-6 py-4 text-sm text-gray-500 dark:text-gray-400">
                            {{ log.details }}
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
        
        <div v-if="filteredLogs.length === 0" class="p-8 text-center text-gray-500 dark:text-gray-400">
            No logs found matching your search.
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { auditService } from '@/services/AuditService';

const searchQuery = ref('');
const logs = auditService.getLogs();

const filteredLogs = computed(() => {
    if (!searchQuery.value) return logs.value;
    const query = searchQuery.value.toLowerCase();
    return logs.value.filter(log => 
        log.user.toLowerCase().includes(query) ||
        log.action.toLowerCase().includes(query) ||
        log.details.toLowerCase().includes(query)
    );
});

const getActionColor = (action) => {
    if (action.includes('UPDATE')) return 'bg-blue-100 text-blue-800 dark:bg-blue-900/50 dark:text-blue-300';
    if (action.includes('DELETE')) return 'bg-red-100 text-red-800 dark:bg-red-900/50 dark:text-red-300';
    if (action.includes('CREATE')) return 'bg-green-100 text-green-800 dark:bg-green-900/50 dark:text-green-300';
    return 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300';
};
</script>
