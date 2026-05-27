<script setup>
import { computed } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { canEditPages, canManageUsers, clearSession } from "../../auth/session";

defineProps({
  activeNav: {
    type: String,
    default: "home"
  }
});

const router = useRouter();
const { t } = useI18n();

const navItems = computed(() => {
  const items = [
    { id: "home", icon: "star", route: "/home" },
    { id: "customers", icon: "rocket", route: "/customers" }
  ];
  if (canManageUsers()) {
    items.push({ id: "users", icon: "user", route: "/admin/users" });
    items.push({ id: "subscriptions", icon: "package", route: "/admin/subscriptions" });
  }
  if (canEditPages()) {
    items.push({ id: "theme", icon: "theme", route: "/admin/theme" });
  }
  return items;
});

const go = (item) => {
  if (item.route) router.push(item.route);
};

const logout = () => {
  clearSession();
  router.push("/login");
};
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar">
      <button
        v-for="item in navItems"
        :key="item.id"
        type="button"
        class="nav-btn"
        :class="{ active: activeNav === item.id }"
        :title="
          item.id === 'users'
            ? t('admin.nav')
            : item.id === 'subscriptions'
              ? t('adminSubs.nav')
              : item.id === 'theme'
                ? t('theme.nav')
                : item.id
        "
        @click="go(item)"
      >
        <span class="nav-icon" :data-icon="item.icon" />
      </button>
    </aside>

    <div class="main-wrap">
      <header class="topbar">
        <div class="topbar-left">
          <span class="logo-mark">a</span>
          <button type="button" class="icon-btn search-btn" aria-label="Search">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="7" />
              <path d="M20 20l-3-3" />
            </svg>
          </button>
        </div>
        <div class="topbar-right">
          <button type="button" class="profile-btn" @click="logout">
            <span class="avatar" />
            <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
              <path d="M7 10l5 5 5-5z" />
            </svg>
          </button>
          <button type="button" class="calendar-btn" aria-label="Calendar">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="4" width="18" height="18" rx="2" />
              <path d="M16 2v4M8 2v4M3 10h18" />
            </svg>
          </button>
        </div>
      </header>

      <main class="page-content">
        <slot />
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-shell {
  display: flex;
  min-height: 100vh;
  background: #f1f4f6;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

.sidebar {
  width: 72px;
  background: #fff;
  border-right: 1px solid #e8ecef;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 0;
  gap: 8px;
}

.nav-btn {
  width: 44px;
  height: 44px;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: #9aa5b1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s, color 0.15s;
}

.nav-btn:hover,
.nav-btn.active {
  background: #f0f4f8;
  color: #3b4a5a;
}

.nav-icon {
  width: 20px;
  height: 20px;
  display: block;
  background: currentColor;
  mask-size: contain;
  mask-repeat: no-repeat;
  mask-position: center;
}

.nav-icon[data-icon="star"] {
  mask-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath fill='black' d='M12 2l2.9 6.9L22 9.3l-5.2 4.5 1.6 7.1L12 17.8 5.6 20.9l1.6-7.1L2 9.3l7.1-.4z'/%3E%3C/svg%3E");
}

.nav-icon[data-icon="rocket"] {
  mask-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath fill='black' d='M12 2c1 4 4 7 8 8-2 2-4 3-6 3l-2 5-2-5c-2 0-4-1-6-3 4-1 7-4 8-8z'/%3E%3C/svg%3E");
}

.nav-icon[data-icon="user"] {
  mask-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath fill='black' d='M12 12a5 5 0 100-10 5 5 0 000 10zm0 2c-4 0-8 2-8 4v2h16v-2c0-2-4-4-8-4z'/%3E%3C/svg%3E");
}

.nav-icon[data-icon="theme"] {
  mask-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath fill='black' d='M12 3a9 9 0 109 9c0-.46-.04-.92-.1-1.36a5.5 5.5 0 01-7.54-7.54A9 9 0 0012 3z'/%3E%3C/svg%3E");
}

.nav-icon[data-icon="package"] {
  mask-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath fill='black' d='M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z'/%3E%3C/svg%3E");
}

.main-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.topbar {
  height: 64px;
  background: #fff;
  border-bottom: 1px solid #e8ecef;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.topbar-left,
.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-mark {
  font-size: 1.75rem;
  font-weight: 700;
  color: #3b4a5a;
  line-height: 1;
}

.icon-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: #f1f4f6;
  color: #6c7a89;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  border: none;
  background: transparent;
  cursor: pointer;
  color: #6c7a89;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #67a387, #4a8fd4);
}

.calendar-btn {
  width: 44px;
  height: 44px;
  border: none;
  border-radius: 10px;
  background: #3d9ea8;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-content {
  flex: 1;
  padding: 24px;
  overflow: auto;
}
</style>
