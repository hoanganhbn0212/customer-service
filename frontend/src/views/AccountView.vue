<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import MobileAppShell from "../components/layout/MobileAppShell.vue";
import MobileScreenHeader from "../components/layout/MobileScreenHeader.vue";
import { useAppScreen } from "../composables/useAppScreen";
import { clearSession } from "../auth/session";
import {
  getMobileAccount,
  listAvailablePackages,
  listMyVouchers,
  requestPackageUpgrade,
  updateMobileAccount
} from "../api/mobileApi";

const router = useRouter();
const { t } = useI18n();
const { screen, accountMenus } = useAppScreen("account");

const account = ref(null);
const packages = ref([]);
const vouchers = ref([]);
const upgradeCode = ref("PRO_15");
const upgradeNote = ref("");
const saveHint = ref("");
const upgradeHint = ref("");

const profileForm = ref({
  fullName: "",
  phone: "",
  email: "",
  avatarUrl: ""
});

const currentPackageCode = computed(() => account.value?.subscription?.packageCode ?? "");

const formatDate = (iso) => {
  if (!iso) return "";
  return new Date(iso).toLocaleDateString("vi-VN");
};

const load = async () => {
  try {
    account.value = await getMobileAccount();
    profileForm.value = {
      fullName: account.value.fullName || "",
      phone: account.value.phone || "",
      email: account.value.email || "",
      avatarUrl: account.value.avatarUrl || ""
    };
    if (account.value.subscription?.packageCode) {
      upgradeCode.value = account.value.subscription.packageCode.startsWith("BASIC")
        ? "PRO_15"
        : "PRO_30";
    }
  } catch {
    account.value = null;
  }
  try {
    packages.value = await listAvailablePackages();
  } catch {
    packages.value = [];
  }
  try {
    vouchers.value = await listMyVouchers();
  } catch {
    vouchers.value = [];
  }
};

const saveProfile = async () => {
  try {
    account.value = await updateMobileAccount({ ...profileForm.value });
    saveHint.value = t("account.saveSuccess");
    setTimeout(() => {
      saveHint.value = "";
    }, 2500);
  } catch {
    saveHint.value = t("home.comingSoon");
  }
};

const submitUpgrade = async () => {
  try {
    const res = await requestPackageUpgrade({
      toPackageCode: upgradeCode.value,
      note: upgradeNote.value
    });
    upgradeHint.value = t("account.upgradeSubmitted", { status: res.status });
  } catch {
    upgradeHint.value = t("home.comingSoon");
  }
};

const logout = () => {
  clearSession();
  router.push("/login");
};

const onMenu = (item) => {
  if (item.type === "logout") {
    logout();
    return;
  }
  if (item.route) {
    router.push(item.route);
  }
};

onMounted(load);
</script>

