<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
          Ustaw nowe hasło
        </h2>
      </div>
      <form class="mt-8 space-y-6" @submit.prevent="handleSubmit">
        <div class="rounded-md shadow-sm -space-y-px">
          <div>
            <label for="password" class="sr-only">Nowe hasło</label>
            <input id="password" name="password" type="password" required v-model="password"
              class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
              placeholder="Nowe hasło" />
          </div>
        </div>

        <div>
          <button type="submit"
            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
            Zmień hasło
          </button>
        </div>
      </form>
      <div v-if="message" class="mt-4 text-green-600 text-center">
        {{ message }}
      </div>
      <div v-if="error" class="mt-4 text-red-600 text-center">
        {{ error }}
      </div>
      <div v-if="success" class="mt-4 text-center">
        <router-link to="/login" class="font-medium text-indigo-600 hover:text-indigo-500">
            Przejdź do logowania
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';

const route = useRoute();
const password = ref('');
const message = ref('');
const error = ref('');
const success = ref(false);

const token = route.query.token;

const handleSubmit = async () => {
    if(!token) {
        error.value = "Brak tokenu resetującego.";
        return;
    }

  try {
    await axios.post('/api/auth/custom-reset-password', { 
        token: token,
        password: password.value 
    });
    message.value = 'Hasło zostało zmienione pomyślnie.';
    success.value = true;
    error.value = '';
  } catch (err) {
    console.error(err);
    error.value = 'Wystąpił błąd. Token może być nieważny lub wygasł.';
    message.value = '';
  }
};
</script>
