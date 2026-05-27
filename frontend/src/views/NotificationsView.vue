<script setup>
import { onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import MobileAppShell from "../components/layout/MobileAppShell.vue";
import MobileScreenHeader from "../components/layout/MobileScreenHeader.vue";
import { useAppScreen } from "../composables/useAppScreen";
import { listNotifications, markNotificationRead } from "../api/mobileApi";

const { t } = useI18n();
const { screen } = useAppScreen("notifications");

const items = ref([]);
const loading = ref(true);

const typeLabel = (type) => {
  if (type === "FEEDBACK_REPLY") return t("notifications.typeFeedback");
  if (type === "PROMOTION") return t("notifications.typePromo");
  if (type === "SCHEDULE") return t("notifications.typeSchedule");
  return type;
};

const formatTime = (iso) => {
  if (!iso) return "";
  const d = new Date(iso);
  return d.toLocaleString("vi-VN", { hour: "2-digit", minute: "2-digit", day: "2-digit", month: "2-digit" });
};

const load = async () => {
  loading.value = true;
  try {
    const data = await listNotifications(0, 50, false);
    items.value = data.items || [];
  } catch {
    items.value = [];
  } finally {
    loading.value = false;
  }
};

const onOpen = async (item) => {
  if (!item.read) {
    try {
      await markNotificationRead(item.id);
      item.read = true;
    } catch {
      /* ignore */
    }
  }
};

onMounted(load);
</script>

<template>
  <MobileAppShell active-tab="notifications">
    <MobileScreenHeader :title="t(screen.titleKey)" :subtitle="t(screen.subtitleKey)" />

    <div class="mobile-page-body">
      <p v-if="loading" class="hint-box">...</p>
      <p v-else-if="!items.length" class="hint-box">{{ t("notifications.empty") }}</p>

      <article
        v-for="item in items"
        :key="item.id"
        class="card list-card"
        :class="{ 'list-card--unread': !item.read }"
        role="button"
        tabindex="0"
        @click="onOpen(item)"
      >
        <div class="list-row" :class="{ unread: !item.read }">
          <div class="list-body">
            <span class="type-tag">{{ typeLabel(item.type) }}</span>
            <strong>{{ item.title }}</strong>
            <p>{{ item.body }}</p>
            <span class="list-meta">{{ formatTime(item.createdAt) }}</span>
          </div>
        </div>
      </article>
    </div>
  </MobileAppShell>
</template>

<style scoped>
@import "../styles/mobile-page.css";

.list-card--unread {
  border-color: #bfdbfe;
}

.type-tag {
  display: inline-block;
  font-size: 0.65rem;
  font-weight: 700;
  color: #1a6dff;
  background: #eff6ff;
  padding: 2px 8px;
  border-radius: 999px;
  margin-bottom: 6px;
}
</style>
