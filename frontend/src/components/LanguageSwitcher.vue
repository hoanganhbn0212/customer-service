<script setup>
import { useI18n } from "vue-i18n";

defineProps({
  variant: {
    type: String,
    default: "light"
  }
});

const { locale, t } = useI18n();

const setLocale = (lang) => {
  locale.value = lang;
  localStorage.setItem("locale", lang);
  document.documentElement.lang = lang;
};

if (!document.documentElement.lang) {
  document.documentElement.lang = locale.value;
}
</script>

<template>
  <div class="lang-switcher" :class="variant" role="group" :aria-label="t('lang.vi')">
    <button
      type="button"
      class="lang-btn"
      :class="{ active: locale === 'vi' }"
      @click="setLocale('vi')"
    >
      VI
    </button>
    <button
      type="button"
      class="lang-btn"
      :class="{ active: locale === 'en' }"
      @click="setLocale('en')"
    >
      EN
    </button>
  </div>
</template>

<style scoped>
.lang-switcher {
  display: inline-flex;
  padding: 3px;
  border-radius: 999px;
  gap: 2px;
}

.lang-switcher.light {
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid #e8edf3;
  box-shadow: 0 2px 10px rgba(15, 40, 80, 0.08);
}

.lang-switcher.dark {
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.28);
  backdrop-filter: blur(8px);
}

.lang-btn {
  min-width: 38px;
  padding: 6px 11px;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  transition: color 0.15s, background 0.15s;
}

.lang-switcher.light .lang-btn {
  color: #64748b;
  background: transparent;
}

.lang-switcher.light .lang-btn.active {
  color: #fff;
  background: #1a6dff;
}

.lang-switcher.dark .lang-btn {
  color: rgba(255, 255, 255, 0.88);
  background: transparent;
}

.lang-switcher.dark .lang-btn.active {
  color: #1a6dff;
  background: #fff;
}
</style>
