<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h1 class="text-2xl font-bold text-gray-800">View Configurations</h1>
      <button @click="createNew" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">
        + New View Config
      </button>
    </div>

    <!-- List -->
    <div class="bg-white rounded-lg shadow overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">View Name</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Component ID</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="view in views" :key="view.id">
            <td class="px-6 py-4 whitespace-nowrap font-medium text-gray-900">{{ view.viewName }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 font-mono">{{ view.componentId }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
              <button @click="edit(view)" class="text-blue-600 hover:text-blue-900 mr-3">Edit</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Edit Modal -->
    <div v-if="editingView" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4">
      <div class="bg-white rounded-lg shadow-xl max-w-2xl w-full p-6">
        <h2 class="text-xl font-bold mb-4">{{ editingView.id ? 'Edit View Config' : 'New View Config' }}</h2>
        
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700">View Name</label>
            <input v-model="editingView.viewName" class="mt-1 block w-full border rounded p-2" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">Component ID</label>
            <input v-model="editingView.componentId" class="mt-1 block w-full border rounded p-2" placeholder="e.g. ORDERS_BOARD" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">Configuration (JSON)</label>
            <textarea v-model="editingView.configurationJson" rows="10" class="mt-1 block w-full border rounded p-2 font-mono text-sm"></textarea>
            <p class="text-xs text-gray-500 mt-1">Define columns, filters, and layout options.</p>
          </div>
        </div>

        <div class="mt-6 flex justify-end space-x-3">
          <button @click="editingView = null" class="px-4 py-2 border rounded text-gray-700 hover:bg-gray-50">Cancel</button>
          <button @click="save" class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">Save</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { planningApi } from '../../../api/axios';

const views = ref([]);
const editingView = ref(null);

const fetchViews = async () => {
  // Mock data
  views.value = [
    { 
      id: 1, 
      viewName: 'Default Orders Board', 
      componentId: 'ORDERS_BOARD', 
      configurationJson: '{\n  "columns": [\n    {"id": "new", "label": "New Orders"},\n    {"id": "ready", "label": "Ready to Ship"}\n  ]\n}' 
    }
  ];
};

const createNew = () => {
  editingView.value = { viewName: '', componentId: '', configurationJson: '{}' };
};

const edit = (view) => {
  editingView.value = { ...view };
};

const save = async () => {
  alert('Saved (Mock)!');
  editingView.value = null;
};

onMounted(fetchViews);
</script>
