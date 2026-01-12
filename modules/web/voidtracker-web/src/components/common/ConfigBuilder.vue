<template>
  <div class="space-y-4">
    <div v-for="(field, key) in schema" :key="key" class="flex flex-col">
      <label :for="key" class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
        {{ field.label }}
        <span v-if="field.required" class="text-red-500">*</span>
      </label>
      
      <!-- Text Input -->
      <input v-if="field.type === 'text'" 
             :id="key"
             v-model="modelValue[key]"
             type="text"
             :placeholder="field.placeholder"
             class="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:text-white sm:text-sm"
      />

      <!-- Number Input -->
      <input v-if="field.type === 'number'" 
             :id="key"
             v-model.number="modelValue[key]"
             type="number"
             :placeholder="field.placeholder"
             class="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:text-white sm:text-sm"
      />

      <!-- Select Input -->
      <select v-if="field.type === 'select'"
              :id="key"
              v-model="modelValue[key]"
              class="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:text-white sm:text-sm">
        <option v-for="opt in field.options" :key="opt.value" :value="opt.value">
          {{ opt.label }}
        </option>
      </select>

      <!-- Toggle (Checkbox) -->
      <div v-if="field.type === 'boolean'" class="flex items-center">
        <button type="button" 
                @click="modelValue[key] = !modelValue[key]"
                :class="[modelValue[key] ? 'bg-blue-600' : 'bg-gray-200 dark:bg-gray-600', 'relative inline-flex flex-shrink-0 h-6 w-11 border-2 border-transparent rounded-full cursor-pointer transition-colors ease-in-out duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500']">
          <span aria-hidden="true" 
                :class="[modelValue[key] ? 'translate-x-5' : 'translate-x-0', 'pointer-events-none inline-block h-5 w-5 rounded-full bg-white shadow transform ring-0 transition ease-in-out duration-200']"></span>
        </button>
        <span class="ml-3 text-sm text-gray-500 dark:text-gray-400">{{ field.description }}</span>
      </div>

      <!-- Textarea -->
      <textarea v-if="field.type === 'textarea'"
                :id="key"
                v-model="modelValue[key]"
                rows="3"
                :placeholder="field.placeholder"
                class="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:text-white sm:text-sm"></textarea>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  modelValue: {
    type: Object,
    required: true
  },
  schema: {
    type: Object,
    required: true
  }
});
</script>
