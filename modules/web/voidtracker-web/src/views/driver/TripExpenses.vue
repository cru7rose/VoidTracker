<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <div class="max-w-3xl mx-auto">
      <div class="mb-8 flex justify-between items-center">
        <div>
          <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Trip Expenses & Reimbursements</h1>
          <p class="text-gray-500 dark:text-gray-400">Submit and track operational expenses for approval.</p>
        </div>
        <button @click="showAddModal = true" class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg shadow-sm flex items-center gap-2">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" /></svg>
          New Expense
        </button>
      </div>

      <!-- Expense Stats -->
      <div class="grid grid-cols-3 gap-4 mb-6">
        <div class="bg-white dark:bg-gray-800 p-4 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700">
          <div class="text-xs text-gray-500 uppercase">Pending Approval</div>
          <div class="text-xl font-bold text-orange-600">350.00 PLN</div>
        </div>
        <div class="bg-white dark:bg-gray-800 p-4 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700">
          <div class="text-xs text-gray-500 uppercase">Approved (This Month)</div>
          <div class="text-xl font-bold text-green-600">1,240.50 PLN</div>
        </div>
        <div class="bg-white dark:bg-gray-800 p-4 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700">
          <div class="text-xs text-gray-500 uppercase">Rejected</div>
          <div class="text-xl font-bold text-red-600">0.00 PLN</div>
        </div>
      </div>

      <!-- Expense List -->
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden">
        <div class="p-4 border-b border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-900 flex justify-between items-center">
          <h3 class="font-semibold text-gray-900 dark:text-white">Expense History</h3>
          <button class="text-sm text-blue-600 hover:text-blue-800">Download Report</button>
        </div>
        <div class="divide-y divide-gray-200 dark:divide-gray-700">
          <div v-for="expense in expenses" :key="expense.id" class="p-4 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors cursor-pointer">
            <div class="flex justify-between items-start">
              <div class="flex items-start gap-4">
                <div :class="['p-3 rounded-lg', getTypeColor(expense.type)]">
                  <component :is="getTypeIcon(expense.type)" class="w-6 h-6" />
                </div>
                <div>
                  <div class="flex items-center gap-2">
                    <span class="font-medium text-gray-900 dark:text-white">{{ expense.type }}</span>
                    <span class="text-xs text-gray-400">• {{ expense.date }}</span>
                  </div>
                  <div class="text-sm text-gray-600 dark:text-gray-300">{{ expense.description }}</div>
                  <div class="text-xs text-gray-500 mt-1">Cost Center: {{ expense.costCenter }}</div>
                </div>
              </div>
              <div class="text-right">
                <div class="font-bold text-gray-900 dark:text-white">{{ expense.amount.toFixed(2) }} {{ expense.currency }}</div>
                <div class="text-xs text-gray-500 mb-1">VAT: {{ expense.vat.toFixed(2) }} {{ expense.currency }}</div>
                <span :class="['text-xs font-medium px-2 py-0.5 rounded-full', getStatusColor(expense.status)]">
                  {{ expense.status }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Add Modal -->
    <div v-if="showAddModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 overflow-y-auto">
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-xl max-w-lg w-full p-6 m-4">
        <h2 class="text-xl font-bold mb-6 text-gray-900 dark:text-white">Submit New Expense</h2>
        <form @submit.prevent="addExpense" class="space-y-6">
          
          <!-- Type & Date -->
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Expense Type</label>
              <select v-model="newExpense.type" class="mt-1 block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm">
                <option>Fuel</option>
                <option>Toll</option>
                <option>Parking</option>
                <option>Vehicle Repair</option>
                <option>Driver Accommodation</option>
                <option>Other</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Date</label>
              <input v-model="newExpense.date" type="date" required class="mt-1 block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm" />
            </div>
          </div>

          <!-- Amount & Currency -->
          <div class="grid grid-cols-3 gap-4">
            <div class="col-span-1">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Currency</label>
              <select v-model="newExpense.currency" class="mt-1 block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm">
                <option>PLN</option>
                <option>EUR</option>
                <option>USD</option>
              </select>
            </div>
            <div class="col-span-2">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Total Amount (Gross)</label>
              <input v-model="newExpense.amount" type="number" step="0.01" required class="mt-1 block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm" />
            </div>
          </div>

          <!-- Details -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Description / Location</label>
            <input v-model="newExpense.description" type="text" placeholder="e.g., Orlen Warsaw - Full Tank" class="mt-1 block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm" />
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Cost Center</label>
              <select v-model="newExpense.costCenter" class="mt-1 block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm">
                <option>General Operations</option>
                <option>Project Alpha</option>
                <option>Fleet Maintenance</option>
              </select>
            </div>
            <div v-if="newExpense.type === 'Fuel'">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Odometer (km)</label>
              <input v-model="newExpense.odometer" type="number" class="mt-1 block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm" />
            </div>
          </div>
          
          <!-- Receipt Upload -->
          <div class="border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-lg p-6 text-center cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors" @click="simulatePhoto">
            <div v-if="!newExpense.photo">
              <svg class="mx-auto h-12 w-12 text-gray-400" stroke="currentColor" fill="none" viewBox="0 0 48 48">
                <path d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
              <p class="mt-1 text-sm text-gray-600 dark:text-gray-400">Click to upload receipt</p>
            </div>
            <div v-else class="flex items-center justify-center gap-2 text-green-600">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
              <span class="font-medium">Receipt Attached</span>
            </div>
          </div>

          <div class="flex justify-end gap-3 mt-6 pt-6 border-t border-gray-200 dark:border-gray-700">
            <button type="button" @click="showAddModal = false" class="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg">Cancel</button>
            <button type="submit" class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg">Submit Expense</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { Truck, CreditCard, MapPin, Wrench, FileText, Home } from 'lucide-vue-next';

const showAddModal = ref(false);
const expenses = ref([
  { id: 1, type: 'Fuel', date: '2023-10-27', amount: 250.50, currency: 'PLN', vat: 46.84, description: 'Orlen Warsaw', costCenter: 'General Operations', status: 'Approved' },
  { id: 2, type: 'Toll', date: '2023-10-27', amount: 35.00, currency: 'PLN', vat: 0.00, description: 'A2 Highway Gate', costCenter: 'Project Alpha', status: 'Pending' },
  { id: 3, type: 'Parking', date: '2023-10-26', amount: 10.00, currency: 'EUR', vat: 1.87, description: 'Berlin Center', costCenter: 'General Operations', status: 'Approved' },
]);

const newExpense = ref({ 
  type: 'Fuel', amount: '', currency: 'PLN', date: '', 
  description: '', costCenter: 'General Operations', 
  odometer: '', photo: null 
});

const simulatePhoto = () => {
  newExpense.value.photo = 'mock_url';
};

const addExpense = () => {
  expenses.value.unshift({
    id: Date.now(),
    status: 'Pending',
    vat: newExpense.value.amount * 0.23, // Mock VAT calc
    ...newExpense.value
  });
  showAddModal.value = false;
  newExpense.value = { type: 'Fuel', amount: '', currency: 'PLN', date: '', description: '', costCenter: 'General Operations', odometer: '', photo: null };
};

const getTypeIcon = (type) => {
  switch (type) {
    case 'Fuel': return Truck;
    case 'Toll': return MapPin;
    case 'Parking': return CreditCard;
    case 'Vehicle Repair': return Wrench;
    case 'Driver Accommodation': return Home;
    default: return FileText;
  }
};

const getTypeColor = (type) => {
  switch (type) {
    case 'Fuel': return 'bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400';
    case 'Toll': return 'bg-purple-100 text-purple-600 dark:bg-purple-900/30 dark:text-purple-400';
    case 'Parking': return 'bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400';
    case 'Vehicle Repair': return 'bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400';
    default: return 'bg-gray-100 text-gray-600';
  }
};

const getStatusColor = (status) => {
  switch (status) {
    case 'Approved': return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300';
    case 'Pending': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-300';
    case 'Rejected': return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300';
    default: return 'bg-gray-100 text-gray-800';
  }
};
</script>
