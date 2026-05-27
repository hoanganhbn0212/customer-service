<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { ApiError, AuthService, ContentService } from "../api";
import {
  getRememberedUserName,
  isAuthenticated,
  setRememberedUserName,
  setSession
} from "../auth/session";
import LanguageSwitcher from "../components/LanguageSwitcher.vue";
import loginIllustration from "../assets/login-illustration.svg";

const router = useRouter();
const { t } = useI18n();

const userName = ref("");
const password = ref("");
const showPassword = ref(false);
const isRegister = ref(false);
const loading = ref(false);
const error = ref("");
const info = ref("");
const headerBackgroundUrl = ref("");
const bodyBackgroundUrl = ref("");

const hasHeaderBg = computed(() => Boolean(headerBackgroundUrl.value?.trim()));
const hasBodyBg = computed(() => Boolean(bodyBackgroundUrl.value?.trim()));

const heroStyle = computed(() => {
  const url = headerBackgroundUrl.value?.trim();
  if (!url) return {};
  return {
    backgroundImage: `url("${url}")`,
    backgroundSize: "cover",
    backgroundPosition: "center top",
    backgroundRepeat: "no-repeat"
  };
});

const bodyBgStyle = computed(() => {
  const url = bodyBackgroundUrl.value?.trim();
  if (!url) return {};
  return {
    backgroundImage: `url("${url}")`,
    backgroundSize: "cover",
    backgroundPosition: "center bottom",
    backgroundRepeat: "no-repeat"
  };
});

const loadTheme = async () => {
  try {
    const theme = await ContentService.getLoginTheme();
    headerBackgroundUrl.value = theme.headerBackgroundUrl || "";
    bodyBackgroundUrl.value = theme.bodyBackgroundUrl || "";
  } catch {
    /* fallback to default gradient / illustration */
  }
};

onMounted(async () => {
  await loadTheme();
  if (isAuthenticated()) {
    await router.replace("/home");
  }
});

const onSubmit = async () => {
  loading.value = true;
  error.value = "";
  info.value = "";
  try {
    if (!userName.value || !password.value) {
      error.value = t("login.errorRequired");
      return;
    }

    if (isRegister.value) {
      await AuthService.register({
        userName: userName.value.trim(),
        password: password.value
      });
      info.value = t("login.registerSuccess");
      isRegister.value = false;
      return;
    }

    const loginResponse = await AuthService.login({
      userName: userName.value.trim(),
      password: password.value
    });

    setSession(loginResponse);
    setRememberedUserName(userName.value.trim());
    await router.replace("/home");
  } catch (e) {
    if (e instanceof ApiError) {
      if (e.status === 401) {
        error.value = e.body?.message || t("login.errorInvalid");
      } else if (e.status === 409) {
        error.value = e.body?.message || t("login.errorUsernameTaken");
      } else if (e.status === 0) {
        error.value = t("login.errorNetwork");
      } else {
        error.value = e.body?.message || t("login.errorInvalid");
      }
    } else {
      error.value = t("login.errorNetwork");
    }
  } finally {
    loading.value = false;
  }
};

const toggleMode = () => {
  isRegister.value = !isRegister.value;
  error.value = "";
  info.value = "";
};

const savedUserName = getRememberedUserName();
if (savedUserName) {
  userName.value = savedUserName;
}
</script>

