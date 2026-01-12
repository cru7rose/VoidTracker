<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <div>
        <h2 class="text-2xl font-bold text-gray-800">Workflow Automation (n8n)</h2>
        <p class="text-gray-500">Manage automation flows and webhooks.</p>
      </div>
      <a href="http://localhost:5678" target="_blank" class="bg-orange-600 hover:bg-orange-700 text-white px-4 py-2 rounded shadow flex items-center gap-2">
         <span>Open n8n Editor</span>
         <span class="text-xs opacity-75">↗</span>
      </a>
    </div>

    <!-- Workflow List -->
    <div class="bg-white rounded-lg shadow overflow-hidden">
        <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
                <tr>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                     <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Trigger</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                    <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
                <tr v-for="flow in workflows" :key="flow.id">
                    <td class="px-6 py-4 whitespace-nowrap">
                         <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full"
                          :class="flow.active ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'">
                           {{ flow.active ? 'Active' : 'Inactive' }}
                         </span>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap font-medium text-gray-900">{{ flow.name }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ flow.triggerType }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-xs text-gray-400 font-mono">{{ flow.id }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium space-x-3">
                        <button @click="toggleFlow(flow)" class="text-blue-600 hover:text-blue-900">{{ flow.active ? 'Deactivate' : 'Activate' }}</button>
                        <a :href="`http://localhost:5678/workflow/${flow.id}`" target="_blank" class="text-gray-600 hover:text-gray-900">Edit</a>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>

    <!-- Embedded Editor (Optional / if supported) -->
    <div class="bg-white rounded-lg shadow p-4">
        <h3 class="font-bold text-gray-800 mb-2">Live Editor Preview</h3>
        <div class="border border-gray-200 rounded h-[600px] bg-gray-50 flex items-center justify-center text-gray-400">
            <!-- Iframe would go here if CORS/Headers allow -->
             <iframe src="http://localhost:5678/workflow/new" class="w-full h-full border-0" title="n8n Editor"></iframe>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const workflows = ref([
    { id: '1', name: 'Dispatch Notification', active: true, triggerType: 'Webhook (POST)' },
    { id: '2', name: 'Order Sync (SAP)', active: false, triggerType: 'Cron (15m)' },
    { id: '3', name: 'Driver Magic Link Generator', active: true, triggerType: 'Webhook' }
]);

const toggleFlow = (flow) => {
    flow.active = !flow.active;
};
</script>
