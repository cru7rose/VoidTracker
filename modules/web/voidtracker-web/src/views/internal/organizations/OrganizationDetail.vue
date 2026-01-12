<template>
  <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 border border-gray-100 dark:border-gray-700">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-xl font-bold text-gray-800 dark:text-white">
        {{ isNew ? 'Create Organization' : (data?.legalName || 'Organization Details') }}
      </h2>
      <div v-if="!isNew" class="flex gap-2">
         <span :class="['px-2 py-1 text-xs font-medium rounded-full', data?.active ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800']">
            {{ data?.active ? 'Active' : 'Inactive' }}
         </span>
         <span class="px-2 py-1 text-xs font-medium rounded-full bg-blue-100 text-blue-800">
            {{ data?.type }}
         </span>
      </div>
    </div>
      <!-- Tabs -->
      <div class="flex border-b border-gray-200 dark:border-gray-700 mb-6">
        <button @click="activeTab = 'details'"
                :class="['px-4 py-2 font-medium text-sm transition-colors border-b-2', activeTab === 'details' ? 'border-blue-600 text-blue-600 dark:text-blue-400' : 'border-transparent text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200']">
          Details
        </button>
        <button @click="activeTab = 'billing'"
                :class="['px-4 py-2 font-medium text-sm transition-colors border-b-2', activeTab === 'billing' ? 'border-blue-600 text-blue-600 dark:text-blue-400' : 'border-transparent text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200']">
          Billing
        </button>
        <button v-if="!isNew" @click="activeTab = 'sites'"
                :class="['px-4 py-2 font-medium text-sm transition-colors border-b-2', activeTab === 'sites' ? 'border-blue-600 text-blue-600 dark:text-blue-400' : 'border-transparent text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200']">
          Sites
        </button>
      </div>

      <!-- Details Tab -->
      <form v-if="activeTab === 'details'" @submit.prevent="saveOrganization">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- Legal Name -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Legal Name</label>
            <input v-model="form.legalName" type="text"
                   class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                   required />
          </div>

          <!-- Type -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Type</label>
            <select v-model="form.type"
                    class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all">
              <option value="INTERNAL">Internal</option>
              <option value="CUSTOMER">Customer</option>
              <option value="PARTNER">Partner</option>
            </select>
          </div>

          <!-- Status -->
          <div class="flex items-center h-full pt-6">
            <label class="flex items-center cursor-pointer">
              <input v-model="form.active" type="checkbox" class="sr-only peer">
              <div class="relative w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 dark:peer-focus:ring-blue-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-blue-600"></div>
              <span class="ms-3 text-sm font-medium text-gray-700 dark:text-gray-300">Active</span>
            </label>
          </div>

          <!-- Configuration -->
          <div class="md:col-span-2">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Configuration (JSON)</label>
            <textarea v-model="configJson" rows="6"
                      class="w-full px-4 py-2 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all font-mono text-sm"
                      placeholder='{"domain": "example.com", "features": ["sso", "audit"]}'></textarea>
          </div>
        </div>

        <div class="mt-8 flex justify-end gap-4">
          <button type="button" @click="router.back()"
                  class="px-4 py-2 text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors dark:bg-gray-700 dark:text-gray-300 dark:hover:bg-gray-600">
            Cancel
          </button>
          <button type="submit"
                  class="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors shadow-sm">
            {{ isNew ? 'Create Organization' : 'Save Changes' }}
          </button>
        </div>
      </form>

      <!-- Billing Tab -->
      <div v-if="activeTab === 'billing'" class="space-y-6">
        <div class="bg-gray-50 dark:bg-gray-700/50 p-4 rounded-lg border border-gray-200 dark:border-gray-600">
            <h3 class="text-sm font-bold text-gray-900 dark:text-white mb-4 uppercase tracking-wider">Configuration</h3>
            <ConfigBuilder v-model="billingConfigModel" :schema="billingSchema" />
            <div class="mt-4 flex justify-end">
                <button @click="saveBillingConfig" class="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700">
                    Update Configuration
                </button>
            </div>
        </div>
        
        <BillingSettlements :orgId="id" />
      </div>

      <!-- Sites Tab (Modified to show Crew if a Site is selected, or list sites if Org is selected) -->
      <!-- Actually, the Dashboard handles Site selection. If we are viewing an Org, 'Sites' tab lists sites. -->
      <div v-if="activeTab === 'sites'">
        <div class="flex justify-between items-center mb-4">
            <h3 class="text-lg font-medium text-gray-900 dark:text-white">Sites</h3>
            <button @click="showCreateSiteModal = true" class="px-3 py-1 bg-green-600 hover:bg-green-700 text-white rounded-lg text-sm">
                + Add Site
            </button>
        </div>
        
        <div class="overflow-x-auto">
            <table class="w-full text-left">
                <thead>
                    <tr class="bg-gray-50 dark:bg-gray-700">
                        <th class="px-4 py-2 text-xs font-semibold text-gray-500 dark:text-gray-300 uppercase">Type</th>
                        <th class="px-4 py-2 text-xs font-semibold text-gray-500 dark:text-gray-300 uppercase">Address</th>
                        <th class="px-4 py-2 text-xs font-semibold text-gray-500 dark:text-gray-300 uppercase">Billing Override</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-gray-100 dark:divide-gray-700">
                    <tr v-for="site in sites" :key="site.siteId">
                        <td class="px-4 py-3 text-sm text-gray-900 dark:text-white">{{ site.siteType }}</td>
                        <td class="px-4 py-3 text-sm text-gray-500 dark:text-gray-400">
                            <pre class="text-xs">{{ JSON.stringify(site.address, null, 2) }}</pre>
                        </td>
                        <td class="px-4 py-3 text-sm">
                            <span v-if="site.billingConfig" class="text-green-600 dark:text-green-400 text-xs font-medium">Yes</span>
                            <span v-else class="text-gray-400 text-xs">No</span>
                        </td>
                    </tr>
                    <tr v-if="sites.length === 0">
                        <td colspan="3" class="px-4 py-6 text-center text-gray-500">No sites found.</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- Simple Create Site Modal (Inline for now) -->
        <div v-if="showCreateSiteModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div class="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-md">
                <h3 class="text-lg font-bold mb-4 text-gray-900 dark:text-white">Add New Site</h3>
                <div class="mb-4">
                    <label class="block text-sm font-medium mb-1 dark:text-gray-300">Type</label>
                    <select v-model="newSite.siteType" class="w-full p-2 border rounded dark:bg-gray-700 dark:border-gray-600 dark:text-white">
                        <option value="WAREHOUSE">Warehouse</option>
                        <option value="STORE">Store</option>
                        <option value="HEADQUARTERS">Headquarters</option>
                        <option value="DROP_OFF_POINT">Drop-off Point</option>
                    </select>
                </div>
                <div class="mb-4">
                    <label class="block text-sm font-medium mb-1 dark:text-gray-300">Address (JSON)</label>
                    <textarea v-model="newSite.addressJson" rows="3" class="w-full p-2 border rounded dark:bg-gray-700 dark:border-gray-600 dark:text-white font-mono text-xs"></textarea>
                </div>
                <div class="flex justify-end gap-2">
                    <button @click="showCreateSiteModal = false" class="px-3 py-1 text-gray-600 hover:bg-gray-100 rounded">Cancel</button>
                    <button @click="createSite" class="px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700">Create</button>
                </div>
            </div>
        </div>
      </div>

  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import api from '@/api/axios';
