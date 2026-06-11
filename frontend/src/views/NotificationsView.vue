<script setup>
import { onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import MobileAppShell from "../components/layout/MobileAppShell.vue";
import MobileScreenHeader from "../components/layout/MobileScreenHeader.vue";
import { useAppScreen } from "../composables/useAppScreen";
import { listNotifications, markNotificationRead } from "../api/mobileApi";

const { t } = useI18n();
const { screen, notifications } = useAppScreen("notifications");

const items = ref([]);
const loading = ref(true);

const typeLabel = (type) => {
  const key = `notifications.type.${type}`;
  const translated = t(key);
  return translated === key ? type : translated;
};

const statusLabel = (item) => {
  return item.read ? t("notifications.status.read") : t("notifications.status.unread");
};

const formatTime = (value) => {
  if (!value) return "";
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit"
  });
};

const normalizeNotification = (item) => ({
  id: item.id,
  title: item.title || (item.titleKey ? t(item.titleKey) : ""),
  body: item.body || (item.bodyKey ? t(item.bodyKey) : ""),
  createdAt: item.createdAt || item.time,
  type: item.type || "INFO",
  read: Boolean(item.read)
});

const load = async () => {
  loading.value = true;
  try {
    const data = await listNotifications(0, 50, false);
    const apiItems = data.items || [];
    items.value = apiItems.length
      ? apiItems.map(normalizeNotification)
      : notifications.value.map(normalizeNotification);
  } catch {
    items.value = notifications.value.map(normalizeNotification);
  } finally {
    loading.value = false;
  }
};

const onOpen = async (item) => {
  if (!item.read) {
    try {
      await markNotificationRead(item.id);
    } catch {
      /* Fallback/demo notifications may not exist in the backend yet. */
    }
    item.read = true;
  }
};

onMounted(load);
</script>

<template>
  <MobileAppShell active-tab="notifications">
    <MobileScreenHeader :title="t(screen.titleKey)" :subtitle="t(screen.subtitleKey)" />

    <div class="mobile-page-body notifications-page">
      <p v-if="loading" class="hint-box">...</p>
      <p v-else-if="!items.length" class="hint-box">{{ t("notifications.empty") }}</p>

      <article
        v-for="item in items"
        :key="item.id"
        class="card notification-card"
        :class="{ 'notification-card--unread': !item.read }"
        role="button"
        tabindex="0"
        @click="onOpen(item)"
      >
        <div class="notification-head">
          <span class="type-tag">{{ typeLabel(item.type) }}</span>
          <span class="read-state" :class="{ unread: !item.read }">{{ statusLabel(item) }}</span>
        </div>

        <h2 class="notification-title">{{ item.title }}</h2>
        <p class="notification-body">{{ item.body }}</p>

        <dl class="notification-meta">
          <div>
            <dt>{{ t("notifications.sentAt") }}</dt>
            <dd>{{ formatTime(item.createdAt) }}</dd>
          </div>
          <div>
            <dt>{{ t("notifications.typeLabel") }}</dt>
            <dd>{{ typeLabel(item.type) }}</dd>
          </div>
        </dl>
      </article>
    </div>
  </MobileAppShell>
</template>

<style scoped>
@import "../styles/mobile-page.css";

.notifications-page {
  gap: 12px;
}

.notification-card {
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.notification-card--unread {
  border-color: #bfdbfe;
  box-shadow: 0 8px 28px rgba(37, 99, 235, 0.12);
}

.notification-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.type-tag,
.read-state {
  display: inline-flex;
  width: fit-content;
  align-items: center;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 0.68rem;
  font-weight: 800;
}

.type-tag {
  color: #1d4ed8;
  background: #eff6ff;
}

.read-state {
  color: #64748b;
  background: #f1f5f9;
}

.read-state.unread {
  color: #b45309;
  background: #fef3c7;
}

.notification-title {
  margin: 0 0 8px;
  color: #0f172a;
  font-size: 0.98rem;
  line-height: 1.35;
}

.notification-body {
  margin: 0;
  color: #475569;
  font-size: 0.86rem;
  line-height: 1.55;
}

.notification-meta {
  display: grid;
  gap: 8px;
  margin: 14px 0 0;
  padding-top: 12px;
  border-top: 1px solid #edf2f7;
}

.notification-meta div {
  display: grid;
  grid-template-columns: 96px 1fr;
  gap: 10px;
}

.notification-meta dt,
.notification-meta dd {
  margin: 0;
  font-size: 0.78rem;
  line-height: 1.4;
}

.notification-meta dt {
  color: #64748b;
  font-weight: 700;
}

.notification-meta dd {
  color: #0f172a;
  font-weight: 600;
}

@media (max-width: 380px) {
  .notification-meta div {
    grid-template-columns: 1fr;
    gap: 3px;
  }
}
</style>
