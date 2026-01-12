<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-2xl w-full space-y-8 bg-white p-10 rounded-xl shadow-lg">
      <div>
        <h2 class="mt-2 text-center text-3xl font-extrabold text-gray-900">
          Create your account
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600">
          Or
          <router-link to="/customer/login" class="font-medium text-blue-600 hover:text-blue-500">
            sign in to your existing account
          </router-link>
        </p>
      </div>
      
      <form class="mt-8 space-y-6" @submit.prevent="handleRegister">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- Account Info -->
            <div class="md:col-span-2">
                <h3 class="text-lg font-medium text-gray-900 border-b pb-2 mb-4">Account Information</h3>
            </div>
            
            <div>
                <label for="username" class="block text-sm font-medium text-gray-700">Username</label>
                <input id="username" v-model="form.username" type="text" required 
                    class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm" />
            </div>
            
            <div>
                <label for="email" class="block text-sm font-medium text-gray-700">Email address</label>
                <input id="email" v-model="form.email" type="email" autocomplete="email" required 
                    class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm" />
            </div>

            <div>
                <label for="password" class="block text-sm font-medium text-gray-700">Password</label>
                <input id="password" v-model="form.password" type="password" autocomplete="new-password" required 
                    class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm" />
            </div>

             <div>
                <label for="fullName" class="block text-sm font-medium text-gray-700">Full Name</label>
                <input id="fullName" v-model="form.fullName" type="text" required 
                    class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm" />
            </div>

            <!-- Company Info -->
            <div class="md:col-span-2 mt-4">
                <h3 class="text-lg font-medium text-gray-900 border-b pb-2 mb-4">Company Details</h3>
            </div>

            <div>
                <label for="companyName" class="block text-sm font-medium text-gray-700">Company Name</label>
                <input id="companyName" v-model="form.companyName" type="text" required 
                    class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm" />
            </div>

            <div>
                <label for="vatId" class="block text-sm font-medium text-gray-700">VAT ID</label>
                <input id="vatId" v-model="form.vatId" type="text" required 
                    class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm" />
            </div>

            <div>
                <label for="phoneNumber" class="block text-sm font-medium text-gray-700">Phone Number</label>
                <input id="phoneNumber" v-model="form.phoneNumber" type="tel" required 
                    class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm" />
            </div>

            <!-- Address Info -->
            <div class="md:col-span-2 mt-4">
                <h3 class="text-lg font-medium text-gray-900 border-b pb-2 mb-4">Address</h3>
            </div>

            <div class="md:col-span-2">
                <label for="street" class="block text-sm font-medium text-gray-700">Street</label>
                <input id="street" v-model="form.street" type="text" required 
                    class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm" />
            </div>

            <div>
                <label for="houseNumber" class="block text-sm font-medium text-gray-700">House Number</label>
                <input id="houseNumber" v-model="form.houseNumber" type="text" required 
                    class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm" />
            </div>

            <div>
                <label for="postalCode" class="block text-sm font-medium text-gray-700">Postal Code</label>
                <input id="postalCode" v-model="form.postalCode" type="text" required 
                    class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm" />
            </div>

            <div>
                <label for="city" class="block text-sm font-medium text-gray-700">City</label>
                <input id="city" v-model="form.city" type="text" required 
                    class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm" />
            </div>

            <div>
                <label for="country" class="block text-sm font-medium text-gray-700">Country</label>
                <input id="country" v-model="form.country" type="text" required 
                    class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm" />
            </div>
        </div>

        <div class="flex items-start">
            <div class="flex items-center h-5">
                <input id="terms" name="terms" type="checkbox" v-model="form.termsAccepted" required
                    class="focus:ring-blue-500 h-4 w-4 text-blue-600 border-gray-300 rounded" />
            </div>
            <div class="ml-3 text-sm">
                <label for="terms" class="font-medium text-gray-700">I agree to the <a href="#" class="text-blue-600 hover:text-blue-500">Privacy Policy</a> and <a href="#" class="text-blue-600 hover:text-blue-500">Terms of Service</a></label>
            </div>
        </div>

        <div>
          <button type="submit" :disabled="loading"
            class="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 transition-colors shadow-sm">
            {{ loading ? 'Creating account...' : 'Create Account' }}
          </button>
        </div>
        
        <div v-if="error" class="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-md text-sm text-center">
          {{ error }}
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { iamApi as api } from '@/api/axios';

const router = useRouter();
const authStore = useAuthStore();

const form = reactive({
    username: '',
    email: '',
    password: '',
    fullName: '',
    companyName: '',
    vatId: '',
    phoneNumber: '',
    street: '',
    houseNumber: '',
    postalCode: '',
    city: '',
    country: '',
    termsAccepted: false
});

const loading = ref(false);
const error = ref('');

const handleRegister = async () => {
  if (!form.termsAccepted) {
      error.value = 'You must accept the Privacy Policy and Terms of Service.';
      return;
  }

  loading.value = true;
  error.value = '';
  
  try {
    const response = await api.post('/api/auth/register', form);
    
    authStore.setAuth(response.data);
    
    router.push('/customer/dashboard');
  } catch (err) {
    console.error('Registration failed', err);
    error.value = err.response?.data?.message || 'Registration failed. Please try again.';
  } finally {
    loading.value = false;
  }
};
</script>
