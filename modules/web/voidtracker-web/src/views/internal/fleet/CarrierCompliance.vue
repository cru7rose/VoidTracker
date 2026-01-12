<template>
  <div class="p-6">
    <div class="mb-6 flex justify-between items-center">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Carrier Compliance</h1>
        <p class="text-gray-500 dark:text-gray-400">Manage 3rd party carrier documentation and legal compliance.</p>
      </div>
      <button @click="showAddModal = true" class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg shadow-sm flex items-center gap-2">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" /></svg>
        Onboard Carrier
      </button>
    </div>

    <!-- Compliance Dashboard -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
      <div class="bg-white dark:bg-gray-800 p-4 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700">
        <div class="text-sm text-gray-500 mb-1">Total Carriers</div>
        <div class="text-2xl font-bold text-gray-900 dark:text-white">42</div>
      </div>
      <div class="bg-white dark:bg-gray-800 p-4 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700">
        <div class="text-sm text-green-500 mb-1">Fully Compliant</div>
        <div class="text-2xl font-bold text-green-600">38</div>
        <div class="text-xs text-gray-500 mt-1">Ready for dispatch</div>
      </div>
      <div class="bg-white dark:bg-gray-800 p-4 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700">
        <div class="text-sm text-orange-500 mb-1">Documents Expiring (30d)</div>
        <div class="text-2xl font-bold text-orange-600">3</div>
        <div class="text-xs text-gray-500 mt-1">Action required</div>
      </div>
      <div class="bg-white dark:bg-gray-800 p-4 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700">
        <div class="text-sm text-red-500 mb-1">Blocked / Non-Compliant</div>
        <div class="text-2xl font-bold text-red-600">1</div>
        <div class="text-xs text-gray-500 mt-1">Missing critical docs</div>
      </div>
    </div>

    <!-- Carrier List -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
        <thead class="bg-gray-50 dark:bg-gray-900">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Carrier / Driver</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">OCP Insurance</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">License</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Contract</th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200 dark:divide-gray-700">
          <tr v-for="carrier in carriers" :key="carrier.id" class="hover:bg-gray-50 dark:hover:bg-gray-700/50">
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center">
                <div class="h-10 w-10 rounded-full bg-gray-200 dark:bg-gray-600 flex items-center justify-center text-sm font-bold text-gray-600 dark:text-gray-300">
                  {{ carrier.initials }}
                </div>
                <div class="ml-4">
                  <div class="text-sm font-medium text-gray-900 dark:text-white">{{ carrier.companyName }}</div>
                  <div class="text-xs text-gray-500">VAT: {{ carrier.vat }} • {{ carrier.contactPerson }}</div>
                </div>
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span :class="['px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full', getStatusColor(carrier.status)]">
                {{ carrier.status }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">
              <div :class="getExpiryColor(carrier.insuranceExpiry)">{{ carrier.insuranceExpiry }}</div>
              <div class="text-xs text-gray-400">Policy: {{ carrier.insurancePolicy }}</div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">
              <div :class="getExpiryColor(carrier.licenseExpiry)">{{ carrier.licenseExpiry }}</div>
              <div class="text-xs text-gray-400">Cat: {{ carrier.licenseCategory }}</div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">
               <span v-if="carrier.contractSigned" class="flex items-center gap-1 text-green-600">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                Active
              </span>
              <span v-else class="text-red-500">Pending</span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
              <button class="text-blue-600 hover:text-blue-900 mr-3">Audit</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Onboarding Modal -->
    <div v-if="showAddModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 overflow-y-auto">
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-xl max-w-3xl w-full p-6 m-4">
        <h2 class="text-xl font-bold mb-6 text-gray-900 dark:text-white">Onboard New Carrier</h2>
        <form @submit.prevent="saveCarrier" class="space-y-6">
          
          <!-- Company Info -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="col-span-2">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Company Name</label>
              <input v-model="newCarrier.companyName" type="text" class="mt-1 block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">VAT Number</label>
              <input v-model="newCarrier.vat" type="text" class="mt-1 block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Contact Person</label>
              <input v-model="newCarrier.contactPerson" type="text" class="mt-1 block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm" />
            </div>
          </div>

          <!-- Documents -->
          <div class="border-t border-gray-200 dark:border-gray-700 pt-6">
            <h3 class="text-sm font-semibold text-gray-900 dark:text-white mb-4">Required Documents</h3>
            
            <!-- Insurance -->
            <div class="bg-gray-50 dark:bg-gray-900 p-4 rounded-lg mb-4">
              <div class="flex justify-between items-start mb-4">
                <div>
                  <h4 class="text-sm font-medium text-gray-900 dark:text-white">OCP Insurance (Carrier Liability)</h4>
                  <p class="text-xs text-gray-500">Upload valid policy document.</p>
                </div>
                <input type="file" class="text-sm text-gray-500" />
              </div>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-xs font-medium text-gray-500">Policy Number</label>
                  <input v-model="newCarrier.insurancePolicy" type="text" class="mt-1 block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm" />
                </div>
                <div>
                  <label class="block text-xs font-medium text-gray-500">Expiry Date</label>
                  <input v-model="newCarrier.insuranceExpiry" type="date" class="mt-1 block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm" />
                </div>
              </div>
            </div>

            <!-- License -->
            <div class="bg-gray-50 dark:bg-gray-900 p-4 rounded-lg">
              <div class="flex justify-between items-start mb-4">
                <div>
                  <h4 class="text-sm font-medium text-gray-900 dark:text-white">Transport License</h4>
                  <p class="text-xs text-gray-500">Community license or driver's license.</p>
                </div>
                <input type="file" class="text-sm text-gray-500" />
              </div>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-xs font-medium text-gray-500">License Category</label>
                  <select v-model="newCarrier.licenseCategory" class="mt-1 block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm">
                    <option>C+E</option>
                    <option>C</option>
                    <option>B</option>
                  </select>
                </div>
                <div>
                  <label class="block text-xs font-medium text-gray-500">Expiry Date</label>
                  <input v-model="newCarrier.licenseExpiry" type="date" class="mt-1 block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm" />
                </div>
              </div>
            </div>
          </div>

          <div class="flex justify-end gap-3 mt-6 pt-6 border-t border-gray-200 dark:border-gray-700">
            <button type="button" @click="showAddModal = false" class="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg">Cancel</button>
            <button type="submit" class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg">Submit for Approval</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const showAddModal = ref(false);

const carriers = ref([
  { 
    id: 1, 
    companyName: 'Trans-Pol Sp. z o.o.', 
    initials: 'TP', 
    vat: 'PL5252525252', 
    contactPerson: 'Jan Kowalski',
    status: 'Compliant', 
    insuranceExpiry: '2024-12-01', 
    insurancePolicy: 'PZU/2023/999',
    licenseExpiry: '2025-05-20',
    licenseCategory: 'C+E',
    contractSigned: true
  },
  { 
    id: 2, 
    companyName: 'FastLogistics Ltd', 
    initials: 'FL', 
    vat: 'PL999888777', 
    contactPerson: 'Adam Nowak',
    status: 'At Risk', 
    insuranceExpiry: '2023-11-15', 
    insurancePolicy: 'ALLIANZ/X/12',
    licenseExpiry: '2024-08-10',
    licenseCategory: 'B',
    contractSigned: true
  },
]);

const newCarrier = ref({
  companyName: '', vat: '', contactPerson: '',
  insurancePolicy: '', insuranceExpiry: '',
  licenseCategory: 'C+E', licenseExpiry: ''
});

const getStatusColor = (status) => {
  switch (status) {
    case 'Compliant': return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300';
    case 'At Risk': return 'bg-orange-100 text-orange-800 dark:bg-orange-900/30 dark:text-orange-300';
    case 'Blocked': return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300';
    default: return 'bg-gray-100 text-gray-800';
  }
};

const getExpiryColor = (dateStr) => {
  const date = new Date(dateStr);
  const now = new Date();
  const days = (date - now) / (1000 * 60 * 60 * 24);
  
  if (days < 0) return 'text-red-600 font-bold';
  if (days < 30) return 'text-orange-600 font-bold';
  return 'text-gray-900 dark:text-white';
};

const saveCarrier = () => {
  carriers.value.push({
    id: Date.now(),
    initials: newCarrier.value.companyName.substring(0, 2).toUpperCase(),
    status: 'Pending Review',
    contractSigned: false,
    ...newCarrier.value
  });
  showAddModal.value = false;
  newCarrier.value = { companyName: '', vat: '', contactPerson: '', insurancePolicy: '', insuranceExpiry: '', licenseCategory: 'C+E', licenseExpiry: '' };
};
</script>
