<template>
  <div class="relative">
    <button @click="isOpen = !isOpen" class="flex items-center gap-2 px-3 py-2 rounded hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
      <span class="text-xl">{{ currentFlag }}</span>
      <span class="text-sm font-medium text-gray-700 dark:text-gray-300 hidden md:block">{{ currentLabel }}</span>
      <svg class="w-4 h-4 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
      </svg>
    </button>

    <div v-if="isOpen" class="absolute right-0 mt-2 w-40 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-100 dark:border-gray-700 py-1 z-50">
      <button v-for="lang in languages" :key="lang.code" 
              @click="switchLanguage(lang.code)"
              class="w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 flex items-center gap-3">
        <span class="text-xl">{{ lang.flag }}</span>
        <span>{{ lang.label }}</span>
        <span v-if="locale === lang.code" class="ml-auto text-blue-600">✓</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';

const { locale } = useI18n();
const isOpen = ref(false);

const languages = [
  { code: 'pl', label: 'Polski', flag: '🇵🇱' },
  { code: 'en', label: 'English', flag: '🇬🇧' }
];

const currentFlag = computed(() => languages.find(l => l.code === locale.value)?.flag);
const currentLabel = computed(() => languages.find(l => l.code === locale.value)?.label);

const switchLanguage = (code) => {
  locale.value = code;
  isOpen.value = false;
  // Persist preference (optional)
  localStorage.setItem('user-locale', code);
};
</script>
