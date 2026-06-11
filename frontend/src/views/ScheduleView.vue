<script setup>
import { onMounted } from "vue";
import { useI18n } from "vue-i18n";
import MobileAppShell from "../components/layout/MobileAppShell.vue";
import MobileScreenHeader from "../components/layout/MobileScreenHeader.vue";
import { useAppScreen } from "../composables/useAppScreen";

const { t } = useI18n();

const { screen, dailyScheduleItems, refreshHome } = useAppScreen("schedule");

onMounted(refreshHome);
</script>

<template>
  <MobileAppShell active-tab="schedule">
    <MobileScreenHeader :title="t(screen.titleKey)" :subtitle="t(screen.subtitleKey)" />

    <div class="mobile-page-body schedule-page">
      <section class="card schedule-card">
        <div class="schedule-head">
          <h2 class="section-title">{{ t("home.schedule") }}</h2>
        </div>

        <ul v-if="dailyScheduleItems.length" class="daily-schedule-list">
          <li v-for="(item, idx) in dailyScheduleItems" :key="idx" class="daily-schedule-card">
            <div class="date-row">
              <span class="field-label">{{ t("schedule.date") }}</span>
              <strong>{{ item.date }}</strong>
            </div>
            <div class="field-row">
              <span class="field-label">{{ t("schedule.task") }}</span>
              <span>{{ item.task }}</span>
            </div>
            <div class="field-row">
              <span class="field-label">{{ t("schedule.service") }}</span>
              <span>{{ item.service }}</span>
            </div>
            <div class="field-row">
              <span class="field-label">{{ t("schedule.status") }}</span>
              <span class="schedule-status">{{ item.status }}</span>
            </div>
            <div class="field-row note-row">
              <span class="field-label">{{ t("schedule.note") }}</span>
              <span>{{ item.note || t("schedule.noNote") }}</span>
            </div>
          </li>
        </ul>
        <p v-else class="empty-day">{{ t("home.noTasks") }}</p>
      </section>
    </div>
  </MobileAppShell>
</template>

<style scoped>
@import "../styles/mobile-page.css";

.schedule-page {
  padding-top: 2px;
}

.schedule-card {
  min-height: 360px;
}

.schedule-head {
  margin-bottom: 16px;
}

.daily-schedule-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 12px;
}

.daily-schedule-card {
  padding: 16px;
  border: 1px solid #e8eef5;
  border-radius: 16px;
  background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
}

.date-row,
.field-row {
  display: grid;
  grid-template-columns: 118px 1fr;
  gap: 10px;
  align-items: start;
}

.date-row {
  margin-bottom: 12px;
}

.date-row strong {
  color: #0f172a;
  font-size: 1rem;
}

.field-row {
  padding: 9px 0;
  border-top: 1px solid #edf2f7;
  color: #0f172a;
  font-size: 0.88rem;
  line-height: 1.45;
}

.field-label {
  color: #64748b;
  font-size: 0.78rem;
  font-weight: 700;
}

.schedule-status {
  width: fit-content;
  padding: 4px 10px;
  border-radius: 999px;
  background: #eff6ff;
  color: #1d4ed8;
  font-weight: 700;
}

.note-row {
  padding-bottom: 0;
}

@media (max-width: 380px) {
  .date-row,
  .field-row {
    grid-template-columns: 1fr;
    gap: 4px;
  }
}
</style>
