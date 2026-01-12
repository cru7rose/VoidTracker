<template>
  <div class="p-6 space-y-8">
    <div class="flex justify-between items-center">
      <h2 class="text-2xl font-bold text-gray-800">Automation Hub (n8n)</h2>
      <button @click="refreshWebhooks" class="text-blue-600 hover:text-blue-800">Refresh Config</button>
    </div>

    <!-- Workflow Visualization: Client Communication -->
    <div class="bg-white p-6 rounded-lg shadow-lg">
      <h3 class="text-lg font-semibold mb-4 flex items-center gap-2">
        <span>📢 Client Communication Flow</span>
        <span class="text-xs bg-green-100 text-green-800 px-2 py-1 rounded">Active</span>
      </h3>
      
      <!-- Visual Flow Diagram -->
      <div class="flex items-center justify-between relative py-8 px-4 bg-gray-50 rounded-xl overflow-x-auto">
        <!-- Step 1 -->
        <div class="flex flex-col items-center z-10">
          <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center text-2xl shadow-sm">📦</div>
          <span class="mt-2 text-sm font-medium">Order Created</span>
        </div>
        
        <!-- Arrow -->
        <div class="flex-1 h-1 bg-gray-300 mx-2 relative">
           <div class="absolute -top-3 left-1/2 transform -translate-x-1/2 text-xs text-gray-500">n8n trigger</div>
        </div>

        <!-- Step 2 -->
        <div class="flex flex-col items-center z-10">
          <div class="w-12 h-12 bg-purple-100 rounded-full flex items-center justify-center text-2xl shadow-sm">⚡</div>
          <span class="mt-2 text-sm font-medium">Process Logic</span>
        </div>

        <!-- Arrow -->
        <div class="flex-1 h-1 bg-gray-300 mx-2"></div>

        <!-- Step 3 -->
        <div class="flex flex-col items-center z-10">
          <div class="w-12 h-12 bg-yellow-100 rounded-full flex items-center justify-center text-2xl shadow-sm">📧</div>
          <span class="mt-2 text-sm font-medium">Send Email</span>
        </div>

        <!-- Arrow -->
        <div class="flex-1 h-1 bg-gray-300 mx-2"></div>

        <!-- Step 4 -->
        <div class="flex flex-col items-center z-10">
          <div class="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center text-2xl shadow-sm">📱</div>
          <span class="mt-2 text-sm font-medium">SMS Driver</span>
        </div>
      </div>

      <!-- Actions -->
      <div class="mt-6 flex items-center gap-4">
        <div class="flex-1">
          <label class="block text-sm font-medium text-gray-700">Webhook URL</label>
          <input v-model="webhooks.CLIENT_COMM" type="text" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm p-2 border" placeholder="https://n8n.your-domain.com/webhook/..." />
        </div>
        <button @click="testWebhook('CLIENT_COMM')" class="mt-6 bg-indigo-600 text-white px-4 py-2 rounded hover:bg-indigo-700 transition-colors">
          Test Flow 🚀
        </button>
      </div>
    </div>

    <!-- Workflow Visualization: Routing Optimization -->
    <div class="bg-white p-6 rounded-lg shadow-lg">
      <h3 class="text-lg font-semibold mb-4 flex items-center gap-2">
        <span>🗺️ Routing Optimization Flow</span>
        <span class="text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded">On Demand</span>
      </h3>

      <!-- Visual Flow Diagram -->
      <div class="flex items-center justify-between relative py-8 px-4 bg-gray-50 rounded-xl overflow-x-auto">
        <div class="flex flex-col items-center z-10">
          <div class="w-12 h-12 bg-gray-200 rounded-full flex items-center justify-center text-2xl">📥</div>
          <span class="mt-2 text-sm font-medium">Fetch Orders</span>
        </div>
        <div class="flex-1 h-1 bg-gray-300 mx-2"></div>
        <div class="flex flex-col items-center z-10">
          <div class="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center text-2xl">📍</div>
          <span class="mt-2 text-sm font-medium">Geocode</span>
        </div>
        <div class="flex-1 h-1 bg-gray-300 mx-2"></div>
        <div class="flex flex-col items-center z-10">
          <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center text-2xl">🧠</div>
          <span class="mt-2 text-sm font-medium">Optimize (AI)</span>
        </div>
        <div class="flex-1 h-1 bg-gray-300 mx-2"></div>
        <div class="flex flex-col items-center z-10">
          <div class="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center text-2xl">💾</div>
          <span class="mt-2 text-sm font-medium">Save Routes</span>
        </div>
      </div>
      
      <div class="mt-6 flex items-center gap-4">
         <div class="flex-1">
          <label class="block text-sm font-medium text-gray-700">Webhook URL</label>
          <input v-model="webhooks.ROUTING_OPT" type="text" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm p-2 border" placeholder="https://n8n.your-domain.com/webhook/..." />
        </div>
        <button @click="testWebhook('ROUTING_OPT')" class="mt-6 bg-indigo-600 text-white px-4 py-2 rounded hover:bg-indigo-700 transition-colors">
          Test Flow 🚀
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import confetti from 'canvas-confetti';
import axios from 'axios';

const webhooks = ref({
  CLIENT_COMM: 'https://n8n.example.com/webhook/client-comm',
  ROUTING_OPT: 'https://n8n.example.com/webhook/routing'
});

const refreshWebhooks = async () => {
  // TODO: Fetch from backend /api/webhooks
};

const testWebhook = async (type) => {
  try {
    // Call backend to trigger the webhook
    // await axios.post(`/api/webhooks/test/${type}`);
    
    // Simulate success for demo
    await new Promise(r => setTimeout(r, 800));
    
    // Confetti!
    confetti({
      particleCount: 100,
      spread: 70,
      origin: { y: 0.6 }
    });
    
    alert(`Success! ${type} workflow triggered.`);
  } catch (e) {
    alert('Failed to trigger webhook.');
  }
};
</script>
