<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="mb-6 flex justify-between items-center">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Tile Dashboard</h1>
        <p class="text-sm text-gray-500">Customize your workspace</p>
      </div>
      <div class="flex space-x-3">
        <button @click="resetLayout" class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50">
          Reset Layout
        </button>
        <button @click="toggleEditMode" :class="isEditMode ? 'bg-blue-600 text-white hover:bg-blue-700' : 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50'" class="px-4 py-2 text-sm font-medium rounded-md transition-colors">
          {{ isEditMode ? 'Save Layout' : 'Edit Layout' }}
        </button>
      </div>
    </div>

    <GridLayout
      v-model:layout="layout"
      :col-num="12"
      :row-height="30"
      :is-draggable="isEditMode"
      :is-resizable="isEditMode"
      :vertical-compact="true"
      :use-css-transforms="true"
    >
      <GridItem
        v-for="item in layout"
        :key="item.i"
        :x="item.x"
        :y="item.y"
        :w="item.w"
        :h="item.h"
        :i="item.i"
        class="transition-shadow duration-200"
        :class="{ 'shadow-xl ring-2 ring-blue-500 ring-opacity-50 z-10': isEditMode }"
      >
        <WidgetWrapper :title="getWidgetTitle(item.i)">
          <component :is="getWidgetComponent(item.i)" />
        </WidgetWrapper>
      </GridItem>
    </GridLayout>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { GridLayout, GridItem } from 'grid-layout-plus'
import WidgetWrapper from '../../components/dashboard/WidgetWrapper.vue'
import StatsWidget from '../../components/dashboard/widgets/StatsWidget.vue'
import MapWidget from '../../components/dashboard/widgets/MapWidget.vue'
import RecentOrdersWidget from '../../components/dashboard/widgets/RecentOrdersWidget.vue'
import { configApi } from '../../api/configApi'
import { useAuthStore } from '../../stores/authStore'

const authStore = useAuthStore()
const isEditMode = ref(false)

const defaultLayout = [
  { i: 'stats', x: 0, y: 0, w: 12, h: 4 },
  { i: 'map', x: 0, y: 4, w: 8, h: 10 },
  { i: 'orders', x: 8, y: 4, w: 4, h: 10 }
]

const layout = ref([...defaultLayout])

const getWidgetTitle = (id) => {
  const titles = {
    stats: 'Key Metrics',
    map: 'Live Fleet Map',
    orders: 'Recent Orders'
  }
  return titles[id] || 'Widget'
}

const getWidgetComponent = (id) => {
  const components = {
    stats: StatsWidget,
    map: MapWidget,
    orders: RecentOrdersWidget
  }
  return components[id]
}

const toggleEditMode = () => {
  isEditMode.value = !isEditMode.value
  if (!isEditMode.value) {
    saveLayout()
  }
}

const saveLayout = async () => {
  // localStorage.setItem('dashboard-layout', JSON.stringify(layout.value))
  if (authStore.user) {
    try {
        const userId = authStore.user.id || authStore.user.username;
        await configApi.updateUserConfig(userId, 'dashboard-layout', JSON.stringify(layout.value));
    } catch (e) {
        console.error('Failed to save layout to backend', e);
    }
  }
}

const loadLayout = async () => {
  // const saved = localStorage.getItem('dashboard-layout')
  if (authStore.user) {
    try {
        const userId = authStore.user.id || authStore.user.username;
        const saved = await configApi.getUserConfig(userId, 'dashboard-layout');
        if (saved) {
             layout.value = JSON.parse(saved);
        }
    } catch (e) {
        console.error('Failed to load layout from backend', e);
    }
  }
}

const resetLayout = () => {
  layout.value = [...defaultLayout]
  saveLayout()
}

onMounted(() => {
  loadLayout()
})
</script>

<style scoped>
:deep(.vgl-item--placeholder) {
  background: rgba(59, 130, 246, 0.1) !important;
  border: 2px dashed rgba(59, 130, 246, 0.5) !important;
  border-radius: 0.75rem !important;
  opacity: 1 !important;
}
</style>
