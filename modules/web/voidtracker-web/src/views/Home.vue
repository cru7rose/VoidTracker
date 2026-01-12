<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-500 to-purple-600">
    <div class="bg-white p-8 rounded-lg shadow-xl max-w-md w-full">
      <h1 class="text-3xl font-bold text-gray-800 mb-6 text-center">DANXILS Enterprise System</h1>
      
      <div class="space-y-4">
        <router-link 
          to="/customer/login"
          class="block w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 px-4 rounded-lg text-center transition duration-200"
        >
          Customer Portal
        </router-link>
        
        <router-link 
          to="/internal/login"
          class="block w-full bg-purple-600 hover:bg-purple-700 text-white font-semibold py-3 px-4 rounded-lg text-center transition duration-200"
        >
          Internal Portal
        </router-link>
      </div>
      
      <div class="mt-8 text-center text-gray-600 text-sm">
        <p>Demo System v1.0</p>
      </div>
    </div>
  </div>
</template>

<script setup>
// Landing page - no logic needed
</script>
