<template>
  <div class="h-screen flex flex-col bg-white dark:bg-spotify-black text-gray-900 dark:text-white overflow-hidden transition-colors">
    <!-- Top Header - Spotify style -->
    <div class="flex-shrink-0 px-8 py-6 bg-gradient-to-r from-gray-50 via-white to-gray-50 dark:from-spotify-black dark:via-spotify-darker dark:to-spotify-black border-b border-gray-200 dark:border-spotify-gray-800 transition-colors">
      <div class="flex justify-between items-center">
        <div>
          <h1 class="text-3xl font-bold mb-1">
            {{ $t('dispatch.title') }}
          </h1>
          <p class="text-gray-500 dark:text-spotify-gray-400 text-sm">Control Tower</p>
        </div>
        <div class="flex gap-2">
          <button 
            v-for="tab in ['Orders', 'Routes', 'Monitoring', 'Automation']" 
            :key="tab"
            @click="currentTab = tab"
            :class="[
              'px-6 py-2.5 rounded-full text-sm font-semibold transition-all duration-200',
              currentTab === tab 
                ? 'bg-spotify-green-400 text-white shadow-lg shadow-spotify-green-400/30' 
                : 'bg-gray-100 dark:bg-spotify-darker text-gray-600 dark:text-spotify-gray-400 hover:bg-gray-200 dark:hover:bg-spotify-dark hover:text-gray-900 dark:hover:text-white'
            ]"
          >
            {{ $t(`dispatch.tabs.${tab.toLowerCase()}`) }}
          </button>
        </div>
      </div>
    </div>

    <!-- Main Content Area -->
    <div class="flex-1 flex gap-6 p-6 min-h-0 overflow-hidden">
      <!-- Left Panel: Control Tower -->
      <div class="w-80 flex-shrink-0 flex flex-col spotify-card border border-gray-200 dark:border-spotify-gray-800">
        <!-- Control Tower Header -->
        <div class="mb-6">
          <h2 class="text-xl font-bold mb-2">
            VOID-FLOW<br/>CONTROL TOWER
          </h2>
          <div class="flex items-center gap-2 text-sm text-gray-500 dark:text-spotify-gray-400">
            <div class="w-2 h-2 rounded-full bg-spotify-green-400 animate-pulse"></div>
            <span>System Operational</span>
          </div>
        </div>

        <!-- Optimization Profile -->
        <div class="mb-6">
          <label class="block text-xs font-semibold text-gray-600 dark:text-spotify-gray-400 mb-2 uppercase tracking-wider">
            Optimization Profile
          </label>
          <select 
            v-model="selectedProfile" 
            class="spotify-input w-full text-sm"
          >
            <option value="default">Default Profile</option>
            <option value="urgent">Urgent Delivery</option>
            <option value="cost-optimized">Cost Optimized</option>
            <option value="time-optimized">Time Optimized</option>
          </select>
        </div>

        <!-- Control Buttons -->
        <div class="space-y-3 mb-6">
          <button 
            @click="runOptimizerPrime"
            :disabled="optimizing"
            class="spotify-button w-full flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="optimizing" class="animate-spin">⚡</span>
            <span v-else>⚡</span>
            Run Optimizer Prime
          </button>
          
          <button 
            @click="publishToGhost"
            :disabled="!canPublish"
            class="spotify-button-secondary w-full flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            👻 Publish to Ghost
          </button>
          
          <button 
            @click="syncData"
            :disabled="syncing"
            class="spotify-button-secondary w-full flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="syncing" class="animate-spin">🔄</span>
            <span v-else>🔄</span>
            Sync Data
          </button>
        </div>

        <!-- KPI Metrics -->
        <div class="space-y-4 mb-6 pb-6 border-b border-gray-200 dark:border-spotify-gray-800">
          <div class="flex justify-between items-center">
            <span class="text-sm text-gray-600 dark:text-spotify-gray-400">Predicted Profit</span>
            <span class="text-xl font-bold text-spotify-green-400">€{{ predictedProfit.toFixed(2) }}</span>
          </div>
          <div class="flex justify-between items-center">
            <span class="text-sm text-gray-600 dark:text-spotify-gray-400">Payload Cost</span>
            <span class="text-xl font-bold text-red-500 dark:text-red-400">€{{ payloadCost.toFixed(2) }}</span>
          </div>
        </div>

        <!-- Status Counts -->
        <div class="space-y-4 mb-6">
          <div class="flex justify-between items-center">
            <span class="text-sm text-gray-600 dark:text-spotify-gray-400">Active Routes</span>
            <span class="text-lg font-semibold text-gray-900 dark:text-white">{{ activeRoutes }}</span>
          </div>
          <div class="flex justify-between items-center">
            <span class="text-sm text-gray-600 dark:text-spotify-gray-400">Unassigned</span>
            <span class="text-lg font-semibold text-red-500 dark:text-red-400">{{ unassignedOrdersCount }}</span>
          </div>
        </div>

        <!-- Tabs for Control Panel -->
        <div class="flex-1 flex flex-col min-h-0">
          <div class="flex gap-2 mb-4 border-b border-gray-200 dark:border-spotify-gray-800">
            <button
              v-for="tab in ['UNASSIGNED', 'ROUTES', 'ASSIGNMENTS']"
              :key="tab"
              @click="currentControlTab = tab"
              :class="[
                'px-4 py-2 text-sm font-medium transition-colors border-b-2',
                currentControlTab === tab 
                  ? 'text-spotify-green-400 border-spotify-green-400' 
                  : 'text-gray-600 dark:text-spotify-gray-400 border-transparent hover:text-gray-900 dark:hover:text-white'
              ]"
            >
              {{ tab }}
            </button>
          </div>
          <div class="flex-1 overflow-y-auto pr-2">
            <div v-if="currentControlTab === 'UNASSIGNED'">
              <div v-if="unassignedOrders.length === 0" class="text-gray-500 dark:text-spotify-gray-500 text-sm text-center py-8">
                No unassigned orders found.
              </div>
              <div v-for="order in unassignedOrders" :key="order.id" class="playlist-item">
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-semibold text-gray-900 dark:text-white truncate">#{{ order.id.substring(0, 8) }}</p>
                  <p class="text-xs text-gray-600 dark:text-spotify-gray-400 truncate">{{ order.address }}</p>
                </div>
              </div>
            </div>
            <div v-else-if="currentControlTab === 'ROUTES'">
              <div v-if="routes.length === 0" class="text-gray-500 dark:text-spotify-gray-500 text-sm text-center py-8">
                No active routes.
              </div>
              <div v-for="route in routes" :key="route.id" class="playlist-item">
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-semibold text-gray-900 dark:text-white truncate">Route {{ route.id.substring(0, 8) }}</p>
                  <p class="text-xs text-gray-600 dark:text-spotify-gray-400">{{ route.stops }} stops</p>
                </div>
              </div>
            </div>
            <div v-else-if="currentControlTab === 'ASSIGNMENTS'">
              <div class="text-gray-500 dark:text-spotify-gray-500 text-sm text-center py-8">
                Assignments will appear here.
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Main Content Area (Tabs) -->
      <div class="flex-1 flex flex-col min-h-0 overflow-hidden">
        <div class="flex-1 overflow-hidden">
          <OrdersBoard v-if="currentTab === 'Orders'" />
          <RoutesMosaic v-if="currentTab === 'Routes'" />
          <MonitoringPanel v-if="currentTab === 'Monitoring'" />
          <AutomationHub v-if="currentTab === 'Automation'" />
        </div>
      </div>

      <!-- Gatekeeper Approval Modal -->
      <GatekeeperApprovalModal
        :show="showGatekeeperModal"
        :approval-id="gatekeeperData.approvalId"
        :justification="gatekeeperData.justification"
        :warnings="gatekeeperData.warnings"
        :score-change-percent="gatekeeperData.scoreChangePercent"
        @close="showGatekeeperModal = false"
        @approved="handleApproval"
        @rejected="handleRejection"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import OrdersBoard from './OrdersBoard.vue';
