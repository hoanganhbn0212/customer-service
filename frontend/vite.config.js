import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

// VITE_DEVICE=1: tat HMR — on dinh hon khi mo tu dien thoai qua Wi-Fi
const forDevice = process.env.VITE_DEVICE === "1";
const forCapacitor = process.env.CAPACITOR === "1";

export default defineConfig({
  base: forCapacitor ? "./" : "/",
  plugins: [vue()],
  server: {
    host: true,
    port: 5173,
    hmr: forDevice ? false : undefined,
    proxy: {
      "/api": {
        target: "http://localhost:8082",
        changeOrigin: true
      }
    }
  },
  preview: {
    host: true,
    port: 4173,
    proxy: {
      "/api": {
        target: "http://localhost:8082",
        changeOrigin: true
      }
    }
  }
});