import BillingSettlements from '@/components/organizations/BillingSettlements.vue';
import ConfigBuilder from '@/components/common/ConfigBuilder.vue';

const props = defineProps({
    id: String,
    data: Object,
    parent: Object
});

const emit = defineEmits(['refresh']);

const route = useRoute();
const router = useRouter();
const isNew = computed(() => props.id === 'create');

const form = ref({
  legalName: '',
  type: 'CUSTOMER',
  active: true
});

const configJson = ref('');
const activeTab = ref('details');
const sites = ref([]);
const showCreateSiteModal = ref(false);
const newSite = ref({ siteType: 'STORE', addressJson: '{}' });

const billingConfigModel = ref({});
const billingSchema = {
    vatNumber: { label: 'VAT Number', type: 'text', placeholder: 'DK12345678' },
    paymentTerms: { 
        label: 'Payment Terms', 
        type: 'select', 
        options: [
            { label: 'Net 15', value: 'Net15' },
            { label: 'Net 30', value: 'Net30' },
            { label: 'Net 60', value: 'Net60' }
        ]
    },
    billingEmail: { label: 'Billing Email', type: 'text', placeholder: 'billing@company.com' },
    enableAutoInvoicing: { label: 'Enable Auto-Invoicing', type: 'boolean', description: 'Automatically generate invoices on the 1st of the month.' }
};

