<script setup>
import { onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import AppShell from "../components/layout/AppShell.vue";
import { listAdminPackages, listSubscriptionProgress, updateSubscriptionProgress } from "../api/adminApi";

const { t } = useI18n();
const loading = ref(false);
const error = ref("");
const success = ref("");
const packageFilter = ref("");
const rows = ref([]);
const packages = ref([]);

const deploymentStatuses = ["IN_PROGRESS", "COMPLETED", "PAUSED"];

const loadPackages = async () => {
  packages.value = await listAdminPackages();
};

const load = async () => {
  loading.value = true;
  error.value = "";
  try {
    rows.value = await listSubscriptionProgress(packageFilter.value || undefined);
  } catch (e) {
    error.value = e?.body?.message || e.message || String(e);
  } finally {
    loading.value = false;
  }
};

const loadAll = async () => {
  loading.value = true;
  error.value = "";
  try {
    await Promise.all([loadPackages(), load()]);
  } catch (e) {
    error.value = e?.body?.message || e.message || String(e);
  } finally {
    loading.value = false;
  }
};

const patchService = (row, serviceId, value) => {
  row.services = row.services.map((s) => (s.serviceId === serviceId ? { ...s, percent: Number(value) } : s));
};

const patchServiceCount = (row, serviceId, field, value) => {
  row.services = row.services.map((s) =>
    s.serviceId === serviceId ? { ...s, [field]: value === "" ? null : Number(value) } : s
  );
};

const saveRow = async (row) => {
  loading.value = true;
  error.value = "";
  success.value = "";
  try {
    await updateSubscriptionProgress(row.subscriptionId, {
      completedPosts: Number(row.completedPosts || 0),
      completedImages: Number(row.completedImages || 0),
      completedVideos: Number(row.completedVideos || 0),
      deploymentStatus: row.deploymentStatus,
      serviceProgress: row.services.map((s) => ({
        serviceId: s.serviceId,
        percent: Number(s.percent || 0),
        completedCount: s.completedCount,
        targetCount: s.targetCount
      }))
    });
    success.value = t("adminProgress.saveSuccess", { user: row.userName });
    await load();
  } catch (e) {
    error.value = e?.body?.message || e.message || String(e);
  } finally {
    loading.value = false;
  }
};

onMounted(loadAll);
</script>

<template>
  <AppShell active-nav="progress">
    <section class="card page-card">
      <h1 class="page-title">{{ t("adminProgress.title") }}</h1>
      <p class="page-desc">{{ t("adminProgress.subtitle") }}</p>

      <div class="toolbar">
        <select v-model="packageFilter" :disabled="loading" @change="load">
          <option value="">{{ t("adminProgress.filterAll") }}</option>
          <option v-for="pkg in packages" :key="pkg.code" :value="pkg.code">
            {{ pkg.label }} ({{ pkg.code }})
          </option>
        </select>
        <button class="btn-outline" :disabled="loading" @click="load">{{ t("admin.refresh") }}</button>
      </div>

      <p v-if="!rows.length && !loading" class="empty">{{ t("adminProgress.empty") }}</p>

      <article v-for="row in rows" :key="row.subscriptionId" class="progress-card">
        <div class="head">
          <strong>{{ row.userName }}</strong>
          <span>{{ row.packageLabel }} ({{ row.packageCode }})</span>
        </div>

        <label class="deployment-field">
          {{ t("adminProgress.deploymentStatus") }}
          <select v-model="row.deploymentStatus" :disabled="loading">
            <option v-for="status in deploymentStatuses" :key="status" :value="status">
              {{ t(`adminProgress.deployment.${status}`) }}
            </option>
          </select>
        </label>

        <div class="quota-grid">
          <label>
            {{ t("adminProgress.posts") }} (0-{{ row.quotaPosts }})
            <input v-model.number="row.completedPosts" type="number" min="0" :max="row.quotaPosts" :disabled="loading" />
          </label>
          <label>
            {{ t("adminProgress.images") }} (0-{{ row.quotaImages }})
            <input v-model.number="row.completedImages" type="number" min="0" :max="row.quotaImages" :disabled="loading" />
          </label>
          <label>
            {{ t("adminProgress.videos") }} (0-{{ row.quotaVideos }})
            <input v-model.number="row.completedVideos" type="number" min="0" :max="row.quotaVideos" :disabled="loading" />
          </label>
        </div>

        <div class="service-grid">
          <div v-for="svc in row.services" :key="svc.serviceId" class="service-item">
            <span>{{ svc.serviceName }} ({{ svc.progressMode }})</span>
            <div class="service-input-row">
              <input
                :value="svc.percent"
                type="number"
                min="0"
                max="100"
                :disabled="loading"
                :aria-label="t('adminProgress.percent')"
                @input="patchService(row, svc.serviceId, $event.target.value)"
              />
              <input
                :value="svc.completedCount"
                type="number"
                min="0"
                :disabled="loading"
                :placeholder="t('adminProgress.completed')"
                @input="patchServiceCount(row, svc.serviceId, 'completedCount', $event.target.value)"
              />
              <input
                :value="svc.targetCount"
                type="number"
                min="0"
                :disabled="loading"
                :placeholder="t('adminProgress.target')"
                @input="patchServiceCount(row, svc.serviceId, 'targetCount', $event.target.value)"
              />
            </div>
          </div>
        </div>

        <button class="btn-primary" :disabled="loading" @click="saveRow(row)">
          {{ t("adminProgress.save") }}
        </button>
      </article>

      <p v-if="success" class="success">{{ success }}</p>
      <p v-if="error" class="error">{{ error }}</p>
    </section>
  </AppShell>
</template>

<style scoped>
.page-card { max-width: 1100px; }
.page-title { margin: 0 0 8px; }
.page-desc { margin: 0 0 14px; color: #64748b; }
.toolbar { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 14px; }
.toolbar select { flex: 1 1 220px; }
.progress-card { border: 1px solid #e2e8f0; border-radius: 12px; padding: 14px; margin-bottom: 12px; }
.head { display: flex; justify-content: space-between; gap: 10px; flex-wrap: wrap; margin-bottom: 10px; }
.deployment-field { margin-bottom: 10px; max-width: 260px; }
.quota-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; margin-bottom: 10px; }
.service-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; margin-bottom: 10px; }
label, .service-item { display: flex; flex-direction: column; gap: 6px; font-size: 0.85rem; color: #475569; }
input, select { width: 100%; min-width: 0; box-sizing: border-box; padding: 8px 10px; border: 1px solid #cbd5e1; border-radius: 8px; font-family: inherit; }
.service-input-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 6px; }
.btn-outline { padding: 8px 12px; border: 1px solid #cbd5e1; background: white; border-radius: 8px; }
.btn-primary { padding: 9px 14px; border: none; background: #1a6dff; color: white; border-radius: 8px; font-weight: 600; }
.success { color: #16a34a; margin-top: 8px; }
.error { color: #dc2626; margin-top: 8px; }
.empty { color: #94a3b8; }

@media (max-width: 760px) {
  .card {
    padding: 16px;
  }

  .toolbar,
  .toolbar select,
  .toolbar button,
  .deployment-field {
    width: 100%;
    max-width: none;
  }

  .quota-grid,
  .service-grid,
  .service-input-row {
    grid-template-columns: 1fr;
  }
}
</style>

