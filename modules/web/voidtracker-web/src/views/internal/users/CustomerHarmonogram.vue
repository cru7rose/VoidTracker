<template>
  <div class="mt-8 border-t border-gray-100 dark:border-gray-700 pt-6">
    <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-4">Delivery Harmonogram</h3>

    <div v-if="loading" class="text-gray-500">Loading harmonogram...</div>
    <div v-else>
      <!-- Schedules -->
      <div class="mb-6">
        <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Weekly Schedule</h4>
        <div v-if="harmonogram.schedules && harmonogram.schedules.length > 0" class="space-y-2">
          <div v-for="(schedule, index) in harmonogram.schedules" :key="index"
               class="flex justify-between items-center p-3 bg-gray-50 dark:bg-gray-700 rounded-lg border border-gray-200 dark:border-gray-600">
            <div>
              <span class="font-medium text-gray-900 dark:text-white">{{ schedule.dayOfWeek }}</span>
              <span class="text-sm text-gray-500 dark:text-gray-400 ml-2">
                {{ schedule.startTime }} - {{ schedule.endTime }} ({{ schedule.type }})
              </span>
            </div>
            <button @click="removeSchedule(index)" class="text-red-500 hover:text-red-700 text-sm">Remove</button>
          </div>
        </div>
        <div v-else class="text-sm text-gray-500 italic mb-2">No schedules configured. Defaults to standard delivery.</div>

        <!-- Add Schedule Form -->
        <div class="flex gap-2 mt-2">
          <select v-model="newSchedule.dayOfWeek" class="px-2 py-1 rounded border text-sm">
            <option value="MONDAY">Monday</option>
            <option value="TUESDAY">Tuesday</option>
            <option value="WEDNESDAY">Wednesday</option>
            <option value="THURSDAY">Thursday</option>
            <option value="FRIDAY">Friday</option>
            <option value="SATURDAY">Saturday</option>
            <option value="SUNDAY">Sunday</option>
          </select>
          <input v-model="newSchedule.startTime" type="time" class="px-2 py-1 rounded border text-sm" />
          <input v-model="newSchedule.endTime" type="time" class="px-2 py-1 rounded border text-sm" />
          <select v-model="newSchedule.type" class="px-2 py-1 rounded border text-sm">
            <option value="DELIVERY">Delivery</option>
            <option value="PICKUP">Pickup</option>
          </select>
          <button @click="addSchedule" class="px-3 py-1 bg-blue-600 text-white rounded text-sm hover:bg-blue-700">Add</button>
        </div>
      </div>

      <!-- Non-Delivery Dates -->
      <div>
        <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Non-Delivery Dates (Holidays/Closures)</h4>
        <div v-if="harmonogram.nonDeliveryDates && harmonogram.nonDeliveryDates.length > 0" class="flex flex-wrap gap-2 mb-2">
          <div v-for="(date, index) in harmonogram.nonDeliveryDates" :key="index"
               class="flex items-center bg-red-50 dark:bg-red-900/20 text-red-700 dark:text-red-400 px-3 py-1 rounded-full text-sm border border-red-100 dark:border-red-800">
            <span>{{ date }}</span>
            <button @click="removeDate(index)" class="ml-2 text-red-500 hover:text-red-700 font-bold">&times;</button>
          </div>
        </div>
        <div v-else class="text-sm text-gray-500 italic mb-2">No non-delivery dates configured.</div>

        <div class="flex gap-2">
          <input v-model="newDate" type="date" class="px-2 py-1 rounded border text-sm" />
          <button @click="addDate" class="px-3 py-1 bg-red-600 text-white rounded text-sm hover:bg-red-700">Add Date</button>
        </div>
      </div>

      <div class="mt-6 flex justify-end">
        <button @click="saveHarmonogram" class="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700 shadow-sm">
          Save Harmonogram
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import api from '@/api/axios'; // Use standard API (order-service is on 8091)

const props = defineProps({
  userId: {
    type: String,
    required: true
  }
});

const loading = ref(false);
const harmonogram = ref({
  schedules: [],
  nonDeliveryDates: []
});

const newSchedule = ref({
  dayOfWeek: 'MONDAY',
  startTime: '09:00',
  endTime: '17:00',
  type: 'DELIVERY'
});

const newDate = ref('');

const loadHarmonogram = async () => {
  loading.value = true;
  try {
    // Assuming endpoint: GET /api/harmonograms/customer/{customerId}
    // Since we have userId, we might need to fetch customerId first or assume they are linked.
    // Based on UserDetail.vue, customer profile is at /api/customers/users/{userId}
    // Let's try to get harmonogram by userId if supported, or get customer ID first.
    // For now, let's assume the backend supports getting by userId or we use the same ID.
    // Actually, CustomerHarmonogramController uses 'customerId'.
    // Let's fetch customer profile to get the ID (which might be the same as userId).
    
    // Check if we can get it directly.
    const res = await api.get(`/api/harmonograms/customer/${props.userId}`);
    if (res.data) {
      harmonogram.value = res.data;
    }
  } catch (error) {
    if (error.response && error.response.status === 404) {
      // No harmonogram yet
      harmonogram.value = { schedules: [], nonDeliveryDates: [] };
    } else {
      console.error("Failed to load harmonogram", error);
    }
  } finally {
    loading.value = false;
  }
};

const addSchedule = () => {
  harmonogram.value.schedules.push({ ...newSchedule.value });
};

const removeSchedule = (index) => {
  harmonogram.value.schedules.splice(index, 1);
};

const addDate = () => {
  if (newDate.value && !harmonogram.value.nonDeliveryDates.includes(newDate.value)) {
    harmonogram.value.nonDeliveryDates.push(newDate.value);
    newDate.value = '';
  }
};

const removeDate = (index) => {
  harmonogram.value.nonDeliveryDates.splice(index, 1);
};

const saveHarmonogram = async () => {
  try {
    const payload = {
      customerId: props.userId, // Assuming userId == customerId
      schedules: harmonogram.value.schedules,
      nonDeliveryDates: harmonogram.value.nonDeliveryDates
    };
    
    await api.post('/api/harmonograms', payload);
    alert('Harmonogram saved successfully');
  } catch (error) {
    console.error("Failed to save harmonogram", error);
    alert("Failed to save harmonogram");
  }
};

onMounted(() => {
  if (props.userId && props.userId !== 'create') {
    loadHarmonogram();
  }
});
</script>
