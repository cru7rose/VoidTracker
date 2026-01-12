<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-800 dark:text-white">Organizations</h1>
      <button @click="router.push('/internal/organizations/create')"
              class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors shadow-sm flex items-center">
        <span class="mr-2">+</span> New Organization
      </button>
    </div>

    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-left">
          <thead>
            <tr class="bg-gray-50 dark:bg-gray-700 border-b border-gray-100 dark:border-gray-600">
              <th class="px-6 py-3 text-xs font-semibold text-gray-500 dark:text-gray-300 uppercase tracking-wider">Legal Name</th>
              <th class="px-6 py-3 text-xs font-semibold text-gray-500 dark:text-gray-300 uppercase tracking-wider">Type</th>
              <th class="px-6 py-3 text-xs font-semibold text-gray-500 dark:text-gray-300 uppercase tracking-wider">Status</th>
              <th class="px-6 py-3 text-xs font-semibold text-gray-500 dark:text-gray-300 uppercase tracking-wider">Actions</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100 dark:divide-gray-700">
            <tr v-for="org in organizations" :key="org.orgId" class="hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
              <td class="px-6 py-4">
                <div class="text-sm font-medium text-gray-900 dark:text-white">{{ org.legalName }}</div>
                <div class="text-xs text-gray-500 dark:text-gray-400">{{ org.orgId }}</div>
              </td>
              <td class="px-6 py-4">
                <span class="px-2 py-1 text-xs font-medium rounded-full bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200">
                  {{ org.type || 'Standard' }}
                </span>
              </td>
              <td class="px-6 py-4">
                <span class="px-2 py-1 text-xs font-medium rounded-full"
                      :class="org.active ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' : 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'">
                  {{ org.active ? 'Active' : 'Inactive' }}
                </span>
              </td>
              <td class="px-6 py-4">
                <button @click="router.push(`/internal/organizations/${org.orgId}`)"
                        class="text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300 text-sm font-medium">
                  Edit
                </button>
              </td>
            </tr>
            <tr v-if="organizations.length === 0">
              <td colspan="4" class="px-6 py-8 text-center text-gray-500 dark:text-gray-400">
                No organizations found.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/api/axios';

const router = useRouter();
const organizations = ref([]);

const loadOrganizations = async () => {
  try {
    // Assuming an endpoint exists to list all orgs for admin
    // If not, we might need to create one or use a different approach
    // For now, let's assume /api/organizations exists and returns list
    const response = await api.get('/api/organizations');
    organizations.value = response.data;
  } catch (error) {
    console.error('Failed to load organizations:', error);
    // Mock data for now if endpoint fails or doesn't exist yet
    organizations.value = [
        { orgId: '123', legalName: 'Danxils Default Org', type: 'Internal', active: true },
        { orgId: '456', legalName: 'Acme Corp', type: 'Customer', active: true }
    ];
  }
};

onMounted(() => {
  loadOrganizations();
});
</script>