<template>
  <div class="layla-login">
    <div class="screen">
      <header class="hero" :class="{ 'has-bg': hasHeaderBg }" :style="heroStyle">
        <LanguageSwitcher variant="dark" class="lang-fab" />

        <div class="hero-copy">
          <p class="brand">Layla</p>
          <h1>{{ isRegister ? t("login.registerTitle") : t("login.title") }}</h1>
          <p class="tagline">
            {{ isRegister ? t("login.registerSubtitle") : t("login.welcomeBack") }}
          </p>
        </div>

        <svg class="hero-wave" viewBox="0 0 390 48" preserveAspectRatio="none" aria-hidden="true">
          <path
            d="M0 32 C78 8 156 56 234 28 C312 0 351 20 390 12 L390 48 L0 48 Z"
            fill="#ffffff"
          />
        </svg>
      </header>

      <main class="body" :class="{ 'has-body-bg': hasBodyBg }">
        <div v-if="hasBodyBg" class="body-bg" :style="bodyBgStyle" aria-hidden="true" />
        <section class="card">
          <form class="form" @submit.prevent="onSubmit">
            <div class="field">
              <span class="field-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                  <circle cx="12" cy="8" r="4" />
                  <path d="M5 20c0-4 3.5-7 7-7s7 3 7 7" stroke-linecap="round" />
                </svg>
              </span>
              <input
                v-model="userName"
                type="text"
                :placeholder="t('login.userNamePlaceholder')"
                autocomplete="username"
                :disabled="loading"
                required
              />
            </div>

            <div class="field">
              <span class="field-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                  <rect x="4" y="11" width="16" height="10" rx="2" />
                  <path d="M8 11V8a4 4 0 018 0v3" stroke-linecap="round" />
                </svg>
              </span>
              <input
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                :placeholder="t('login.passwordPlaceholder')"
                autocomplete="current-password"
                :disabled="loading"
                required
                minlength="6"
              />
              <button
                type="button"
                class="eye-btn"
                tabindex="-1"
                :aria-label="showPassword ? t('login.hidePassword') : t('login.showPassword')"
                @click="showPassword = !showPassword"
              >
                <svg v-if="!showPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                  <path d="M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12z" />
                  <circle cx="12" cy="12" r="3" />
                </svg>
                <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                  <path d="M3 3l18 18" stroke-linecap="round" />
                  <path d="M10.6 10.6A3 3 0 0012 15a3 3 0 002.4-4.4" />
                  <path d="M9.9 5.1A10.8 10.8 0 0112 5c7 0 10 7 10 7a17.9 17.9 0 01-3.2 4.2" />
                  <path d="M6.7 6.7C4.1 8.4 2.4 10.6 2 12s4 7 10 7c1.8 0 3.5-.4 5-1.2" />
                </svg>
              </button>
            </div>

            <p v-if="!isRegister" class="forgot">
              <a href="#" @click.prevent>{{ t("login.forgotPassword") }}</a>
            </p>

            <transition name="fade">
              <p v-if="error" class="alert alert-error" role="alert">{{ error }}</p>
            </transition>
            <transition name="fade">
              <p v-if="info" class="alert alert-info">{{ info }}</p>
            </transition>

            <button type="submit" class="btn primary" :disabled="loading">
              <span v-if="loading" class="spinner" aria-hidden="true" />
              {{
                loading
                  ? t("login.signingIn")
                  : isRegister
                    ? t("login.register")
                    : t("login.signIn")
              }}
            </button>

            <button type="button" class="btn ghost" :disabled="loading" @click="toggleMode">
              {{ isRegister ? t("login.backToLogin") : t("login.registerAccount") }}
            </button>
          </form>
        </section>

        <footer v-if="!hasBodyBg" class="promo">
          <img :src="loginIllustration" alt="" class="promo-art" width="320" height="200" />
          <h2>{{ t("login.promoTitle") }}</h2>
          <p>{{ t("login.promoDesc") }}</p>
        </footer>
      </main>
    </div>
  </div>
</template>

<style scoped>
.layla-login {
  --blue-900: #0a3d9e;
  --blue-700: #1a6dff;
  --blue-500: #3d8cff;
  --text: #0f172a;
  --muted: #64748b;
  --line: #e6edf5;
  --card-shadow: 0 18px 50px rgba(15, 61, 140, 0.14);

  min-height: 100vh;
  min-height: 100dvh;
  display: flex;
  justify-content: center;
  background: #f0f4fa;
  font-family: "Plus Jakarta Sans", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
  color: var(--text);
  -webkit-font-smoothing: antialiased;
}

.screen {
  width: 100%;
  max-width: 420px;
  min-height: 100vh;
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  background: #fff;
  position: relative;
  overflow-x: hidden;
}

@media (min-width: 440px) {
  .layla-login {
    padding: 20px 0;
    align-items: center;
  }

  .screen {
    min-height: min(900px, calc(100dvh - 40px));
    border-radius: 32px;
    overflow: hidden;
    box-shadow:
      0 0 0 1px rgba(255, 255, 255, 0.6),
      0 24px 80px rgba(10, 50, 120, 0.18);
  }
}

.hero {
  position: relative;
  padding: calc(16px + env(safe-area-inset-top, 0)) 24px 0;
  background: linear-gradient(160deg, var(--blue-900) 0%, var(--blue-700) 48%, var(--blue-500) 100%);
  color: #fff;
  text-align: center;
  flex-shrink: 0;
}

.hero::before {
  content: "";
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 80% 60% at 20% 0%, rgba(255, 255, 255, 0.14), transparent 55%),
    radial-gradient(ellipse 60% 50% at 90% 20%, rgba(255, 255, 255, 0.08), transparent 50%);
  pointer-events: none;
}

.hero.has-bg::before {
  background: rgba(8, 40, 110, 0.42);
}

.hero.has-bg .hero-copy {
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.25);
}

.lang-fab {
  position: absolute;
  top: calc(14px + env(safe-area-inset-top, 0));
  right: 20px;
  z-index: 2;
}

