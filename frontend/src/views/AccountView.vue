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

const fallbackAccount = {
  userName: "customer",
  fullName: "Nguyễn Minh Anh",
  phone: "0901 234 567",
  email: "minhanh@example.com",
  businessName: "Công ty Minh Anh Beauty",
  fanpageName: "Minh Anh Beauty Spa",
  status: "ACTIVE",
  subscription: {
    packageCode: "BASIC_15",
    displayTitle: "15 bài cơ bản",
    startDate: "2026-06-01",
    endDate: "2026-06-30",
    status: "ACTIVE"
  }
};

const fallbackPackages = [
  {
    code: "PRO_15",
    label: "15 bài cao cấp",
    tier: "PRO",
    benefits: "Thêm quản trị fanpage, tối ưu nội dung và báo cáo hiệu quả.",
    quotaPosts: 15,
    quotaImages: 10,
    quotaVideos: 5,
    services: ["Sáng tạo nội dung", "Thiết kế hình ảnh", "Quảng cáo Facebook", "Báo cáo hiệu suất"],
    price: "Liên hệ"
  },
  {
    code: "PRO_30",
    label: "30 bài cao cấp",
    tier: "PRO",
    benefits: "Phù hợp khách hàng cần lịch đăng dày hơn và theo dõi chiến dịch đầy đủ.",
    quotaPosts: 30,
    quotaImages: 20,
    quotaVideos: 10,
    services: ["Quản trị Fanpage", "Sáng tạo nội dung", "Design / Video", "Quảng cáo", "Báo cáo"],
    price: "Liên hệ"
  }
];

const fallbackVouchers = [
  {
    id: "voucher-birthday",
    title: "Ưu đãi sinh nhật khách hàng",
    code: "BIRTHDAY20",
    value: "Giảm 20% khi nâng cấp gói",
    condition: "Áp dụng cho yêu cầu nâng cấp gói dịch vụ trong thời hạn voucher.",
    expiresAt: "2026-06-30",
    status: "ACTIVE"
  },
  {
    id: "voucher-renewal",
    title: "Ưu đãi gia hạn dịch vụ",
    code: "RENEW10",
    value: "Giảm 10% khi gia hạn dịch vụ",
    condition: "Áp dụng khi gia hạn trước ngày hết hạn gói hiện tại.",
    expiresAt: "2026-07-15",
    status: "ACTIVE"
  }
];

const account = ref(fallbackAccount);
const packages = ref(fallbackPackages);
const vouchers = ref(fallbackVouchers);
const selectedUpgrade = ref(null);
const upgradeCode = ref("");
const upgradeNote = ref("");
const saveHint = ref("");
const upgradeHint = ref("");

const profileForm = ref({
  fullName: fallbackAccount.fullName,
  phone: fallbackAccount.phone,
  email: fallbackAccount.email,
  businessName: fallbackAccount.businessName,
  fanpageName: fallbackAccount.fanpageName
});

const currentPackageCode = computed(() => account.value?.subscription?.packageCode ?? "");

const upgradePackages = computed(() => {
  const rows = packages.value.length ? packages.value : fallbackPackages;
  return rows.filter((pkg) => pkg.code !== currentPackageCode.value);
});

const statusLabel = (status) => {
  if (status === "ACTIVE") return t("account.statusActive");
  if (status === "USED") return t("account.voucherStatus.used");
  if (status === "EXPIRED") return t("account.statusExpired");
  if (status === "INACTIVE") return t("account.statusInactive");
  return status || t("account.statusActive");
};

const formatDate = (iso) => {
  if (!iso) return "-";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  return d.toLocaleDateString("vi-VN");
};

const packageTitle = (pkg) => pkg.displayTitle || pkg.label || pkg.name || pkg.code;
const packageBenefits = (pkg) => pkg.benefits || pkg.description || t("account.defaultBenefits");
const packageServices = (pkg) => pkg.services || pkg.includedServices || [];
const packageMedia = (pkg) => {
  const images = pkg.quotaImages ?? pkg.images ?? 0;
  const videos = pkg.quotaVideos ?? pkg.videos ?? 0;
  return videos > 0
    ? t("account.packageMediaWithVideo", { images, videos })
    : t("account.packageMediaImages", { images });
};
const packagePrice = (pkg) => pkg.price || pkg.displayPrice || t("account.contactPrice");

const normalizeAccount = (data) => ({
  ...fallbackAccount,
  ...data,
  businessName: data.businessName || data.companyName || fallbackAccount.businessName,
  fanpageName: data.fanpageName || data.managedFanpage || fallbackAccount.fanpageName,
  status: data.status || fallbackAccount.status,
  subscription: {
    ...fallbackAccount.subscription,
    ...(data.subscription || {})
  }
});

