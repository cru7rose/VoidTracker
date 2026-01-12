<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h1 class="text-2xl font-bold text-gray-800">Business Rules</h1>
      <button @click="createNew" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">
        + New Rule
      </button>
    </div>

    <!-- List -->
    <div class="bg-white rounded-lg shadow overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Condition</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="rule in rules" :key="rule.id">
            <td class="px-6 py-4 whitespace-nowrap font-medium text-gray-900">{{ rule.name }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
              <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-purple-100 text-purple-800">
                {{ rule.ruleType }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 font-mono">{{ rule.condition }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
              <button @click="edit(rule)" class="text-blue-600 hover:text-blue-900 mr-3">Edit</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Edit Modal -->
    <div v-if="editingRule" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4">
      <div class="bg-white rounded-lg shadow-xl max-w-2xl w-full p-6">
        <h2 class="text-xl font-bold mb-4">{{ editingRule.id ? 'Edit Rule' : 'New Rule' }}</h2>
        
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700">Rule Name</label>
            <input v-model="editingRule.name" class="mt-1 block w-full border rounded p-2" placeholder="e.g. Max Weight Check" />
          </div>
          
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700">Rule Type</label>
              <select v-model="editingRule.ruleType" class="mt-1 block w-full border rounded p-2">
                <option value="CAPACITY_CHECK">Capacity Check</option>
                <option value="TIME_WINDOW">Time Window</option>
                <option value="AETR_COMPLIANCE">AETR Compliance</option>
                <option value="CUSTOM">Custom</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">Severity</label>
              <select v-model="editingRule.severity" class="mt-1 block w-full border rounded p-2">
                <option value="ERROR">Error (Block)</option>
                <option value="WARNING">Warning (Alert)</option>
                <option value="INFO">Info (Log)</option>
              </select>
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700">Condition (Expression)</label>
            <input v-model="editingRule.condition" class="mt-1 block w-full border rounded p-2 font-mono" placeholder="e.g. weight > 1000" />
            <p class="text-xs text-gray-500 mt-1">Use standard comparison operators.</p>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700">Action (Expression)</label>
            <input v-model="editingRule.actionExpression" class="mt-1 block w-full border rounded p-2 font-mono" placeholder="e.g. rejectOrder()" />
          </div>
        </div>

        <div class="mt-6 flex justify-end space-x-3">
          <button @click="editingRule = null" class="px-4 py-2 border rounded text-gray-700 hover:bg-gray-50">Cancel</button>
          <button @click="save" class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">Save</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { planningApi } from '../../../api/axios';

const rules = ref([]);
const editingRule = ref(null);

const fetchRules = async () => {
  // Mock data
  rules.value = [
    { id: 1, name: 'Heavy Load Restriction', ruleType: 'CAPACITY_CHECK', condition: 'weight > 1000', actionExpression: 'reject()', severity: 'ERROR' },
    { id: 2, name: 'Late Delivery Warning', ruleType: 'TIME_WINDOW', condition: 'delay > 15', actionExpression: 'alert()', severity: 'WARNING' }
  ];
};

const createNew = () => {
  editingRule.value = { name: '', ruleType: 'CAPACITY_CHECK', condition: '', actionExpression: '', severity: 'ERROR' };
};

const edit = (rule) => {
  editingRule.value = { ...rule };
};

const save = async () => {
  alert('Saved (Mock)!');
  editingRule.value = null;
};

onMounted(fetchRules);
</script>
