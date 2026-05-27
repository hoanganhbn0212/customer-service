<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    default: ""
  },
  showBell: {
    type: Boolean,
    default: false
  },
  unreadCount: {
    type: Number,
    default: 0
  }
});

const emit = defineEmits(["bell"]);
</script>

<template>
  <header class="mobile-hero">
    <div class="hero-top">
      <p class="brand">Layla</p>
      <button
        v-if="showBell"
        type="button"
        class="bell-btn"
        aria-label="Notifications"
        @click="emit('bell')"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 22a2 2 0 002-2H10a2 2 0 002 2z" />
          <path d="M18 10a6 6 0 10-12 0c0 5-2 6-6 6-6h12s-2 1-6 6z" />
        </svg>
        <span v-if="unreadCount > 0" class="bell-dot">{{ unreadCount > 9 ? "9+" : unreadCount }}</span>
      </button>
    </div>
    <h1 class="hero-title">{{ title }}</h1>
    <p v-if="subtitle" class="hero-subtitle">{{ subtitle }}</p>
  </header>
</template>

<style scoped>
.mobile-hero {
  padding: calc(14px + env(safe-area-inset-top, 0)) 20px 20px;
  background: linear-gradient(165deg, #0a3d9e 0%, #1a6dff 55%, #3d8cff 100%);
  color: #fff;
  flex-shrink: 0;
}

.hero-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.brand {
  margin: 0;
  font-size: 1.65rem;
  font-weight: 800;
  font-style: italic;
  letter-spacing: -0.02em;
}

.bell-btn {
  position: relative;
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.bell-btn svg {
  width: 22px;
  height: 22px;
}

.bell-dot {
  position: absolute;
  top: 4px;
  right: 4px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 999px;
  background: #ff4d4f;
  border: 2px solid #1a6dff;
  font-size: 0.6rem;
  font-weight: 800;
  line-height: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-title {
  margin: 0;
  font-size: 1.3rem;
  font-weight: 700;
}

.hero-subtitle {
  margin: 8px 0 0;
  font-size: 0.88rem;
  opacity: 0.9;
  font-weight: 500;
}
</style>
