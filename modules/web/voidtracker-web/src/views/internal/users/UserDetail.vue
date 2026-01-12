<template>
  <div class="p-6 max-w-4xl mx-auto">
    <div class="flex items-center mb-6">
      <button @click="router.back()" class="mr-4 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200">
        &larr; Back
      </button>
      <h1 class="text-2xl font-bold text-gray-800 dark:text-white">
        {{ isNew ? 'Create User' : 'Edit User' }}
      </h1>
    </div>

    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 border border-gray-100 dark:border-gray-700">
      <form @submit.prevent="saveUser">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- Username -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Username</label>
            <input v-model="form.username" type="text" :disabled="!isNew"
                   class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                   required />
          </div>

          <!-- Email -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Email</label>
            <input v-model="form.email" type="email"
                   class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                   required />
          </div>

          <!-- Legacy ID -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Legacy ID</label>
            <input v-model="form.legacyId" type="text"
                   class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                   placeholder="Optional" />
          </div>

          <!-- Full Name -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Full Name</label>
            <input v-model="form.fullName" type="text"
                   class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all" />
          </div>

          <!-- Avatar URL -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Avatar URL</label>
            <input v-model="form.avatarUrl" type="text"
                   class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all" />
          </div>

          <!-- Bio -->
          <div class="md:col-span-2">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Bio</label>
            <textarea v-model="form.bio" rows="3"
                      class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all"></textarea>
          </div>

          <!-- Status -->
          <div class="flex items-center h-full pt-6">
            <label class="flex items-center cursor-pointer">
              <input v-model="form.enabled" type="checkbox" class="sr-only peer">
              <div class="relative w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 dark:peer-focus:ring-blue-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-blue-600"></div>
              <span class="ms-3 text-sm font-medium text-gray-700 dark:text-gray-300">Active Account</span>
            </label>
          </div>

          <!-- Roles -->
          <div class="md:col-span-2">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Roles</label>
            <div class="flex gap-4 flex-wrap">
              <label v-for="role in availableRoles" :key="role" class="flex items-center space-x-2 cursor-pointer">
                <input type="checkbox" :value="role" v-model="form.roles"
                       class="w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 rounded focus:ring-blue-500 dark:focus:ring-blue-600 dark:ring-offset-gray-800 focus:ring-2 dark:bg-gray-700 dark:border-gray-600">
                <span class="text-gray-700 dark:text-gray-300">{{ role.replace('ROLE_', '') }}</span>
              </label>
            </div>
          </div>
        </div>

        <div class="mt-8 flex justify-end gap-4">
          <button type="button" @click="router.back()"
                  class="px-4 py-2 text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors dark:bg-gray-700 dark:text-gray-300 dark:hover:bg-gray-600">
            Cancel
          </button>
          <button type="submit"
                  class="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors shadow-sm">
            {{ isNew ? 'Create User' : 'Save Changes' }}
          </button>
        </div>
      </form>
    </div>

    <!-- Password Reset Section (Admin Only) -->
    <div v-if="!isNew" class="mt-6 bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 border border-gray-100 dark:border-gray-700">
        <h2 class="text-lg font-semibold text-gray-800 dark:text-white mb-4">Reset Password</h2>
        <div class="flex gap-4 items-end">
            <div class="flex-1">
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">New Password</label>
                <input v-model="newPassword" type="password"
                       class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all" />
            </div>
            <button @click="resetPassword"
                    class="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors shadow-sm h-10">
                Reset Password
            </button>
        </div>
    </div>

    <!-- User Preferences Section -->
    <div v-if="!isNew" class="mt-6 bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 border border-gray-100 dark:border-gray-700">
        <h2 class="text-lg font-semibold text-gray-800 dark:text-white mb-4">User Preferences</h2>
        <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Configuration (JSON)</label>
            <textarea v-model="preferencesJson" rows="4"
                      class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all font-mono text-sm"
                      placeholder='{"theme": "dark", "notifications": true}'></textarea>
        </div>
        <div class="flex justify-end">
            <button @click="savePreferences"
                    class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors shadow-sm">
                Save Preferences
            </button>
        </div>
    </div>

    <!-- Organization Access Section -->
    <div v-if="!isNew" class="mt-6 bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 border border-gray-100 dark:border-gray-700">
        <h2 class="text-lg font-semibold text-gray-800 dark:text-white mb-4">Organization Access</h2>
        
        <!-- Add Organization Form -->
        <div class="flex gap-4 mb-6 items-end">
            <div class="flex-1">
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Select Organization</label>
                <select v-model="selectedOrgId" class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white">
                    <option value="" disabled>Select an organization...</option>
                    <option v-for="org in allOrganizations" :key="org.orgId" :value="org.orgId">
                        {{ org.legalName }} ({{ org.orgId }})
                    </option>
                </select>
            </div>
            <div class="w-48">
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Role</label>
                <select v-model="selectedOrgRole" class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white">
                    <option value="customer">Customer</option>
                    <option value="admin">Admin</option>
                    <option value="driver">Driver</option>
                </select>
            </div>
            <button @click="addOrgAccess" :disabled="!selectedOrgId"
                    class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors shadow-sm disabled:opacity-50 disabled:cursor-not-allowed h-10">
                Add
            </button>
        </div>

        <div v-if="organizations.length > 0" class="space-y-4">
            <div v-for="org in organizations" :key="org.orgId" class="border border-gray-200 dark:border-gray-600 rounded-lg p-4">
                <div class="flex justify-between items-center mb-2">
                    <h3 class="font-medium text-gray-900 dark:text-white">{{ org.legalName }}</h3>
                    <span class="text-xs bg-gray-100 dark:bg-gray-700 px-2 py-1 rounded text-gray-600 dark:text-gray-300">{{ org.role }}</span>
                </div>
                <div class="mb-2">
                    <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Config Overrides (JSON)</label>
                    <textarea v-model="org.configOverridesJson" rows="2"
                              class="w-full px-3 py-2 rounded border border-gray-300 dark:border-gray-600 bg-gray-50 dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-1 focus:ring-blue-500 outline-none transition-all font-mono text-xs"></textarea>
                </div>
                <div class="flex justify-end">
                    <button @click="saveOrgAccess(org)"
                            class="px-3 py-1 bg-gray-600 hover:bg-gray-700 text-white text-xs rounded transition-colors">
                        Update Config
                    </button>
                </div>
            </div>
        </div>
        <div v-else class="text-gray-500 dark:text-gray-400 italic">No organizations assigned.</div>
    </div>

    <!-- Site Access Section -->
    <div v-if="!isNew && organizations.length > 0" class="mt-6 bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 border border-gray-100 dark:border-gray-700">
        <h2 class="text-lg font-semibold text-gray-800 dark:text-white mb-4">Site Access</h2>
        
        <!-- Add Site Access -->
        <div class="flex gap-4 mb-6 items-end">
            <div class="flex-1">
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Select Site</label>
                <select v-model="selectedSiteId" class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white">
                    <option value="" disabled>Select a site...</option>
                    <optgroup v-for="org in availableSitesByOrg" :key="org.orgId" :label="org.legalName">
                        <option v-for="site in org.sites" :key="site.siteId" :value="site.siteId">
                            {{ site.siteType }} - {{ site.address?.city || 'Unknown City' }}
                        </option>
                    </optgroup>
                </select>
            </div>
            <div class="w-48">
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Role</label>
                <select v-model="selectedSiteRole" class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white">
                    <option value="SITE_STAFF">Staff</option>
                    <option value="SITE_MANAGER">Manager</option>
                </select>
            </div>
            <button @click="assignSite" :disabled="!selectedSiteId"
                    class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors shadow-sm disabled:opacity-50 disabled:cursor-not-allowed h-10">
                Assign
            </button>
        </div>

        <!-- List Assigned Sites -->
        <div v-if="assignedSites.length > 0" class="space-y-2">
            <div v-for="access in assignedSites" :key="access.id" 
                 class="flex justify-between items-center p-3 bg-gray-50 dark:bg-gray-700 rounded-lg border border-gray-200 dark:border-gray-600">
                <div>
                    <div class="font-medium text-gray-900 dark:text-white">{{ getSiteName(access.siteId) }}</div>
                    <div class="text-xs text-gray-500 dark:text-gray-400">{{ access.role }}</div>
                </div>
                <button @click="removeSiteAccess(access.id)" class="text-red-500 hover:text-red-700 text-sm">Remove</button>
            </div>
        </div>
        <div v-else class="text-gray-500 dark:text-gray-400 italic">No sites assigned.</div>
    </div>

    <!-- Customer Profile Section -->
    <div v-if="showCustomerSection" class="mt-6 bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 border border-gray-100 dark:border-gray-700">
        <h2 class="text-lg font-semibold text-gray-800 dark:text-white mb-4">Customer Profile</h2>
        <form @submit.prevent="saveCustomerProfile">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Name</label>
                    <input v-model="customerProfile.name" type="text"
                           class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all" />
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Contact Info</label>
                    <input v-model="customerProfile.contactInfo" type="text"
                           class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all" />
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Legacy ID</label>
                    <input v-model="customerProfile.legacyId" type="text"
                           class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all" />
                </div>
                <div class="md:col-span-2">
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Configuration (JSON Attributes)</label>
                    <textarea v-model="customerProfile.attributes" rows="4"
                              class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all font-mono text-sm"
                              placeholder='{"openingHours": "9-17", "priority": "high"}'></textarea>
                </div>
            </div>
            <div class="mt-6 flex justify-end">
                <button type="submit"
                        class="px-6 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg transition-colors shadow-sm">
                    Save Customer Profile
                </button>
            </div>
        </form>

        <!-- Address Management -->
        <div class="mt-8 border-t border-gray-100 dark:border-gray-700 pt-6">
            <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-4">Address Database</h3>
            
            <!-- Address List -->
            <div v-if="customerProfile.addresses && customerProfile.addresses.length > 0" class="space-y-4 mb-6">
                <div v-for="addr in customerProfile.addresses" :key="addr.id" 
                     class="flex justify-between items-start p-4 bg-gray-50 dark:bg-gray-700 rounded-lg border border-gray-200 dark:border-gray-600">
                    <div>
                        <div class="font-medium text-gray-900 dark:text-white">{{ addr.street }} {{ addr.houseNumber }}</div>
                        <div class="text-sm text-gray-600 dark:text-gray-300">{{ addr.postalCode }} {{ addr.city }}, {{ addr.country }}</div>
                        <div class="text-xs text-gray-500 mt-1 uppercase tracking-wider">{{ addr.type || 'MAIN' }}</div>
                    </div>
                    <button @click="deleteAddress(addr.id)" class="text-red-500 hover:text-red-700 text-sm">Delete</button>
                </div>
            </div>
            <div v-else class="text-gray-500 dark:text-gray-400 mb-6 italic">No addresses configured.</div>

            <!-- Add Address Form -->
            <form @submit.prevent="addAddress" class="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg border border-gray-200 dark:border-gray-600">
                <h4 class="text-sm font-medium text-gray-800 dark:text-white mb-3">Add New Address</h4>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <input v-model="newAddress.street" placeholder="Street" required class="px-3 py-2 rounded border dark:bg-gray-600 dark:border-gray-500 dark:text-white" />
                    <input v-model="newAddress.houseNumber" placeholder="House No." class="px-3 py-2 rounded border dark:bg-gray-600 dark:border-gray-500 dark:text-white" />
                    <input v-model="newAddress.postalCode" placeholder="Postal Code" class="px-3 py-2 rounded border dark:bg-gray-600 dark:border-gray-500 dark:text-white" />
                    <input v-model="newAddress.city" placeholder="City" required class="px-3 py-2 rounded border dark:bg-gray-600 dark:border-gray-500 dark:text-white" />
                    <input v-model="newAddress.country" placeholder="Country" required class="px-3 py-2 rounded border dark:bg-gray-600 dark:border-gray-500 dark:text-white" />
                    <select v-model="newAddress.type" class="px-3 py-2 rounded border dark:bg-gray-600 dark:border-gray-500 dark:text-white">
                        <option value="MAIN">Main</option>
                        <option value="DELIVERY">Delivery</option>
                        <option value="INVOICE">Invoice</option>
                        <option value="PICKUP">Pickup</option>
                    </select>
                </div>
                <div class="mt-4 flex justify-end">
                    <button type="submit" class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded text-sm">Add Address</button>
                </div>
            </form>
        </div>
    </div>




    <!-- Harmonogram Section -->
    <CustomerHarmonogram v-if="showCustomerSection" :userId="route.params.userId" />

    <!-- Driver Profile Section -->
    <div v-if="showDriverSection" class="mt-6 bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 border border-gray-100 dark:border-gray-700">
        <h2 class="text-lg font-semibold text-gray-800 dark:text-white mb-4">Driver Profile</h2>
        <form @submit.prevent="saveDriverProfile">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Department</label>
                    <input v-model="driverProfile.department" type="text"
                           class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all" />
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Job Title</label>
                    <input v-model="driverProfile.jobTitle" type="text"
                           class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all" />
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Legacy ID</label>
                    <input v-model="driverProfile.legacyId" type="text"
                           class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all" />
                </div>
                <div class="md:col-span-2">
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Configuration (JSON Attributes)</label>
                    <textarea v-model="driverProfile.attributes" rows="4"
                              class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all font-mono text-sm"
                              placeholder='{"licenseNumber": "B12345", "vehicleId": "VAN-01"}'></textarea>
                </div>
            </div>
            <div class="mt-6 flex justify-end">
                <button type="submit"
                        class="px-6 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg transition-colors shadow-sm">
                    Save Driver Profile
                </button>
            </div>
        </form>

        <!-- Magic Link Generation -->
        <div class="mt-8 border-t border-gray-100 dark:border-gray-700 pt-6">
            <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-4">Driver Access</h3>
            
            <div class="flex flex-col gap-4">
                <div class="flex items-center gap-4">
                    <button @click="generateMagicLink" class="px-4 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded-lg transition-colors shadow-sm">
                        Generate Magic Link
                    </button>
                    <div v-if="magicLink" class="flex-1 p-3 bg-gray-50 dark:bg-gray-700 rounded border border-gray-200 dark:border-gray-600 font-mono text-sm break-all">
                        {{ magicLink }}
                    </div>
                </div>

                <div v-if="magicLink" class="flex gap-4 mt-2">
                    <button @click="sendMagicLink('EMAIL')" class="flex items-center gap-2 px-4 py-2 bg-blue-100 text-blue-700 hover:bg-blue-200 rounded-lg transition-colors">
                        <span>📧</span> Send via Email ({{ configStore.config.communication.smtpHost }})
                    </button>
                    <button @click="sendMagicLink('SMS')" class="flex items-center gap-2 px-4 py-2 bg-green-100 text-green-700 hover:bg-green-200 rounded-lg transition-colors">
                        <span>📱</span> Send via SMS ({{ configStore.config.communication.smsProvider }})
                    </button>
                </div>
            </div>

            <p class="text-sm text-gray-500 dark:text-gray-400 mt-2">
                Send this link to the driver via SMS or Email. It allows one-time login.
            </p>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { iamApi as api } from '@/api/axios';
