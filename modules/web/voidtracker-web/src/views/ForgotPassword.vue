<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
          Zresetuj hasło
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600">
          Podaj swój adres email, a my wyślemy Ci link do resetowania hasła.
        </p>
      </div>
      <form class="mt-8 space-y-6" @submit.prevent="handleSubmit">
        <div class="rounded-md shadow-sm -space-y-px">
          <div>
            <label for="email-address" class="sr-only">Email address</label>
            <input id="email-address" name="email" type="email" autocomplete="email" required v-model="email"
              class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
              placeholder="Adres email" />
          </div>
        </div>

        <div>
          <button type="submit"
            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
            Wyślij link
          </button>
        </div>
        <div class="text-sm text-center">
            <router-link to="/login" class="font-medium text-indigo-600 hover:text-indigo-500">
            Powrót do logowania
            </router-link>
        </div>
      </form>
      <div v-if="message" class="mt-4 text-green-600 text-center">
        {{ message }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';

const email = ref('');
const message = ref('');

const handleSubmit = async () => {
  try {
    await axios.post('/api/auth/custom-forgot-password', { email: email.value });
    message.value = 'Jeśli podany adres email istnieje w naszej bazie, wysłaliśmy na niego instrukcję resetowania hasła.';
  } catch (error) {
    console.error(error);
    // Even on error (to prevent enumeration), show success message or generic error
    message.value = 'Wystąpił błąd. Spróbuj ponownie później.';
  }
};
</script>