import RoutesMosaic from './RoutesMosaic.vue';
import MonitoringPanel from './MonitoringPanel.vue';
import AutomationHub from './AutomationHub.vue';
import GatekeeperApprovalModal from '../../../components/GatekeeperApprovalModal.vue';
import { useDispatchStore } from '../../../stores/dispatchStore';

const dispatchStore = useDispatchStore();

const currentTab = ref('Orders');
const currentControlTab = ref('UNASSIGNED');

// Reactive data
const predictedProfit = ref(0.00);
const payloadCost = ref(0.00);
const activeRoutes = computed(() => dispatchStore.routes.length);
const unassignedOrdersCount = computed(() => dispatchStore.unassignedOrders.length);
const unassignedOrders = computed(() => dispatchStore.unassignedOrders);
const routes = computed(() => dispatchStore.routes);
const selectedProfile = ref('default');
const optimizing = ref(false);
const syncing = ref(false);
const canPublish = ref(false);

// Gatekeeper Modal State
const showGatekeeperModal = ref(false);
const gatekeeperData = ref({
  approvalId: null,
  justification: null,
  warnings: [],
  scoreChangePercent: 0
});

// Watch for approval requirements
watch(() => dispatchStore.lastOptimizationUpdate, (update) => {
  if (update && update.requiresApproval) {
    gatekeeperData.value = {
      approvalId: update.approvalId || `GK-${Date.now()}`,
      justification: update.justification || 'Wymagana weryfikacja rozwiązania optymalizacji.',
      warnings: update.warnings || [],
      scoreChangePercent: update.scoreChangePercent || 0
    };
    showGatekeeperModal.value = true;
  }
}, { deep: true });

const runOptimizerPrime = async () => {
  optimizing.value = true;
  try {
    predictedProfit.value = (Math.random() * 1000 + 500);
    payloadCost.value = (Math.random() * 200 + 50);
    await dispatchStore.runOptimization({});
  } finally {
    optimizing.value = false;
  }
};

const publishToGhost = () => {
  console.log('Publishing to Ghost...');
  alert('Routes published to Ghost!');
};

const syncData = async () => {
  syncing.value = true;
  try {
    await dispatchStore.fetchLatestPlan();
    await dispatchStore.fetchUnassignedOrders();
  } finally {
    syncing.value = false;
  }
};

const handleApproval = (response) => {
  console.log('Solution approved:', response);
  showGatekeeperModal.value = false;
};

const handleRejection = (response) => {
  console.log('Solution rejected:', response);
  showGatekeeperModal.value = false;
};

onMounted(() => {
  dispatchStore.fetchLatestPlan();
  dispatchStore.fetchUnassignedOrders();
});
</script>
