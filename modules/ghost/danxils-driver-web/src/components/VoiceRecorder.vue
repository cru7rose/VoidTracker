<template>
  <div class="voice-recorder">
    <button
      @click="toggleRecording"
      class="p-4 rounded-full transition-all duration-300 flex items-center justify-center shadow-lg"
      :class="isRecording ? 'bg-red-500 animate-pulse' : 'bg-blue-600 hover:bg-blue-700'"
    >
      <svg v-if="!isRecording" xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z" />
      </svg>
      <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 10a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1v-4z" />
      </svg>
    </button>
    <p v-if="isProcessing" class="mt-2 text-xs text-gray-500 animate-pulse">Transcribing...</p>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { voiceApi } from '../api/voiceApi';

const emit = defineEmits(['transcription']);

const isRecording = ref(false);
const isProcessing = ref(false);
let mediaRecorder = null;
let audioChunks = [];

const toggleRecording = async () => {
  if (isRecording.value) {
    stopRecording();
  } else {
    await startRecording();
  }
};

const startRecording = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    mediaRecorder = new MediaRecorder(stream);
    audioChunks = [];

    mediaRecorder.ondataavailable = (event) => {
      audioChunks.push(event.data);
    };

    mediaRecorder.onstop = async () => {
      const audioBlob = new Blob(audioChunks, { type: 'audio/webm' });
      await processAudio(audioBlob);
    };

    mediaRecorder.start();
    isRecording.value = true;
  } catch (error) {
    console.error('Error accessing microphone:', error);
    alert('Could not access microphone. Please check permissions.');
  }
};

const stopRecording = () => {
  if (mediaRecorder && mediaRecorder.state !== 'inactive') {
    mediaRecorder.stop();
    isRecording.value = false;
  }
};

const processAudio = async (audioBlob) => {
  isProcessing.value = true;
  try {
    const text = await voiceApi.transcribe(audioBlob);
    emit('transcription', text);
  } catch (error) {
    console.error('Transcription failed:', error);
    alert('Failed to transcribe audio.');
  } finally {
    isProcessing.value = false;
  }
};
</script>
