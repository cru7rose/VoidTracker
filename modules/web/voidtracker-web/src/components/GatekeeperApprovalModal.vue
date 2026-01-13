<template>
  <div v-if="show" class="fixed inset-0 z-[9999] flex items-center justify-center bg-black/80 backdrop-blur-sm">
    <div class="bg-void-black border-2 border-void-cyan-600 rounded-lg shadow-2xl w-full max-w-2xl mx-4">
      <!-- Header -->
      <div class="p-6 border-b border-void-cyan-900">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="w-12 h-12 rounded-full bg-void-cyan-900 flex items-center justify-center">
              <span class="text-2xl">🤖</span>
            </div>
            <div>
              <h2 class="text-xl font-bold text-void-cyan-300">Gatekeeper - Wymagana Weryfikacja</h2>
              <p class="text-sm text-void-cyan-500">AI Agent wykrył znaczącą zmianę w optymalizacji</p>
            </div>
          </div>
          <button 
            @click="close"
            class="text-void-cyan-500 hover:text-void-cyan-300 transition-colors"
          >
            ✕
          </button>
        </div>
      </div>

      <!-- Content -->
      <div class="p-6 space-y-4">
        <!-- AI Justification -->
        <div class="bg-void-cyan-950 border border-void-cyan-800 rounded-lg p-4">
          <div class="flex items-start gap-3">
            <span class="text-2xl">💡</span>
            <div class="flex-1">
              <h3 class="font-bold text-void-cyan-300 mb-2">Uzasadnienie AI Agent:</h3>
              <p class="text-sm text-void-cyan-400 whitespace-pre-wrap">{{ justification || 'Generowanie uzasadnienia...' }}</p>
            </div>
          </div>
        </div>

        <!-- Warnings -->
        <div v-if="warnings && warnings.length > 0" class="space-y-2">
          <h3 class="font-bold text-void-cyan-300 text-sm">Ostrzeżenia:</h3>
          <ul class="space-y-1">
            <li v-for="(warning, idx) in warnings" :key="idx" 
                class="text-sm text-void-cyan-400 flex items-start gap-2">
              <span class="text-yellow-500">⚠️</span>
              <span>{{ warning }}</span>
            </li>
          </ul>
        </div>

        <!-- Metrics -->
        <div class="grid grid-cols-2 gap-4 pt-2">
          <div class="bg-void-cyan-950 rounded p-3">
            <div class="text-xs text-void-cyan-500 mb-1">Zmiana Wyniku</div>
            <div class="text-lg font-bold" :class="scoreChangePercent > 0 ? 'text-red-400' : 'text-green-400'">
              {{ scoreChangePercent > 0 ? '+' : '' }}{{ scoreChangePercent.toFixed(2) }}%
            </div>
          </div>
          <div class="bg-void-cyan-950 rounded p-3">
            <div class="text-xs text-void-cyan-500 mb-1">Status</div>
            <div class="text-lg font-bold text-void-cyan-300">Wymaga Zatwierdzenia</div>
          </div>
        </div>

        <!-- User Comment -->
        <div>
          <label class="block text-sm font-medium text-void-cyan-300 mb-2">
            Komentarz (opcjonalnie):
          </label>
          <textarea 
            v-model="userComment"
            class="w-full px-3 py-2 bg-void-black border border-void-cyan-800 rounded text-void-cyan-300 text-sm focus:border-void-cyan-600 focus:outline-none"
            rows="2"
            placeholder="Dodaj komentarz do decyzji..."
          ></textarea>
        </div>
      </div>

      <!-- Actions -->
      <div class="p-6 border-t border-void-cyan-900 flex gap-3">
        <button 
          @click="reject"
          :disabled="processing"
          class="flex-1 px-4 py-2 bg-red-900/50 border border-red-600 text-red-300 rounded hover:bg-red-900/70 disabled:opacity-50 transition-colors font-bold"
        >
          ❌ Odrzuć
        </button>
        <button 
          @click="approve"
          :disabled="processing"
          class="flex-1 px-4 py-2 bg-green-900/50 border border-green-600 text-green-300 rounded hover:bg-green-900/70 disabled:opacity-50 transition-colors font-bold"
        >
          ✅ Zatwierdź i Publikuj
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { planningApi } from '../api/axios';

const props = defineProps({
  show: Boolean,
  approvalId: String,
  justification: String,
  warnings: Array,
  scoreChangePercent: Number
});

const emit = defineEmits(['close', 'approved', 'rejected']);

const userComment = ref('');
const processing = ref(false);

const approve = async () => {
  processing.value = true;
  try {
    const response = await planningApi.post('/gatekeeper/approve', {
      approvalId: props.approvalId,
      approved: true,
      comment: userComment.value
    });
    
    emit('approved', response.data);
    close();
  } catch (e) {
    console.error('Approval failed', e);
    alert('Nie udało się zatwierdzić rozwiązania');
  } finally {
    processing.value = false;
  }
};

const reject = async () => {
  processing.value = true;
  try {
    const response = await planningApi.post('/gatekeeper/approve', {
      approvalId: props.approvalId,
      approved: false,
      comment: userComment.value
    });
    
    emit('rejected', response.data);
    close();
  } catch (e) {
    console.error('Rejection failed', e);
    alert('Nie udało się odrzucić rozwiązania');
  } finally {
    processing.value = false;
  }
};

const close = () => {
  emit('close');
};
</script>
