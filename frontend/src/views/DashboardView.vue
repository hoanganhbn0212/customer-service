<script setup>
import { onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import { ApiError, CustomerService, HealthService } from "../api";
import AppShell from "../components/layout/AppShell.vue";

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

onMounted(refreshAll);
</script>

<template>
  <AppShell active-nav="customers">
    <section class="card page-card">
      <h1 class="page-title">{{ t("dashboard.title") }}</h1>
      <p class="backend-status">
        {{ t("dashboard.backend") }}: <strong>{{ backendStatus }}</strong>
      </p>

      <section class="section">
        <h2>{{ t("dashboard.addCustomer") }}</h2>
        <form class="form" @submit.prevent="createCustomer">
          <input v-model="form.name" :placeholder="t('dashboard.namePlaceholder')" required />
          <input
            v-model="form.email"
            type="email"
            :placeholder="t('dashboard.emailPlaceholder')"
            required
          />
          <button type="submit" class="btn-action" :disabled="loading">
            {{ t("dashboard.create") }}
          </button>
        </form>
      </section>

      <section class="section">
        <h2>{{ t("dashboard.list") }}</h2>
        <button type="button" class="btn-action outline" :disabled="loading" @click="refreshAll">
          {{ t("dashboard.refresh") }}
        </button>
        <ul v-if="customers.length" class="customer-list">
          <li v-for="c in customers" :key="c.id">
            <span>{{ c.id }} — {{ c.name }} ({{ c.email }})</span>
            <button type="button" class="btn-danger" :disabled="loading" @click="removeCustomer(c.id)">
              {{ t("dashboard.delete") }}
            </button>
          </li>
        </ul>
        <p v-else class="empty">{{ t("dashboard.empty") }}</p>
      </section>

      <p v-if="error" class="error">{{ error }}</p>
    </section>
  </AppShell>
</template>

<style scoped>
.page-card {
  max-width: 800px;
}

.page-title {
  margin: 0 0 8px;
  font-size: 1.5rem;
  color: #3b4a5a;
}

.backend-status {
  margin: 0 0 24px;
  color: #7a8794;
  font-size: 0.9rem;
}

.section {
  margin-top: 24px;
}

.section h2 {
  margin: 0 0 12px;
  font-size: 1rem;
  color: #3b4a5a;
}

.form {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

input {
  padding: 10px 12px;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  font-size: 0.9rem;
}

.btn-action {
  padding: 10px 16px;
  border: none;
  border-radius: 8px;
  background: #67a387;
  color: #fff;
  font-weight: 600;
  cursor: pointer;
}

.btn-action.outline {
  background: #fff;
  color: #3b4a5a;
  border: 1px solid #dee2e6;
  margin-bottom: 12px;
}

.btn-action:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-danger {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  background: #fee;
  color: #c62828;
  cursor: pointer;
}

.customer-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.customer-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f3f5;
}

.empty {
  color: #9aa5b1;
}

.error {
  color: #c62828;
  margin-top: 16px;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}
</style>
