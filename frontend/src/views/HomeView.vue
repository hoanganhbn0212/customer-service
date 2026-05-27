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
  overallStatus,
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

const packageLabel = (p) => subscriptionTitle.value || t(p.labelKey);
const serviceName = (item) => item.name || t(item.nameKey);
const taskTitle = (task) => task.title || t(task.titleKey);

const formatDate = (iso) => {
  const [y, m, d] = iso.split("-");
  return `${d}/${m}/${y}`;
};

const statusLabel = (status) => {
  if (status === "done") return t("home.statusDone");
  if (status === "progress") return t("home.statusProgress");
  return t("home.statusPending");
};

const serviceIcon = (id) => {
  const map = {
    fanpage: "doc",
    content: "edit",
    ads: "ads",
    report: "chart",
    posts: "edit",
    design: "image",
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
      <article v-if="hasBlock('packageCard')" class="card package-card">
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
          <span class="chevron" aria-hidden="true">›</span>
        </button>
      </article>

      <section v-if="hasBlock('overallProgress')" class="card">
        <h2 class="section-title">{{ t("home.overallProgress") }}</h2>
        <div class="progress-block">
          <div class="ring-wrap">
            <svg class="ring" viewBox="0 0 120 120">
              <circle cx="60" cy="60" :r="progressRing.r" fill="none" stroke="#e8eef5" stroke-width="10" />
              <circle
                cx="60"
                cy="60"
                :r="progressRing.r"
                fill="none"
                stroke="#1a6dff"
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
            <div class="bar-track">
              <div class="bar-fill" :style="{ width: `${overallPercent}%` }" />
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
          <li v-for="item in services" :key="item.id">
            <span class="svc-icon" :data-icon="serviceIcon(item.id)" />
            <span class="svc-body">
              <span class="svc-name">{{ item.index }}. {{ serviceName(item) }}</span>
              <span class="svc-percent">{{ item.percent }}%</span>
            </span>
            <span class="status-pill" :class="item.status">{{ statusLabel(item.status) }}</span>
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
</style>
