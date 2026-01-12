<template>
  <div class="h-full flex flex-col bg-white dark:bg-gray-800 border-r border-gray-200 dark:border-gray-700">
    <div class="p-4 border-b border-gray-200 dark:border-gray-700">
      <h2 class="text-sm font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-3">Hierarchy</h2>
      <div class="relative">
        <input type="text" v-model="searchQuery" placeholder="Search..."
               class="w-full pl-8 pr-4 py-2 bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none" />
        <span class="absolute left-2.5 top-2.5 text-gray-400">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
        </span>
      </div>
    </div>

    <div class="flex-1 overflow-y-auto p-2">
      <div v-if="loading" class="text-center py-4 text-gray-500">Loading...</div>
      
      <div v-else class="space-y-1">
        <div v-for="org in filteredOrgs" :key="org.orgId">
          <!-- Organization Node -->
          <div @click="toggleOrg(org)" 
               :class="['flex items-center px-3 py-2 rounded-lg cursor-pointer transition-colors group', 
                        selectedId === org.orgId ? 'bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400' : 'hover:bg-gray-50 dark:hover:bg-gray-700 text-gray-700 dark:text-gray-300']">
            <button @click.stop="toggleExpand(org)" class="mr-2 p-0.5 rounded hover:bg-gray-200 dark:hover:bg-gray-600 text-gray-400">
              <svg :class="['w-3 h-3 transition-transform', org.expanded ? 'rotate-90' : '']" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
              </svg>
            </button>
            <span class="truncate font-medium text-sm flex-1">{{ org.legalName }}</span>
            <span v-if="!org.active" class="w-2 h-2 rounded-full bg-red-400 ml-2"></span>
          </div>

          <!-- Sites (Children) -->
          <div v-if="org.expanded" class="ml-4 pl-2 border-l border-gray-200 dark:border-gray-700 mt-1 space-y-1">
            <div v-if="org.loadingSites" class="text-xs text-gray-400 py-1 pl-4">Loading sites...</div>
            <div v-else-if="org.sites && org.sites.length === 0" class="text-xs text-gray-400 py-1 pl-4">No sites</div>
            
            <div v-for="site in org.sites" :key="site.siteId"
                 @click="selectSite(org, site)"
                 :class="['flex items-center px-3 py-1.5 rounded-md cursor-pointer text-sm transition-colors',
                          selectedId === site.siteId ? 'bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400' : 'hover:bg-gray-50 dark:hover:bg-gray-700 text-gray-600 dark:text-gray-400']">
              <span class="w-4 h-4 mr-2 flex items-center justify-center text-xs opacity-70">
                <i v-if="site.siteType === 'WAREHOUSE'" class="fas fa-warehouse"></i>
                <i v-else-if="site.siteType === 'STORE'" class="fas fa-store"></i>
                <i v-else class="fas fa-map-marker-alt"></i>
              </span>
              <span class="truncate">{{ site.address?.city || 'Unknown' }} <span class="text-xs opacity-50 ml-1">({{ site.siteType }})</span></span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="p-4 border-t border-gray-200 dark:border-gray-700">
        <button @click="$emit('create-org')" class="w-full py-2 px-4 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-medium transition-colors shadow-sm">
            + New Organization
        </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import api from '@/api/axios';

const props = defineProps({
  modelValue: String // Selected ID (Org or Site)
});

const emit = defineEmits(['update:modelValue', 'select', 'create-org']);

const organizations = ref([]);
const loading = ref(true);
const searchQuery = ref('');
const selectedId = ref(props.modelValue);

const filteredOrgs = computed(() => {
  if (!searchQuery.value) return organizations.value;
  const query = searchQuery.value.toLowerCase();
  return organizations.value.filter(org => 
    org.legalName.toLowerCase().includes(query)
  );
});

const loadOrganizations = async () => {
  loading.value = true;
  try {
    const response = await api.get('/api/organizations');
    organizations.value = response.data.map(org => ({
      ...org,
      expanded: false,
      sites: [],
      sitesLoaded: false,
      loadingSites: false
    }));
  } catch (error) {
    console.error('Failed to load organizations:', error);
  } finally {
    loading.value = false;
  }
};

const toggleExpand = async (org) => {
  org.expanded = !org.expanded;
  if (org.expanded && !org.sitesLoaded) {
    await loadSites(org);
  }
};

const loadSites = async (org) => {
  org.loadingSites = true;
  try {
    const response = await api.get(`/api/organizations/${org.orgId}/sites`);
    org.sites = response.data;
    org.sitesLoaded = true;
  } catch (error) {
    console.error(`Failed to load sites for ${org.legalName}:`, error);
  } finally {
    org.loadingSites = false;
  }
};

const toggleOrg = (org) => {
  // If clicking the org row, we select the org AND expand it
  selectedId.value = org.orgId;
  emit('select', { type: 'ORGANIZATION', data: org });
  if (!org.expanded) {
    toggleExpand(org);
  }
};

const selectSite = (org, site) => {
  selectedId.value = site.siteId;
  emit('select', { type: 'SITE', data: site, parent: org });
};

watch(() => props.modelValue, (newVal) => {
  selectedId.value = newVal;
});

onMounted(() => {
  loadOrganizations();
});
</script>
