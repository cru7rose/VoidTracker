<template>
  <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700 p-6">
    <h3 class="text-lg font-bold text-gray-900 dark:text-white mb-6 flex items-center gap-2">
      <svg class="w-5 h-5 text-yellow-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" /></svg>
      Driver Performance Scorecard
    </h3>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
      <!-- Safety Score -->
      <div class="p-4 bg-green-50 dark:bg-green-900/20 rounded-xl border border-green-100 dark:border-green-800">
        <div class="text-sm text-green-600 dark:text-green-400 font-medium mb-1">Safety Score</div>
        <div class="text-3xl font-bold text-green-700 dark:text-green-300">{{ stats.safetyScore }}<span class="text-sm font-normal text-gray-500">/100</span></div>
        <div class="mt-2 w-full bg-green-200 dark:bg-green-800 rounded-full h-2">
          <div class="bg-green-500 h-2 rounded-full" :style="{ width: stats.safetyScore + '%' }"></div>
        </div>
      </div>

      <!-- On-Time Delivery -->
      <div class="p-4 bg-blue-50 dark:bg-blue-900/20 rounded-xl border border-blue-100 dark:border-blue-800">
        <div class="text-sm text-blue-600 dark:text-blue-400 font-medium mb-1">On-Time Delivery</div>
        <div class="text-3xl font-bold text-blue-700 dark:text-blue-300">{{ stats.onTimeRate }}%</div>
        <div class="mt-2 w-full bg-blue-200 dark:bg-blue-800 rounded-full h-2">
          <div class="bg-blue-500 h-2 rounded-full" :style="{ width: stats.onTimeRate + '%' }"></div>
        </div>
      </div>

      <!-- Customer Rating -->
      <div class="p-4 bg-purple-50 dark:bg-purple-900/20 rounded-xl border border-purple-100 dark:border-purple-800">
        <div class="text-sm text-purple-600 dark:text-purple-400 font-medium mb-1">Customer Rating</div>
        <div class="text-3xl font-bold text-purple-700 dark:text-purple-300">{{ stats.rating }}<span class="text-sm font-normal text-gray-500">/5.0</span></div>
        <div class="flex gap-1 mt-2">
          <svg v-for="i in 5" :key="i" :class="['w-4 h-4', i <= Math.round(stats.rating) ? 'text-purple-500' : 'text-gray-300']" fill="currentColor" viewBox="0 0 20 20"><path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" /></svg>
        </div>
      </div>
    </div>

    <!-- Recent Feedback -->
    <h4 class="text-sm font-semibold text-gray-900 dark:text-white mb-3">Recent Feedback</h4>
    <div class="space-y-3">
      <div v-for="feedback in recentFeedback" :key="feedback.id" class="flex gap-3 p-3 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
        <div :class="['w-1 h-full rounded-full', feedback.positive ? 'bg-green-500' : 'bg-red-500']"></div>
        <div>
          <p class="text-sm text-gray-900 dark:text-white font-medium">{{ feedback.comment }}</p>
          <p class="text-xs text-gray-500">{{ feedback.date }} • {{ feedback.customer }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

// Mock Data - In real app, props would pass this in
const stats = ref({
  safetyScore: 94,
  onTimeRate: 98.5,
  rating: 4.8
});

const recentFeedback = ref([
  { id: 1, positive: true, comment: "Driver was very polite and helpful!", date: "2 hours ago", customer: "Acme Corp" },
  { id: 2, positive: true, comment: "Perfect delivery, right on time.", date: "Yesterday", customer: "John Doe" },
  { id: 3, positive: false, comment: "Package was left in the rain.", date: "3 days ago", customer: "Jane Smith" }
]);
</script>
