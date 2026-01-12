<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-800 dark:text-white">User Management</h1>
      <button @click="openCreateModal" class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg transition-colors">
        Create User
      </button>
    </div>

    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden border border-gray-100 dark:border-gray-700">
      <table class="w-full text-left">
        <thead class="bg-gray-50 dark:bg-gray-700/50 text-gray-600 dark:text-gray-300">
          <tr>
            <th class="p-4 font-medium">Username</th>
            <th class="p-4 font-medium">Full Name</th>
            <th class="p-4 font-medium">Email</th>
            <th class="p-4 font-medium">Roles</th>
            <th class="p-4 font-medium">Legacy ID</th>
            <th class="p-4 font-medium">Status</th>
            <th class="p-4 font-medium">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100 dark:divide-gray-700">
          <tr v-for="user in users" :key="user.userId" class="hover:bg-gray-50 dark:hover:bg-gray-700/30 transition-colors">
            <td class="p-4 text-gray-800 dark:text-gray-200">{{ user.username }}</td>
            <td class="p-4 text-gray-600 dark:text-gray-400">{{ user.fullName || '-' }}</td>
            <td class="p-4 text-gray-600 dark:text-gray-400">{{ user.email }}</td>
            <td class="p-4">
              <div class="flex gap-1 flex-wrap">
                <span v-for="role in user.roles" :key="role" 
                      class="px-2 py-0.5 text-xs rounded-full bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300">
                  {{ role.replace('ROLE_', '') }}
                </span>
              </div>
            </td>
            <td class="p-4 text-gray-600 dark:text-gray-400 font-mono text-sm">{{ user.legacyId || '-' }}</td>
            <td class="p-4">
              <span :class="user.enabled ? 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300' : 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300'"
                    class="px-2 py-0.5 text-xs rounded-full">
                {{ user.enabled ? 'Active' : 'Inactive' }}
              </span>
            </td>
            <td class="p-4">
              <button @click="editUser(user)" class="text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300 mr-3">
                Edit
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Simple Modal for Create/Edit would go here, or navigate to detail page -->
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { iamApi as api } from '@/api/axios'; // Use iamApi for user management

const users = ref([]);
const router = useRouter();

const fetchUsers = async () => {
  try {
    const response = await api.get('/api/users');
    users.value = response.data;
  } catch (error) {
    console.error('Failed to fetch users:', error);
  }
};

const openCreateModal = () => {
  router.push('/internal/users/create');
};

const editUser = (user) => {
  router.push(`/internal/users/${user.userId}`);
};

onMounted(() => {
  fetchUsers();
});
</script>