<template>
  <MobileAppShell active-tab="account">
    <MobileScreenHeader :title="t(screen.titleKey)" />

    <div class="mobile-page-body account-page">
      <section class="card profile-card">
        <h2 class="section-title">{{ t("account.profileTitle") }}</h2>
        <p v-if="account?.userName" class="user-line">@{{ account.userName }}</p>
        <label class="field-label" for="fullName">{{ t("account.fullName") }}</label>
        <input id="fullName" v-model="profileForm.fullName" type="text" class="field-input" />
        <label class="field-label" for="phone">{{ t("account.phone") }}</label>
        <input id="phone" v-model="profileForm.phone" type="tel" class="field-input" />
        <label class="field-label" for="email">{{ t("account.email") }}</label>
        <input id="email" v-model="profileForm.email" type="email" class="field-input" />
        <button type="button" class="btn primary" @click="saveProfile">{{ t("account.save") }}</button>
        <p v-if="saveHint" class="hint success">{{ saveHint }}</p>
      </section>

      <section v-if="account?.subscription" class="card">
        <h2 class="section-title">{{ t("account.currentPackage") }}</h2>
        <p class="pkg-name">{{ account.subscription.displayTitle }}</p>
        <p class="pkg-meta">
          {{ formatDate(account.subscription.startDate) }} – {{ formatDate(account.subscription.endDate) }}
        </p>
      </section>

      <section class="card">
        <h2 class="section-title">{{ t("account.upgradeTitle") }}</h2>
        <label class="field-label" for="upgrade-pkg">{{ t("account.upgradeSelect") }}</label>
        <select id="upgrade-pkg" v-model="upgradeCode" class="field-input">
          <option
            v-for="pkg in packages"
            :key="pkg.code"
            :value="pkg.code"
            :disabled="pkg.code === currentPackageCode"
          >
            {{ pkg.label }} ({{ pkg.tier }})
          </option>
        </select>
        <label class="field-label" for="upgrade-note">{{ t("account.upgradeNote") }}</label>
        <input id="upgrade-note" v-model="upgradeNote" type="text" class="field-input" />
        <button type="button" class="btn secondary" @click="submitUpgrade">{{ t("account.upgradeSubmit") }}</button>
        <p v-if="upgradeHint" class="hint">{{ upgradeHint }}</p>
      </section>

      <section class="card">
        <h2 class="section-title">{{ t("account.vouchersTitle") }}</h2>
        <p v-if="!vouchers.length" class="hint-box">{{ t("account.vouchersEmpty") }}</p>
        <article v-for="v in vouchers" :key="v.id" class="voucher-item">
          <div class="voucher-head">
            <strong>{{ v.title }}</strong>
            <span class="voucher-code">{{ v.code }}</span>
          </div>
          <p v-if="v.description">{{ v.description }}</p>
          <p class="voucher-exp">{{ t("account.voucherExpires", { date: formatDate(v.expiresAt) }) }}</p>
        </article>
      </section>

      <template v-for="item in accountMenus" :key="item.id">
        <button
          v-if="item.type === 'logout'"
          type="button"
          class="menu-btn danger"
          @click="onMenu(item)"
        >
          {{ t(item.labelKey) }}
        </button>
        <template v-else-if="item.type === 'group'">
          <p class="section-label">{{ t(item.labelKey) }}</p>
          <button
            v-for="sub in item.items"
            :key="sub.id"
            type="button"
            class="menu-btn"
            @click="onMenu(sub)"
          >
            {{ t(sub.labelKey) }}
          </button>
        </template>
      </template>
    </div>
  </MobileAppShell>
</template>

<style scoped>
@import "../styles/mobile-page.css";

.account-page {
  gap: 12px;
}

.profile-card .section-title {
  margin-top: 0;
}

.user-line {
  margin: 0 0 12px;
  font-size: 0.85rem;
  color: #64748b;
  font-weight: 600;
}

.field-label {
  display: block;
  font-size: 0.8rem;
  font-weight: 600;
  color: #475569;
  margin: 10px 0 6px;
}

.field-input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  font-size: 0.9rem;
  font-family: inherit;
  box-sizing: border-box;
  background: #f8fafc;
}

.btn {
  width: 100%;
  margin-top: 14px;
  padding: 12px;
  border-radius: 12px;
  font-weight: 700;
  font-family: inherit;
  cursor: pointer;
  border: none;
}

.btn.primary {
  background: linear-gradient(135deg, #1a6dff, #3d8cff);
  color: #fff;
}

.btn.secondary {
  background: #fff;
  color: #1a6dff;
  border: 2px solid #1a6dff;
}

.hint {
  margin: 10px 0 0;
  font-size: 0.82rem;
  color: #64748b;
}

.hint.success {
  color: #16a34a;
}

.pkg-name {
  margin: 0 0 6px;
  font-weight: 700;
  color: #0f172a;
}

.pkg-meta {
  margin: 0;
  font-size: 0.85rem;
  color: #64748b;
}

.voucher-item {
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
}

.voucher-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.voucher-head {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: flex-start;
  margin-bottom: 6px;
}

.voucher-code {
  font-size: 0.75rem;
  font-weight: 700;
  color: #1a6dff;
  background: #eff6ff;
  padding: 4px 8px;
  border-radius: 8px;
  white-space: nowrap;
}

.voucher-item p {
  margin: 0 0 4px;
  font-size: 0.82rem;
  color: #64748b;
  line-height: 1.45;
}

.voucher-exp {
  font-size: 0.75rem !important;
  font-weight: 600;
}
</style>
