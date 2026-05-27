<script setup>
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";

defineProps({
  activeTab: {
    type: String,
    default: "overview"
  }
});

const router = useRouter();
const { t } = useI18n();

const tabs = [
  { id: "overview", labelKey: "nav.overview", icon: "overview", route: "/home" },
  { id: "services", labelKey: "nav.services", icon: "services", route: "/services" },
  { id: "notifications", labelKey: "nav.notifications", icon: "bell", route: "/notifications" },
  { id: "account", labelKey: "nav.account", icon: "account", route: "/account" }
];

const go = (tab) => {
  if (tab.route) router.push(tab.route);
};
</script>

<template>
  <div class="mobile-shell">
    <div class="mobile-screen">
      <main class="mobile-content">
        <slot />
      </main>

      <nav class="bottom-nav" aria-label="Main">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          type="button"
          class="tab-btn"
          :class="{ active: activeTab === tab.id }"
          @click="go(tab)"
        >
          <span class="tab-icon" :data-icon="tab.icon" aria-hidden="true" />
          <span class="tab-label">{{ t(tab.labelKey) }}</span>
        </button>
      </nav>
    </div>
  </div>
</template>

<style scoped>
.mobile-shell {
  height: 100dvh;
  max-height: 100dvh;
  overflow: hidden;
  display: flex;
  justify-content: center;
  background: #e8eef5;
  font-family: "Plus Jakarta Sans", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
}

.mobile-screen {
  --bottom-nav-height: calc(62px + env(safe-area-inset-bottom, 0px));

  width: 100%;
  max-width: 430px;
  height: 100dvh;
  max-height: 100dvh;
  display: flex;
  flex-direction: column;
  background: #f3f6fb;
  position: relative;
  overflow: hidden;
}

/* Điện thoại lớn / màn hình desktop: khung giống mockup điện thoại */
@media (min-width: 440px) and (max-width: 767px) {
  .mobile-shell {
    padding: 16px 0;
    align-items: center;
  }

  .mobile-screen {
    height: min(920px, calc(100dvh - 32px));
    max-height: min(920px, calc(100dvh - 32px));
    border-radius: 28px;
    box-shadow: 0 16px 48px rgba(10, 50, 120, 0.14);
  }

  .bottom-nav {
    border-radius: 0 0 28px 28px;
  }
}

/* iPad & tablet: full width, bottom nav vẫn cố định */
@media (min-width: 768px) {
  .mobile-shell {
    padding: 0;
    align-items: stretch;
    background: #f3f6fb;
  }

  .mobile-screen {
    width: 100%;
    max-width: 100%;
    height: 100dvh;
    max-height: 100dvh;
    border-radius: 0;
    box-shadow: none;
  }

  .bottom-nav {
    border-radius: 0;
    padding: 10px 12px calc(12px + env(safe-area-inset-bottom, 0));
  }

  .tab-label {
    font-size: 0.75rem;
  }

  .tab-icon {
    width: 24px;
    height: 24px;
  }
}

.mobile-content {
  flex: 1;
  min-height: 0;
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  padding-bottom: var(--bottom-nav-height);
  -webkit-overflow-scrolling: touch;
  overscroll-behavior-y: contain;
  touch-action: pan-y;
}

.bottom-nav {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 100;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  background: #fff;
  border-top: 1px solid #e6edf5;
  padding: 8px 6px calc(10px + env(safe-area-inset-bottom, 0));
  box-shadow: 0 -4px 20px rgba(15, 40, 80, 0.06);
}

.tab-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  border: none;
  background: transparent;
  color: #94a3b8;
  cursor: pointer;
  padding: 6px 4px;
  border-radius: 12px;
}

.tab-btn.active {
  color: #1a6dff;
}

.tab-label {
  font-size: 0.65rem;
  font-weight: 700;
}

.tab-icon {
  width: 22px;
  height: 22px;
  background: currentColor;
  mask-size: contain;
  mask-repeat: no-repeat;
  mask-position: center;
}

.tab-icon[data-icon="overview"] {
  mask-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath fill='black' d='M4 4h7v7H4V4zm9 0h7v7h-7V4zM4 13h7v7H4v-7zm9 0h7v7h-7v-7z'/%3E%3C/svg%3E");
}

.tab-icon[data-icon="services"] {
  mask-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath fill='black' d='M4 6h16v2H4V6zm0 5h16v2H4v-2zm0 5h10v2H4v-2z'/%3E%3C/svg%3E");
}

.tab-icon[data-icon="bell"] {
  mask-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath fill='black' d='M12 22a2 2 0 002-2H10a2 2 0 002 2zm6-6V11a6 6 0 10-12 0v5l-2 2v1h16v-1l-2-2z'/%3E%3C/svg%3E");
}

.tab-icon[data-icon="account"] {
  mask-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath fill='black' d='M12 12a5 5 0 100-10 5 5 0 000 10zm0 2c-4 0-8 2-8 4v2h16v-2c0-2-4-4-8-4z'/%3E%3C/svg%3E");
}
</style>
