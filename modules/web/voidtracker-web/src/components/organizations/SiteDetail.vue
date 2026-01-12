<template>
  <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 border border-gray-100 dark:border-gray-700">
    <div class="flex justify-between items-center mb-6">
      <div>
        <h2 class="text-xl font-bold text-gray-800 dark:text-white">
          {{ data?.address?.city || 'Site Details' }}
        </h2>
        <p class="text-sm text-gray-500 dark:text-gray-400">
            {{ data?.siteType }} • {{ parent?.legalName }}
        </p>
      </div>
      <div class="flex gap-2">
         <span v-if="data?.billingConfig" class="px-2 py-1 text-xs font-medium rounded-full bg-purple-100 text-purple-800">
            Billing Override
         </span>
      </div>
    </div>

    <!-- Tabs -->
    <div class="flex border-b border-gray-200 dark:border-gray-700 mb-6">
      <button @click="activeTab = 'overview'"
              :class="['px-4 py-2 font-medium text-sm transition-colors border-b-2', activeTab === 'overview' ? 'border-blue-600 text-blue-600 dark:text-blue-400' : 'border-transparent text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200']">
        Overview
      </button>
      <button @click="activeTab = 'crew'"
              :class="['px-4 py-2 font-medium text-sm transition-colors border-b-2', activeTab === 'crew' ? 'border-blue-600 text-blue-600 dark:text-blue-400' : 'border-transparent text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200']">
        Crew
      </button>
    </div>

    <!-- Overview Tab -->
    <div v-if="activeTab === 'overview'">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
                <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Address</h3>
                <div class="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg border border-gray-200 dark:border-gray-600 font-mono text-sm">
                    <pre>{{ JSON.stringify(data?.address, null, 2) }}</pre>
                </div>
            </div>
            <div>
                <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Configuration</h3>
                <div class="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg border border-gray-200 dark:border-gray-600 font-mono text-sm">
                    <pre>{{ data?.billingConfig ? JSON.stringify(data.billingConfig, null, 2) : 'Inherits from Organization' }}</pre>
                </div>
            </div>
        </div>
    </div>

    <!-- Crew Tab -->
    <div v-if="activeTab === 'crew'">
        <SiteCrew :siteId="id" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import SiteCrew from '@/components/organizations/SiteCrew.vue';

const props = defineProps({
    id: String,
    data: Object,
    parent: Object
});

const activeTab = ref('overview');
</script>
