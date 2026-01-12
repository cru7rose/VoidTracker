<template>
  <div class="h-full flex flex-col bg-white">
    <!-- Header -->
    <div class="p-4 border-b border-gray-200 flex justify-between items-center bg-gray-50">
      <h2 class="font-bold text-lg text-gray-800">Optimization Profiles</h2>
      <button @click="openCreate" class="px-3 py-1.5 bg-blue-600 text-white rounded text-sm hover:bg-blue-700 shadow flex items-center gap-1">
        <span>+</span> New Profile
      </button>
    </div>

    <!-- List -->
    <div class="flex-1 overflow-y-auto p-4">
      <div v-if="loading" class="text-center text-gray-500 py-8">Loading profiles...</div>
      <div v-else-if="profiles.length === 0" class="text-center text-gray-400 py-8">No profiles found. Create one to get started.</div>
      
      <div class="grid gap-3">
        <div 
          v-for="profile in profiles" 
          :key="profile.id"
          class="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow cursor-pointer bg-white group"
          :class="{'ring-2 ring-blue-500': selectedProfileId === profile.id}"
          @click="selectProfile(profile)"
        >
          <div class="flex justify-between items-start">
            <div>
               <h3 class="font-bold text-gray-800">{{ profile.name }}</h3>
               <div class="text-xs text-gray-500 mt-1 flex gap-3">
                  <span class="bg-gray-100 px-2 py-0.5 rounded">🕒 {{ profile.workStartTime }} Start</span>
                  <span class="bg-gray-100 px-2 py-0.5 rounded">⏱️ {{ profile.maxRouteDurationMinutes / 60 }}h Max</span>
                  <span class="bg-gray-100 px-2 py-0.5 rounded">🚛 {{ profile.vehicleSelectionMode }}</span>
               </div>
            </div>
            
            <div class="flex gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
               <button @click.stop="editProfile(profile)" class="p-1 text-gray-400 hover:text-blue-600">✏️</button>
               <button @click.stop="deleteProfile(profile.id)" class="p-1 text-gray-400 hover:text-red-600">🗑️</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Footer Action -->
    <div class="p-4 border-t border-gray-200 bg-gray-50 flex justify-end gap-3">
        <button @click="$emit('cancel')" class="px-4 py-2 text-sm text-gray-600 hover:bg-gray-200 rounded">Cancel</button>
        <button 
            @click="$emit('confirm', selectedProfileId)" 
            :disabled="!selectedProfileId"
            class="px-4 py-2 text-sm bg-green-600 text-white font-bold rounded shadow hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
            Use Selected Profile
        </button>
    </div>

    <!-- Edit/Create Modal (Nested) -->
    <div v-if="showForm" class="absolute inset-0 z-50 bg-black/60 flex items-center justify-center p-4">
       <div class="bg-white rounded-lg shadow-xl w-full max-w-md p-6 animate-fade-in-up">
           <h3 class="font-bold text-lg mb-4">{{ isEditing ? 'Edit Profile' : 'New Profile' }}</h3>
           
           <div class="space-y-4">
               <div>
                   <label class="block text-sm font-medium text-gray-700 mb-1">Profile Name</label>
                   <input v-model="form.name" class="w-full border rounded p-2 text-sm focus:ring-2 focus:ring-blue-500 outline-none" placeholder="e.g. Standard Delivery">
               </div>
               
               <div class="grid grid-cols-2 gap-4">
                   <div>
                       <label class="block text-sm font-medium text-gray-700 mb-1">Start Time</label>
                       <input type="time" v-model="form.workStartTime" class="w-full border rounded p-2 text-sm">
                   </div>
                   <div>
                       <label class="block text-sm font-medium text-gray-700 mb-1">Max Duration (h)</label>
                       <input type="number" v-model="form.maxDurationHours" class="w-full border rounded p-2 text-sm">
                   </div>
               </div>

               <div>
                   <label class="block text-sm font-medium text-gray-700 mb-1">Vehicle Mode</label>
                   <select v-model="form.vehicleSelectionMode" class="w-full border rounded p-2 text-sm">
                       <option value="ALL">All Vehicles</option>
                       <option value="ACTIVE">Active Only (Available=True)</option>
                       <option value="DEPOT_SPECIFIC">Depot Specific (Future)</option>
                   </select>
               </div>
           </div>

           <div class="mt-6 flex justify-end gap-3">
               <button @click="showForm = false" class="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded text-sm">Cancel</button>
               <button @click="saveForm" class="px-4 py-2 bg-blue-600 text-white rounded text-sm font-bold shadow hover:bg-blue-700">Save Profile</button>
           </div>
       </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { planningApi } from '../../api/axios';