// Watch for ID changes to reload data
watch(() => props.id, (newId) => {
    if (newId) loadOrganization();
}, { immediate: true });

const loadOrganization = async () => {
  if (isNew.value) {
      form.value = { legalName: '', type: 'CUSTOMER', active: true };
      configJson.value = '';
      sites.value = [];
      return;
  }
  
  // If data is passed via props, use it, otherwise fetch
  if (props.data && props.data.legalName) {
      const org = props.data;
      form.value = {
        legalName: org.legalName,
        type: org.type || 'CUSTOMER',
        active: org.active !== false
      };
      if (org.configuration) {
          configJson.value = JSON.stringify(org.configuration, null, 2);
      }
      if (org.billingConfig) {
          billingConfigModel.value = org.billingConfig;
      } else {
          billingConfigModel.value = {};
      }
      // Load sites
      loadSites();
  } else {
      // Fetch from API
      try {
        const response = await api.get(`/api/organizations/${props.id}`);
        const org = response.data;
        form.value = {
          legalName: org.legalName,
          type: org.type || 'CUSTOMER',
          active: org.active !== false
        };
        if (org.configuration) {
            configJson.value = JSON.stringify(org.configuration, null, 2);
        }
        if (org.billingConfig) {
            billingConfigModel.value = org.billingConfig;
        } else {
            billingConfigModel.value = {};
        }
        loadSites();
      } catch (error) {
        console.error('Failed to load organization:', error);
      }
  }
};

const loadSites = async () => {
    try {
        const sitesResponse = await api.get(`/api/organizations/${props.id}/sites`);
        sites.value = sitesResponse.data;
    } catch (error) {
        console.error('Failed to load sites:', error);
    }
};

const saveOrganization = async () => {
  try {
    const payload = {
        ...form.value,
        configuration: configJson.value ? JSON.parse(configJson.value) : null
    };

    if (isNew.value) {
        await api.post('/api/organizations', payload);
        alert('Organization created successfully');
        emit('refresh');
    } else {
        await api.put(`/api/organizations/${props.id}`, payload);
        alert('Organization updated successfully');
        emit('refresh');
    }
  } catch (error) {
    console.error('Failed to save organization:', error);
    alert('Failed to save organization. Check JSON format.');
  }
};

const saveBillingConfig = async () => {
    try {
        const payload = {
            ...form.value,
            configuration: configJson.value ? JSON.parse(configJson.value) : null,
            billingConfig: billingConfigModel.value
        };
        await api.put(`/api/organizations/${props.id}`, payload);
        alert('Billing configuration updated successfully');
        emit('refresh');
    } catch (error) {
        console.error('Failed to save billing config:', error);
        alert('Failed to save configuration.');
    }
};

const createSite = async () => {
    try {
        const payload = {
            siteType: newSite.value.siteType,
            address: JSON.parse(newSite.value.addressJson)
        };
        await api.post(`/api/organizations/${props.id}/sites`, payload);
        showCreateSiteModal.value = false;
        newSite.value = { siteType: 'STORE', addressJson: '{}' };
        loadSites();
        emit('refresh'); // Refresh tree
    } catch (error) {
        console.error('Failed to create site:', error);
        alert('Failed to create site. Check JSON.');
    }
};
</script>
