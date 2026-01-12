<template>
  <div class="bg-white p-8 max-w-4xl mx-auto shadow-lg print:shadow-none print:p-0" id="invoice-template">
    <!-- Header -->
    <div class="flex justify-between items-start mb-8 border-b pb-8">
      <div>
        <h1 class="text-3xl font-bold text-gray-900 mb-2">INVOICE</h1>
        <p class="text-gray-500">#{{ invoice.number }}</p>
      </div>
      <div class="text-right">
        <div class="text-2xl font-bold text-blue-600 mb-1">{{ configStore.config.general.systemName }}</div>
        <p class="text-sm text-gray-500 whitespace-pre-line">{{ configStore.config.billing.companyAddress }}</p>
      </div>
    </div>

    <!-- Bill To / Details -->
    <div class="flex justify-between mb-8">
      <div>
        <h3 class="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">Bill To</h3>
        <p class="font-bold text-gray-900">{{ invoice.customerName }}</p>
        <p class="text-gray-600">{{ invoice.customerAddress }}</p>
        <p class="text-gray-600">{{ invoice.customerVat }}</p>
      </div>
      <div class="text-right">
        <div class="mb-2">
            <span class="text-xs font-bold text-gray-500 uppercase tracking-wider">Date:</span>
            <span class="ml-2 font-medium">{{ invoice.date }}</span>
        </div>
        <div class="mb-2">
            <span class="text-xs font-bold text-gray-500 uppercase tracking-wider">Due Date:</span>
            <span class="ml-2 font-medium">{{ invoice.dueDate }}</span>
        </div>
      </div>
    </div>

    <!-- Line Items -->
    <table class="w-full mb-8">
      <thead>
        <tr class="border-b-2 border-gray-200">
          <th class="text-left py-3 text-sm font-bold text-gray-600 uppercase">Description</th>
          <th class="text-right py-3 text-sm font-bold text-gray-600 uppercase">Qty</th>
          <th class="text-right py-3 text-sm font-bold text-gray-600 uppercase">Unit Price</th>
          <th class="text-right py-3 text-sm font-bold text-gray-600 uppercase">Total</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(item, index) in invoice.items" :key="index" class="border-b border-gray-100">
          <td class="py-3 text-gray-800">{{ item.description }}</td>
          <td class="py-3 text-right text-gray-600 font-mono">{{ item.qty }}</td>
          <td class="py-3 text-right text-gray-600 font-mono">{{ formatCurrency(item.unitPrice) }}</td>
          <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 text-right">{{ formatCurrency(item.qty * item.unitPrice) }}</td>
          <td class="px-6 py-4 whitespace-nowrap text-right">
            <button v-if="!item.disputed" @click="openDisputeModal(item)" class="text-xs text-red-600 hover:text-red-800 font-medium">Dispute</button>
            <span v-else class="text-xs text-orange-500 font-medium flex items-center justify-end gap-1">
              <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
              Disputed
            </span>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Surcharges Section -->
    <div v-if="appliedSurcharges.length > 0" class="mb-8 border-b border-gray-100 pb-4">
        <h4 class="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">Surcharges & Fees</h4>
        <div v-for="surcharge in appliedSurcharges" :key="surcharge.id" class="flex justify-between text-sm mb-1">
            <span class="text-gray-600">{{ surcharge.name }}</span>
            <span class="font-mono text-gray-900">{{ formatCurrency(surcharge.amount) }}</span>
        </div>
    </div>

    <!-- Totals -->
    <div class="flex justify-end">
      <div class="w-64 space-y-2">
        <div class="flex justify-between text-gray-600">
          <span>Subtotal:</span>
          <span class="font-mono">{{ formatCurrency(subtotal) }}</span>
        </div>
        <div class="flex justify-between text-gray-600">
          <span>VAT ({{ invoice.vatRate }}%):</span>
          <span class="font-mono">{{ formatCurrency(vatAmount) }}</span>
        </div>
        <div class="flex justify-between text-xl font-bold text-gray-900 border-t pt-2 mt-2">
          <span>Total:</span>
          <span class="font-mono">{{ formatCurrency(total) }}</span>
        </div>
      </div>
    </div>

    <!-- Footer -->
    <div class="mt-12 pt-8 border-t text-center text-sm text-gray-500">
      <p>{{ configStore.config.billing.footerText }}</p>
      <p class="mt-1">Please pay within {{ invoice.paymentTerms }}.</p>
    </div>
    
    <!-- Dispute Modal -->
    <div v-if="showDisputeModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-xl max-w-md w-full p-6">
        <h2 class="text-xl font-bold mb-4 text-gray-900 dark:text-white">Dispute Charge</h2>
        <p class="text-sm text-gray-500 mb-4">Please explain why you are disputing this charge: <strong>{{ selectedItem?.description }}</strong></p>
        
        <textarea v-model="disputeReason" rows="3" class="block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm mb-4" placeholder="Enter reason..."></textarea>
        
        <div class="flex justify-end gap-3">
          <button @click="showDisputeModal = false" class="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg">Cancel</button>
          <button @click="submitDispute" class="px-4 py-2 bg-red-600 hover:bg-red-700 text-white font-medium rounded-lg">Submit Dispute</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { useConfigStore } from '@/stores/configStore';

const configStore = useConfigStore();

const props = defineProps({
  invoice: {
    type: Object,
    required: true
  }
});

const showDisputeModal = ref(false);
const selectedItem = ref(null);
const disputeReason = ref('');

const subtotal = computed(() => {
  return props.invoice.items.reduce((sum, item) => sum + (item.qty * item.unitPrice), 0);
});

const appliedSurcharges = computed(() => {
    const rules = configStore.config.billing.surcharges || [];
    return rules.filter(rule => {
        const value = props.invoice[rule.conditionField]; // e.g. invoice.weight
        if (value === undefined) return false;
        
        const threshold = parseFloat(rule.conditionValue) || rule.conditionValue;
        
        switch (rule.conditionOperator) {
            case 'gt': return value > threshold;
            case 'lt': return value < threshold;
            case 'eq': return value == threshold;
            default: return false;
        }
    });
});

const surchargesTotal = computed(() => {
    return appliedSurcharges.value.reduce((sum, s) => sum + s.amount, 0);
});

const vatAmount = computed(() => {
    return (subtotal.value + surchargesTotal.value) * (props.invoice.vatRate / 100);
});

const total = computed(() => {
  return subtotal.value + surchargesTotal.value + vatAmount.value;
});

const openDisputeModal = (item) => {
  selectedItem.value = item;
  disputeReason.value = '';
  showDisputeModal.value = true;
};

const submitDispute = () => {
  if (selectedItem.value) {
    selectedItem.value.disputed = true;
    // In real app, send API request with disputeReason.value
    console.log('Dispute submitted:', selectedItem.value, disputeReason.value);
  }
  showDisputeModal.value = false;
};

const formatCurrency = (value) => {
  return new Intl.NumberFormat('da-DK', { style: 'currency', currency: 'DKK' }).format(value);
};
</script>

<style scoped>
@media print {
  @page { margin: 0; }
  body { margin: 1.6cm; }
}
</style>
