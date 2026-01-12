<template>
  <div v-if="show" class="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
    <div class="bg-void-darker border border-white/10 rounded-lg shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
      <!-- Header -->
      <div class="border-b border-white/10 p-6 flex justify-between items-center bg-gradient-to-r from-void-darker to-void-gray">
        <div>
          <h2 class="text-2xl font-bold text-white">Edit Route Assignment</h2>
          <p class="text-xs text-gray-400 mt-1">Route ID: {{ assignment?.id?.substring(0, 8) }}...</p>
        </div>
        <button @click="close" class="text-gray-400 hover:text-white transition">
          <span class="material-icons">close</span>
        </button>
      </div>

      <!-- Form -->
      <div class="p-6 space-y-6">
        <!-- Route Name -->
        <div>
          <label class="block text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">Route Name</label>
          <input 
            v-model="formData.routeName" 
            type="text" 
            class="w-full bg-void-gray border border-white/10 rounded px-4 py-3 text-white focus:border-void-cyan-500 focus:ring-2 focus:ring-void-cyan-500/20 transition"
            placeholder="e.g. Warsaw North Route"
          />
        </div>

        <!-- Driver Selection -->
        <div>
          <label class="block text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">
            Driver
            <span v-if="loadingDrivers" class="text-void-cyan-400 ml-2">(Loading...)</span>
          </label>
          <select 
            v-model="formData.driverId" 
            class="w-full bg-void-gray border border-white/10 rounded px-4 py-3 text-white focus:border-void-cyan-500 focus:ring-2 focus:ring-void-cyan-500/20 transition"
            :disabled="loadingDrivers"
          >
            <option :value="null">-- Unassigned --</option>
            <option v-for="driver in drivers" :key="driver.id" :value="driver.id">
              {{ driver.name }} ({{ driver.email }})
            </option>
          </select>
        </div>

        <!-- Vehicle Selection -->
        <div>
          <label class="block text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">
            Vehicle
            <span v-if="loadingVehicles" class="text-void-cyan-400 ml-2">(Loading...)</span>
          </label>
          <select 
            v-model="formData.vehicleId" 
            class="w-full bg-void-gray border border-white/10 rounded px-4 py-3 text-white focus:border-void-cyan-500 focus:ring-2 focus:ring-void-cyan-500/20 transition"
            :disabled="loadingVehicles"
          >
            <option :value="null">-- Unassigned --</option>
            <option v-for="vehicle in vehicles" :key="vehicle.id" :value="vehicle.id">
              {{ vehicle.licensePlate }} - {{ vehicle.type }} ({{ vehicle.capacity }}kg)
            </option>
          </select>
        </div>

        <!-- Carrier Selection (Optional) -->
        <div>
          <label class="block text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">Carrier (Optional)</label>
          <input 
            v-model="formData.carrierId" 
            type="text" 
            class="w-full bg-void-gray border border-white/10 rounded px-4 py-3 text-white focus:border-void-cyan-500 focus:ring-2 focus:ring-void-cyan-500/20 transition"
            placeholder="Carrier UUID (leave empty for internal)"
          />
        </div>

        <!-- Status -->
        <div>
          <label class="block text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">Status</label>
          <select 
            v-model="formData.status" 
            class="w-full bg-void-gray border border-white/10 rounded px-4 py-3 text-white focus:border-void-cyan-500 focus:ring-2 focus:ring-void-cyan-500/20 transition"
          >
            <option value="DRAFT">Draft</option>
            <option value="ASSIGNED">Assigned</option>
            <option value="PUBLISHED">Published</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="COMPLETED">Completed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>

        <!-- Route Info (Read-only) -->
        <div class="bg-void-gray/50 border border-white/5 rounded p-4">
          <h3 class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-3">Route Details</h3>
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div>
              <span class="text-gray-500">Stops:</span>
              <span class="text-white ml-2 font-mono">{{ assignment?.stopCount || 0 }}</span>
            </div>
            <div>
              <span class="text-gray-500">Distance:</span>
              <span class="text-white ml-2 font-mono">{{ (assignment?.totalDistanceKm || 0).toFixed(1) }} km</span>
            </div>
            <div class="col-span-2">
              <span class="text-gray-500">Created:</span>
              <span class="text-white ml-2">{{ formatDate(assignment?.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer Actions -->
      <div class="border-t border-white/10 p-6 bg-void-gray/30 flex justify-between items-center">
        <button 
          @click="deleteAssignment" 
          :disabled="saving"
          class="px-4 py-2 bg-red-500/10 border border-red-500/30 text-red-400 rounded hover:bg-red-500/20 transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
        >
          <span class="material-icons text-sm">delete</span>
          Delete
        </button>
        
        <div class="flex gap-3">
          <button 
            @click="close" 
            class="px-6 py-2 bg-white/5 border border-white/10 text-gray-300 rounded hover:bg-white/10 transition"
          >
            Cancel
          </button>
          
          <button 
            v-if="formData.driverId && formData.status === 'ASSIGNED'"
            @click="publishRoute" 
            :disabled="saving"
            class="px-6 py-2 bg-void-pink-500/20 border border-void-pink-500/50 text-void-pink-400 rounded hover:bg-void-pink-500/30 transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
          >
            <span class="material-icons text-sm">send</span>
            {{ saving ? 'Publishing...' : 'Publish to Driver' }}
          </button>
          
          <button 
            @click="save" 
            :disabled="saving"
            class="px-6 py-2 bg-void-cyan-500/20 border border-void-cyan-500/50 text-void-cyan-400 rounded hover:bg-void-cyan-500/30 transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
          >
            <span class="material-icons text-sm">save</span>
            {{ saving ? 'Saving...' : 'Save Changes' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue';
import axios from 'axios';
import { useAuthStore } from '../stores/authStore';

const props = defineProps({
  show: Boolean,
  assignment: Object
});

const emit = defineEmits(['close', 'saved', 'deleted', 'published']);

const authStore = useAuthStore();
const saving = ref(false);
const loadingDrivers = ref(false);
const loadingVehicles = ref(false);

const drivers = ref([]);
const vehicles = ref([]);

const formData = reactive({
  routeName: '',
  driverId: null,
  vehicleId: null,
  carrierId: null,
  status: 'DRAFT'
});

// Watch for assignment changes to populate form
watch(() => props.assignment, (newVal) => {
  if (newVal) {
    formData.routeName = newVal.routeName || '';
    formData.driverId = newVal.driverId || null;
    formData.vehicleId = newVal.vehicleId || null;
    formData.carrierId = newVal.carrierId || null;
    formData.status = newVal.status || 'DRAFT';
  }
}, { immediate: true });

// Watch for modal open to fetch drivers/vehicles
watch(() => props.show, async (show) => {
  if (show) {
    await Promise.all([fetchDrivers(), fetchVehicles()]);
  }
});

async function fetchDrivers() {
  loadingDrivers.value = true;
  try {
    const headers = { Authorization: `Bearer ${authStore.token}` };
    const res = await axios.get('/api/auth/users', { headers });
    // Filter for users with DRIVER role
    drivers.value = (res.data || [])
      .filter(u => u.roles?.includes('ROLE_DRIVER') || u.roles?.includes('DRIVER'))
      .map(u => ({
        id: u.id,
        name: u.username || u.email || 'Unknown',
        email: u.email
      }));
  } catch (e) {
    console.error('Failed to fetch drivers:', e);
    drivers.value = [];
  } finally {
    loadingDrivers.value = false;
  }
}

async function fetchVehicles() {
  loadingVehicles.value = true;
  try {
    const headers = { Authorization: `Bearer ${authStore.token}` };
    const res = await axios.get('/api/mesh/profiles', { headers });
    vehicles.value = (res.data || []).map(v => ({
      id: v.id,
      licensePlate: v.licensePlate || v.id.substring(0, 8),
      type: v.vehicleType || 'VAN',
      capacity: v.capacityKg || 1000
    }));
  } catch (e) {
    console.error('Failed to fetch vehicles:', e);
    vehicles.value = [];
  } finally {
    loadingVehicles.value = false;
  }
}

async function save() {
  saving.value = true;
  try {
    const headers = { Authorization: `Bearer ${authStore.token}` };
    
    const payload = {
      routeName: formData.routeName,
      driverId: formData.driverId,
      vehicleId: formData.vehicleId,
      carrierId: formData.carrierId || null,
      status: formData.status
    };
    
    await axios.put(`/api/planning/assignments/${props.assignment.id}`, payload, { headers });
    console.log('✅ Assignment updated');
    emit('saved');
    close();
  } catch (e) {
    console.error('Failed to save assignment:', e);
    alert('Failed to save changes: ' + (e.response?.data?.error || e.message));
  } finally {
    saving.value = false;
  }
}

async function deleteAssignment() {
  if (!confirm(`Are you sure you want to delete "${props.assignment.routeName}"?`)) {
    return;
  }
  
  saving.value = true;
  try {
    const headers = { Authorization: `Bearer ${authStore.token}` };
    await axios.delete(`/api/planning/assignments/${props.assignment.id}`, { headers });
    console.log('✅ Assignment deleted');
    emit('deleted');
    close();
  } catch (e) {
    console.error('Failed to delete assignment:', e);
    alert('Failed to delete assignment: ' + (e.response?.data?.error || e.message));
  } finally {
    saving.value = false;
  }
}

async function publishRoute() {
  saving.value = true;
  try {
    const headers = { Authorization: `Bearer ${authStore.token}` };
    const res = await axios.post(`/api/planning/assignments/${props.assignment.id}/publish`, {}, { headers });
    console.log('✅ Route published, magic link:', res.data.magicLink);
    alert(`Route published! Magic link: ${res.data.magicLink}`);
    emit('published');
    close();
  } catch (e) {
    console.error('Failed to publish route:', e);
    alert('Failed to publish route: ' + (e.response?.data?.error || e.message));
  } finally {
    saving.value = false;
  }
}

function close() {
  emit('close');
}

function formatDate(isoString) {
  if (!isoString) return '-';
  return new Date(isoString).toLocaleString('pl-PL', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}
</script>