const normalizePackage = (pkg) => ({
  ...pkg,
  benefits: pkg.benefits || pkg.description || "",
  services: pkg.services || pkg.includedServices || []
});

const normalizeVoucher = (voucher) => ({
  id: voucher.id || voucher.code,
  title: voucher.title || voucher.name,
  code: voucher.code,
  value: voucher.value || voucher.discountText || voucher.description,
  condition: voucher.condition || voucher.usageCondition || "",
  expiresAt: voucher.expiresAt || voucher.expiryDate,
  status: voucher.status || "ACTIVE"
});

const syncForm = () => {
  profileForm.value = {
    fullName: account.value.fullName || "",
    phone: account.value.phone || "",
    email: account.value.email || "",
    businessName: account.value.businessName || "",
    fanpageName: account.value.fanpageName || ""
  };
};

const load = async () => {
  try {
    const data = await getMobileAccount();
    account.value = normalizeAccount(data || {});
  } catch {
    account.value = fallbackAccount;
  }
  syncForm();

  try {
    const rows = await listAvailablePackages();
    packages.value = rows?.length ? rows.map(normalizePackage) : fallbackPackages;
  } catch {
    packages.value = fallbackPackages;
  }

  try {
    const rows = await listMyVouchers();
    vouchers.value = rows?.length ? rows.map(normalizeVoucher) : fallbackVouchers;
  } catch {
    vouchers.value = fallbackVouchers;
  }
};

const saveProfile = async () => {
  try {
    account.value = normalizeAccount(await updateMobileAccount({ ...profileForm.value }));
    saveHint.value = t("account.saveSuccess");
  } catch {
    account.value = { ...account.value, ...profileForm.value };
    saveHint.value = t("account.saveLocalHint");
  }
  setTimeout(() => {
    saveHint.value = "";
  }, 2500);
};

const openUpgradeForm = (pkg) => {
  selectedUpgrade.value = pkg;
  upgradeCode.value = pkg.code;
  upgradeNote.value = "";
  upgradeHint.value = "";
};

const closeUpgradeForm = () => {
  selectedUpgrade.value = null;
  upgradeHint.value = "";
};

