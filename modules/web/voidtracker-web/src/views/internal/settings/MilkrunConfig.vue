<template>
  <div class="h-full flex flex-col p-6">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-bold text-gray-800">Milkrun Configuration</h2>
      <button 
        @click="openModal()" 
        class="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 flex items-center gap-2"
      >
        <span class="material-icons text-sm">add</span> Add New
      </button>
    </div>

    <!-- List -->
    <div class="flex-1 bg-white rounded-lg shadow overflow-hidden">
        <div v-if="loading" class="text-center py-12">Loading...</div>
        <div v-else-if="milkruns.length === 0" class="text-center py-12 text-gray-500">
            No milkruns defined yet.
        </div>
        <table v-else class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
                <tr>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Name</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Schedule</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Stops</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
                </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
                <tr v-for="run in milkruns" :key="run.id" class="hover:bg-gray-50">
                    <td class="px-6 py-4 whitespace-nowrap font-medium text-gray-900">{{ run.name }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ run.schedule }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ run.stops ? run.stops.length : 0 }} stops</td>
                    <td class="px-6 py-4 whitespace-nowrap">
                        <span :class="run.active ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'" class="px-2 py-1 text-xs rounded-full">
                            {{ run.active ? 'Active' : 'Draft' }}
                        </span>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                        <button @click="openModal(run)" class="text-indigo-600 hover:text-indigo-900 mr-3">Edit</button>
                        <button @click="deleteMilkrun(run.id)" class="text-red-600 hover:text-red-900">Delete</button>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>

    <!-- Edit Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full flex items-center justify-center z-50">
        <div class="bg-white p-8 rounded-lg shadow-xl w-[800px] max-h-[90vh] overflow-y-auto">
            <h3 class="text-xl font-bold mb-4">{{ currentRun.id ? 'Edit' : 'New' }} Milkrun</h3>
            
            <div class="grid grid-cols-2 gap-4 mb-4">
                <div>
                    <label class="block text-sm font-medium text-gray-700">Name</label>
                    <input v-model="currentRun.name" type="text" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2">
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700">Schedule (Cron or Text)</label>
                    <input v-model="currentRun.schedule" type="text" placeholder="e.g., 0 8 * * 1-5" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2">
                </div>
                <div>
                     <label class="block text-sm font-medium text-gray-700">Driver (Optional)</label>
                     <input v-model="currentRun.driverId" type="text" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2">
                </div>
                <div>
                    <label class="flex items-center gap-2 mt-8">
                        <input v-model="currentRun.active" type="checkbox" class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500">
                        <span class="text-sm font-medium text-gray-700">Active</span>
                    </label>
                </div>
            </div>

            <div class="mb-4">
                <div class="flex justify-between items-center mb-2">
                    <label class="block text-sm font-medium text-gray-700">Stops</label>
                    <button @click="addStop" class="text-sm text-green-600 font-medium hover:text-green-800">+ Add Stop</button>
                </div>
                <div v-if="!currentRun.stops || currentRun.stops.length === 0" class="text-sm text-gray-500 border border-dashed border-gray-300 p-4 rounded text-center">
                    No stops defined.
                </div>
                <div v-else class="space-y-2 max-h-60 overflow-y-auto border border-gray-200 p-2 rounded">
                    <div v-for="(stop, idx) in currentRun.stops" :key="idx" class="flex gap-2 items-center bg-gray-50 p-2 rounded">
                        <span class="text-xs text-gray-500 font-mono w-6">{{ idx + 1 }}.</span>
                        <select v-model="stop.activityType" class="text-xs border-gray-300 rounded">
                            <option value="PICKUP">Pickup</option>
                            <option value="DELIVERY">Delivery</option>
                        </select>
                        <input v-model="stop.address" type="text" placeholder="Address" class="flex-1 text-sm border-gray-300 rounded p-1">
                        <input v-model.number="stop.lat" type="number" placeholder="Lat" class="w-20 text-sm border-gray-300 rounded p-1">
                        <input v-model.number="stop.lon" type="number" placeholder="Lon" class="w-20 text-sm border-gray-300 rounded p-1">
                        <button @click="removeStop(idx)" class="text-red-500 hover:text-red-700">×</button>
                    </div>
                </div>
            </div>

            <div class="flex justify-end gap-3 mt-6">
                <button @click="showModal = false" class="px-4 py-2 text-gray-600 hover:text-gray-800">Cancel</button>
                <button @click="saveMilkrun" class="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700">Save</button>
            </div>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { planningApi } from '../../../api/axios'; // Adjust import path if needed

const loading = ref(false);
const milkruns = ref([]);
const showModal = ref(false);
const currentRun = ref({});

const emptyRun = {
    name: '',
    schedule: '',
    driverId: '',
    active: true,
    stops: []
};

onMounted(() => {
    fetchMilkruns();
});

async function fetchMilkruns() {
    loading.value = true;
    try {
        const res = await planningApi.get('/api/milkruns');
        milkruns.value = res.data;
    } catch (e) {
        console.error("Failed to load milkruns", e);
    } finally {
        loading.value = false;
    }
}

function openModal(run = null) {
    if (run) {
        currentRun.value = JSON.parse(JSON.stringify(run)); // Deep copy
        if (!currentRun.value.stops) currentRun.value.stops = [];
    } else {
        currentRun.value = JSON.parse(JSON.stringify(emptyRun));
    }
    showModal.value = true;
}

function addStop() {
    if (!currentRun.value.stops) currentRun.value.stops = [];
    currentRun.value.stops.push({
        activityType: 'DELIVERY',
        address: '',
        lat: 0.0,
        lon: 0.0
    });
}

function removeStop(index) {
    currentRun.value.stops.splice(index, 1);
}

async function saveMilkrun() {
    try {
        if (currentRun.value.id) {
            await planningApi.put(`/api/milkruns/${currentRun.value.id}`, currentRun.value);
        } else {
            await planningApi.post('/api/milkruns', currentRun.value);
        }
        await fetchMilkruns();
        showModal.value = false;
    } catch (e) {
        console.error("Failed to save milkrun", e);
        alert("Failed to save. Check console.");
    }
}

async function deleteMilkrun(id) {
    if (!confirm("Are you sure?")) return;
    try {
        await planningApi.delete(`/api/milkruns/${id}`);
        await fetchMilkruns();
    } catch (e) {
        console.error("Failed to delete", e);
    }
}
</script>
