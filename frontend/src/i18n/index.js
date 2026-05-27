import { createI18n } from "vue-i18n";
import en from "../locales/en.json";
import vi from "../locales/vi.json";

const savedLocale = localStorage.getItem("locale");
const defaultLocale = savedLocale === "en" || savedLocale === "vi" ? savedLocale : "vi";

const i18n = createI18n({
  legacy: false,
  locale: defaultLocale,
  fallbackLocale: "en",
  messages: { en, vi }
});

export default i18n;