import { useConfigStore } from '@/stores/configStore';
import CustomerHarmonogram from './CustomerHarmonogram.vue';

const configStore = useConfigStore();

const route = useRoute();
const router = useRouter();
const isNew = computed(() => route.params.userId === 'create' || route.name === 'UserCreate');

const availableRoles = ['ROLE_ADMIN', 'ROLE_DRIVER', 'ROLE_CUSTOMER', 'ROLE_SUPER_USER', 'ROLE_OPERATOR'];

const form = ref({
  username: '',
  email: '',
  fullName: '',
  avatarUrl: '',
  bio: '',
  legacyId: '',
  enabled: true,
  roles: []
});

const preferencesJson = ref('');
const organizations = ref([]);

const customerProfile = ref({
  name: '',
  contactInfo: '',
  legacyId: '',
  attributes: '',
  addresses: []
});
const hasCustomerProfile = ref(false);
const driverProfile = ref({
  department: '',
  jobTitle: '',
  legacyId: '',
  attributes: ''
});
const hasDriverProfile = ref(false);
const showDriverSection = computed(() => form.value.roles.includes('ROLE_DRIVER') && !isNew.value);
const showCustomerSection = computed(() => form.value.roles.includes('ROLE_CUSTOMER') && !isNew.value);
const magicLink = ref('');
const newPassword = ref('');
const newAddress = ref({ street: '', houseNumber: '', postalCode: '', city: '', country: '', type: 'MAIN' });

