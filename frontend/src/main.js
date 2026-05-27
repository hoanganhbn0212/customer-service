import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import i18n from "./i18n";
import { OpenAPI } from "./api";
import { getApiBase } from "./api/apiBase";
import { getAccessToken } from "./auth/session";
import "./assets/main.css";

const FETCH_TIMEOUT_MS = 12000;
const nativeFetch = window.fetch.bind(window);
window.fetch = (input, init = {}) => {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), FETCH_TIMEOUT_MS);
  return nativeFetch(input, { ...init, signal: controller.signal }).finally(() =>
    clearTimeout(timer)
  );
};

// Relative URL → Vite proxy forwards /api to backend (avoids CORS)
OpenAPI.BASE = getApiBase();
OpenAPI.TOKEN = () => getAccessToken() ?? "";

createApp(App).use(router).use(i18n).mount("#app");
