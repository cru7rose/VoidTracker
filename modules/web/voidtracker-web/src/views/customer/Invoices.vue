<template>
  <div class="p-6">
    <h1 class="text-2xl font-bold text-gray-900 mb-6">{{ t('customer.invoices.title') }}</h1>

    <div class="bg-white rounded-lg shadow overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{{ t('customer.invoices.invoice_number') }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{{ t('customer.invoices.month') }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{{ t('customer.invoices.amount') }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{{ t('customer.invoices.status') }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{{ t('common.actions') }}</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <!-- Mock Data -->
          <tr v-for="invoice in invoices" :key="invoice.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap font-medium text-gray-900">{{ invoice.invoiceNumber }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-gray-500">{{ invoice.month }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-gray-900">{{ invoice.amount }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span :class="getStatusClass(invoice.status)" class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full">
                {{ invoice.status }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
              <button class="text-indigo-600 hover:text-indigo-900">{{ t('common.download') }}</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const invoices = ref([
  { id: 1, invoiceNumber: 'INV-2025-11', month: 'November 2025', amount: '€ 12,450.00', status: 'Paid' },
  { id: 2, invoiceNumber: 'INV-2025-10', month: 'October 2025', amount: '€ 11,200.00', status: 'Paid' },
  { id: 3, invoiceNumber: 'INV-2025-09', month: 'September 2025', amount: '€ 13,100.00', status: 'Paid' },
]);

function getStatusClass(status) {
  return status === 'Paid' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800';
}
</script>