// Site Access State
const availableSitesByOrg = ref([]);
const assignedSites = ref([]);
const selectedSiteId = ref('');
const selectedSiteRole = ref('SITE_STAFF');

// Org Linking State
const allOrganizations = ref([]);
const selectedOrgId = ref('');
const selectedOrgRole = ref('customer');

const loadAllOrganizations = async () => {
    try {
        const response = await api.get('/api/organizations');
        allOrganizations.value = response.data;
    } catch (error) {
        console.error('Failed to load organizations:', error);
    }
};

const addOrgAccess = async () => {
    try {
        await api.post(`/api/users/${route.params.userId}/organizations/${selectedOrgId.value}`, {
            roleDefinitionId: selectedOrgRole.value
        });
        alert('Organization access added');
        selectedOrgId.value = '';
        loadUser(); // Reload user to see new org
    } catch (error) {
        console.error('Failed to add org access:', error);
        alert('Failed to add org access: ' + (error.response?.data?.message || error.message));
    }
};

const loadCustomerProfile = async () => {
    try {
        const response = await api.get(`/api/customers/users/${route.params.userId}`);
        customerProfile.value = response.data;
        hasCustomerProfile.value = true;
    } catch (error) {
        // 404 is expected if profile doesn't exist
        hasCustomerProfile.value = false;
    }
};

