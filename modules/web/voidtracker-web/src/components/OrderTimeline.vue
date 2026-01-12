<template>
  <div class="space-y-4">
    <div class="flex items-center space-x-4">
      <div v-for="(step, index) in steps" :key="index" class="flex items-center">
        <div class="flex flex-col items-center">
          <div 
            :class="[
              'w-10 h-10 rounded-full flex items-center justify-center text-sm font-semibold',
              step.completed ? 'bg-success-500 text-white' : 
              step.current ? 'bg-primary-500 text-white' : 
              'bg-gray-200 text-gray-600'
            ]"
          >
            <svg v-if="step.completed" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
            <span v-else>{{ index + 1 }}</span>
          </div>
          <div class="mt-2 text-center">
            <p class="text-xs font-medium text-gray-900">{{ step.label }}</p>
            <p v-if="step.timestamp" class="text-xs text-gray-500">{{ formatTime(step.timestamp) }}</p>
          </div>
        </div>
        
        <div 
          v-if="index < steps.length - 1" 
          :class="[
            'w-16 h-1 mx-2',
            steps[index + 1].completed ? 'bg-success-500' : 'bg-gray-200'
          ]"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { format } from 'date-fns';

const props = defineProps({
  status: {
    type: String,
    required: true
  },
  timestamps: {
    type: Object,
    default: () => ({})
  }
});

const statusSteps = {
  'INGESTED': 1,
  'PLANNED': 2,
  'OUT_FOR_DELIVERY': 3,
  'DELIVERED': 4,
  'EXCEPTION': 0
};

const steps = computed(() => {
  const currentStep = statusSteps[props.status] || 0;
  
  return [
    {
      label: 'Received',
      completed: currentStep >= 1,
      current: currentStep === 1,
      timestamp: props.timestamps.ingested
    },
    {
      label: 'Planned',
      completed: currentStep >= 2,
      current: currentStep === 2,
      timestamp: props.timestamps.planned
    },
    {
      label: 'In Transit',
      completed: currentStep >= 3,
      current: currentStep === 3,
      timestamp: props.timestamps.outForDelivery
    },
    {
      label: 'Delivered',
      completed: currentStep >= 4,
      current: currentStep === 4,
      timestamp: props.timestamps.delivered
    }
  ];
});

function formatTime(timestamp) {
  if (!timestamp) return '';
  return format(new Date(timestamp), 'MMM dd, HH:mm');
}
</script>
