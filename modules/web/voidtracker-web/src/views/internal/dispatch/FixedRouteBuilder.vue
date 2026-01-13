<template>
  <div class="h-full flex flex-col bg-void-black text-void-cyan-400 overflow-hidden">
    <!-- Header: Spotify-style -->
    <div class="px-6 py-4 border-b border-void-cyan-900">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold mb-1">Kreator Tras Stałych</h1>
          <p class="text-sm text-void-cyan-300">Zbuduj trasę jak playlistę w Spotify</p>
        </div>
        <button 
          @click="saveRoute" 
          :disabled="!canSave"
          class="px-6 py-2 bg-void-cyan-600 text-void-black font-bold rounded-full hover:bg-void-cyan-500 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
        >
          💾 Zapisz Trasę
        </button>
      </div>
    </div>

    <div class="flex-1 flex min-h-0">
      <!-- Left: Map & Points Selection (60%) -->
      <div class="flex-1 flex flex-col border-r border-void-cyan-900">
        <!-- Map Container -->
        <div class="flex-1 relative bg-void-black">
          <div class="absolute inset-0 flex items-center justify-center text-void-cyan-400/50">
            <div class="text-center">
              <div class="text-4xl mb-2">🗺️</div>
              <p class="text-sm">Mapa będzie tutaj</p>
              <p class="text-xs mt-1">Kliknij na mapie aby dodać punkt</p>
            </div>
          </div>
          
          <!-- Floating Controls -->
          <div class="absolute top-4 left-4 z-10 bg-void-cyan-900/90 backdrop-blur-sm rounded-lg p-3 space-y-2">
            <button 
              @click="addPointFromMap"
              class="w-full px-3 py-2 bg-void-cyan-600 text-void-black text-sm font-bold rounded hover:bg-void-cyan-500"
            >
              ➕ Dodaj Punkt
            </button>
            <button 
              @click="importFromFile"
              class="w-full px-3 py-2 bg-void-cyan-800 text-void-cyan-300 text-sm rounded hover:bg-void-cyan-700"
            >
              📁 Import CSV
            </button>
            <button 
              @click="optimizeSequence"
              :disabled="routeStops.length < 2"
              class="w-full px-3 py-2 bg-void-cyan-800 text-void-cyan-300 text-sm rounded hover:bg-void-cyan-700 disabled:opacity-50"
            >
              ⚡ Optymalizuj Sekwencję (TSP)
            </button>
          </div>
        </div>

        <!-- Elastic Shell Configuration -->
        <div class="p-4 bg-void-cyan-950 border-t border-void-cyan-900">
          <h3 class="text-sm font-bold mb-3 text-void-cyan-300">⚙️ Elastic Shell (Milkrun)</h3>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-xs text-void-cyan-400 mb-1">Max Detour (km)</label>
              <input 
                v-model.number="routeConfig.maxDetourKm" 
                type="number" 
                min="0" 
                step="0.5"
                class="w-full px-3 py-2 bg-void-black border border-void-cyan-800 rounded text-void-cyan-300 focus:border-void-cyan-600 focus:outline-none"
                placeholder="10.0"
              />
              <p class="text-xs text-void-cyan-500 mt-1">Ile km trasa może zboczyć dla paczki z giełdy</p>
            </div>
            <div>
              <label class="block text-xs text-void-cyan-400 mb-1">Reserved Space (%)</label>
              <input 
                v-model.number="routeConfig.reservedSpacePercent" 
                type="number" 
                min="0" 
                max="100" 
                step="5"
                class="w-full px-3 py-2 bg-void-black border border-void-cyan-800 rounded text-void-cyan-300 focus:border-void-cyan-600 focus:outline-none"
                placeholder="20"
              />
              <p class="text-xs text-void-cyan-500 mt-1">Ile % auta zostawić na nagłe zlecenia</p>
            </div>
          </div>
          
          <!-- AI Suggestion -->
          <div v-if="aiSuggestion" class="mt-3 p-3 bg-void-cyan-900/50 rounded border border-void-cyan-800">
            <div class="flex items-start gap-2">
              <span class="text-lg">🤖</span>
              <div class="flex-1">
                <p class="text-xs font-bold text-void-cyan-300 mb-1">AI Suggestion</p>
                <p class="text-xs text-void-cyan-400">{{ aiSuggestion }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Right: Playlist-style Stops List (40%) -->
      <div class="w-96 flex flex-col bg-void-black">
        <div class="p-4 border-b border-void-cyan-900">
          <div class="flex items-center justify-between mb-2">
            <h3 class="font-bold text-void-cyan-300">Punkty Trasy</h3>
            <span class="text-xs text-void-cyan-500">{{ routeStops.length }} punktów</span>
          </div>
          <input 
            v-model="routeName"
            type="text"
            placeholder="Nazwa trasy (np. 'Warszawa Centrum - Poranek')"
            class="w-full px-3 py-2 bg-void-cyan-950 border border-void-cyan-800 rounded text-void-cyan-300 text-sm focus:border-void-cyan-600 focus:outline-none"
          />
        </div>

        <!-- Stops List (Playlist Style) -->
        <div class="flex-1 overflow-y-auto">
          <div v-if="routeStops.length === 0" class="p-8 text-center text-void-cyan-500">
            <div class="text-4xl mb-2">📋</div>
            <p class="text-sm">Brak punktów</p>
            <p class="text-xs mt-1">Dodaj punkty z mapy lub importu</p>
          </div>

          <div 
            v-for="(stop, index) in routeStops" 
            :key="stop.id"
            class="group flex items-center gap-3 p-3 hover:bg-void-cyan-950 border-b border-void-cyan-900 transition-colors cursor-move"
            draggable="true"
            @dragstart="onDragStart($event, index)"
            @dragover.prevent
            @drop="onDrop($event, index)"
          >
            <!-- Drag Handle -->
            <div class="text-void-cyan-600 opacity-0 group-hover:opacity-100 transition-opacity cursor-grab active:cursor-grabbing">
              ☰
            </div>
            
            <!-- Stop Number -->
            <div class="w-8 h-8 rounded-full bg-void-cyan-900 text-void-cyan-300 flex items-center justify-center text-sm font-bold flex-shrink-0">
              {{ index + 1 }}
            </div>

            <!-- Stop Info -->
            <div class="flex-1 min-w-0">
              <div class="font-medium text-void-cyan-300 text-sm truncate">
                {{ stop.name || stop.address || `Punkt ${index + 1}` }}
              </div>
              <div class="text-xs text-void-cyan-500 flex items-center gap-2 mt-0.5">
                <span>📍 {{ stop.lat?.toFixed(4) }}, {{ stop.lon?.toFixed(4) }}</span>
                <span v-if="stop.weight">⚖️ {{ stop.weight }}kg</span>
              </div>
            </div>

            <!-- Actions -->
            <div class="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
              <button 
                @click="editStop(stop)"
                class="p-1 text-void-cyan-500 hover:text-void-cyan-300"
                title="Edytuj"
              >
                ✏️
              </button>
              <button 
                @click="removeStop(index)"
                class="p-1 text-red-500 hover:text-red-400"
                title="Usuń"
              >
                🗑️
              </button>
            </div>
          </div>
        </div>

        <!-- Summary Footer -->
        <div class="p-4 border-t border-void-cyan-900 bg-void-cyan-950">
          <div class="grid grid-cols-2 gap-3 text-xs">
            <div>
              <span class="text-void-cyan-500">Dystans:</span>
              <span class="font-bold text-void-cyan-300 ml-1">{{ totalDistance.toFixed(1) }} km</span>
            </div>
            <div>
              <span class="text-void-cyan-500">Czas:</span>
              <span class="font-bold text-void-cyan-300 ml-1">{{ totalTime.toFixed(1) }} h</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue';
import { planningApi } from '../../../api/axios';

const routeName = ref('');
const routeStops = ref([]);
const aiSuggestion = ref(null);

const routeConfig = reactive({
  maxDetourKm: 10.0,
  reservedSpacePercent: 20
});

const canSave = computed(() => {
  return routeName.value.trim().length > 0 && routeStops.length > 0;
});

const totalDistance = computed(() => {
  // TODO: Calculate actual distance using routing API
  return routeStops.length * 5; // Placeholder
});

const totalTime = computed(() => {
  // TODO: Calculate actual time
  return totalDistance.value / 50; // Placeholder: 50 km/h average
});

const addPointFromMap = () => {
  // TODO: Open map picker
  const newStop = {
    id: Date.now(),
    name: `Punkt ${routeStops.value.length + 1}`,
    lat: 52.2297 + Math.random() * 0.1,
    lon: 21.0122 + Math.random() * 0.1,
    weight: null,
    volume: null
  };
  routeStops.value.push(newStop);
};

const importFromFile = () => {
  // TODO: Implement CSV import
  alert('Import CSV - w trakcie implementacji');
};

const optimizeSequence = async () => {
  if (routeStops.value.length < 2) return;
  
  try {
    // Call TSP optimization API
    const response = await planningApi.post('/routes/optimize-sequence', {
      stops: routeStops.value.map(s => ({ lat: s.lat, lon: s.lon }))
    });
    
    // Reorder stops based on optimized sequence
    const optimizedIndices = response.data.sequence;
    routeStops.value = optimizedIndices.map(i => routeStops.value[i]);
    
    // Show AI suggestion
    aiSuggestion.value = `Optymalizacja zakończona! Oszczędność: ${response.data.savings?.toFixed(1)} km`;
  } catch (e) {
    console.error('Optimization failed', e);
    alert('Nie udało się zoptymalizować sekwencji');
  }
};

const editStop = (stop) => {
  // TODO: Open edit modal
  const newName = prompt('Nazwa punktu:', stop.name);
  if (newName) stop.name = newName;
};

const removeStop = (index) => {
  routeStops.value.splice(index, 1);
};

const onDragStart = (event, index) => {
  event.dataTransfer.setData('text/plain', index);
  event.dataTransfer.effectAllowed = 'move';
};

const onDrop = (event, targetIndex) => {
  const sourceIndex = parseInt(event.dataTransfer.getData('text/plain'));
  if (sourceIndex === targetIndex) return;
  
  const [removed] = routeStops.value.splice(sourceIndex, 1);
  routeStops.value.splice(targetIndex, 0, removed);
};

const saveRoute = async () => {
  if (!canSave.value) return;
  
  try {
    const payload = {
      name: routeName.value,
      isFixedRoute: true,
      fixedStops: routeStops.value.map(s => ({
        lat: s.lat,
        lon: s.lon,
        weight: s.weight,
        volume: s.volume,
        description: s.name
      })),
      maxDetourKm: routeConfig.maxDetourKm,
      reservedSpacePercent: routeConfig.reservedSpacePercent
    };
    
    await planningApi.post('/routes/fixed/create', payload);
    alert('✅ Trasa stała zapisana!');
    
    // Reset form
    routeName.value = '';
    routeStops.value = [];
    routeConfig.maxDetourKm = 10.0;
    routeConfig.reservedSpacePercent = 20;
  } catch (e) {
    console.error('Save failed', e);
    alert('Nie udało się zapisać trasy');
  }
};
</script>
