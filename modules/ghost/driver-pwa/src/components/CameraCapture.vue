<template>
  <div class="camera-capture">
    <div v-if="!isCapturing" class="preview-container bg-gray-800 rounded-lg p-4">
      <button
        @click="startCapture"
        class="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 rounded-lg transition-colors flex items-center justify-center gap-2"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
          <circle cx="12" cy="13" r="4"/>
        </svg>
        Otwórz kamerę
      </button>
    </div>

    <div v-else class="camera-container relative bg-black rounded-lg overflow-hidden">
      <video
        ref="videoElement"
        autoplay
        playsinline
        class="w-full h-auto"
      ></video>
      
      <div class="absolute bottom-4 left-0 right-0 flex justify-center gap-4">
        <button
          @click="cancelCapture"
          class="bg-red-600 hover:bg-red-700 text-white px-6 py-3 rounded-full font-semibold"
        >
          Anuluj
        </button>
        <button
          @click="capturePhoto"
          class="bg-white hover:bg-gray-200 text-gray-900 w-16 h-16 rounded-full border-4 border-gray-300 shadow-lg"
        >
          <span class="sr-only">Zrób zdjęcie</span>
        </button>
      </div>
    </div>

    <div v-if="capturedImage" class="preview-image mt-4">
      <img :src="capturedImage" alt="Captured" class="w-full rounded-lg" />
      <div class="mt-2 flex gap-2">
        <button
          @click="retakePhoto"
          class="flex-1 bg-gray-700 hover:bg-gray-600 text-white py-2 rounded-lg"
        >
          Zrób ponownie
        </button>
        <button
          @click="confirmPhoto"
          class="flex-1 bg-green-600 hover:bg-green-700 text-white py-2 rounded-lg"
        >
          Zatwierdź
        </button>
      </div>
    </div>

    <div v-if="error" class="mt-4 text-red-400 text-sm">
      {{ error }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue'

const emit = defineEmits<{
  (e: 'photo-captured', file: File, dataUrl: string): void
  (e: 'error', error: string): void
}>()

const videoElement = ref<HTMLVideoElement | null>(null)
const isCapturing = ref(false)
const capturedImage = ref<string | null>(null)
const error = ref<string | null>(null)
let stream: MediaStream | null = null
let canvas: HTMLCanvasElement | null = null

const startCapture = async () => {
  try {
    error.value = null
    stream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: 'environment' }, // Back camera
      audio: false
    })

    if (videoElement.value) {
      videoElement.value.srcObject = stream
      isCapturing.value = true
    }
  } catch (err: any) {
    error.value = 'Nie można uzyskać dostępu do kamery. Sprawdź uprawnienia.'
    emit('error', err.message)
    console.error('Camera error:', err)
  }
}

const capturePhoto = () => {
  if (!videoElement.value) return

  canvas = document.createElement('canvas')
  canvas.width = videoElement.value.videoWidth
  canvas.height = videoElement.value.videoHeight

  const ctx = canvas.getContext('2d')
  if (ctx) {
    ctx.drawImage(videoElement.value, 0, 0)
    capturedImage.value = canvas.toDataURL('image/jpeg', 0.9)
    stopCapture()
  }
}

const confirmPhoto = () => {
  if (!canvas || !capturedImage.value) return

  canvas.toBlob((blob) => {
    if (blob) {
      const file = new File([blob], `photo_${Date.now()}.jpg`, { type: 'image/jpeg' })
      emit('photo-captured', file, capturedImage.value!)
      reset()
    }
  }, 'image/jpeg', 0.9)
}

const retakePhoto = () => {
  capturedImage.value = null
  startCapture()
}

const cancelCapture = () => {
  stopCapture()
  reset()
}

const stopCapture = () => {
  if (stream) {
    stream.getTracks().forEach(track => track.stop())
    stream = null
  }
  isCapturing.value = false
}

const reset = () => {
  capturedImage.value = null
  error.value = null
  canvas = null
}

onUnmounted(() => {
  stopCapture()
})
</script>

<style scoped>
.camera-container {
  min-height: 400px;
}
</style>
