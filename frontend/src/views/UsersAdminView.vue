<script setup>
import { onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import { ApiError, AdminService } from "../api";
import { AdminCreateUserRequest } from "../api/models/AdminCreateUserRequest";
import { AdminUpdateUserRequest } from "../api/models/AdminUpdateUserRequest";
import AppShell from "../components/layout/AppShell.vue";
import { canManageUsers } from "../auth/session";

const { t } = useI18n();

const users = ref([]);
const loading = ref(false);
const error = ref("");
const success = ref("");

const createForm = ref({
  userName: "",
  password: "",
  role: AdminCreateUserRequest.role.USER,
  enabled: AdminCreateUserRequest.enabled.ACTIVE
});

const loadUsers = async () => {
  loading.value = true;
  error.value = "";
  try {
    users.value = await AdminService.listUsers();
  } catch (e) {
    error.value = e instanceof ApiError ? e.body?.message || e.message : String(e);
  } finally {
    loading.value = false;
  }
};

const createUser = async () => {
  loading.value = true;
  error.value = "";
  success.value = "";
  try {
    await AdminService.createUser({
      userName: createForm.value.userName.trim(),
      password: createForm.value.password,
      role: createForm.value.role,
      enabled: createForm.value.enabled
    });
    createForm.value = {
      userName: "",
      password: "",
      role: AdminCreateUserRequest.role.USER,
      enabled: AdminCreateUserRequest.enabled.ACTIVE
    };
    success.value = t("admin.createSuccess");
    await loadUsers();
  } catch (e) {
    error.value = e instanceof ApiError ? e.body?.message || e.message : String(e);
  } finally {
    loading.value = false;
  }
};

const updateUser = async (user, patch) => {
  loading.value = true;
  error.value = "";
  success.value = "";
  try {
    await AdminService.updateUser(user.id, patch);
    success.value = t("admin.updateSuccess");
    await loadUsers();
  } catch (e) {
    error.value = e instanceof ApiError ? e.body?.message || e.message : String(e);
  } finally {
    loading.value = false;
  }
};

const toggleStatus = (user) => {
  const next =
    user.enabled === "ACTIVE"
      ? AdminUpdateUserRequest.enabled.INACTIVE
      : AdminUpdateUserRequest.enabled.ACTIVE;
  updateUser(user, { enabled: next });
};

const roleEnum = (roleValue) => {
  if (roleValue === "ADMIN") return AdminUpdateUserRequest.role.ADMIN;
  if (roleValue === "DEVELOP") return AdminUpdateUserRequest.role.DEVELOP;
  return AdminUpdateUserRequest.role.USER;
};

const changeRole = (user, roleValue) => {
  updateUser(user, { role: roleEnum(roleValue) });
};

onMounted(loadUsers);
</script>

<template>
  <AppShell active-nav="users">
    <section class="card page-card">
      <h1 class="page-title">{{ t("admin.title") }}</h1>
      <p class="page-desc">{{ t("admin.subtitle") }}</p>

      <section v-if="canManageUsers()" class="section">
        <h2>{{ t("admin.createUser") }}</h2>
        <form class="form-grid" @submit.prevent="createUser">
          <input
            v-model="createForm.userName"
            :placeholder="t('admin.userName')"
            required
            :disabled="loading"
          />
          <input
            v-model="createForm.password"
            type="password"
            :placeholder="t('admin.password')"
            required
            minlength="6"
            :disabled="loading"
          />
          <select v-model="createForm.role" :disabled="loading">
            <option :value="AdminCreateUserRequest.role.USER">{{ t("admin.roleUser") }}</option>
            <option :value="AdminCreateUserRequest.role.DEVELOP">{{ t("admin.roleDevelop") }}</option>
            <option :value="AdminCreateUserRequest.role.ADMIN">{{ t("admin.roleAdmin") }}</option>
          </select>
          <select v-model="createForm.enabled" :disabled="loading">
            <option :value="AdminCreateUserRequest.enabled.ACTIVE">{{ t("admin.statusActive") }}</option>
            <option :value="AdminCreateUserRequest.enabled.INACTIVE">{{ t("admin.statusInactive") }}</option>
          </select>
          <button type="submit" class="btn-primary" :disabled="loading">
            {{ t("admin.create") }}
          </button>
        </form>
      </section>

      <section class="section">
        <div class="section-head">
          <h2>{{ t("admin.userList") }}</h2>
          <button type="button" class="btn-outline" :disabled="loading" @click="loadUsers">
            {{ t("admin.refresh") }}
          </button>
        </div>

        <table v-if="users.length" class="user-table">
          <thead>
            <tr>
              <th>{{ t("admin.userName") }}</th>
              <th>{{ t("admin.role") }}</th>
              <th>{{ t("admin.status") }}</th>
              <th>{{ t("admin.actions") }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td>{{ user.userName }}</td>
              <td>
                <select
                  :value="user.role"
                  :disabled="loading || !canManageUsers()"
                  @change="changeRole(user, $event.target.value)"
                >
                  <option value="USER">{{ t("admin.roleUser") }}</option>
                  <option value="DEVELOP">{{ t("admin.roleDevelop") }}</option>
                  <option value="ADMIN">{{ t("admin.roleAdmin") }}</option>
                </select>
              </td>
              <td>
                <span class="badge" :class="user.enabled === 'ACTIVE' ? 'on' : 'off'">
                  {{ user.enabled === "ACTIVE" ? t("admin.statusActive") : t("admin.statusInactive") }}
                </span>
              </td>
              <td>
                <button
                  type="button"
                  class="btn-outline"
                  :disabled="loading || !canManageUsers()"
                  @click="toggleStatus(user)"
                >
                  {{
                    user.enabled === "ACTIVE"
                      ? t("admin.deactivate")
                      : t("admin.activate")
                  }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <p v-else class="empty">{{ t("admin.empty") }}</p>
      </section>

      <p v-if="success" class="success">{{ success }}</p>
      <p v-if="error" class="error">{{ error }}</p>
    </section>
  </AppShell>
</template>

<style scoped>
.page-card {
  max-width: 960px;
}

.page-title {
  margin: 0 0 8px;
  font-size: 1.5rem;
  color: #3b4a5a;
}

.page-desc {
  margin: 0 0 24px;
  color: #7a8794;
}

.section {
  margin-top: 28px;
}

.section h2 {
  margin: 0 0 12px;
  font-size: 1rem;
  color: #3b4a5a;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 10px;
  align-items: center;
}

input,
select {
  padding: 10px 12px;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  font-size: 0.9rem;
}

.btn-primary {
  padding: 10px 16px;
  border: none;
  border-radius: 8px;
  background: #67a387;
  color: #fff;
  font-weight: 600;
  cursor: pointer;
}

.btn-outline {
  padding: 8px 12px;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
}

.user-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}

.user-table th,
.user-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #f0f3f5;
}

.badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
}

.badge.on {
  background: #e8f5ee;
  color: #2e7d52;
}

.badge.off {
  background: #fdecea;
  color: #c62828;
}

.empty {
  color: #9aa5b1;
}

.success {
  color: #2e7d52;
  margin-top: 16px;
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
