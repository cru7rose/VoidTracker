<template>
  <div v-if="show" class="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm">
    <div class="w-full max-w-2xl mx-4 bg-void-darker border border-void-gray/30 rounded-lg shadow-2xl overflow-hidden">
      <!-- Header -->
      <div class="p-6 border-b border-white/10 bg-gradient-to-r from-void-amber-500/10 to-transparent">
        <div class="flex items-center justify-between">
          <div>
            <h2 class="text-2xl font-bold text-void-amber-400 flex items-center gap-2">
              <span class="material-icons">shield</span>
              Gatekeeper Approval Required
            </h2>
            <p class="text-sm text-gray-400 mt-1">Optymalizacja wymaga zatwierdzenia przed publikacją</p>
          </div>
          <button @click="close" class="text-gray-400 hover:text-white transition">
            <span class="material-icons">close</span>
          </button>
        </div>
      </div>

      <!-- Content -->
      <div class="p-6 space-y-6 max-h-[60vh] overflow-y-auto">
        <!-- Score Change Warning -->
        <div class="bg-void-amber-500/10 border border-void-amber-500/30 rounded-lg p-4">
          <div class="flex items-center gap-2 mb-2">
            <span class="material-icons text-void-amber-400">warning</span>
            <span class="font-bold text-void-amber-400">Znacząca zmiana wyniku</span>
          </div>
          <p class="text-white text-lg font-mono">
            {{ scoreChange > 0 ? '+' : '' }}{{ scoreChange.toFixed(2) }}%
          </p>
        </div>

        <!-- LLM Justification -->
        <div v-if="justification" class="bg-void-gray/20 border border-white/10 rounded-lg p-4">
          <h3 class="text-sm font-bold text-gray-400 mb-2 uppercase tracking-wider">Uzasadnienie AI</h3>
          <p class="text-gray-300 leading-relaxed">{{ justification }}</p>
        </div>

        <!-- Warnings List -->
        <div v-if="warnings && warnings.length > 0" class="space-y-2">
          <h3 class="text-sm font-bold text-gray-400 uppercase tracking-wider">Ostrzeżenia</h3>
          <ul class="space-y-2">
            <li v-for="(warning, idx) in warnings" :key="idx" 
                class="flex items-start gap-2 text-sm text-gray-300 bg-void-gray/10 rounded p-2">
              <span class="material-icons text-void-amber-400 text-sm">info</span>
              <span>{{ warning }}</span>
            </li>
          </ul>
        </div>

        <!-- Solution Details -->
        <div class="grid grid-cols-2 gap-4 text-sm">
          <div>
            <span class="text-gray-500">Solution ID:</span>
            <span class="text-gray-300 ml-2 font-mono">{{ solutionId }}</span>
          </div>
          <div>
            <span class="text-gray-500">Vehicles:</span>
            <span class="text-gray-300 ml-2">{{ vehicleCount }}</span>
          </div>
        </div>

        <!-- Rejection Reason Input (if rejecting) -->
        <div v-if="showRejectInput" class="space-y-2">
          <label class="block text-sm font-bold text-gray-400 uppercase tracking-wider">
            Powód odrzucenia
          </label>
          <textarea
            v-model="rejectionReason"
            placeholder="Wyjaśnij dlaczego odrzucasz tę optymalizację..."
            class="w-full bg-void-darker border border-white/10 rounded p-3 text-gray-300 placeholder-gray-600 focus:ring-2 focus:ring-void-pink-500 focus:border-void-pink-500"
            rows="3"
          ></textarea>
        </div>
      </div>

      <!-- Footer Actions -->
      <div class="border-t border-white/10 p-6 bg-void-gray/30 flex justify-between items-center">
        <button 
          @click="close" 
          class="px-6 py-2 bg-white/5 border border-white/10 text-gray-300 rounded hover:bg-white/10 transition"
        >
          Cancel
        </button>
        
        <div class="flex gap-3">
          <button 
            v-if="!showRejectInput"
            @click="showRejectInput = true" 
            class="px-6 py-2 bg-red-500/10 border border-red-500/30 text-red-400 rounded hover:bg-red-500/20 transition flex items-center gap-2"
          >
            <span class="material-icons text-sm">close</span>
            Reject
          </button>
          
          <button 
            v-if="showRejectInput"
            @click="handleReject" 
            :disabled="!rejectionReason || saving"
            class="px-6 py-2 bg-red-500/20 border border-red-500/50 text-red-400 rounded hover:bg-red-500/30 transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
          >
            <span class="material-icons text-sm">block</span>
            {{ saving ? 'Rejecting...' : 'Confirm Rejection' }}
          </button>
          
          <button 
            @click="handleApprove" 
            :disabled="saving || showRejectInput"
            class="px-6 py-2 bg-void-cyan-500/20 border border-void-cyan-500/50 text-void-cyan-400 rounded hover:bg-void-cyan-500/30 transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
          >
            <span class="material-icons text-sm">check_circle</span>
            {{ saving ? 'Approving...' : 'Approve & Publish' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useGatekeeperStore } from '../stores/gatekeeperStore';

const props = defineProps({
  show: Boolean,
  solutionId: String,
  scoreChange: Number,
  justification: String,
  warnings: Array,
  vehicleCount: Number
});

const emit = defineEmits(['close', 'approved', 'rejected']);

const gatekeeperStore = useGatekeeperStore();
const saving = ref(false);
const showRejectInput = ref(false);
const rejectionReason = ref('');

function close() {
  showRejectInput.value = false;
  rejectionReason.value = '';
  emit('close');
}

async function handleApprove() {
  saving.value = true;
  try {
    await gatekeeperStore.approveSolution(props.solutionId);
    emit('approved', props.solutionId);
    close();
  } catch (e) {
    alert('Failed to approve solution: ' + (e.response?.data?.error || e.message));
  } finally {
    saving.value = false;
  }
}

async function handleReject() {
  if (!rejectionReason.value.trim()) {
    alert('Please provide a reason for rejection');
    return;
  }
  
  saving.value = true;
  try {
    await gatekeeperStore.rejectSolution(props.solutionId, rejectionReason.value);
    emit('rejected', props.solutionId);
    close();
  } catch (e) {
    alert('Failed to reject solution: ' + (e.response?.data?.error || e.message));
  } finally {
    saving.value = false;
  }
}
</script>
