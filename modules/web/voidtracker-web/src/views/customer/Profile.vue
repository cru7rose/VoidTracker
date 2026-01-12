<template>
  <div class="p-6 max-w-4xl mx-auto">
    <h1 class="text-2xl font-bold text-gray-800 dark:text-white mb-6">My Profile</h1>

    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 border border-gray-100 dark:border-gray-700">
      <form @submit.prevent="saveProfile">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- User Info (Read Only) -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Username</label>
            <input v-model="user.username" type="text" disabled
                   class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-gray-100 dark:bg-gray-700 text-gray-500 dark:text-gray-400 cursor-not-allowed" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Email</label>
            <input v-model="user.email" type="email" disabled
                   class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-gray-100 dark:bg-gray-700 text-gray-500 dark:text-gray-400 cursor-not-allowed" />
          </div>

          <!-- Customer Profile Info -->
          <div class="md:col-span-2 border-t border-gray-100 dark:border-gray-700 pt-6 mt-2">
            <h2 class="text-lg font-semibold text-gray-800 dark:text-white mb-4">Customer Details</h2>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Full Name / Company Name</label>
            <input v-model="profile.name" type="text"
                   class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                   placeholder="Enter your name or company name" />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Legacy ID (Read Only)</label>
            <input v-model="profile.legacyId" type="text" disabled
                   class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-gray-100 dark:bg-gray-700 text-gray-500 dark:text-gray-400 cursor-not-allowed"
                   placeholder="Not assigned" />
          </div>

          <div class="md:col-span-2">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Contact Info (JSON)</label>
            <textarea v-model="profile.contactInfo" rows="4"
                      class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all font-mono text-sm"
                      placeholder='{"phone": "+123456789", "address": "..."}'></textarea>
            <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">Please enter contact details in JSON format.</p>
          </div>
        </div>

        <div class="mt-8 flex justify-end">
          <button type="submit"
                  class="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors shadow-sm">
            Save Profile
          </button>
        </div>
      </form>
    </div>

    <!-- Change Password Section -->
    <div class="mt-6 bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 border border-gray-100 dark:border-gray-700">
        <h2 class="text-lg font-semibold text-gray-800 dark:text-white mb-4">Change Password</h2>
        <form @submit.prevent="changePassword">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Current Password</label>
                    <input v-model="passwordForm.oldPassword" type="password" required
                           class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all" />
                </div>
                <div></div> <!-- Spacer -->
                <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">New Password</label>
                    <input v-model="passwordForm.newPassword" type="password" required
                           class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all" />
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Confirm New Password</label>
                    <input v-model="passwordForm.confirmPassword" type="password" required
                           class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all" />
                </div>
            </div>
            <div class="mt-6 flex justify-end">
                <button type="submit"
                        class="px-6 py-2 bg-gray-800 hover:bg-gray-900 text-white rounded-lg transition-colors shadow-sm dark:bg-gray-600 dark:hover:bg-gray-500">
                    Change Password
                </button>
            </div>
        </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '@/api/axios';
import { useAuthStore } from '@/stores/authStore'; // Assuming auth store exists

const authStore = useAuthStore();
const user = ref({
  username: '',
  email: ''
});
const profile = ref({
  name: '',
  legacyId: '',
  contactInfo: ''
});

const loadProfile = async () => {
  // Load basic user info from auth store or API
  // For now assuming we can get it from /api/users/me or similar, but let's use what we have in store if possible
  // Or fetch from backend
  try {
      // Fetch user info (assuming endpoint exists or we use store)
      // user.value = ... 
      // Fetch customer profile
      const response = await api.get('/api/customers/me');
      const data = response.data;
      profile.value = {
          name: data.name || '',
          legacyId: data.legacyId || '',
          contactInfo: data.contactInfo || ''
      };
      
      // If we need user info and it's not in profile response, we might need another call
      // For this PoC, let's assume we can get it or just leave it blank/from store
      if (authStore.user) {
          user.value.username = authStore.user.username;
          user.value.email = authStore.user.email; // might not be in token
      }
  } catch (error) {
      console.error('Failed to load profile:', error);
      // If 404, it means profile doesn't exist yet, which is fine, we create it on save
  }
};

const saveProfile = async () => {
  try {
      // Check if profile exists (we could track this with a flag or just try update and fallback to create)
      // But the controller has separate endpoints. Let's try update, if 404/error then create?
      // Or better, check if we loaded an ID.
      // Actually, the controller has /me for both POST and PUT.
      // Let's try PUT first.
      
      await api.put('/api/customers/me', {
          name: profile.value.name,
          contactInfo: profile.value.contactInfo,
          legacyId: profile.value.legacyId // Usually read-only but sending it back is fine
      });
      alert('Profile updated successfully');
  } catch (error) {
      // If PUT fails, maybe try POST? Or maybe the backend handles upsert?
      // My backend implementation:
      // updateCustomerProfile throws if not found.
      // createCustomerProfile throws if exists.
      // So we need to know.
      
      // Let's try POST if PUT failed (assuming it failed because not found)
      try {
           await api.post('/api/customers/me', {
              name: profile.value.name,
              contactInfo: profile.value.contactInfo,
              legacyId: profile.value.legacyId
          });
          alert('Profile created successfully');
      } catch (createError) {
          console.error('Failed to save profile:', createError);
          alert('Failed to save profile');
      }
  }
};

const passwordForm = ref({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
});

const changePassword = async () => {
    if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
        alert('New passwords do not match');
        return;
    }
    
    try {
        await api.post('/api/auth/change-password', {
            oldPassword: passwordForm.value.oldPassword,
            newPassword: passwordForm.value.newPassword
        });
        alert('Password changed successfully');
        passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' };
    } catch (error) {
        console.error('Failed to change password:', error);
        alert(error.response?.data?.message || 'Failed to change password');
    }
};

onMounted(() => {
  loadProfile();
});
</script>
