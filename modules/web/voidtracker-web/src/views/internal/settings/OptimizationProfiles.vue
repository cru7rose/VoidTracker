<template>
  <div class="bg-white rounded-lg shadow p-6">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-xl font-bold text-gray-800">Optimization Profiles (Timefold)</h2>
      <button @click="createProfile" class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded">
        + New Profile
      </button>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Sidebar List -->
      <div class="col-span-1 border-r border-gray-200 pr-4 space-y-2">
        <div 
          v-for="profile in profiles" 
          :key="profile.id"
          @click="selectProfile(profile)"
          class="p-3 rounded cursor-pointer transition-colors"
          :class="selectedProfile?.id === profile.id ? 'bg-blue-50 border-l-4 border-blue-500' : 'hover:bg-gray-50'"
        >
          <div class="font-medium text-gray-900">{{ profile.name }}</div>
          <div class="text-xs text-gray-500">{{ profile.code }}</div>
        </div>
      </div>

      <!-- Edit Form -->
      <div class="col-span-2 pl-4" v-if="selectedProfile">
        <form @submit.prevent="saveProfile" class="space-y-4">
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700">Profile Name</label>
              <input v-model="selectedProfile.name" type="text" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">Code</label>
              <input v-model="selectedProfile.code" type="text" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm" required />
            </div>
          </div>

          <div class="border-t border-gray-200 pt-4">
            <h3 class="text-lg font-medium text-gray-900 mb-3">Solver Configuration</h3>
            
            <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700">Termination (Seconds)</label>
                  <input v-model.number="selectedProfile.terminationSeconds" type="number" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm" />
                  <p class="text-xs text-gray-500 mt-1">Max time the solver will run.</p>
                </div>
                 <div>
                  <label class="block text-sm font-medium text-gray-700">Base Algorithm / Strategy</label>
                   <select v-model="selectedProfile.baseAlgorithm" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm">
                      <option value="DEFAULT">Default (Auto)</option>
                      <option value="CVRPTW">CVRPTW (Capacitated + Time Windows)</option>
                      <option value="OVRP">Open VRP (No return to depot)</option>
                   </select>
                </div>
            </div>

            <div class="grid grid-cols-2 gap-4 mt-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700">Construction Heuristic</label>
                  <select v-model="selectedProfile.constructionHeuristicType" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm">
                    <option value="FIRST_FIT">First Fit</option>
                    <option value="FIRST_FIT_DECREASING">First Fit Decreasing</option>
                    <option value="CHEAPEST_INSERTION">Cheapest Insertion</option>
                    <option value="ALLOCATE_ENTITY_FROM_QUEUE">Allocate Entity From Queue</option>
                  </select>
                </div>
                 <div>
                  <label class="block text-sm font-medium text-gray-700">Local Search</label>
                  <select v-model="selectedProfile.localSearchType" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm">
                    <option value="TABU_SEARCH">Tabu Search</option>
                    <option value="HILL_CLIMBING">Hill Climbing</option>
                    <option value="LATE_ACCEPTANCE">Late Acceptance</option>
                    <option value="SIMULATED_ANNEALING">Simulated Annealing</option>
                  </select>
                </div>
            </div>
          </div>

          <div class="border-t border-gray-200 pt-4">
             <h3 class="text-lg font-medium text-gray-900 mb-3">Constraints & Weights (JSON)</h3>
             <textarea v-model="selectedProfile.constraintsJson" rows="5" class="shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md font-mono" placeholder='{ "capacity": "HARD", "timeWindow": "HARD", "minimizeDistance": "SOFT" }'></textarea>
          </div>
          
           <div class="border-t border-gray-200 pt-4">
             <details>
               <summary class="text-sm font-medium text-blue-600 cursor-pointer">Advanced: XML Override</summary>
               <div class="mt-2">
                 <p class="text-xs text-gray-500 mb-2">Paste full Timefold XML config here to override all settings above.</p>
                 <textarea v-model="selectedProfile.solverConfigXml" rows="8" class="shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md font-mono"></textarea>
               </div>
             </details>
          </div>

          <div class="flex justify-end pt-4">
            <button type="submit" class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded shadow-sm">
              Save Profile
            </button>
          </div>
        </form>
      </div>
      
      <div v-else class="col-span-2 flex items-center justify-center text-gray-400">
         Select a profile to edit or create a new one.
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
// import { definitionsApi } from '../../../api'; // Mocking for now

const profiles = ref([]);
const selectedProfile = ref(null);

onMounted(async () => {
    // Mock Data
    profiles.value = [
        { 
            id: '1', name: 'Standard Delivery', code: 'STD_DELIVERY', 
            baseAlgorithm: 'CVRPTW', terminationSeconds: 30,
            constructionHeuristicType: 'FIRST_FIT_DECREASING', localSearchType: 'LATE_ACCEPTANCE',
            constraintsJson: '{\n  "capacity": "HARD",\n  "timeWindow": "HARD"\n}' 
        },
        { 
            id: '2', name: 'Rush Hour (Fast)', code: 'RUSH_HOUR', 
            baseAlgorithm: 'CVRPTW', terminationSeconds: 10,
            constructionHeuristicType: 'CHEAPEST_INSERTION', localSearchType: 'HILL_CLIMBING',
             constraintsJson: '{}' 
        }
    ];
});

const selectProfile = (p) => {
    selectedProfile.value = { ...p }; // Clone
};

const createProfile = () => {
    selectedProfile.value = {
        name: 'New Profile',
        code: 'NEW_PROFILE',
        baseAlgorithm: 'DEFAULT',
        terminationSeconds: 60,
        constructionHeuristicType: 'FIRST_FIT',
        localSearchType: 'TABU_SEARCH',
        constraintsJson: '{}'
    };
};

const saveProfile = async () => {
    console.log("Saving", selectedProfile.value);
    alert("Profile saved locally (API integration pending)");
    // await definitionsApi.saveProfile(selectedProfile.value);
};
</script>
