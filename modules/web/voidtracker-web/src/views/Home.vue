<template>
  <div class="min-h-screen flex items-center justify-center bg-void-black cyber-grid relative overflow-hidden">
    <!-- Animated Background Effects -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div class="absolute top-1/4 left-1/4 w-96 h-96 bg-void-cyan-500/10 rounded-full blur-3xl animate-float"></div>
      <div class="absolute bottom-1/4 right-1/4 w-96 h-96 bg-void-pink-500/10 rounded-full blur-3xl animate-float" style="animation-delay: 2s;"></div>
    </div>

    <!-- Main Content -->
    <div class="relative z-10 hologram-panel p-12 rounded-2xl max-w-2xl w-full mx-4 border-2 border-void-cyan-600/50">
      <div class="text-center mb-8">
        <div class="inline-block mb-4">
          <div class="w-20 h-20 rounded-xl bg-gradient-to-br from-void-cyan-400 to-void-pink-400 flex items-center justify-center shadow-2xl shadow-void-cyan-500/50 animate-neon-flicker">
            <span class="text-void-black font-bold text-4xl font-mono">V</span>
          </div>
        </div>
        <h1 class="text-4xl font-bold font-mono mb-2 neon-glow text-void-cyan-400 tracking-wider">
          VOID-FLOW
        </h1>
        <p class="text-void-cyan-500 text-sm font-mono uppercase tracking-widest">OPERATIONAL CONTROL TOWER</p>
        <p class="text-void-cyan-600 text-xs font-mono mt-2">v2.0</p>
      </div>
      
      <div class="space-y-4">
        <router-link 
          to="/customer/login"
          class="block w-full neon-button py-4 px-6 rounded-lg text-center transition-all duration-300 font-mono text-lg uppercase tracking-wider"
        >
          <span class="flex items-center justify-center gap-3">
            <span>🌐</span>
            <span>Customer Portal</span>
          </span>
        </router-link>
        
        <router-link 
          to="/internal/login"
          class="block w-full bg-void-pink-900/50 border-2 border-void-pink-400 text-void-pink-300 font-bold py-4 px-6 rounded-lg text-center transition-all duration-300 font-mono text-lg uppercase tracking-wider hover:shadow-[0_0_30px_rgba(255,0,110,0.6)] hover:-translate-y-1"
          style="box-shadow: 0 0 20px rgba(255, 0, 110, 0.4), inset 0 0 20px rgba(255, 0, 110, 0.1);"
        >
          <span class="flex items-center justify-center gap-3">
            <span>⚡</span>
            <span>Internal Portal</span>
          </span>
        </router-link>
      </div>
      
      <div class="mt-8 text-center">
        <div class="inline-flex items-center gap-2 text-void-cyan-600 text-xs font-mono">
          <div class="w-2 h-2 rounded-full bg-green-500 animate-pulse"></div>
          <span>SYSTEM OPERATIONAL</span>
        </div>
        <p class="text-void-cyan-700 text-xs font-mono mt-2">VoidTracker Enterprise v2.0</p>
      </div>
    </div>
  </div>
</template>

<script setup>
// Landing page - no logic needed
</script>
