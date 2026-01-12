<template>
  <div class="flex h-full bg-gray-100">
    <!-- Settings Sidebar -->
    <div class="w-64 bg-white border-r shadow-sm flex flex-col">
      <div class="p-6 border-b">
        <h2 class="text-xl font-bold text-gray-800">Settings</h2>
        <p class="text-sm text-gray-500">System Configuration</p>
      </div>
      <nav class="flex-1 p-4 space-y-1">
        <router-link 
          to="/internal/settings/profiles" 
          class="block px-4 py-2 rounded-lg text-gray-600 hover:bg-blue-50 hover:text-blue-600 transition-colors"
          active-class="bg-blue-50 text-blue-600 font-semibold"
        >
          Optimization Profiles
        </router-link>
        <router-link 
          to="/internal/settings/rules" 
          class="block px-4 py-2 rounded-lg text-gray-600 hover:bg-blue-50 hover:text-blue-600 transition-colors"
          active-class="bg-blue-50 text-blue-600 font-semibold"
        >
          Business Rules
        </router-link>
        <router-link 
          to="/internal/settings/views" 
          class="block px-4 py-2 rounded-lg text-gray-600 hover:bg-blue-50 hover:text-blue-600 transition-colors"
          active-class="bg-blue-50 text-blue-600 font-semibold"
        >
          View Configurations
        </router-link>
        <router-link 
          to="/internal/settings/config" 
          class="block px-4 py-2 rounded-lg text-gray-600 hover:bg-blue-50 hover:text-blue-600 transition-colors"
          active-class="bg-blue-50 text-blue-600 font-semibold"
        >
          System Config
        </router-link>
        <router-link 
          to="/internal/settings/workflows" 
          class="block px-4 py-2 rounded-lg text-gray-600 hover:bg-blue-50 hover:text-blue-600 transition-colors"
          active-class="bg-blue-50 text-blue-600 font-semibold"
        >
          Workflow Automation
        </router-link>
      </nav>
    </div>

    <!-- Content Area -->
    <div class="flex-1 overflow-y-auto p-8">
      <router-view></router-view>
    </div>
  </div>
</template>

<script setup>
// No script needed for layout
</script>
