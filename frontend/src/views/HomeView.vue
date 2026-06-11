<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import MobileAppShell from "../components/layout/MobileAppShell.vue";
import MobileScreenHeader from "../components/layout/MobileScreenHeader.vue";
import { useAppScreen } from "../composables/useAppScreen";
import { getUnreadNotificationCount } from "../api/mobileApi";

const router = useRouter();
const { t } = useI18n();

const {
  screen,
  pkg,
  subscriptionTitle,
  packageStart,
  packageEnd,
  overallPercent,
  overallCompleted,
  overallTotal,
  progressBreakdown,
  overallStatus,
  subscriptionStatus,
  dashboardStatus,
  services,
  weekDays,
  tasksForSelectedDay,
  monthLabel,
  progressRing,
  selectDay,
  prevMonth,
  nextMonth,
  hasBlock,
  refreshHome
} = useAppScreen("home");

const unreadCount = ref(0);

onMounted(async () => {
  await refreshHome();
  try {
    const data = await getUnreadNotificationCount();
    unreadCount.value = data.count ?? 0;
  } catch {
    unreadCount.value = 0;
  }
});

const packageLabel = (p) => subscriptionTitle.value || p.label || t(p.labelKey);
const serviceName = (item) => item.name || t(item.nameKey);
const taskTitle = (task) => task.title || t(task.titleKey);
const serviceTrackMode = (item) => String(item.trackMode || "").toLowerCase();
const isQuantityService = (item) => serviceTrackMode(item) === "quantity";

const formatDate = (iso) => {
  if (!iso) return "—";
  const [y, m, d] = iso.split("-");
  return `${d}/${m}/${y}`;
};

const statusLabel = (status) => {
  if (status === "done") return t("home.statusDone");
  if (status === "progress") return t("home.statusProgress");
  if (status === "paused") return t("home.statusPaused");
  return t("home.statusPending");
};

const packageTypeLabel = (p) => {
  const tierKey = p.tier === "PRO" ? "home.packageTierPremium" : "home.packageTierBasic";
  return t("home.packageType", {
    posts: p.quotaPosts ?? 0,
    tier: t(tierKey)
  });
};

const mediaQuotaLabel = (p) => {
  const images = p.quotaImages ?? 0;
  const videos = p.quotaVideos ?? 0;
  if (videos > 0) {
    return t("home.mediaQuotaWithVideo", { images, videos });
  }
  return t("home.mediaQuotaImages", { images });
};

const packageStatusLabel = (status) => {
  if (status === "ACTIVE") return t("home.pkgActive");
  if (status === "EXPIRED") return t("home.pkgExpired");
  return t("home.pkgCancelled");
};

const serviceIcon = (id) => {
  const map = {
    fanpage: "doc",
    content: "edit",
    ads: "ads",
    report: "chart",
    posts: "edit",
    design: "image",
    video: "video",
    cover: "image",
    like: "heart"
  };
  return map[id] || "doc";
};

const goNotifications = () => router.push("/notifications");
</script>

