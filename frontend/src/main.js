import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import i18n from "./i18n";
import { OpenAPI } from "./api";
import { getAccessToken } from "./auth/session";
import "./assets/main.css";

// Relative URL → Vite proxy forwards /api to backend (avoids CORS)
OpenAPI.BASE = "";
OpenAPI.TOKEN = () => getAccessToken() ?? "";

createApp(App).use(router).use(i18n).mount("#app");
