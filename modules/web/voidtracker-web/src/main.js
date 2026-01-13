import { createApp } from 'vue';
import { createPinia } from 'pinia';
import './style.css';
import App from './App.vue';
import router from './router';
import { useThemeStore } from './stores/themeStore';

import i18n from './i18n';

const pinia = createPinia();
const app = createApp(App);

app.use(pinia);
app.use(router);
app.use(i18n);

// Apply theme immediately on mount
const themeStore = useThemeStore();
themeStore.applyTheme();

app.mount('#app');
