<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { ApiError, CustomerService, HealthService } from "../api";
import LanguageSwitcher from "../components/LanguageSwitcher.vue";

const router = useRouter();
const { t } = useI18n();

const backendStatus = ref(t("dashboard.notChecked"));
const customers = ref([]);
const loading = ref(false);
const error = ref("");
const form = ref({ name: "", email: "" });

const loadHealth = async () => {
  const health = await HealthService.getHealth();
  backendStatus.value = `${health.service}: ${health.status}`;
};

const loadCustomers = async () => {
  customers.value = await CustomerService.listCustomers();
};

const refreshAll = async () => {
  loading.value = true;
  error.value = "";
  try {
    await loadHealth();
    await loadCustomers();
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : String(e);
  } finally {
    loading.value = false;
  }
};

const createCustomer = async () => {
  loading.value = true;
  error.value = "";
  try {
    await CustomerService.createCustomer({
      name: form.value.name,
      email: form.value.email
    });
    form.value = { name: "", email: "" };
    await loadCustomers();
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : String(e);
  } finally {
    loading.value = false;
  }
};

const removeCustomer = async (id) => {
  loading.value = true;
  error.value = "";
  try {
    await CustomerService.deleteCustomer(id);
    await loadCustomers();
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : String(e);
  } finally {
    loading.value = false;
  }
};

const logout = () => {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("tokenType");
  router.push("/login");
};

onMounted(refreshAll);
</script>

<template>
  <main class="container">
    <div class="lang-corner">
      <LanguageSwitcher />
    </div>

    <header class="top-bar">
      <h1>{{ t("dashboard.title") }}</h1>
      <button type="button" class="btn-logout" @click="logout">
        {{ t("dashboard.logout") }}
      </button>
    </header>
    <p>{{ t("dashboard.backend") }}: <strong>{{ backendStatus }}</strong></p>

    <section>
      <h2>{{ t("dashboard.addCustomer") }}</h2>
      <form class="form" @submit.prevent="createCustomer">
        <input v-model="form.name" :placeholder="t('dashboard.namePlaceholder')" required />
        <input
          v-model="form.email"
          type="email"
          :placeholder="t('dashboard.emailPlaceholder')"
          required
        />
        <button type="submit" :disabled="loading">{{ t("dashboard.create") }}</button>
      </form>
    </section>

    <section>
      <h2>{{ t("dashboard.list") }}</h2>
      <button @click="refreshAll" :disabled="loading">{{ t("dashboard.refresh") }}</button>
      <ul v-if="customers.length">
        <li v-for="c in customers" :key="c.id">
          <span>{{ c.id }} — {{ c.name }} ({{ c.email }})</span>
          <button @click="removeCustomer(c.id)" :disabled="loading">
            {{ t("dashboard.delete") }}
          </button>
        </li>
      </ul>
      <p v-else>{{ t("dashboard.empty") }}</p>
    </section>

    <p v-if="error" class="error">{{ error }}</p>
  </main>
</template>

<style scoped>
.container {
  position: relative;
  max-width: 720px;
  margin: 40px auto;
  padding: 0 16px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

.lang-corner {
  position: absolute;
  top: -28px;
  left: 0;
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 8px;
}

.top-bar h1 {
  margin: 0;
}

.btn-logout {
  padding: 8px 14px;
  font-size: 0.875rem;
  color: #495057;
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  cursor: pointer;
}

.btn-logout:hover {
  background: #e9ecef;
}

section {
  margin-top: 24px;
}

.form {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

input {
  padding: 8px;
  border: 1px solid #dee2e6;
  border-radius: 6px;
}

button {
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
}

ul {
  list-style: none;
  padding: 0;
}

li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.error {
  color: #c62828;
  margin-top: 16px;
}
</style>
