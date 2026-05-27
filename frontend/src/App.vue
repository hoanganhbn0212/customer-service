<script setup>
import { onMounted, ref } from "vue";
import {
  ApiError,
  CustomerService,
  HealthService
} from "./api";

const backendStatus = ref("Not checked");
const customers = ref([]);
const loading = ref(false);
const error = ref("");

const form = ref({
  name: "",
  email: ""
});

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
  <main class="container">
    <h1>Customer Service</h1>
    <p>Backend: <strong>{{ backendStatus }}</strong></p>

    <section>
      <h2>Thêm khách hàng</h2>
      <form @submit.prevent="createCustomer" class="form">
        <input v-model="form.name" placeholder="Tên" required />
        <input v-model="form.email" type="email" placeholder="Email" required />
        <button type="submit" :disabled="loading">Tạo</button>
      </form>
    </section>

    <section>
      <h2>Danh sách</h2>
      <button @click="refreshAll" :disabled="loading">Làm mới</button>
      <ul v-if="customers.length">
        <li v-for="c in customers" :key="c.id">
          <span>{{ c.id }} — {{ c.name }} ({{ c.email }})</span>
          <button @click="removeCustomer(c.id)" :disabled="loading">Xóa</button>
        </li>
      </ul>
      <p v-else>Chưa có khách hàng.</p>
    </section>

    <p v-if="error" class="error">{{ error }}</p>
  </main>
</template>

<style scoped>
.container {
  max-width: 720px;
  margin: 40px auto;
  font-family: Arial, sans-serif;
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
}

button {
  padding: 8px 12px;
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
