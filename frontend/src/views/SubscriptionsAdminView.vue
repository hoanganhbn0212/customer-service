<script setup>
import { computed, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import { ApiError, AdminService } from "../api";
import AppShell from "../components/layout/AppShell.vue";
import {
  assignUserSubscription,
  createAdminPackage,
  deactivateAdminPackage,
  listAdminPackages,
  listPackageUpgradeRequests,
  reviewPackageUpgradeRequest,
  updateAdminPackage
} from "../api/adminApi";

const { t } = useI18n();

const activeTab = ref("assign");
const users = ref([]);
const packages = ref([]);
const upgradeRequests = ref([]);
const upgradeFilter = ref("PENDING");

const loading = ref(false);
const error = ref("");
const success = ref("");

const assignForm = ref({
  userId: "",
  packageCode: "PRO_15",
  displayTitle: "",
  startDate: "",
  endDate: ""
});

const packageForm = ref({
  code: "",
  tier: "BASIC",
  label: "",
  quotaPosts: 15,
  quotaImages: 15,
  quotaVideos: 0,
  sortOrder: 0
});
const editingPackageCode = ref("");
const reviewNote = ref("");

const pendingCount = computed(
  () => upgradeRequests.value.filter((r) => r.status === "PENDING").length
);

const packageLabel = (code) => {
  const pkg = packages.value.find((p) => p.code === code);
  return pkg ? pkg.label : code;
};

const formatDateTime = (iso) => {
  if (!iso) return "-";
  return new Date(iso).toLocaleString("vi-VN");
};

const loadUsers = async () => {
  users.value = await AdminService.listUsers();
};

const loadPackages = async () => {
  packages.value = await listAdminPackages();
  if (packages.value.length && !packages.value.some((pkg) => pkg.code === assignForm.value.packageCode)) {
    assignForm.value.packageCode = packages.value[0].code;
  }
};

const loadUpgrades = async () => {
  upgradeRequests.value = await listPackageUpgradeRequests(upgradeFilter.value || undefined);
};

const loadAll = async () => {
  loading.value = true;
  error.value = "";
  try {
    await Promise.all([loadUsers(), loadPackages(), loadUpgrades()]);
  } catch (e) {
    error.value = e instanceof ApiError ? e.body?.message || e.message : String(e);
  } finally {
    loading.value = false;
  }
};

const submitAssign = async () => {
  if (!assignForm.value.userId) {
    error.value = t("adminSubs.selectUserRequired");
    return;
  }
  loading.value = true;
  error.value = "";
  success.value = "";
  try {
    const body = {
      packageCode: assignForm.value.packageCode,
      displayTitle: assignForm.value.displayTitle.trim() || undefined,
      startDate: assignForm.value.startDate || undefined,
      endDate: assignForm.value.endDate || undefined
    };
    await assignUserSubscription(assignForm.value.userId, body);
    success.value = t("adminSubs.assignSuccess");
    assignForm.value.displayTitle = "";
    assignForm.value.startDate = "";
    assignForm.value.endDate = "";
  } catch (e) {
    error.value = e instanceof ApiError ? e.body?.message || e.message : e.body?.code || String(e);
  } finally {
    loading.value = false;
  }
};

const resetPackageForm = () => {
  editingPackageCode.value = "";
  packageForm.value = {
    code: "",
    tier: "BASIC",
    label: "",
    quotaPosts: 15,
    quotaImages: 15,
    quotaVideos: 0,
    sortOrder: packages.value.length + 1
  };
};

const editPackage = (pkg) => {
  editingPackageCode.value = pkg.code;
  packageForm.value = {
    code: pkg.code,
    tier: pkg.tier,
    label: pkg.label,
    quotaPosts: pkg.quotaPosts,
    quotaImages: pkg.quotaImages,
    quotaVideos: pkg.quotaVideos,
    sortOrder: packages.value.findIndex((item) => item.code === pkg.code) + 1
  };
};

const submitPackage = async () => {
  loading.value = true;
  error.value = "";
  success.value = "";
  const body = {
    code: packageForm.value.code.trim(),
    tier: packageForm.value.tier,
    label: packageForm.value.label.trim(),
    quotaPosts: Number(packageForm.value.quotaPosts || 0),
    quotaImages: Number(packageForm.value.quotaImages || 0),
    quotaVideos: Number(packageForm.value.quotaVideos || 0),
    sortOrder: Number(packageForm.value.sortOrder || 0),
    active: true
  };
  try {
    if (editingPackageCode.value) {
      await updateAdminPackage(editingPackageCode.value, body);
      success.value = t("adminSubs.packageUpdateSuccess");
    } else {
      await createAdminPackage(body);
      success.value = t("adminSubs.packageCreateSuccess");
    }
    await loadPackages();
    resetPackageForm();
  } catch (e) {
    error.value = e instanceof ApiError ? e.body?.message || e.message : e.body?.code || String(e);
  } finally {
    loading.value = false;
  }
};

const deactivatePackage = async (pkg) => {
  loading.value = true;
  error.value = "";
  success.value = "";
  try {
    await deactivateAdminPackage(pkg.code);
    success.value = t("adminSubs.packageDeactivateSuccess");
    await loadPackages();
    if (editingPackageCode.value === pkg.code) {
      resetPackageForm();
    }
  } catch (e) {
    error.value = e instanceof ApiError ? e.body?.message || e.message : e.body?.code || String(e);
  } finally {
    loading.value = false;
  }
};

const review = async (row, status) => {
  loading.value = true;
  error.value = "";
  success.value = "";
  try {
    await reviewPackageUpgradeRequest(row.id, {
      status,
      adminNote: reviewNote.value.trim() || undefined
    });
    success.value =
      status === "APPROVED" ? t("adminSubs.approveSuccess") : t("adminSubs.rejectSuccess");
    reviewNote.value = "";
    await loadUpgrades();
  } catch (e) {
    error.value = e instanceof ApiError ? e.body?.message || e.message : e.body?.code || String(e);
  } finally {
    loading.value = false;
  }
};

const onFilterChange = async () => {
  loading.value = true;
  try {
    await loadUpgrades();
  } catch (e) {
    error.value = String(e);
  } finally {
    loading.value = false;
  }
};

onMounted(loadAll);
</script>

<template>
  <AppShell active-nav="subscriptions">
    <section class="card page-card">
      <h1 class="page-title">{{ t("adminSubs.title") }}</h1>
      <p class="page-desc">{{ t("adminSubs.subtitle") }}</p>

      <div class="tabs">
        <button
          type="button"
          class="tab"
          :class="{ active: activeTab === 'assign' }"
          @click="activeTab = 'assign'"
        >
          {{ t("adminSubs.tabAssign") }}
        </button>
        <button
          type="button"
          class="tab"
          :class="{ active: activeTab === 'upgrades' }"
          @click="activeTab = 'upgrades'"
        >
          {{ t("adminSubs.tabUpgrades") }}
          <span v-if="pendingCount" class="badge-pending">{{ pendingCount }}</span>
        </button>
        <button
          type="button"
          class="tab"
          :class="{ active: activeTab === 'packages' }"
          @click="activeTab = 'packages'"
        >
          {{ t("adminSubs.tabPackages") }}
        </button>
      </div>

      <template v-if="activeTab === 'assign'">
        <section class="section">
          <h2>{{ t("adminSubs.assignTitle") }}</h2>
          <form class="assign-form" @submit.prevent="submitAssign">
            <label class="field-label">{{ t("adminSubs.selectUser") }}</label>
            <select v-model="assignForm.userId" required :disabled="loading">
              <option value="">{{ t("adminSubs.selectUserPlaceholder") }}</option>
              <option v-for="u in users" :key="u.id" :value="u.id">
                {{ u.userName }} ({{ u.role }})
              </option>
            </select>

            <label class="field-label">{{ t("adminSubs.selectPackage") }}</label>
            <select v-model="assignForm.packageCode" required :disabled="loading">
              <option v-for="pkg in packages" :key="pkg.code" :value="pkg.code">
                {{ pkg.label }} - {{ pkg.tier }}
              </option>
            </select>

            <label class="field-label">{{ t("adminSubs.displayTitle") }}</label>
            <input
              v-model="assignForm.displayTitle"
              type="text"
              :placeholder="t('adminSubs.displayTitlePlaceholder')"
              :disabled="loading"
            />

            <div class="date-row">
              <div class="date-field">
                <label class="field-label">{{ t("adminSubs.startDate") }}</label>
                <input v-model="assignForm.startDate" type="date" :disabled="loading" />
              </div>
              <div class="date-field">
                <label class="field-label">{{ t("adminSubs.endDate") }}</label>
                <input v-model="assignForm.endDate" type="date" :disabled="loading" />
              </div>
            </div>

            <button type="submit" class="btn-primary" :disabled="loading">
              {{ t("adminSubs.assignSubmit") }}
            </button>
          </form>
        </section>
      </template>

      <template v-else-if="activeTab === 'upgrades'">
        <section class="section">
          <div class="section-head">
            <h2>{{ t("adminSubs.upgradesTitle") }}</h2>
            <div class="filter-row">
              <select v-model="upgradeFilter" :disabled="loading" @change="onFilterChange">
                <option value="PENDING">{{ t("adminSubs.statusPending") }}</option>
                <option value="APPROVED">{{ t("adminSubs.statusApproved") }}</option>
                <option value="REJECTED">{{ t("adminSubs.statusRejected") }}</option>
                <option value="">{{ t("adminSubs.statusAll") }}</option>
              </select>
              <button type="button" class="btn-outline" :disabled="loading" @click="loadUpgrades">
                {{ t("admin.refresh") }}
              </button>
            </div>
          </div>

          <label class="field-label">{{ t("adminSubs.adminNote") }}</label>
          <input
            v-model="reviewNote"
            type="text"
            class="review-note-input"
            :placeholder="t('adminSubs.adminNotePlaceholder')"
            :disabled="loading"
          />

          <p v-if="!upgradeRequests.length" class="empty">{{ t("adminSubs.upgradesEmpty") }}</p>

          <table v-else class="data-table">
            <thead>
              <tr>
                <th>{{ t("admin.userName") }}</th>
                <th>{{ t("adminSubs.fromPackage") }}</th>
                <th>{{ t("adminSubs.toPackage") }}</th>
                <th>{{ t("adminSubs.customerNote") }}</th>
                <th>{{ t("admin.status") }}</th>
                <th>{{ t("adminSubs.requestedAt") }}</th>
                <th>{{ t("admin.actions") }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in upgradeRequests" :key="row.id">
                <td>{{ row.userName }}</td>
                <td>{{ packageLabel(row.fromPackageCode) }}</td>
                <td>{{ packageLabel(row.toPackageCode) }}</td>
                <td class="note-cell">{{ row.note || "-" }}</td>
                <td>
                  <span class="status-badge" :class="row.status.toLowerCase()">{{ row.status }}</span>
                </td>
                <td>{{ formatDateTime(row.createdAt) }}</td>
                <td>
                  <template v-if="row.status === 'PENDING'">
                    <button
                      type="button"
                      class="btn-approve"
                      :disabled="loading"
                      @click="review(row, 'APPROVED')"
                    >
                      {{ t("adminSubs.approve") }}
                    </button>
                    <button
                      type="button"
                      class="btn-reject"
                      :disabled="loading"
                      @click="review(row, 'REJECTED')"
                    >
                      {{ t("adminSubs.reject") }}
                    </button>
                  </template>
                  <span v-else class="reviewed-meta">
                    {{ row.adminNote || "-" }}
                    <small v-if="row.reviewedAt">{{ formatDateTime(row.reviewedAt) }}</small>
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </section>
      </template>

      <template v-else>
        <section class="section package-admin-grid">
          <form class="assign-form package-form" @submit.prevent="submitPackage">
            <h2>
              {{
                editingPackageCode
                  ? t("adminSubs.packageEditTitle", { code: editingPackageCode })
                  : t("adminSubs.packageCreateTitle")
              }}
            </h2>

            <label class="field-label">{{ t("adminSubs.packageCode") }}</label>
            <input
              v-model="packageForm.code"
              type="text"
              :disabled="loading || Boolean(editingPackageCode)"
              required
            />

            <label class="field-label">{{ t("adminSubs.packageLabel") }}</label>
            <input v-model="packageForm.label" type="text" :disabled="loading" required />

            <label class="field-label">{{ t("adminSubs.packageTier") }}</label>
            <select v-model="packageForm.tier" :disabled="loading">
              <option value="BASIC">BASIC</option>
              <option value="PRO">PRO</option>
            </select>

            <div class="quota-row">
              <div>
                <label class="field-label">{{ t("adminSubs.quotaPosts") }}</label>
                <input v-model.number="packageForm.quotaPosts" type="number" min="0" :disabled="loading" />
              </div>
              <div>
                <label class="field-label">{{ t("adminSubs.quotaImages") }}</label>
                <input v-model.number="packageForm.quotaImages" type="number" min="0" :disabled="loading" />
              </div>
              <div>
                <label class="field-label">{{ t("adminSubs.quotaVideos") }}</label>
                <input v-model.number="packageForm.quotaVideos" type="number" min="0" :disabled="loading" />
              </div>
            </div>

            <label class="field-label">{{ t("adminSubs.sortOrder") }}</label>
            <input v-model.number="packageForm.sortOrder" type="number" :disabled="loading" />

            <div class="form-actions">
              <button type="submit" class="btn-primary" :disabled="loading">
                {{ editingPackageCode ? t("adminSubs.packageUpdate") : t("adminSubs.packageCreate") }}
              </button>
              <button type="button" class="btn-outline" :disabled="loading" @click="resetPackageForm">
                {{ t("adminSubs.packageReset") }}
              </button>
            </div>
          </form>

          <div class="package-list-panel">
            <div class="section-head">
              <h2>{{ t("adminSubs.packageListTitle") }}</h2>
              <button type="button" class="btn-outline" :disabled="loading" @click="loadPackages">
                {{ t("admin.refresh") }}
              </button>
            </div>
            <p v-if="!packages.length" class="empty">{{ t("adminSubs.packageEmpty") }}</p>
            <table v-else class="data-table package-table">
              <thead>
                <tr>
                  <th>{{ t("adminSubs.packageCode") }}</th>
                  <th>{{ t("adminSubs.packageLabel") }}</th>
                  <th>{{ t("adminSubs.packageTier") }}</th>
                  <th>{{ t("adminSubs.packageQuota") }}</th>
                  <th>{{ t("admin.actions") }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="pkg in packages" :key="pkg.code">
                  <td>{{ pkg.code }}</td>
                  <td>{{ pkg.label }}</td>
                  <td>{{ pkg.tier }}</td>
                  <td>{{ pkg.quotaPosts }} / {{ pkg.quotaImages }} / {{ pkg.quotaVideos }}</td>
                  <td>
                    <button type="button" class="btn-outline small" :disabled="loading" @click="editPackage(pkg)">
                      {{ t("adminSubs.packageEdit") }}
                    </button>
                    <button type="button" class="btn-reject" :disabled="loading" @click="deactivatePackage(pkg)">
                      {{ t("adminSubs.packageDeactivate") }}
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </template>

      <p v-if="success" class="success">{{ success }}</p>
      <p v-if="error" class="error">{{ error }}</p>
    </section>
  </AppShell>
</template>

<style scoped>
.page-card {
  max-width: 1100px;
}

.page-title {
  margin: 0 0 8px;
  font-size: 1.5rem;
  color: #3b4a5a;
}

.page-desc {
  margin: 0 0 20px;
  color: #7a8794;
}

.tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;
  border-bottom: 2px solid #e8ecef;
  padding-bottom: 0;
}

.tab {
  border: none;
  background: transparent;
  padding: 10px 16px;
  font-weight: 600;
  color: #94a3b8;
  cursor: pointer;
  position: relative;
  font-family: inherit;
}

.tab.active {
  color: #67a387;
}

.tab.active::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: -2px;
  height: 2px;
  background: #67a387;
}

.badge-pending {
  margin-left: 6px;
  background: #ff4d4f;
  color: #fff;
  font-size: 0.7rem;
  padding: 2px 6px;
  border-radius: 999px;
}

.section {
  margin-top: 8px;
}

.section h2 {
  margin: 0 0 16px;
  font-size: 1rem;
  color: #3b4a5a;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
}

.section-head h2 {
  margin: 0;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.assign-form {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 480px;
}

.field-label {
  margin-top: 12px;
  margin-bottom: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  color: #475569;
}

input,
select {
  width: 100%;
  box-sizing: border-box;
  min-height: 42px;
  padding: 10px 12px;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  font-size: 0.9rem;
  font-family: inherit;
}

.date-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(180px, 1fr));
  gap: 12px;
}