const loadDriverProfile = async () => {
    try {
        const response = await api.get(`/api/employees/users/${route.params.userId}`);
        driverProfile.value = response.data;
        hasDriverProfile.value = true;
    } catch (error) {
        hasDriverProfile.value = false;
    }
};

const loadUser = async () => {
  if (isNew.value) return;
  try {
    const response = await api.get(`/api/users/${route.params.userId}`);
    const user = response.data;
    form.value = {
      username: user.username,
      email: user.email,
      fullName: user.fullName || '',
      avatarUrl: user.avatarUrl || '',
      bio: user.bio || '',
      legacyId: user.legacyId || '',
      enabled: user.enabled,
      roles: user.roles || []
    };
    
    // Load preferences
    if (user.preferences) {
        preferencesJson.value = JSON.stringify(user.preferences, null, 2);
    }

    // Load organizations (assuming available in user response or separate endpoint)
    // For now, let's assume user.organizations is populated if available, or we fetch it
    if (user.organizations) {
        organizations.value = user.organizations.map(org => ({
            ...org,
            configOverridesJson: org.configOverrides ? JSON.stringify(org.configOverrides, null, 2) : ''
        }));
    }
    
    if (form.value.roles.includes('ROLE_CUSTOMER')) {
        loadCustomerProfile();
    }
    if (form.value.roles.includes('ROLE_DRIVER')) {
        loadDriverProfile();
    }
    
    // Load available sites and assigned sites
    if (organizations.value.length > 0) {
        loadSiteData();
    }
    
    loadAllOrganizations(); // Load all orgs for dropdown
  } catch (error) {
    console.error('Failed to load user:', error);
    alert('Failed to load user details');
  }
};

