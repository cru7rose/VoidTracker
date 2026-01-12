<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { Html5QrcodeScanner, Html5QrcodeSupportedFormats } from 'html5-qrcode'

const emit = defineEmits<{
  (e: 'scan', decodedText: string, decodedResult: any): void
  (e: 'error', errorMessage: string): void
}>()

const scannerId = 'reader'
let scanner: Html5QrcodeScanner | null = null

onMounted(() => {
  scanner = new Html5QrcodeScanner(
    scannerId,
    { 
      fps: 10, 
      qrbox: { width: 250, height: 250 },
      formatsToSupport: [ Html5QrcodeSupportedFormats.CODE_128, Html5QrcodeSupportedFormats.QR_CODE ]
    },
    /* verbose= */ false
  )

  scanner.render(
    (decodedText, decodedResult) => {
      emit('scan', decodedText, decodedResult)
    },
    (errorMessage) => {
      // parse error, ignore it.
      console.debug('Scan error', errorMessage)
    }
  )
})

onUnmounted(() => {
  if (scanner) {
    scanner.clear().catch(error => {
      console.error('Failed to clear html5-qrcode scanner. ', error)
    })
  }
})
</script>

<template>
  <div class="scanner-container bg-black rounded-lg overflow-hidden">
    <div :id="scannerId" width="600px"></div>
  </div>
</template>

<style>
/* Custom styling for the scanner if needed */
#reader {
  width: 100%;
}
</style>