<template>
  <MobileAppShell active-tab="overview">
    <MobileScreenHeader
      :title="t(screen.titleKey)"
      :show-bell="screen.showBell"
      :unread-count="unreadCount"
      @bell="goNotifications"
    />

    <div class="mobile-page-body">
      <article v-if="hasBlock('packageCard')" class="card package-card blue-card">
        <button type="button" class="package-inner">
          <span class="crown-wrap" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M4 18h16l-2-9-4 4-3-5-3 5-4-4-2 9zm2.2-2l1.4-6.3 3.2 3.2 3.4-5.7 3.4 5.7 3.2-3.2 1.4 6.3H6.2z"
              />
            </svg>
          </span>
          <span class="package-text">
            <span class="package-row">
              <strong>{{ packageLabel(pkg) }}</strong>
              <span class="tier-pill">{{ t("home.packageTypeLabel") }}: {{ packageTypeLabel(pkg) }}</span>
              <span class="status-pill" :class="dashboardStatus">
                {{ statusLabel(dashboardStatus) }}
              </span>
            </span>
            <span class="package-meta-grid">
              <span>
                <small>{{ t("home.packagePosts") }}</small>
                <b>{{ pkg.quotaPosts ?? 0 }}</b>
              </span>
              <span>
                <small>{{ t("home.packageMedia") }}</small>
                <b>{{ mediaQuotaLabel(pkg) }}</b>
              </span>
              <span>
                <small>{{ t("home.packageLifecycle") }}</small>
                <b>{{ packageStatusLabel(subscriptionStatus) }}</b>
              </span>
            </span>
            <span class="package-date">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="4" width="18" height="18" rx="2" />
                <path d="M16 2v4M8 2v4M3 10h18" />
              </svg>
              {{ formatDate(packageStart) }} – {{ formatDate(packageEnd) }}
            </span>
          </span>
        </button>
      </article>

      <section v-if="hasBlock('overallProgress')" class="card">
        <h2 class="section-title">{{ t("home.overallProgress") }}</h2>
        <div class="progress-block">
          <div class="ring-wrap">
            <svg class="ring" viewBox="0 0 120 120">
              <circle cx="60" cy="60" :r="progressRing.r" fill="none" stroke="#dbeafe" stroke-width="10" />
              <circle
                cx="60"
                cy="60"
                :r="progressRing.r"
                fill="none"
                stroke="#2563eb"
                stroke-width="10"
                stroke-linecap="round"
                :stroke-dasharray="progressRing.c"
                :stroke-dashoffset="progressRing.offset"
                transform="rotate(-90 60 60)"
              />
            </svg>
            <span class="ring-label">{{ overallPercent }}%</span>
          </div>
          <div class="progress-meta">
            <p class="meta-title">{{ t("home.completionLabel") }}</p>
            <p class="formula-line">
              {{ t("home.progressFormula", { completed: overallCompleted, total: overallTotal }) }}
            </p>
            <div class="bar-track">
              <div class="bar-fill" :style="{ width: `${overallPercent}%` }" />
            </div>
            <div class="progress-breakdown">
              <div v-for="row in progressBreakdown" :key="row.id" class="progress-row">
                <span>{{ t(row.labelKey) }}</span>
                <strong>{{ row.completed }}/{{ row.total }}</strong>
                <div class="bar-track tiny">
                  <div class="bar-fill" :style="{ width: `${row.percent}%` }" />
                </div>
              </div>
            </div>
            <p class="status-line" :class="overallStatus">
              <span class="status-dot" :class="overallStatus" />
              {{ statusLabel(overallStatus) }}
            </p>
          </div>
        </div>
      </section>

      <section v-if="hasBlock('serviceList')" class="card">
        <h2 class="section-title">{{ t("home.serviceList") }}</h2>
        <ul class="service-list">
          <li v-for="item in services" :key="item.id" class="svc-row">
            <span class="svc-icon" :data-icon="serviceIcon(item.id)" />
            <div class="svc-body">
              <div class="svc-head">
                <span class="svc-name">{{ item.index }}. {{ serviceName(item) }}</span>
                <span class="status-pill mini" :class="item.status">{{ statusLabel(item.status) }}</span>
              </div>

              <template v-if="isQuantityService(item)">
                <div class="svc-quantity">
                  <strong>{{ item.completedCount ?? 0 }}</strong> / {{ item.totalCount ?? 0 }}
                  <span class="svc-percent">{{ item.percent }}%</span>
                </div>
                <div class="bar-track small">
                  <div class="bar-fill" :style="{ width: `${item.percent}%` }" />
                </div>
              </template>
              <template v-else>
                <div class="svc-status-only">{{ t("home.statusOnlyHint") }}: {{ statusLabel(item.status) }}</div>
              </template>
            </div>
          </li>
        </ul>
      </section>

      <section v-if="hasBlock('schedule')" class="card">
        <div class="schedule-head">
          <h2 class="section-title">{{ t("home.schedule") }}</h2>
          <div class="month-nav">
            <button type="button" class="nav-arrow" aria-label="Previous month" @click="prevMonth">‹</button>
            <span>{{ t("home.monthYear", { month: monthLabel.month, year: monthLabel.year }) }}</span>
            <button type="button" class="nav-arrow" aria-label="Next month" @click="nextMonth">›</button>
          </div>
        </div>

        <div class="week-strip">
          <button
            v-for="day in weekDays"
            :key="day.key"
            type="button"
            class="day-cell"
            :class="{ selected: day.isSelected }"
            @click="selectDay(day)"
          >
            <span class="dow">{{ t(day.labelKey) }}</span>
            <span class="dom">{{ day.date }}</span>
          </button>
        </div>

        <ul v-if="tasksForSelectedDay.length" class="task-list">
          <li v-for="(task, idx) in tasksForSelectedDay" :key="idx" class="task-item">
            <strong>{{ taskTitle(task) }}</strong>
            <span class="task-time">{{ task.time }}</span>
          </li>
        </ul>
        <p v-else class="empty-day">{{ t("home.noTasks") }}</p>
      </section>
    </div>
  </MobileAppShell>
</template>

<style scoped>
@import "../styles/mobile-page.css";

.blue-card {
  border: 1px solid #dbeafe;
  background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
}

.formula-line {
  margin: 0 0 8px;
  color: #1e40af;
  font-size: 0.82rem;
}

.package-meta-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  margin-top: 10px;
}

.package-meta-grid span {
  border: 1px solid #dbeafe;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.72);
  padding: 8px;
}

.package-meta-grid small {
  display: block;
  color: #64748b;
  font-size: 0.68rem;
  font-weight: 700;
  margin-bottom: 3px;
}

.package-meta-grid b {
  display: block;
  color: #0f172a;
  font-size: 0.78rem;
  line-height: 1.25;
}

.progress-breakdown {
  display: grid;
  gap: 8px;
  margin-top: 12px;
}

.progress-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 6px 12px;
  align-items: center;
  font-size: 0.82rem;
  color: #475569;
}

.progress-row strong {
  color: #1e40af;
}

.bar-track.tiny {
  grid-column: 1 / -1;
  height: 6px;
  margin: 0;
}

.svc-row {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.svc-body {
  flex: 1;
  min-width: 0;
}

.svc-head {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: center;
}

.svc-quantity {
  margin-top: 4px;
  color: #1e3a8a;
  font-size: 0.82rem;
  display: flex;
  justify-content: space-between;
}

.svc-status-only {
  margin-top: 6px;
  color: #64748b;
  font-size: 0.8rem;
}

.bar-track.small {
  margin-top: 6px;
  height: 8px;
}

.status-pill.active {
  background: #dcfce7;
  color: #166534;
}

.status-pill.progress {
  background: #eff6ff;
  color: #1d4ed8;
}

.status-pill.done {
  background: #dcfce7;
  color: #166534;
}

.status-pill.paused {
  background: #fef3c7;
  color: #92400e;
}

.status-pill.expired,
.status-pill.cancelled {
  background: #fee2e2;
  color: #991b1b;
}

.status-pill.mini {
  font-size: 0.68rem;
  padding: 2px 8px;
}

@media (max-width: 420px) {
  .package-meta-grid {
    grid-template-columns: 1fr;
  }
}
</style>