const loadSiteData = async () => {
    try {
        // Fetch sites for all assigned organizations
        const sitesPromises = organizations.value.map(async org => {
            const res = await api.get(`/api/organizations/${org.orgId}/sites`);
            return { orgId: org.orgId, legalName: org.legalName, sites: res.data };
        });
        availableSitesByOrg.value = await Promise.all(sitesPromises);

        // Fetch user's assigned sites (assuming endpoint exists)
        // For now, we might need to add this endpoint to UserSiteAccessController
        // Let's assume GET /api/users/{userId}/sites exists
        // If not, we need to create it. I'll add a placeholder for now.
        // const assignedRes = await api.get(`/api/users/${route.params.userId}/sites`);
        // assignedSites.value = assignedRes.data;
        assignedSites.value = []; // Placeholder until endpoint is ready
    } catch (error) {
        console.error('Failed to load site data:', error);
    }
};

const getSiteName = (siteId) => {
    for (const org of availableSitesByOrg.value) {
        const site = org.sites.find(s => s.siteId === siteId);
        if (site) return `${site.siteType} - ${site.address?.city || 'Unknown'}`;
    }
    return siteId;
};

const assignSite = async () => {
    try {
        await api.post(`/api/users/${route.params.userId}/sites`, {
            siteId: selectedSiteId.value,
            role: selectedSiteRole.value
        });
        alert('Site assigned successfully');
        loadSiteData(); // Reload
        selectedSiteId.value = '';
    } catch (error) {
        console.error('Failed to assign site:', error);
        alert('Failed to assign site');
    }
};

const removeSiteAccess = async (accessId) => {
    if (!confirm('Remove site access?')) return;
    try {
        await api.delete(`/api/users/${route.params.userId}/sites/${accessId}`);
        loadSiteData();
    } catch (error) {
        console.error('Failed to remove site access:', error);
        alert('Failed to remove site access');
    }
};