.date-field {
  min-width: 0;
}

.quota-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.package-admin-grid {
  display: grid;
  grid-template-columns: minmax(280px, 380px) 1fr;
  gap: 24px;
  align-items: start;
}

.package-form {
  background: #f8fafc;
  border: 1px solid #e8ecef;
  border-radius: 12px;
  padding: 18px;
}

.package-list-panel {
  min-width: 0;
}

.package-table td:last-child {
  white-space: nowrap;
}

.form-actions {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.review-note-input {
  width: 100%;
  max-width: 480px;
  margin-bottom: 16px;
}

.btn-primary {
  margin-top: 16px;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  background: #67a387;
  color: #fff;
  font-weight: 600;
  cursor: pointer;
  align-self: flex-start;
}

.btn-outline {
  padding: 8px 12px;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
}

.btn-outline.small {
  padding: 6px 10px;
  font-size: 0.8rem;
  margin-right: 6px;
}

.data-table {
  display: block;
  width: 100%;
  overflow-x: auto;
  border-collapse: collapse;
  font-size: 0.88rem;
  -webkit-overflow-scrolling: touch;
}

.data-table thead,
.data-table tbody {
  min-width: 760px;
  display: table;
  width: 100%;
  table-layout: auto;
}

.data-table th,
.data-table td {
  padding: 12px 10px;
  text-align: left;
  border-bottom: 1px solid #f0f3f5;
  vertical-align: top;
}

.note-cell {
  max-width: 180px;
  word-break: break-word;
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.72rem;
  font-weight: 700;
}

.status-badge.pending {
  background: #fff7ed;
  color: #ea580c;
}

.status-badge.approved {
  background: #ecfdf3;
  color: #16a34a;
}

.status-badge.rejected {
  background: #fef2f2;
  color: #dc2626;
}

.btn-approve,
.btn-reject {
  padding: 6px 10px;
  border: none;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  margin-right: 6px;
  margin-bottom: 4px;
}

.btn-approve {
  background: #ecfdf3;
  color: #16a34a;
}

.btn-reject {
  background: #fef2f2;
  color: #dc2626;
}

.reviewed-meta {
  display: block;
  font-size: 0.82rem;
  color: #64748b;
  max-width: 200px;
}

.reviewed-meta small {
  display: block;
  margin-top: 4px;
  color: #94a3b8;
}

.empty {
  color: #9aa5b1;
}

.success {
  color: #2e7d52;
  margin-top: 16px;
}

.error {
  color: #c62828;
  margin-top: 16px;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

@media (max-width: 900px) {
  .date-row,
  .package-admin-grid {
    grid-template-columns: 1fr;
  }

  .quota-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 700px) {
  .card {
    padding: 16px;
  }

  .tab {
    flex: 1 1 140px;
  }

  .filter-row,
  .filter-row select,
  .filter-row button {
    width: 100%;
  }
}
</style>
