<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import MobileAppShell from "../components/layout/MobileAppShell.vue";
import MobileScreenHeader from "../components/layout/MobileScreenHeader.vue";
import { useAppScreen } from "../composables/useAppScreen";
import { SERVICE_FILTER_IDS } from "../config/appScreenConfig";

const router = useRouter();
const { t } = useI18n();

const {
  screen,
  pkg,
  tier,
  subscriptionTitle,
  activePackageTitleKey,
  packageStart,
  packageEnd,
  services,
  implementationTasks,
  refreshServices
} = useAppScreen("services");

const activeTab = ref("implementation");
const activeFilter = ref("all");

onMounted(() => {
  refreshServices(activeFilter.value);
});

watch(activeFilter, (category) => {
  refreshServices(category);
});

const packageTitle = computed(
  () => subscriptionTitle.value || (activePackageTitleKey.value ? t(activePackageTitleKey.value) : t(pkg.value.labelKey))
);

const taskTitle = (task) => task.title || t(task.titleKey);
const serviceName = (item) => item.name || t(item.nameKey);
const serviceDesc = (item) => item.desc || (item.descKey ? t(item.descKey) : "");

const formatDate = (iso) => {
  const [y, m, d] = iso.split("-");
  return `${d}/${m}/${y}`;
};

const formatDisplayDate = (iso) => {
  const [y, m, d] = iso.split("-");
  return `${d}/${m}/${y}`;
};

const statusLabel = (status) => {
  if (status === "approved") return t("services.status.approved");
  if (status === "waiting_feedback") return t("services.status.waiting");
  return t("services.status.inProgress");
};

const filteredTasks = computed(() => implementationTasks.value);

const visibleFilters = computed(() => {
  if (tier.value === "BASIC") {
    return SERVICE_FILTER_IDS.filter((id) => id === "all" || id === "content");
  }
  return SERVICE_FILTER_IDS;
});

const taskPercent = (task) => {
  if (!task.total) return 0;
  return Math.min(100, Math.round((task.current / task.total) * 100));
};

const onTaskClick = (task) => {
  if (task.reviewable && task.deliverableId) {
    router.push({ name: "service-review", params: { deliverableId: task.deliverableId } });
    return;
  }
  if (task.category === "content") {
    window.alert(t("services.contentDetailHint"));
  }
};

const goNotifications = () => router.push("/notifications");
</script>

<template>
  <MobileAppShell active-tab="services">
    <MobileScreenHeader
      :title="t(screen.titleKey)"
      :show-bell="screen.showBell"
      @bell="goNotifications"
    />

    <div class="mobile-page-body services-page">
      <article class="card package-card active-package-card">
        <div class="package-inner static">
          <span class="crown-wrap" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M4 18h16l-2-9-4 4-3-5-3 5-4-4-2 9zm2.2-2l1.4-6.3 3.2 3.2 3.4-5.7 3.4 5.7 3.2-3.2 1.4 6.3H6.2z"
              />
            </svg>
          </span>
          <span class="package-text">
            <span class="package-row">
              <strong>{{ packageTitle }}</strong>
              <span class="tier-pill">{{ t(pkg.tierKey) }}</span>
            </span>
            <span class="package-date">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="4" width="18" height="18" rx="2" />
                <path d="M16 2v4M8 2v4M3 10h18" />
              </svg>
              {{ formatDate(packageStart) }} – {{ formatDate(packageEnd) }}
            </span>
          </span>
        </div>
      </article>

      <div class="screen-tabs" role="tablist">
        <button
          type="button"
          role="tab"
          class="screen-tab"
          :class="{ active: activeTab === 'implementation' }"
          :aria-selected="activeTab === 'implementation'"
          @click="activeTab = 'implementation'"
        >
          {{ t("services.tab.implementation") }}
        </button>
        <button
          type="button"
          role="tab"
          class="screen-tab"
          :class="{ active: activeTab === 'packageInfo' }"
          :aria-selected="activeTab === 'packageInfo'"
          @click="activeTab = 'packageInfo'"
        >
          {{ t("services.tab.packageInfo") }}
        </button>
      </div>

      <template v-if="activeTab === 'implementation'">
        <div class="filter-chips" role="group" :aria-label="t('services.filterLabel')">
          <button
            v-for="filterId in visibleFilters"
            :key="filterId"
            type="button"
            class="chip"
            :class="{ active: activeFilter === filterId }"
            @click="activeFilter = filterId"
          >
            {{ t(`services.filter.${filterId}`) }}
          </button>
        </div>

        <p v-if="!filteredTasks.length" class="hint-box">{{ t("services.emptyTasks") }}</p>

        <article
          v-for="task in filteredTasks"
          :key="task.id"
          class="card impl-card"
          role="button"
          tabindex="0"
          @click="onTaskClick(task)"
          @keydown.enter="onTaskClick(task)"
        >
          <div class="impl-head">
            <strong>{{ t(task.titleKey) }}</strong>
            <span class="status-pill" :class="task.status">{{ statusLabel(task.status) }}</span>
          </div>
          <div class="impl-progress-row">
            <span class="impl-count">{{ task.current }}/{{ task.total }}</span>
            <div class="bar-track compact">
              <div class="bar-fill" :style="{ width: `${taskPercent(task)}%` }" />
            </div>
          </div>
          <p class="impl-date">{{ formatDisplayDate(task.date) }}</p>
        </article>
      </template>

      <template v-else>
        <p class="package-info-intro">{{ t("services.packageInfoIntro") }}</p>
        <article v-for="item in services" :key="item.id" class="card info-card">
          <strong>{{ serviceName(item) }}</strong>
          <p v-if="serviceDesc(item)">{{ serviceDesc(item) }}</p>
        </article>
        <p v-if="!services.length" class="hint-box">{{ t("services.empty") }}</p>
      </template>
    </div>
  </MobileAppShell>
