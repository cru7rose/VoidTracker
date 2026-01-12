<template>
  <form @submit.prevent="handleSubmit" class="space-y-4">
    <div v-for="field in schema" :key="field.field_key" class="space-y-1">
      <label :for="field.field_key" class="block text-sm font-medium text-gray-700">
        {{ field.label }} <span v-if="field.required" class="text-red-500">*</span>
      </label>

      <!-- Text Input -->
      <input
        v-if="field.type === 'text'"
        type="text"
        :id="field.field_key"
        :value="modelValue[field.field_key]"
        @input="updateValue(field.field_key, $event.target.value)"
        :required="field.required"
        class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-brand-500 focus:border-brand-500 sm:text-sm"
      />

      <!-- Number Input -->
      <input
        v-if="field.type === 'number'"
        type="number"
        :id="field.field_key"
        :value="modelValue[field.field_key]"
        @input="updateValue(field.field_key, Number($event.target.value))"
        :required="field.required"
        class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-brand-500 focus:border-brand-500 sm:text-sm"
      />

      <!-- Boolean Toggle -->
      <div v-if="field.type === 'boolean'" class="flex items-center">
        <button
          type="button"
          @click="updateValue(field.field_key, !modelValue[field.field_key])"
          :class="modelValue[field.field_key] ? 'bg-brand-600' : 'bg-gray-200'"
          class="relative inline-flex flex-shrink-0 h-6 w-11 border-2 border-transparent rounded-full cursor-pointer transition-colors ease-in-out duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-brand-500"
        >
          <span class="sr-only">Use setting</span>
          <span
            aria-hidden="true"
            :class="modelValue[field.field_key] ? 'translate-x-5' : 'translate-x-0'"
            class="pointer-events-none inline-block h-5 w-5 rounded-full bg-white shadow transform ring-0 transition ease-in-out duration-200"
          ></span>
        </button>
        <span class="ml-3 text-sm text-gray-900">{{ field.label }}</span>
      </div>

      <!-- Select Input -->
      <select
        v-if="field.type === 'select'"
        :id="field.field_key"
        :value="modelValue[field.field_key]"
        @change="updateValue(field.field_key, $event.target.value)"
        :required="field.required"
        class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-brand-500 focus:border-brand-500 sm:text-sm"
      >
        <option value="" disabled>Select an option</option>
        <option v-for="option in field.options" :key="option" :value="option">
          {{ option }}
        </option>
      </select>
    </div>

    <div class="pt-4">
      <button
        type="submit"
        class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-brand-600 hover:bg-brand-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-brand-500"
      >
        Save Details
      </button>
    </div>
  </form>
</template>

<script setup>
const props = defineProps({
  schema: {
    type: Array,
    required: true,
    default: () => []
  },
  modelValue: {
    type: Object,
    required: true,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const updateValue = (key, value) => {
  const newValue = { ...props.modelValue, [key]: value }
  emit('update:modelValue', newValue)
}

const handleSubmit = () => {
  emit('submit', props.modelValue)
}
</script>
