<template>
  <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700 overflow-hidden">
    <div class="p-4 border-b border-gray-200 dark:border-gray-700 flex flex-col sm:flex-row justify-between items-center gap-4 bg-gray-50/50 dark:bg-gray-800/50">
      <div class="flex items-center gap-4">
        <h3 class="text-lg font-bold text-gray-900 dark:text-white flex items-center gap-2">
            <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path></svg>
            Settlements
        </h3>
        <div class="h-6 w-px bg-gray-300 dark:bg-gray-600"></div>
        <div class="flex items-center gap-2">
            <select class="text-sm border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:text-white">
                <option>All Statuses</option>
                <option>Pending</option>
                <option>Invoiced</option>
                <option>Paid</option>
            </select>
            <input type="date" class="text-sm border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:text-white" />
            <span class="text-gray-400">-</span>
            <input type="date" class="text-sm border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:text-white" />
        </div>
      </div>
      <div class="flex gap-2">
        <button class="px-3 py-1.5 text-xs font-medium text-gray-700 bg-white border border-gray-300 rounded-md shadow-sm hover:bg-gray-50 dark:bg-gray-700 dark:text-gray-200 dark:border-gray-600 dark:hover:bg-gray-600">
          Export CSV
        </button>
        <button @click="generateInvoice" class="px-3 py-1.5 text-xs font-medium text-white bg-blue-600 border border-transparent rounded-md shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
          Generate Invoice
        </button>
      </div>
    </div>

    <!-- Invoice Preview Modal -->
    <div v-if="showInvoiceModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-4xl max-h-[90vh] overflow-y-auto flex flex-col">
            <div class="p-4 border-b flex justify-between items-center sticky top-0 bg-white dark:bg-gray-800 z-10">
                <h3 class="text-lg font-bold">Invoice Preview</h3>
                <div class="flex gap-2">
                    <button @click="printInvoice" class="px-3 py-1 bg-gray-100 hover:bg-gray-200 rounded text-sm">🖨️ Print</button>
                    <button @click="showInvoiceModal = false" class="text-gray-500 hover:text-gray-700">✕</button>
                </div>
            </div>
            <div class="p-4 bg-gray-100 dark:bg-gray-900 flex-1">
                <InvoicePreview :invoice="mockInvoiceData" />
            </div>
        </div>
    </div>

    <div class="overflow-x-auto">
      <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
        <thead class="bg-gray-50 dark:bg-gray-800">
          <tr>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider cursor-pointer hover:text-gray-700 dark:hover:text-gray-200">Date ↕</th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Service / Item</th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Site</th>
            <th scope="col" class="px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Qty</th>
            <th scope="col" class="px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Unit Price</th>
            <th scope="col" class="px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider cursor-pointer hover:text-gray-700 dark:hover:text-gray-200">Total ↕</th>
            <th scope="col" class="px-6 py-3 text-center text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Status</th>
          </tr>
        </thead>
        <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
          <tr v-for="(item, index) in mockItems" :key="index" class="hover:bg-blue-50/50 dark:hover:bg-blue-900/20 transition-colors">
            <td class="px-6 py-2 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400 font-mono">{{ item.date }}</td>
            <td class="px-6 py-2 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-white">{{ item.service }}</td>
            <td class="px-6 py-2 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{{ item.site }}</td>
            <td class="px-6 py-2 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400 text-right font-mono">{{ item.qty }}</td>
            <td class="px-6 py-2 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400 text-right font-mono">{{ formatCurrency(item.unitPrice) }}</td>
            <td class="px-6 py-2 whitespace-nowrap text-sm font-bold text-gray-900 dark:text-white text-right font-mono">{{ formatCurrency(item.qty * item.unitPrice) }}</td>
            <td class="px-6 py-2 whitespace-nowrap text-center">
              <span :class="['inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium', 
                             item.status === 'Pending' ? 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/50 dark:text-yellow-300' : 'bg-green-100 text-green-800 dark:bg-green-900/50 dark:text-green-300']">
                {{ item.status }}
              </span>
            </td>
          </tr>
        </tbody>
        <tfoot class="bg-gray-50 dark:bg-gray-700 font-medium">
            <tr>
                <td colspan="5" class="px-6 py-3 text-right text-gray-900 dark:text-white">Total Pending:</td>
                <td class="px-6 py-3 text-right text-blue-600 dark:text-blue-400">{{ formatCurrency(totalPending) }}</td>
                <td></td>
            </tr>
        </tfoot>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import InvoicePreview from '@/views/internal/organizations/InvoicePreview.vue';

const props = defineProps({
    orgId: String
});

const showInvoiceModal = ref(false);
const mockInvoiceData = ref({});

const generateInvoice = () => {
    // In a real app, gather selected items
    mockInvoiceData.value = {
        number: 'INV-2023-001',
        date: new Date().toLocaleDateString(),
        dueDate: new Date(Date.now() + 30*24*60*60*1000).toLocaleDateString(),
        customerName: 'ACME Corp',
        customerAddress: 'Business Park 1, Warsaw',
        customerVat: 'PL5252525252',
        vatRate: 23,
        paymentTerms: 'Net 30',
        items: [
            { description: 'Delivery Service - Zone A', qty: 10, unitPrice: 150 },
            { description: 'Fuel Surcharge', qty: 1, unitPrice: 450 },
            { description: 'Express Handling', qty: 5, unitPrice: 75 }
        ]
    };
    showInvoiceModal.value = true;
};

const printInvoice = () => {
    window.print();
};

// Mock Data for now - in real app would fetch from API based on orgId
const mockItems = ref([
    { date: '2025-12-01', service: 'Laundry Pickup (Standard)', site: 'Hotel Grand', qty: 45, unitPrice: 2.50, status: 'Pending' },
    { date: '2025-12-01', service: 'Delivery Fee', site: 'Hotel Grand', qty: 1, unitPrice: 15.00, status: 'Pending' },
    { date: '2025-11-30', service: 'Dry Cleaning (Express)', site: 'City Center Inn', qty: 12, unitPrice: 8.00, status: 'Invoiced' },
    { date: '2025-11-29', service: 'Linen Supply', site: 'Resort Spa', qty: 200, unitPrice: 0.50, status: 'Invoiced' },
]);

const totalPending = computed(() => {
    return mockItems.value
        .filter(i => i.status === 'Pending')
        .reduce((sum, item) => sum + (item.qty * item.unitPrice), 0);
});

const formatCurrency = (val) => {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(val);
};
</script>
