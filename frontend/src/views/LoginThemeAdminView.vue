<script setup>
import { onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import { ApiError, ContentService } from "../api";
import AppShell from "../components/layout/AppShell.vue";
import { canEditPages } from "../auth/session";

const { t } = useI18n();

const headerBackgroundUrl = ref("");
const bodyBackgroundUrl = ref("");
const loading = ref(false);
const error = ref("");
const success = ref("");

const loadTheme = async () => {
  loading.value = true;
  error.value = "";
  try {
    const theme = await ContentService.getLoginTheme();
    headerBackgroundUrl.value = theme.headerBackgroundUrl || "";
    bodyBackgroundUrl.value = theme.bodyBackgroundUrl || "";
  } catch (e) {
    error.value = e instanceof ApiError ? e.body?.message || e.message : String(e);
  } finally {
    loading.value = false;
  }
};

const saveTheme = async () => {
  loading.value = true;
  error.value = "";
  success.value = "";
  try {
    await ContentService.saveLoginTheme({
      headerBackgroundUrl: headerBackgroundUrl.value.trim(),
      bodyBackgroundUrl: bodyBackgroundUrl.value.trim()
    });
    success.value = t("theme.saveSuccess");
  } catch (e) {
    error.value = e instanceof ApiError ? e.body?.message || e.message : String(e);
  } finally {
    loading.value = false;
  }
};

onMounted(loadTheme);
</script>

<template>
  <AppShell active-nav="theme">
    <section class="card page-card">
      <h1 class="page-title">{{ t("theme.title") }}</h1>
      <p class="page-desc">{{ t("theme.subtitle") }}</p>
      <p v-if="!canEditPages()" class="readonly-hint">{{ t("theme.readonlyHint") }}</p>

      <form class="theme-form" @submit.prevent="saveTheme">
        <label class="field-block">
          <span>{{ t("theme.headerBg") }}</span>
          <input
            v-model="headerBackgroundUrl"
            type="url"
            :placeholder="t('theme.urlPlaceholder')"
            :disabled="loading || !canEditPages()"
            :readonly="!canEditPages()"
          />
        </label>
        <label class="field-block">
          <span>{{ t("theme.bodyBg") }}</span>
          <input
            v-model="bodyBackgroundUrl"
            type="url"
            :placeholder="t('theme.urlPlaceholder')"
            :disabled="loading || !canEditPages()"
            :readonly="!canEditPages()"
          />
        </label>

        <div class="preview-row">
          <div class="preview">
            <p>{{ t("theme.headerPreview") }}</p>
            <div
              class="preview-box header"
              :style="headerBackgroundUrl ? { backgroundImage: `url(${headerBackgroundUrl})` } : {}"
            />
          </div>
          <div class="preview">
            <p>{{ t("theme.bodyPreview") }}</p>
            <div
              class="preview-box body"
              :style="bodyBackgroundUrl ? { backgroundImage: `url(${bodyBackgroundUrl})` } : {}"
            />
          </div>
        </div>

        <button v-if="canEditPages()" type="submit" class="btn-save" :disabled="loading">
          {{ loading ? t("theme.saving") : t("theme.save") }}
        </button>
      </form>

      <p v-if="success" class="msg success">{{ success }}</p>
      <p v-if="error" class="msg error">{{ error }}</p>
    </section>
  </AppShell>
</template>

<style scoped>
.page-card {
  max-width: 720px;
}

.page-title {
  margin: 0 0 8px;
  font-size: 1.5rem;
  color: #3b4a5a;
}

.page-desc {
  margin: 0 0 24px;
  color: #7a8794;
}

.theme-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.field-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 0.9rem;
  font-weight: 600;
  color: #3b4a5a;
}

.field-block input {
  padding: 12px 14px;
  border: 1px solid #dee2e6;
  border-radius: 10px;
  font-size: 0.9rem;
}

.preview-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.preview p {
  margin: 0 0 8px;
  font-size: 0.8rem;
  color: #64748b;
}

.preview-box {
  height: 120px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background-size: cover;
  background-position: center;
}

.preview-box.header {
  background: linear-gradient(160deg, #0a3d9e, #3d8cff);
}

.preview-box.body {
  background: #f8fafc;
}

.btn-save {
  align-self: flex-start;
  padding: 12px 24px;
  border: none;
  border-radius: 10px;
  background: #1a6dff;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
}

.btn-save:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.msg {
  margin-top: 16px;
  font-size: 0.9rem;
}

.msg.success {
  color: #2e7d52;
}

.msg.error {
  color: #c62828;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

@media (max-width: 640px) {
  .preview-row {
    grid-template-columns: 1fr;
  }
}
</style>
