<template>
  <div class="relative">
    <button 
      @click="toggleLocale" 
      class="flex items-center gap-2 px-3 py-1.5 rounded-full bg-surface-800 hover:bg-surface-700 transition-colors border border-white/10 text-sm font-medium text-white"
    >
      <span>{{ currentFlag }}</span>
      <span>{{ currentLocale.toUpperCase() }}</span>
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { locale } = useI18n()

const currentLocale = computed(() => locale.value)

const currentFlag = computed(() => {
  return locale.value === 'pl' ? '🇵🇱' : '🇬🇧'
})

const toggleLocale = () => {
  locale.value = locale.value === 'en' ? 'pl' : 'en'
}
</script>
