<template>
  <div class="flex h-[calc(100vh-64px)] overflow-hidden bg-gray-50 dark:bg-gray-900 font-inter">
    <!-- Sidebar Tree (Glassmorphism) -->
    <div class="w-80 flex-shrink-0 z-20 shadow-xl bg-white/80 dark:bg-gray-800/80 backdrop-blur-md border-r border-gray-200/50 dark:border-gray-700/50">
      <OrganizationTree 
        v-model="selectedId" 
        @select="handleSelection"
        @create-org="handleCreateOrg" 
      />
    </div>

    <!-- Main Content Area -->
    <div class="flex-1 flex flex-col overflow-hidden relative">
      
      <!-- Top Bar: KPIs & View Toggle -->
      <div class="h-16 bg-white/90 dark:bg-gray-800/90 backdrop-blur-sm border-b border-gray-200 dark:border-gray-700 flex items-center justify-between px-6 z-10">
        <!-- KPI Widgets -->
        <div class="flex gap-6">
            <div class="flex items-center gap-3">
                <div class="p-2 rounded-lg bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                </div>
                <div>
                    <p class="text-xs text-gray-500 dark:text-gray-400 uppercase font-semibold tracking-wider">Revenue (MTD)</p>
                    <p class="text-sm font-bold text-gray-900 dark:text-white">$1.2M <span class="text-green-500 text-xs">↑ 12%</span></p>
                </div>
            </div>
            <div class="flex items-center gap-3">
                <div class="p-2 rounded-lg bg-purple-50 dark:bg-purple-900/30 text-purple-600 dark:text-purple-400">
                     <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path></svg>
                </div>
                <div>
                    <p class="text-xs text-gray-500 dark:text-gray-400 uppercase font-semibold tracking-wider">Active Sites</p>
                    <p class="text-sm font-bold text-gray-900 dark:text-white">142 <span class="text-gray-400 text-xs">/ 150</span></p>
                </div>
            </div>
             <div class="flex items-center gap-3">
                <div class="p-2 rounded-lg bg-red-50 dark:bg-red-900/30 text-red-600 dark:text-red-400">
                     <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>
                </div>
                <div>
                    <p class="text-xs text-gray-500 dark:text-gray-400 uppercase font-semibold tracking-wider">Critical Issues</p>
                    <p class="text-sm font-bold text-gray-900 dark:text-white">3 <span class="text-red-500 text-xs">New</span></p>
                </div>
            </div>
        </div>

        <!-- View Toggle -->
        <div class="bg-gray-100 dark:bg-gray-700 p-1 rounded-lg flex">
            <button @click="viewMode = 'list'" 
                    :class="['px-3 py-1.5 rounded-md text-xs font-medium transition-all', viewMode === 'list' ? 'bg-white dark:bg-gray-600 shadow text-gray-900 dark:text-white' : 'text-gray-500 dark:text-gray-400 hover:text-gray-700']">
                List View
            </button>
            <button @click="viewMode = 'map'" 
                    :class="['px-3 py-1.5 rounded-md text-xs font-medium transition-all', viewMode === 'map' ? 'bg-white dark:bg-gray-600 shadow text-gray-900 dark:text-white' : 'text-gray-500 dark:text-gray-400 hover:text-gray-700']">
                Map View
            </button>
        </div>
      </div>

      <!-- Map View Overlay -->
      <div v-if="viewMode === 'map'" class="absolute inset-0 top-16 z-0">
        <GlobalCommandMap :sites="allSites" @select-site="handleMapSelection" />
      </div>

      <!-- List/Detail View Content -->
      <div v-else class="flex-1 overflow-y-auto relative z-0 p-6">
        <div v-if="!selectedItem" class="flex flex-col items-center justify-center h-full text-gray-400">
            <div class="w-24 h-24 bg-gray-100 dark:bg-gray-800 rounded-full flex items-center justify-center mb-4">
                <svg class="w-10 h-10 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path></svg>
            </div>
            <p class="text-lg font-medium text-gray-600 dark:text-gray-300">Select an Organization or Site</p>
            <p class="text-sm">Use the hierarchy on the left or switch to Map View.</p>
        </div>

        <div v-else class="max-w-6xl mx-auto">
            <!-- Breadcrumbs -->
            <div class="flex items-center text-sm text-gray-500 mb-6">
                <span class="hover:text-gray-700 cursor-pointer">Organizations</span>
                <span class="mx-2">/</span>
                <span v-if="selectedType === 'SITE'" class="hover:text-gray-700 cursor-pointer">{{ selectedParent?.legalName }}</span>
                <span v-if="selectedType === 'SITE'" class="mx-2">/</span>
                <span class="font-medium text-gray-900 dark:text-white">{{ selectedItem.legalName || selectedItem.address?.city || 'New Organization' }}</span>
            </div>

            <!-- Dynamic Component based on selection -->
            <component 
                :is="currentComponent" 
                :id="selectedId" 
                :data="selectedItem"
                :parent="selectedParent"
                @refresh="refreshTree"
            />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, shallowRef } from 'vue';
import { useRouter } from 'vue-router';
import OrganizationTree from '@/components/organizations/OrganizationTree.vue';
import OrganizationDetail from '@/views/internal/organizations/OrganizationDetail.vue';
import SiteDetail from '@/components/organizations/SiteDetail.vue'; 
import GlobalCommandMap from '@/components/organizations/GlobalCommandMap.vue';

const router = useRouter();
const selectedId = ref(null);
const selectedItem = ref(null);
const selectedType = ref(null);
const selectedParent = ref(null);
const viewMode = ref('list'); // 'list' or 'map'
const allSites = ref([]); // To be populated with all sites for the map

const currentComponent = computed(() => {
    if (selectedType.value === 'ORGANIZATION') return OrganizationDetail;
    if (selectedType.value === 'SITE') return SiteDetail;
    if (selectedType.value === 'CREATE') return OrganizationDetail;
    return null;
});

const handleSelection = (selection) => {
    selectedType.value = selection.type;
    selectedItem.value = selection.data;
    selectedParent.value = selection.parent;
    selectedId.value = selection.type === 'ORGANIZATION' ? selection.data.orgId : selection.data.siteId;
    
    // Switch to list view when selecting from tree
    viewMode.value = 'list';
};

const handleMapSelection = (siteId) => {
    // Find site data (mock logic for now, in real app we'd look up in allSites)
    // For now we just switch view mode and try to find it if we had the data
    console.log('Selected site from map:', siteId);
    // Ideally we would set selectedItem here and switch to list view to show details
    // viewMode.value = 'list';
};

const handleCreateOrg = () => {
    selectedType.value = 'CREATE';
    selectedItem.value = { orgId: 'create' }; 
    selectedId.value = 'create';
    selectedParent.value = null;
    viewMode.value = 'list';
};

const refreshTree = () => {
    // Logic to trigger tree refresh if needed
};

// Mock loading all sites for the map
// In production, this would be an API call to /api/sites/all or similar
allSites.value = [
    { siteId: '1', siteType: 'WAREHOUSE', address: { latitude: 55.6761, longitude: 12.5683, city: 'Copenhagen' } },
    { siteId: '2', siteType: 'STORE', address: { latitude: 56.1629, longitude: 10.2039, city: 'Aarhus' } },
    { siteId: '3', siteType: 'STORE', address: { latitude: 55.3959, longitude: 10.3883, city: 'Odense' } }
];
</script>
