<template>
  <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700 overflow-hidden">
    <div class="p-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
      <h3 class="text-lg font-medium text-gray-900 dark:text-white">Site Crew</h3>
      <button @click="showAssignModal = true" class="px-3 py-1.5 text-sm bg-blue-600 text-white hover:bg-blue-700 rounded shadow-sm">
        + Assign Crew Member
      </button>
    </div>

    <div class="overflow-x-auto">
      <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
        <thead class="bg-gray-50 dark:bg-gray-800">
          <tr>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Name</th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Role</th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Shift Status</th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Contact</th>
            <th scope="col" class="relative px-6 py-3">
              <span class="sr-only">Actions</span>
            </th>
          </tr>
        </thead>
        <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
          <tr v-for="member in crew" :key="member.id" class="hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center">
                <div class="flex-shrink-0 h-8 w-8">
                  <div class="h-8 w-8 rounded-full bg-gray-200 dark:bg-gray-600 flex items-center justify-center text-xs font-bold text-gray-600 dark:text-gray-300">
                    {{ getInitials(member.name) }}
                  </div>
                </div>
                <div class="ml-4">
                  <div class="text-sm font-medium text-gray-900 dark:text-white">{{ member.name }}</div>
                </div>
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
                <span :class="['px-2 inline-flex text-xs leading-5 font-semibold rounded-full', 
                               member.role === 'SITE_MANAGER' ? 'bg-purple-100 text-purple-800 dark:bg-purple-900/50 dark:text-purple-300' : 'bg-blue-100 text-blue-800 dark:bg-blue-900/50 dark:text-blue-300']">
                    {{ member.role === 'SITE_MANAGER' ? 'Manager' : 'Staff' }}
                </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
                <span :class="['px-2 inline-flex text-xs leading-5 font-semibold rounded-full', 
                               member.shiftStatus === 'ON_DUTY' ? 'bg-green-100 text-green-800 dark:bg-green-900/50 dark:text-green-300' : 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300']">
                    {{ member.shiftStatus === 'ON_DUTY' ? 'On Duty' : 'Off Duty' }}
                </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                {{ member.email }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
              <button @click="removeMember(member.id)" class="text-red-600 hover:text-red-900 dark:hover:text-red-400">Remove</button>
            </td>
          </tr>
          <tr v-if="crew.length === 0">
            <td colspan="5" class="px-6 py-10 text-center text-sm text-gray-500 dark:text-gray-400 italic">
                No crew assigned to this site yet.
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Assign Modal (Inline) -->
    <div v-if="showAssignModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-md">
            <h3 class="text-lg font-bold mb-4 text-gray-900 dark:text-white">Assign Crew Member</h3>
            <div class="mb-4">
                <label class="block text-sm font-medium mb-1 dark:text-gray-300">User Email</label>
                <input v-model="newUserEmail" type="email" class="w-full p-2 border rounded dark:bg-gray-700 dark:border-gray-600 dark:text-white" placeholder="user@example.com">
            </div>
            <div class="mb-4">
                <label class="block text-sm font-medium mb-1 dark:text-gray-300">Role</label>
                <select v-model="newUserRole" class="w-full p-2 border rounded dark:bg-gray-700 dark:border-gray-600 dark:text-white">
                    <option value="SITE_STAFF">Staff</option>
                    <option value="SITE_MANAGER">Manager</option>
                </select>
            </div>
            <div class="flex justify-end gap-2">
                <button @click="showAssignModal = false" class="px-3 py-1 text-gray-600 hover:bg-gray-100 rounded">Cancel</button>
                <button @click="assignMember" class="px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700">Assign</button>
            </div>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '@/api/axios';

const props = defineProps({
    siteId: String
});

const crew = ref([]);
const showAssignModal = ref(false);
const newUserEmail = ref('');
const newUserRole = ref('SITE_STAFF');

const loadCrew = async () => {
    try {
        // In a real app, we'd fetch users assigned to this site
        // const response = await api.get(`/api/sites/${props.siteId}/users`);
        // crew.value = response.data;
        
        // Mock data for now
        crew.value = [
            { id: '1', name: 'Alice Smith', email: 'alice@example.com', role: 'SITE_MANAGER', shiftStatus: 'ON_DUTY' },
            { id: '2', name: 'Bob Jones', email: 'bob@example.com', role: 'SITE_STAFF', shiftStatus: 'OFF_DUTY' },
            { id: '3', name: 'Charlie Day', email: 'charlie@example.com', role: 'SITE_STAFF', shiftStatus: 'ON_DUTY' }
        ];
    } catch (error) {
        console.error('Failed to load crew:', error);
    }
};

const assignMember = async () => {
    // Mock assignment logic
    crew.value.push({
        id: Date.now().toString(),
        name: newUserEmail.value.split('@')[0], // Mock name
        email: newUserEmail.value,
        role: newUserRole.value,
        shiftStatus: 'OFF_DUTY'
    });
    showAssignModal.value = false;
    newUserEmail.value = '';
};

const removeMember = (id) => {
    if(confirm('Remove this member?')) {
        crew.value = crew.value.filter(m => m.id !== id);
    }
};

const getInitials = (name) => {
    return name ? name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2) : '?';
};

onMounted(() => {
    loadCrew();
});
</script>