// Props & Emitter
const emit = defineEmits(['confirm', 'cancel']);

// State
const profiles = ref([]);
const loading = ref(false);
const selectedProfileId = ref(null);
const showForm = ref(false);
const isEditing = ref(false);
const form = ref({
    id: null,
    name: '',
    workStartTime: '08:00',
    maxDurationHours: 8,
    vehicleSelectionMode: 'ACTIVE'
});

// API (Mock or Real)
// Assuming we proxy /api/planning -> planning-service
const fetchProfiles = async () => {
    loading.value = true;
    try {
        // Use authenticated planningApi instance
        const res = await planningApi.get('/profiles');
        profiles.value = res.data;
    } catch (e) {
        console.warn("API unavailable, using mock", e);
        profiles.value = [
            { id: 'c0cfcb9c-2758-4f99-b4cb-54565773f7d1', name: 'PUDO Cluster (Locker Focus)', workStartTime: '08:00', maxRouteDurationMinutes: 480, vehicleSelectionMode: 'ACTIVE' },
            { id: '8cd0911c-03e4-4da8-8cff-936e9e9f8c18', name: 'Dynamic Simulation', workStartTime: '06:00', maxRouteDurationMinutes: 600, vehicleSelectionMode: 'ALL' },
            { id: 'c2a23200-2f4d-4ae2-84e4-0b8c6e8c4f84', name: 'Strict Constraint (AETR)', workStartTime: '07:00', maxRouteDurationMinutes: 540, vehicleSelectionMode: 'ACTIVE' }
        ];
    } finally {
        loading.value = false;
    }
};

const saveForm = async () => {
    try {
        const payload = {
            name: form.value.name,
            workStartTime: form.value.workStartTime,
            maxRouteDurationMinutes: form.value.maxDurationHours * 60,
            vehicleSelectionMode: form.value.vehicleSelectionMode
        };

        if (isEditing.value) {
           await planningApi.put(`/profiles/${form.value.id}`, payload);
        } else {
           await planningApi.post('/profiles', payload);
        }
        showForm.value = false;
        fetchProfiles();
    } catch (e) {
        alert("Failed to save profile (Mock mode active?)");
        console.error(e);
        // Mock Update (using a random UUID-like string for mock robustness)
        if (!isEditing.value) profiles.value.push({ ...payload, id: 'a0000000-0000-0000-0000-' + Date.now().toString().substring(0, 12), maxRouteDurationMinutes: payload.maxRouteDurationMinutes });
        showForm.value = false;
    }
};

const deleteProfile = async (id) => {
    if(!confirm("Delete this profile?")) return;
    try {
        await planningApi.delete(`/profiles/${id}`);
        fetchProfiles();
    } catch (e) {
        console.error(e);
        // Mock
        profiles.value = profiles.value.filter(p => p.id !== id);
    }
};

// Actions
const selectProfile = (p) => selectedProfileId.value = p.id;
const openCreate = () => {
    form.value = { name: '', workStartTime: '08:00', maxDurationHours: 8, vehicleSelectionMode: 'ACTIVE' };
    isEditing.value = false;
    showForm.value = true;
};
const editProfile = (p) => {
    form.value = {
        id: p.id,
        name: p.name,
        workStartTime: p.workStartTime,
        maxDurationHours: p.maxRouteDurationMinutes / 60,
        vehicleSelectionMode: p.vehicleSelectionMode
    };
    isEditing.value = true;
    showForm.value = true;
};

// Init
onMounted(fetchProfiles);
</script>
