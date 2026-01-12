<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <div class="max-w-2xl mx-auto">
      <div class="mb-8">
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Pre-Trip Inspection</h1>
        <p class="text-gray-500 dark:text-gray-400">Complete the checklist before starting your route.</p>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden">
        <div class="p-6 space-y-8">
          <!-- Dynamic Questions -->
          <div v-for="item in config.dvir.questions" :key="item.id" class="border-b border-gray-100 dark:border-gray-700 last:border-0 pb-6 last:pb-0">
            <p class="font-medium text-gray-900 dark:text-white mb-3">{{ item.label }}</p>
            
            <!-- Pass/Fail -->
            <div v-if="item.type === 'pass_fail'" class="flex gap-4">
              <button 
                @click="answers[item.id] = 'pass'"
                :class="['flex-1 py-3 px-4 rounded-lg border font-medium transition-colors flex items-center justify-center gap-2', 
                  answers[item.id] === 'pass' ? 'bg-green-50 border-green-500 text-green-700 dark:bg-green-900/20 dark:text-green-400' : 'border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700']"
              >
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
                Pass
              </button>
              <button 
                @click="answers[item.id] = 'fail'"
                :class="['flex-1 py-3 px-4 rounded-lg border font-medium transition-colors flex items-center justify-center gap-2', 
                  answers[item.id] === 'fail' ? 'bg-red-50 border-red-500 text-red-700 dark:bg-red-900/20 dark:text-red-400' : 'border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700']"
              >
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
                Fail
              </button>
            </div>

            <!-- Photo -->
            <div v-if="item.type === 'photo'" class="space-y-3">
              <div v-if="!answers[item.id]" class="border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-lg p-8 text-center cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700/50" @click="simulatePhotoUpload(item.id)">
                <svg class="w-8 h-8 mx-auto text-gray-400 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" /><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" /></svg>
                <span class="text-sm text-gray-500">Tap to take photo</span>
              </div>
              <div v-else class="relative rounded-lg overflow-hidden bg-gray-100 dark:bg-gray-800 aspect-video flex items-center justify-center">
                <span class="text-gray-500">Photo Uploaded</span>
                <button @click="answers[item.id] = null" class="absolute top-2 right-2 bg-black/50 text-white p-1 rounded-full hover:bg-black/70">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
                </button>
              </div>
            </div>

            <!-- Text -->
            <div v-if="item.type === 'text'">
              <textarea v-model="answers[item.id]" rows="3" class="block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm" placeholder="Enter details..."></textarea>
            </div>
          </div>

          <!-- Signature -->
          <div class="pt-6 border-t border-gray-200 dark:border-gray-700">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Driver Signature</label>
            <div class="border border-gray-300 dark:border-gray-600 rounded-lg h-32 bg-white dark:bg-gray-700 flex items-center justify-center text-gray-400 cursor-crosshair hover:bg-gray-50 dark:hover:bg-gray-600/50" @click="signed = true">
              <span v-if="!signed">Tap to sign</span>
              <span v-else class="font-cursive text-2xl text-blue-600 dark:text-blue-400">John Doe</span>
            </div>
          </div>

          <!-- Submit -->
          <div class="pt-4">
            <button 
              @click="submitInspection"
              :disabled="!isComplete"
              :class="['w-full py-3 px-4 rounded-lg font-bold text-white shadow-sm transition-all',
                isComplete ? 'bg-blue-600 hover:bg-blue-700' : 'bg-gray-300 dark:bg-gray-700 cursor-not-allowed']"
            >
              Submit Inspection
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useConfigStore } from '../../stores/configStore';
import { useRouter } from 'vue-router';

const config = useConfigStore();
const router = useRouter();
const answers = ref({});
const signed = ref(false);

const isComplete = computed(() => {
  const allAnswered = config.dvir.questions.every(q => answers.value[q.id]);
  return allAnswered && signed.value;
});

const simulatePhotoUpload = (id) => {
  // Simulate file selection
  setTimeout(() => {
    answers.value[id] = 'mock_photo_url';
  }, 500);
};

const submitInspection = () => {
  if (!isComplete.value) return;
  
  // Mock API call
  console.log('Submitting DVIR:', { answers: answers.value, signed: signed.value });
  
  alert('Inspection Submitted Successfully!');
  router.push('/driver/dashboard'); // Assuming this route exists, or back to home
};
</script>

<style scoped>
.font-cursive {
  font-family: 'Brush Script MT', cursive;
}
</style>