.hero-copy {
  position: relative;
  z-index: 1;
  padding: 52px 8px 56px;
}

.brand {
  margin: 0 0 20px;
  font-size: clamp(2.4rem, 9vw, 2.85rem);
  font-weight: 800;
  font-style: italic;
  letter-spacing: -0.03em;
  line-height: 1;
  text-shadow: 0 2px 16px rgba(0, 0, 0, 0.12);
}

.hero-copy h1 {
  margin: 0 0 10px;
  font-size: 1.55rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.tagline {
  margin: 0;
  font-size: 0.92rem;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
}

.hero-wave {
  display: block;
  width: 100%;
  height: 48px;
  margin-bottom: -1px;
}

.body {
  flex: 1;
  margin-top: -28px;
  padding: 0 22px calc(28px + env(safe-area-inset-bottom, 0));
  position: relative;
  z-index: 2;
}

.body.has-body-bg {
  min-height: 280px;
  padding-bottom: calc(40px + env(safe-area-inset-bottom, 0));
}

.body-bg {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  top: 120px;
  z-index: 0;
  pointer-events: none;
}

.body.has-body-bg .card {
  position: relative;
  z-index: 1;
}

.card {
  background: #fff;
  border-radius: 22px;
  padding: 26px 22px 22px;
  box-shadow: var(--card-shadow);
  border: 1px solid rgba(230, 237, 245, 0.9);
}

.form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.field {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 54px;
  padding: 0 16px;
  border: 1.5px solid var(--line);
  border-radius: 14px;
  background: #fafbfd;
  transition:
    border-color 0.2s,
    background 0.2s,
    box-shadow 0.2s;
}

.field:focus-within {
  border-color: var(--blue-700);
  background: #fff;
  box-shadow: 0 0 0 4px rgba(26, 109, 255, 0.1);
}

.field-icon {
  width: 22px;
  height: 22px;
  color: #94a3b8;
  flex-shrink: 0;
  display: flex;
}

.field-icon svg {
  width: 22px;
  height: 22px;
}

.field input {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
  background: transparent;
  font: inherit;
  font-size: 0.94rem;
  font-weight: 500;
  color: var(--text);
}

.field input::placeholder {
  color: #a0aec0;
  font-weight: 400;
}

.eye-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  color: #94a3b8;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  border-radius: 8px;
}

.eye-btn:hover {
  color: var(--blue-700);
  background: rgba(26, 109, 255, 0.06);
}

.eye-btn svg {
  width: 20px;
  height: 20px;
}

.forgot {
  margin: 2px 2px 4px;
  text-align: right;
}

.forgot a {
  font-size: 0.86rem;
  font-weight: 600;
  color: var(--blue-700);
  text-decoration: none;
}

.forgot a:hover {
  text-decoration: underline;
}

.alert {
  margin: 0;
  padding: 10px 12px;
  border-radius: 10px;
  font-size: 0.84rem;
  font-weight: 500;
  line-height: 1.4;
}

.alert-error {
  background: #fef2f2;
  color: #b91c1c;
}

.alert-info {
  background: #eff6ff;
  color: #1d4ed8;
}

.btn {
  width: 100%;
  height: 52px;
  border-radius: 14px;
  font: inherit;
  font-size: 1rem;
  font-weight: 700;
  letter-spacing: -0.01em;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition:
    transform 0.12s ease,
    box-shadow 0.2s ease,
    opacity 0.2s;
}

.btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.btn:not(:disabled):active {
  transform: scale(0.985);
}

.btn.primary {
  margin-top: 4px;
  border: none;
  color: #fff;
  background: linear-gradient(180deg, #2b7bff 0%, var(--blue-700) 100%);
  box-shadow: 0 10px 24px rgba(26, 109, 255, 0.35);
}

.btn.primary:not(:disabled):hover {
  box-shadow: 0 14px 28px rgba(26, 109, 255, 0.42);
}

.btn.ghost {
  border: 2px solid var(--blue-700);
  background: #fff;
  color: var(--blue-700);
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.35);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.promo {
  margin-top: 32px;
  text-align: center;
  padding: 0 6px 12px;
}

.promo-art {
  width: min(100%, 300px);
  height: auto;
  margin: 0 auto 18px;
  display: block;
  filter: drop-shadow(0 8px 20px rgba(26, 109, 255, 0.08));
}

.promo h2 {
  margin: 0 0 10px;
  font-size: 1.02rem;
  font-weight: 800;
  color: var(--blue-700);
  line-height: 1.4;
  letter-spacing: -0.02em;
}

.promo p {
  margin: 0;
  font-size: 0.84rem;
  line-height: 1.65;
  color: var(--muted);
  font-weight: 500;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
