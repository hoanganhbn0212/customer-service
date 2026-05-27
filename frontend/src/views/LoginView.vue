<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import LanguageSwitcher from "../components/LanguageSwitcher.vue";

const router = useRouter();
const { t } = useI18n();

const userName = ref("");
const password = ref("");
const rememberMe = ref(false);
const loading = ref(false);
const error = ref("");

const onSubmit = async () => {
  loading.value = true;
  error.value = "";
  try {
    if (!userName.value || !password.value) {
      error.value = t("login.errorRequired");
      return;
    }

    const response = await fetch("/api/v1/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        userName: userName.value,
        password: password.value
      })
    });

    if (!response.ok) {
      const errBody = await response.json().catch(() => ({}));
      error.value = errBody.message || t("login.errorInvalid");
      return;
    }

    const data = await response.json();
    localStorage.setItem("accessToken", data.accessToken);
    localStorage.setItem("tokenType", data.tokenType || "Bearer");

    if (rememberMe.value) {
      localStorage.setItem("rememberUserName", userName.value);
    } else {
      localStorage.removeItem("rememberUserName");
    }

    router.push("/dashboard");
  } catch {
    error.value = t("login.errorNetwork");
  } finally {
    loading.value = false;
  }
};

const savedUserName = localStorage.getItem("rememberUserName");
if (savedUserName) {
  userName.value = savedUserName;
  rememberMe.value = true;
}
</script>

<template>
  <div class="login-page">
    <div class="lang-corner">
      <LanguageSwitcher />
    </div>

    <aside class="login-brand">
      <div class="brand-inner">
        <div class="brand-icon" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 10.5L12 4l9 6.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1v-9.5z" />
          </svg>
        </div>
        <h1 class="brand-title">{{ t("login.brandTitle") }}</h1>
      </div>
    </aside>

    <main class="login-form-panel">
      <div class="form-wrap">
        <header class="form-header">
          <h2>{{ t("login.title") }}</h2>
          <p>{{ t("login.subtitle") }}</p>
        </header>

        <form class="login-form" @submit.prevent="onSubmit">
          <div class="field">
            <label for="userName">{{ t("login.userName") }}</label>
            <input
              id="userName"
              v-model="userName"
              type="text"
              :placeholder="t('login.userNamePlaceholder')"
              autocomplete="username"
              required
            />
          </div>

          <div class="field">
            <label for="password">{{ t("login.password") }}</label>
            <input
              id="password"
              v-model="password"
              type="password"
              :placeholder="t('login.passwordPlaceholder')"
              autocomplete="current-password"
              required
            />
          </div>

          <div class="form-row">
            <label class="checkbox-label">
              <input v-model="rememberMe" type="checkbox" />
              <span>{{ t("login.rememberMe") }}</span>
            </label>
            <a href="#" class="link" @click.prevent>{{ t("login.forgotPassword") }}</a>
          </div>

          <p v-if="error" class="form-error">{{ error }}</p>

          <button type="submit" class="btn-primary" :disabled="loading">
            {{ loading ? t("login.signingIn") : t("login.signIn") }}
          </button>
        </form>

      </div>
    </main>
  </div>
</template>

<style scoped>
.login-page {
  position: relative;
  display: flex;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.lang-corner {
  position: absolute;
  top: 20px;
  left: 20px;
  z-index: 10;
}

.login-brand {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #67a387;
  padding: 48px 32px;
}

.brand-inner {
  max-width: 380px;
  text-align: center;
  color: #fff;
}

.brand-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
}

.brand-icon svg {
  width: 28px;
  height: 28px;
}

.brand-title {
  margin: 0 0 16px;
  font-size: 1.75rem;
  font-weight: 700;
  line-height: 1.3;
}

.brand-subtitle {
  margin: 0;
  font-size: 0.95rem;
  line-height: 1.6;
  opacity: 0.92;
}

.login-form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  padding: 48px 32px;
}

.form-wrap {
  width: 100%;
  max-width: 400px;
}

.form-header h2 {
  margin: 0 0 8px;
  font-size: 1.5rem;
  font-weight: 700;
  color: #1a1a2e;
}

.form-header p {
  margin: 0 0 32px;
  font-size: 0.9rem;
  color: #6c757d;
}

.field {
  margin-bottom: 20px;
}

.field label {
  display: block;
  margin-bottom: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  color: #343a40;
}

.field input {
  width: 100%;
  padding: 12px 14px;
  font-size: 0.95rem;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  outline: none;
  transition: border-color 0.15s, box-shadow 0.15s;
  box-sizing: border-box;
}

.field input::placeholder {
  color: #adb5bd;
}

.field input:focus {
  border-color: #67a387;
  box-shadow: 0 0 0 3px rgba(103, 163, 135, 0.2);
}

.form-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  gap: 12px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.875rem;
  color: #495057;
  cursor: pointer;
  user-select: none;
}

.checkbox-label input {
  width: 16px;
  height: 16px;
  accent-color: #67a387;
}

.link {
  font-size: 0.875rem;
  color: #3b82f6;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}

.form-error {
  margin: -8px 0 16px;
  font-size: 0.875rem;
  color: #dc3545;
}

.btn-primary {
  width: 100%;
  padding: 14px 20px;
  font-size: 0.95rem;
  font-weight: 600;
  color: #fff;
  background: #67a387;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
}

.btn-primary:hover:not(:disabled) {
  background: #5a9378;
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

@media (max-width: 900px) {
  .login-page {
    flex-direction: column;
  }

  .lang-corner {
    top: 12px;
    left: 12px;
  }

  .login-brand {
    min-height: 220px;
    padding: 32px 24px;
  }

  .brand-title {
    font-size: 1.35rem;
  }

  .brand-subtitle {
    font-size: 0.85rem;
  }
}
</style>
