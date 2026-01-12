<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 flex flex-col items-center justify-center p-4 font-inter">
    <div class="max-w-md w-full bg-white dark:bg-gray-800 rounded-xl shadow-lg p-8 border border-gray-100 dark:border-gray-700">
      <div class="text-center mb-8">
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white mb-2">Welcome to {{ configStore.config.general.systemName }}</h1>
        <p class="text-gray-500 dark:text-gray-400">Please complete the following steps to activate your driver account.</p>
      </div>

      <div class="space-y-6">
        <div v-for="(step, index) in steps" :key="index" 
             :class="['p-4 rounded-lg border transition-all', 
                      step.completed ? 'bg-green-50 border-green-200 dark:bg-green-900/20 dark:border-green-800' : 'bg-white border-gray-200 dark:bg-gray-700 dark:border-gray-600']">
          
          <div class="flex items-start gap-3">
            <div :class="['w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold flex-shrink-0 mt-0.5', 
                          step.completed ? 'bg-green-500 text-white' : 'bg-gray-200 text-gray-600 dark:bg-gray-600 dark:text-gray-300']">
              <span v-if="step.completed">✓</span>
              <span v-else>{{ index + 1 }}</span>
            </div>
            <div class="flex-1">
              <h3 class="font-medium text-gray-900 dark:text-white">{{ step.label }}</h3>
              
              <!-- Upload Step -->
              <div v-if="step.type === 'upload' && !step.completed" class="mt-3">
                <label class="block w-full px-4 py-2 border-2 border-dashed border-gray-300 dark:border-gray-500 rounded-lg text-center cursor-pointer hover:border-blue-500 transition-colors">
                  <span class="text-sm text-gray-500 dark:text-gray-400">Click to upload file</span>
                  <input type="file" class="hidden" @change="handleUpload($event, index)">
                </label>
              </div>

              <!-- Video Step -->
              <div v-if="step.type === 'video' && !step.completed" class="mt-3">
                <div class="aspect-video bg-black rounded-lg flex items-center justify-center text-white mb-2">
                    <!-- Mock Video Player -->
                    <span>▶️ Video Player ({{ step.url }})</span>
                </div>
                <button @click="completeStep(index)" class="text-sm text-blue-600 hover:text-blue-800 font-medium">
                    I have watched the video
                </button>
              </div>

              <!-- Checkbox Step -->
              <div v-if="step.type === 'checkbox' && !step.completed" class="mt-3">
                <label class="flex items-center gap-2 cursor-pointer">
                    <input type="checkbox" @change="completeStep(index)" class="w-4 h-4 text-blue-600 rounded border-gray-300 focus:ring-blue-500">
                    <span class="text-sm text-gray-600 dark:text-gray-300">I agree</span>
                </label>
              </div>

              <div v-if="step.completed" class="mt-1 text-xs text-green-600 dark:text-green-400 font-medium">
                Completed
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="mt-8">
        <button @click="finishOnboarding" 
                :disabled="!allCompleted"
                class="w-full py-3 px-4 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-lg shadow-sm transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
          Start Driving
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useConfigStore } from '@/stores/configStore';

const router = useRouter();
const configStore = useConfigStore();

const steps = ref([]);

onMounted(() => {
    // Load steps from config and add 'completed' state
    const configSteps = configStore.config.onboarding?.steps || [];
    steps.value = configSteps.map(s => ({ ...s, completed: false }));
});

const allCompleted = computed(() => {
    return steps.value.every(s => s.completed);
});

const handleUpload = (event, index) => {
    const file = event.target.files[0];
    if (file) {
        // Mock upload
        setTimeout(() => {
            steps.value[index].completed = true;
        }, 500);
    }
};

const completeStep = (index) => {
    steps.value[index].completed = true;
};

const finishOnboarding = () => {
    // In real app, save progress to backend
    router.push('/driver/tasks');
};
</script>
