<script setup>
import { useI18n } from "vue-i18n";

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
  <div class="lang-switcher" role="group" :aria-label="t('lang.vi')">
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
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid #dee2e6;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.lang-btn {
  min-width: 40px;
  padding: 6px 12px;
  font-size: 0.8rem;
  font-weight: 600;
  color: #6c757d;
  background: transparent;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: color 0.15s, background 0.15s;
}

.lang-btn:hover {
  color: #343a40;
}

.lang-btn.active {
  color: #fff;
  background: #67a387;
}
</style>
