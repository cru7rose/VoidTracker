<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-900">Manifest Management</h1>
      <button 
        @click="generateManifests"
        :disabled="generating"
        class="px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700 disabled:opacity-50 transition-colors"
      >
        {{ generating ? 'Generating...' : 'Generate Manifests' }}
      </button>
    </div>

    <!-- Filters -->
    <div class="bg-white rounded-lg shadow p-4 mb-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Date</label>
          <input 
            v-model="filters.date" 
            type="date"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
          />
        </div>
        
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Status</label>
          <select 
            v-model="filters.status"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
          >
            <option value="">All Statuses</option>
            <option value="DRAFT">Draft</option>
            <option value="ASSIGNED">Assigned</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="COMPLETED">Completed</option>
          </select>
        </div>
        
        <div class="flex items-end">
          <button 
            @click="loadManifests"
            class="px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700 transition-colors"
          >
            Apply Filters
          </button>
        </div>
      </div>
    </div>

    <!-- Manifests List -->
    <div v-if="loading" class="text-center py-12">
      <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      <p class="mt-4 text-gray-600">Loading manifests...</p>
    </div>

    <div v-else-if="manifests.length === 0" class="bg-white rounded-lg shadow p-12 text-center">
      <p class="text-gray-500">No manifests found. Generate manifests to get started.</p>
    </div>

    <div v-else class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div 
        v-for="manifest in manifests" 
        :key="manifest.id"
        class="bg-white rounded-lg shadow hover:shadow-lg transition-shadow"
      >
        <div class="p-6">
          <div class="flex justify-between items-start mb-4">
            <div>
              <h3 class="text-lg font-semibold text-gray-900">
                Manifest #{{ manifest.id.substring(0, 8) }}
              </h3>
              <p class="text-sm text-gray-500">{{ formatDate(manifest.date) }}</p>
            </div>
            <span 
              :class="getStatusClass(manifest.status)"
              class="px-3 py-1 text-xs font-semibold rounded-full"
            >
              {{ manifest.status }}
            </span>
          </div>

          <div class="space-y-2 mb-4">
            <div class="flex justify-between text-sm">
              <span class="text-gray-600">Driver:</span>
              <span class="font-medium text-gray-900">
                {{ manifest.driverName || 'Not Assigned' }}
              </span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-gray-600">Vehicle:</span>
              <span class="font-medium text-gray-900">
                {{ manifest.vehicleName || manifest.vehicleId?.substring(0, 8) || 'N/A' }}
              </span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-gray-600">Stops:</span>
              <span class="font-medium text-gray-900">{{ manifest.routes?.length || 0 }}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-gray-600">Distance:</span>
              <span class="font-medium text-gray-900">
                {{ (manifest.totalDistanceMeters / 1000).toFixed(1) }} km
              </span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-gray-600">Est. Duration:</span>
              <span class="font-medium text-gray-900">
                {{ formatDuration(manifest.estimatedDurationMillis) }}
              </span>
            </div>
            <!-- Modern Manifest Fields -->
            <div class="flex justify-between text-sm border-t pt-2 mt-2">
              <span class="text-gray-600">External Ref:</span>
              <span class="font-mono text-xs bg-gray-100 px-2 py-0.5 rounded text-gray-900">
                {{ manifest.externalReference || 'N/A' }}
              </span>
            </div>
            <div class="flex justify-between text-sm" v-if="manifest.metadata">
               <span class="text-gray-600">Metadata:</span>
               <span class="font-mono text-xs text-gray-500 truncate max-w-[150px]" :title="JSON.stringify(manifest.metadata)">
                 {{ JSON.stringify(manifest.metadata) }}
               </span>
            </div>
          </div>

          <div class="flex gap-2">
            <button 
              v-if="manifest.status === 'DRAFT'"
              @click="openAssignDriver(manifest)"
              class="flex-1 px-3 py-2 bg-primary-600 text-white text-sm rounded-md hover:bg-primary-700 transition-colors"
            >
              Assign Driver
            </button>
            
            <button 
              @click="viewManifestDetails(manifest.id)"
              class="flex-1 px-3 py-2 border border-gray-300 text-gray-700 text-sm rounded-md hover:bg-gray-50 transition-colors"
            >
              View Details
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Assign Driver Modal -->
    <div 
      v-if="showAssignModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      @click.self="closeAssignModal"
    >
      <div class="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4 animate-slide-in">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">Assign Driver</h3>
        
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">Select Driver</label>
          <select 
            v-model="selectedDriverId"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
          >
            <option value="">Choose a driver...</option>
            <option v-for="driver in availableDrivers" :key="driver.id" :value="driver.id">
              {{ driver.name }}
            </option>
          </select>
        </div>

        <div class="flex gap-3">
          <button 
            @click="closeAssignModal"
            class="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors"
          >
            Cancel
          </button>
          <button 
            @click="assignDriver"
            :disabled="!selectedDriverId || assigning"
            class="flex-1 px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700 disabled:opacity-50 transition-colors"
          >
            {{ assigning ? 'Assigning...' : 'Assign' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useManifestStore } from '../../stores/manifestStore';
import { format } from 'date-fns';

const router = useRouter();
const manifestStore = useManifestStore();

const loading = ref(true);
const generating = ref(false);
const manifests = ref([]);
const filters = ref({
  date: new Date().toISOString().split('T')[0],
  status: ''
});

const showAssignModal = ref(false);
const selectedManifest = ref(null);
const selectedDriverId = ref('');
const assigning = ref(false);

// Mock drivers - in production, fetch from API
const availableDrivers = ref([
  { id: '1', name: 'Jan Kowalski' },
  { id: '2', name: 'Anna Nowak' },
  { id: '3', name: 'Piotr Wiśniewski' },
  { id: '4', name: 'Maria Dąbrowska' },
  { id: '5', name: 'Tomasz Lewandowski' }
]);

onMounted(() => {
  loadManifests();
});

async function loadManifests() {
  loading.value = true;
  try {
    manifests.value = await manifestStore.fetchManifests(filters.value);
  } catch (error) {
    console.error('Failed to load manifests:', error);
  } finally {
    loading.value = false;
  }
}

async function generateManifests() {
  generating.value = true;
  try {
    await manifestStore.generateManifests();
    await loadManifests();
  } catch (error) {
    console.error('Failed to generate manifests:', error);
    alert('Failed to generate manifests. Please try again.');
  } finally {
    generating.value = false;
  }
}

function openAssignDriver(manifest) {
  selectedManifest.value = manifest;
  selectedDriverId.value = '';
  showAssignModal.value = true;
}

function closeAssignModal() {
  showAssignModal.value = false;
  selectedManifest.value = null;
  selectedDriverId.value = '';
}

async function assignDriver() {
  if (!selectedDriverId.value || !selectedManifest.value) return;
  
  assigning.value = true;
  try {
    await manifestStore.assignDriver(selectedManifest.value.id, selectedDriverId.value);
    await loadManifests();
    closeAssignModal();
  } catch (error) {
    console.error('Failed to assign driver:', error);
    alert('Failed to assign driver. Please try again.');
  } finally {
    assigning.value = false;
  }
}

function viewManifestDetails(manifestId) {
  router.push(`/internal/manifests/${manifestId}`);
}

function formatDate(dateString) {
  return format(new Date(dateString), 'MMM dd, yyyy');
}

function formatDuration(milliseconds) {
  const hours = Math.floor(milliseconds / (1000 * 60 * 60));
  const minutes = Math.floor((milliseconds % (1000 * 60 * 60)) / (1000 * 60));
  return `${hours}h ${minutes}m`;
}

function getStatusClass(status) {
  const classes = {
    'DRAFT': 'bg-gray-100 text-gray-800',
    'ASSIGNED': 'bg-blue-100 text-blue-800',
    'IN_PROGRESS': 'bg-yellow-100 text-yellow-800',
    'COMPLETED': 'bg-green-100 text-green-800'
  };
  return classes[status] || 'bg-gray-100 text-gray-800';
}
</script>