const saveUser = async () => {
  try {
    if (isNew.value) {
        await api.post('/api/users/initiate-registration', {
            email: form.value.email,
            roles: form.value.roles,
            fullName: form.value.fullName,
            avatarUrl: form.value.avatarUrl,
            bio: form.value.bio,
            preferences: preferencesJson.value // Initial preferences
        });
        alert('Registration initiated. User will receive an email (simulated).');
    } else {
      await api.put(`/api/users/${route.params.userId}`, {
        email: form.value.email,
        fullName: form.value.fullName,
        avatarUrl: form.value.avatarUrl,
        bio: form.value.bio,
        legacyId: form.value.legacyId,
        enabled: form.value.enabled,
        roles: form.value.roles,
        preferences: preferencesJson.value
      });
      alert('User updated successfully');
    }
    router.push('/internal/users');
  } catch (error) {
    console.error('Failed to save user:', error);
    alert('Failed to save user');
  }
};

const savePreferences = async () => {
    try {
        await api.put(`/api/users/${route.params.userId}`, {
            preferences: preferencesJson.value
        });
        alert('Preferences saved');
    } catch (error) {
        console.error('Failed to save preferences:', error);
        alert('Failed to save preferences');
    }
};

const saveOrgAccess = async (org) => {
    try {
        await api.put(`/api/users/${route.params.userId}/organizations/${org.orgId}`, {
            configOverrides: org.configOverridesJson
        });
        alert(`Configuration for ${org.legalName} updated`);
    } catch (error) {
        console.error('Failed to update org config:', error);
        alert('Failed to update org config');
    }
};

const resetPassword = async () => {
    if (!newPassword.value) return;
    try {
        await api.put(`/api/users/${route.params.userId}/reset-password`, {
            password: newPassword.value
        });
        alert('Password reset successfully');
        newPassword.value = '';
    } catch (error) {
        console.error('Failed to reset password:', error);
        alert('Failed to reset password');
    }
};

const saveCustomerProfile = async () => {
    try {
        if (hasCustomerProfile.value) {
            await api.put(`/api/customers/users/${route.params.userId}`, customerProfile.value);
        } else {
            await api.post(`/api/customers/users/${route.params.userId}`, customerProfile.value);
            hasCustomerProfile.value = true;
        }
        alert('Customer profile saved');
        loadCustomerProfile(); // Reload to get updated data
    } catch (error) {
        console.error('Failed to save customer profile:', error);
        alert('Failed to save customer profile');
    }
};

const saveDriverProfile = async () => {
    try {
        if (hasDriverProfile.value) {
            await api.put(`/api/employees/users/${route.params.userId}`, driverProfile.value);
        } else {
            await api.post(`/api/employees/users/${route.params.userId}`, driverProfile.value);
            hasDriverProfile.value = true;
        }
        alert('Driver profile saved');
        loadDriverProfile();
    } catch (error) {
        console.error('Failed to save driver profile:', error);
        alert('Failed to save driver profile');
    }
};

const generateMagicLink = async () => {
    try {
        const response = await api.post(`/api/auth/magic-link/generate`, null, {
            params: { email: form.value.email }
        });
        magicLink.value = `${window.location.origin}/login?token=${response.data.token}`;
    } catch (error) {
        console.error('Failed to generate magic link:', error);
        alert('Failed to generate magic link');
    }
};

const addAddress = async () => {
    try {
        await api.post(`/api/customers/users/${route.params.userId}/addresses`, newAddress.value);
        alert('Address added successfully');
        newAddress.value = { street: '', houseNumber: '', postalCode: '', city: '', country: '', type: 'MAIN' };
        loadCustomerProfile();
    } catch (error) {
        console.error('Failed to add address:', error);
        alert('Failed to add address');
    }
};

const deleteAddress = async (addressId) => {
    if (!confirm('Are you sure you want to delete this address?')) return;
    try {
        await api.delete(`/api/customers/users/${route.params.userId}/addresses/${addressId}`);
        alert('Address deleted successfully');
        loadCustomerProfile();
    } catch (error) {
        console.error('Failed to delete address:', error);
        alert('Failed to delete address');
    }
};

onMounted(() => {
  loadUser();
});
</script>