</template>

<style scoped>
@import "../styles/mobile-page.css";

.services-page {
  gap: 12px;
}

.active-package-card {
  margin-top: -4px;
}

.package-inner.static {
  cursor: default;
}

.screen-tabs {
  display: flex;
  gap: 20px;
  border-bottom: 2px solid #e8eef5;
  padding: 0 4px 0;
  margin-top: 4px;
}

.screen-tab {
  border: none;
  background: transparent;
  padding: 10px 2px 12px;
  font-size: 0.88rem;
  font-weight: 600;
  color: #94a3b8;
  cursor: pointer;
  position: relative;
  font-family: inherit;
}

.screen-tab.active {
  color: #1a6dff;
}

.screen-tab.active::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: -2px;
  height: 2px;
  background: #1a6dff;
  border-radius: 2px 2px 0 0;
}

.filter-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 4px 0 2px;
}

.chip {
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #64748b;
  font-size: 0.8rem;
  font-weight: 600;
  padding: 8px 14px;
  border-radius: 999px;
  cursor: pointer;
  font-family: inherit;
}

.chip.active {
  background: #1a6dff;
  border-color: #1a6dff;
  color: #fff;
}

.impl-card {
  padding: 16px;
  cursor: pointer;
  transition: box-shadow 0.15s ease;
}

.impl-card:active {
  box-shadow: 0 4px 16px rgba(15, 50, 100, 0.1);
}

.impl-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}

.impl-head strong {
  font-size: 0.92rem;
  color: #0f172a;
  line-height: 1.35;
}

.impl-progress-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.impl-count {
  font-size: 0.82rem;
  font-weight: 700;
  color: #475569;
  min-width: 42px;
}

.bar-track.compact {
  flex: 1;
  margin: 0;
  height: 6px;
}

.impl-date {
  margin: 0;
  font-size: 0.78rem;
  font-weight: 600;
  color: #94a3b8;
}

.status-pill.approved {
  background: #ecfdf3;
  color: #16a34a;
}

.status-pill.in_progress {
  background: #eff6ff;
  color: #1a6dff;
}

.status-pill.waiting_feedback {
  background: #fff7ed;
  color: #ea580c;
}

.package-info-intro {
  margin: 0;
  font-size: 0.85rem;
  color: #64748b;
  line-height: 1.5;
}

.info-card strong {
  display: block;
  font-size: 0.92rem;
  color: #0f172a;
  margin-bottom: 6px;
}

.info-card p {
  margin: 0;
  font-size: 0.82rem;
  color: #64748b;
  line-height: 1.45;
}
</style>