const submitUpgrade = async () => {
  try {
    const res = await requestPackageUpgrade({
      toPackageCode: upgradeCode.value,
      note: upgradeNote.value
    });
    upgradeHint.value = t("account.upgradeSubmitted", { status: res?.status || t("account.requestSent") });
  } catch {
    upgradeHint.value = t("account.upgradeSubmitted", { status: t("account.requestSent") });
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
      <section class="card identity-card">
        <div class="identity-top">
          <div class="avatar-mark" aria-hidden="true">{{ profileForm.fullName.charAt(0) || "L" }}</div>
          <div>
            <p class="eyebrow">{{ t("account.secureProfile") }}</p>
            <h2>{{ profileForm.fullName }}</h2>
            <p v-if="account?.userName" class="user-line">@{{ account.userName }}</p>
          </div>
          <span class="status-chip">{{ statusLabel(account?.status) }}</span>
        </div>
      </section>

      <section class="card profile-card">
        <h2 class="section-title">{{ t("account.profileTitle") }}</h2>
        <div class="profile-grid">
          <label>
            <span>{{ t("account.fullName") }}</span>
            <input v-model="profileForm.fullName" type="text" />
          </label>
          <label>
            <span>{{ t("account.phone") }}</span>
            <input v-model="profileForm.phone" type="tel" />
          </label>
          <label>
            <span>{{ t("account.email") }}</span>
            <input v-model="profileForm.email" type="email" />
          </label>
          <label>
            <span>{{ t("account.businessName") }}</span>
            <input v-model="profileForm.businessName" type="text" />
          </label>
          <label>
            <span>{{ t("account.fanpageName") }}</span>
            <input v-model="profileForm.fanpageName" type="text" />
          </label>
        </div>
        <button type="button" class="btn primary" @click="saveProfile">{{ t("account.save") }}</button>
        <p v-if="saveHint" class="hint success">{{ saveHint }}</p>
      </section>

      <section class="card package-summary-card">
        <h2 class="section-title">{{ t("account.currentPackage") }}</h2>
        <div class="summary-row strong">
          <span>{{ t("account.packageName") }}</span>
          <strong>{{ packageTitle(account.subscription) }}</strong>
        </div>
        <div class="summary-row">
          <span>{{ t("account.packageStart") }}</span>
          <strong>{{ formatDate(account.subscription?.startDate) }}</strong>
        </div>
        <div class="summary-row">
          <span>{{ t("account.packageEnd") }}</span>
          <strong>{{ formatDate(account.subscription?.endDate) }}</strong>
        </div>
        <div class="summary-row">
          <span>{{ t("account.accountStatus") }}</span>
          <strong>{{ statusLabel(account?.status) }}</strong>
        </div>
      </section>

      <section class="card upgrade-card">
        <div class="section-heading">
          <h2 class="section-title">{{ t("account.upgradeTitle") }}</h2>
          <p>{{ t("account.upgradeIntro", { current: packageTitle(account.subscription) }) }}</p>
        </div>

        <article v-for="pkg in upgradePackages" :key="pkg.code" class="upgrade-option">
          <div class="upgrade-top">
            <div>
              <h3>{{ packageTitle(pkg) }}</h3>
              <p>{{ packageBenefits(pkg) }}</p>
            </div>
            <span class="price-chip">{{ packagePrice(pkg) }}</span>
          </div>
          <div class="package-metrics">
            <span>{{ t("account.postsCount", { count: pkg.quotaPosts ?? pkg.posts ?? 0 }) }}</span>
            <span>{{ packageMedia(pkg) }}</span>
          </div>
          <ul v-if="packageServices(pkg).length" class="service-tags">
            <li v-for="service in packageServices(pkg)" :key="service">{{ service }}</li>
          </ul>
          <button type="button" class="btn secondary" @click="openUpgradeForm(pkg)">
            {{ t("account.upgradeSubmit") }}
          </button>
        </article>

        <div v-if="selectedUpgrade" class="upgrade-form">
          <div class="form-head">
            <h3>{{ t("account.upgradeFormTitle") }}</h3>
            <button type="button" @click="closeUpgradeForm">{{ t("review.close") }}</button>
          </div>
          <label>
            <span>{{ t("account.upgradeSelect") }}</span>
            <select v-model="upgradeCode">
              <option v-for="pkg in upgradePackages" :key="pkg.code" :value="pkg.code">
                {{ packageTitle(pkg) }}
              </option>
            </select>
          </label>
          <label>
            <span>{{ t("account.upgradeMessage") }}</span>
            <textarea v-model="upgradeNote" rows="3" :placeholder="t('account.upgradeMessagePlaceholder')" />
          </label>
          <button type="button" class="btn primary" @click="submitUpgrade">{{ t("account.sendRequest") }}</button>
          <p v-if="upgradeHint" class="hint success">{{ upgradeHint }}</p>
        </div>
      </section>

      <section class="card voucher-card">
        <h2 class="section-title">{{ t("account.vouchersTitle") }}</h2>
        <p v-if="!vouchers.length" class="hint-box">{{ t("account.vouchersEmpty") }}</p>
        <article v-for="voucher in vouchers" :key="voucher.id" class="voucher-item">
          <div class="voucher-head">
            <div>
              <strong>{{ voucher.title }}</strong>
              <span class="voucher-code">{{ voucher.code }}</span>
            </div>
            <span class="voucher-status" :class="String(voucher.status).toLowerCase()">
              {{ statusLabel(voucher.status) }}
            </span>
          </div>
          <p><b>{{ t("account.voucherValue") }}:</b> {{ voucher.value }}</p>
          <p><b>{{ t("account.voucherCondition") }}:</b> {{ voucher.condition || t("account.noCondition") }}</p>
          <p><b>{{ t("account.voucherExpiry") }}:</b> {{ formatDate(voucher.expiresAt) }}</p>
        </article>
      </section>

      <template v-for="item in accountMenus" :key="item.id">
        <button v-if="item.type === 'logout'" type="button" class="menu-btn danger" @click="onMenu(item)">
          {{ t(item.labelKey) }}
        </button>
        <template v-else-if="item.type === 'group'">
          <p class="section-label">{{ t(item.labelKey) }}</p>
          <button v-for="sub in item.items" :key="sub.id" type="button" class="menu-btn" @click="onMenu(sub)">
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

.identity-card {
  color: #fff;
  border: none;
  background: linear-gradient(135deg, #0a3d9e 0%, #1a6dff 58%, #6aa7ff 100%);
  box-shadow: 0 16px 36px rgba(15, 61, 94, 0.2);
}

.identity-top {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-mark {
  width: 48px;
  height: 48px;
  display: grid;
  place-items: center;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.28);
  font-size: 1.35rem;
  font-weight: 900;
}

.eyebrow,
.user-line {
  margin: 0;
  opacity: 0.78;
  font-size: 0.78rem;
  font-weight: 700;
}

.identity-top h2 {
  margin: 3px 0;
  font-size: 1.08rem;
}

.status-chip,
.price-chip,
.voucher-status {
  margin-left: auto;
  width: fit-content;
  border-radius: 999px;
  padding: 5px 10px;
  font-size: 0.68rem;
  font-weight: 800;
  white-space: nowrap;
}

.status-chip {
  color: #064e3b;
  background: #dcfce7;
}

.profile-grid {
  display: grid;
  gap: 10px;
}

.profile-grid label,
.upgrade-form label {
  display: grid;
  gap: 6px;
}

.profile-grid span,
.upgrade-form span {
  color: #475569;
  font-size: 0.78rem;
  font-weight: 700;
}

.profile-grid input,
.upgrade-form select,
.upgrade-form textarea {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid #dbe3ed;
  border-radius: 13px;
  background: #f8fafc;
  padding: 12px 13px;
  color: #0f172a;
  font: inherit;
}

.btn {
  width: 100%;
  margin-top: 14px;
  padding: 12px;
  border-radius: 13px;
  border: none;
  cursor: pointer;
  font: inherit;
  font-weight: 800;
}

.btn.primary {
  color: #fff;
  background: linear-gradient(135deg, #1a6dff, #3d8cff);
}

.btn.secondary {
  color: #1a6dff;
  background: #fff;
  border: 2px solid #1a6dff;
}

.hint {
  margin: 10px 0 0;
  color: #64748b;
  font-size: 0.82rem;
}

.hint.success {
  color: #15803d;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 11px 0;
  border-bottom: 1px solid #edf2f7;
  font-size: 0.84rem;
}

.summary-row:last-child {
  border-bottom: none;
}

.summary-row span {
  color: #64748b;
  font-weight: 700;
}

.summary-row strong {
  color: #0f172a;
  text-align: right;
}

.section-heading p,
.upgrade-option p {
  margin: 0;
  color: #64748b;
  font-size: 0.83rem;
  line-height: 1.5;
}

.upgrade-option {
  margin-top: 12px;
  padding: 14px;
  border: 1px solid #dbeafe;
  border-radius: 16px;
  background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
}

.upgrade-top {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.upgrade-top h3 {
  margin: 0 0 5px;
  color: #0f172a;
  font-size: 0.96rem;
}

.price-chip {
  color: #1d4ed8;
  background: #eff6ff;
}

.package-metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin-top: 12px;
}

.package-metrics span {
  padding: 9px;
  border-radius: 12px;
  background: #f1f5f9;
  color: #334155;
  font-size: 0.78rem;
  font-weight: 800;
}

.service-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  list-style: none;
  margin: 12px 0 0;
  padding: 0;
}

.service-tags li {
  padding: 5px 8px;
  border-radius: 999px;
  background: #eff6ff;
  color: #1d4ed8;
  font-size: 0.72rem;
  font-weight: 700;
}

.upgrade-form {
  margin-top: 14px;
  padding: 14px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.form-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.form-head h3 {
  margin: 0;
  color: #0f172a;
  font-size: 0.95rem;
}

.form-head button {
  min-height: 32px;
  border: 1px solid #dbeafe;
  border-radius: 999px;
  background: #fff;
  color: #1d4ed8;
  cursor: pointer;
  font-weight: 800;
  padding: 5px 10px;
}

.voucher-item {
  padding: 14px 0;
  border-bottom: 1px solid #edf2f7;
}

.voucher-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.voucher-head {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
}

.voucher-head strong {
  display: block;
  color: #0f172a;
  font-size: 0.9rem;
  margin-bottom: 5px;
}

.voucher-code {
  display: inline-flex;
  width: fit-content;
  border-radius: 8px;
  background: #fff7ed;
  color: #c2410c;
  padding: 4px 8px;
  font-size: 0.75rem;
  font-weight: 900;
}

.voucher-status {
  color: #166534;
  background: #dcfce7;
}

.voucher-status.used {
  color: #475569;
  background: #f1f5f9;
}

.voucher-status.expired {
  color: #991b1b;
  background: #fee2e2;
}

.voucher-item p {
  margin: 5px 0 0;
  color: #64748b;
  font-size: 0.82rem;
  line-height: 1.45;
}

.voucher-item b {
  color: #334155;
}

@media (max-width: 380px) {
  .identity-top,
  .upgrade-top,
  .voucher-head {
    align-items: flex-start;
  }

  .package-metrics {
    grid-template-columns: 1fr;
  }
}
</style>
