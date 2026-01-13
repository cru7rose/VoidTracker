<template>
  <div class="signature-pad bg-gray-800 rounded-lg p-4">
    <div class="mb-4">
      <label class="block text-sm font-medium text-gray-300 mb-2">
        {{ label || 'Podpis odbiorcy' }}
      </label>
      <canvas
        ref="canvasElement"
        @mousedown="startDrawing"
        @mousemove="draw"
        @mouseup="stopDrawing"
        @mouseleave="stopDrawing"
        @touchstart="startDrawing"
        @touchmove="draw"
        @touchend="stopDrawing"
        class="w-full border-2 border-gray-600 rounded-lg bg-white cursor-crosshair"
        :style="{ height: height + 'px' }"
      ></canvas>
    </div>

    <div class="mb-4">
      <input
        v-if="captureRecipientName"
        v-model="recipientName"
        type="text"
        placeholder="Imię i nazwisko odbiorcy"
        class="w-full bg-gray-700 border border-gray-600 rounded-lg p-2 text-white placeholder-gray-400"
      />
    </div>

    <div class="flex gap-2">
      <button
        @click="clearSignature"
        class="flex-1 bg-gray-700 hover:bg-gray-600 text-white py-2 rounded-lg"
      >
        Wyczyść
      </button>
      <button
        @click="saveSignature"
        :disabled="!hasSignature"
        class="flex-1 bg-blue-600 hover:bg-blue-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white py-2 rounded-lg"
      >
        Zatwierdź podpis
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'

interface Props {
  label?: string
  height?: number
  captureRecipientName?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  height: 200,
  captureRecipientName: true
})

const emit = defineEmits<{
  (e: 'signature-saved', dataUrl: string, recipientName?: string): void
}>()

const canvasElement = ref<HTMLCanvasElement | null>(null)
const recipientName = ref('')
const hasSignature = ref(false)
let ctx: CanvasRenderingContext2D | null = null
let isDrawing = false
let lastX = 0
let lastY = 0

onMounted(() => {
  if (canvasElement.value) {
    const canvas = canvasElement.value
    ctx = canvas.getContext('2d')
    
    if (ctx) {
      // Set canvas size
      const rect = canvas.getBoundingClientRect()
      canvas.width = rect.width
      canvas.height = props.height

      // Configure drawing style
      ctx.strokeStyle = '#000000'
      ctx.lineWidth = 2
      ctx.lineCap = 'round'
      ctx.lineJoin = 'round'
    }
  }
})

const getEventPos = (e: MouseEvent | TouchEvent) => {
  if (!canvasElement.value) return { x: 0, y: 0 }
  
  const rect = canvasElement.value.getBoundingClientRect()
  
  if (e instanceof MouseEvent) {
    return {
      x: e.clientX - rect.left,
      y: e.clientY - rect.top
    }
  } else {
    const touch = e.touches[0] || e.changedTouches[0]
    return {
      x: touch.clientX - rect.left,
      y: touch.clientY - rect.top
    }
  }
}

const startDrawing = (e: MouseEvent | TouchEvent) => {
  e.preventDefault()
  isDrawing = true
  const pos = getEventPos(e)
  lastX = pos.x
  lastY = pos.y
}

const draw = (e: MouseEvent | TouchEvent) => {
  if (!isDrawing || !ctx) return
  e.preventDefault()

  const pos = getEventPos(e)
  
  ctx.beginPath()
  ctx.moveTo(lastX, lastY)
  ctx.lineTo(pos.x, pos.y)
  ctx.stroke()
  
  lastX = pos.x
  lastY = pos.y
  hasSignature.value = true
}

const stopDrawing = () => {
  isDrawing = false
}

const clearSignature = () => {
  if (!ctx || !canvasElement.value) return
  
  ctx.clearRect(0, 0, canvasElement.value.width, canvasElement.value.height)
  hasSignature.value = false
}

const saveSignature = () => {
  if (!canvasElement.value || !hasSignature.value) return

  const dataUrl = canvasElement.value.toDataURL('image/png')
  emit('signature-saved', dataUrl, props.captureRecipientName ? recipientName.value : undefined)
}
</script>

<style scoped>
canvas {
  touch-action: none;
}
</style>
