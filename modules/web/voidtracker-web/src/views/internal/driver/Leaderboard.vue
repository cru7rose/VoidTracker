<template>
  <div class="p-6">
    <div class="mb-8 flex justify-between items-end">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Driver Leaderboard</h1>
        <p class="text-gray-500 dark:text-gray-400">Top performing drivers this week.</p>
      </div>
      <div class="flex bg-gray-100 dark:bg-gray-700 rounded-lg p-1">
        <button v-for="period in ['Weekly', 'Monthly', 'All-Time']" :key="period"
                @click="activePeriod = period"
                :class="['px-4 py-1.5 text-sm font-medium rounded-md transition-all', activePeriod === period ? 'bg-white dark:bg-gray-600 shadow text-gray-900 dark:text-white' : 'text-gray-500 dark:text-gray-400 hover:text-gray-700']">
          {{ period }}
        </button>
      </div>
    </div>

    <!-- Top 3 Podium -->
    <div class="grid grid-cols-3 gap-4 mb-10 items-end max-w-3xl mx-auto">
      <!-- 2nd Place -->
      <div class="flex flex-col items-center">
        <div class="w-20 h-20 rounded-full border-4 border-gray-300 bg-gray-200 flex items-center justify-center text-2xl mb-3 overflow-hidden">
             <span class="text-gray-500 font-bold">2</span>
        </div>
        <div class="text-center">
          <div class="font-bold text-gray-900 dark:text-white">{{ drivers[1].name }}</div>
          <div class="text-sm text-gray-500">{{ drivers[1].score }} pts</div>
        </div>
        <div class="h-24 w-full bg-gray-300/20 rounded-t-xl mt-2"></div>
      </div>

      <!-- 1st Place -->
      <div class="flex flex-col items-center">
        <div class="text-yellow-500 mb-2">
            <svg class="w-8 h-8" fill="currentColor" viewBox="0 0 20 20"><path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" /></svg>
        </div>
        <div class="w-24 h-24 rounded-full border-4 border-yellow-400 bg-yellow-100 flex items-center justify-center text-3xl mb-3 overflow-hidden shadow-lg">
             <span class="text-yellow-600 font-bold">1</span>
        </div>
        <div class="text-center">
          <div class="font-bold text-lg text-gray-900 dark:text-white">{{ drivers[0].name }}</div>
          <div class="text-sm text-yellow-600 font-bold">{{ drivers[0].score }} pts</div>
        </div>
        <div class="h-32 w-full bg-yellow-400/20 rounded-t-xl mt-2"></div>
      </div>

      <!-- 3rd Place -->
      <div class="flex flex-col items-center">
        <div class="w-20 h-20 rounded-full border-4 border-orange-300 bg-orange-100 flex items-center justify-center text-2xl mb-3 overflow-hidden">
             <span class="text-orange-600 font-bold">3</span>
        </div>
        <div class="text-center">
          <div class="font-bold text-gray-900 dark:text-white">{{ drivers[2].name }}</div>
          <div class="text-sm text-gray-500">{{ drivers[2].score }} pts</div>
        </div>
        <div class="h-16 w-full bg-orange-300/20 rounded-t-xl mt-2"></div>
      </div>
    </div>

    <!-- List -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700 overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
        <thead class="bg-gray-50 dark:bg-gray-900">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Rank</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Driver</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Score</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Trend</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200 dark:divide-gray-700">
          <tr v-for="(driver, idx) in drivers.slice(3)" :key="driver.id" class="hover:bg-gray-50 dark:hover:bg-gray-700/50">
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-500">#{{ idx + 4 }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-white">{{ driver.name }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ driver.score }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">
                <span v-if="driver.trend > 0" class="text-green-600 flex items-center">
                    <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" /></svg>
                    +{{ driver.trend }}
                </span>
                <span v-else class="text-red-500 flex items-center">
                    <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 17h8m0 0V9m0 8l-8-8-4 4-6-6" /></svg>
                    {{ driver.trend }}
                </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const activePeriod = ref('Weekly');

// Mock Data
const drivers = ref([
  { id: 1, name: 'Michael Schumacher', score: 985, trend: 12 },
  { id: 2, name: 'Lewis Hamilton', score: 972, trend: 5 },
  { id: 3, name: 'Max Verstappen', score: 968, trend: -2 },
  { id: 4, name: 'Fernando Alonso', score: 945, trend: 8 },
  { id: 5, name: 'Sebastian Vettel', score: 930, trend: 0 },
  { id: 6, name: 'Charles Leclerc', score: 915, trend: -5 },
]);
</script>
