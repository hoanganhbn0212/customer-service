import { computed, ref } from "vue";
import {
  DEMO_DEFAULTS,
  PACKAGE_CODES,
  PACKAGES,
  SCREENS,
  getAccountMenusForRole,
  getNotificationsForTier,
  getPackageTier,
  getActivePackageTitleKey,
  getImplementationTasksForTier,
  getServicesForPackage,
  loadStoredPackage,
  saveStoredPackage
} from "../config/appScreenConfig";
import { getMobileHome, getMobileServices } from "../api/mobileApi";
import { getUserRole } from "../auth/session";

const DOW_KEYS = [
  "home.dowMon",
  "home.dowTue",
  "home.dowWed",
  "home.dowThu",
  "home.dowFri",
  "home.dowSat",
  "home.dowSun"
];

function dateKey(d) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
}

function parseDate(iso) {
  const [y, m, d] = iso.split("-").map(Number);
  return new Date(y, m - 1, d);
}

const activePackage = ref(loadStoredPackage());
const packageStart = ref(DEMO_DEFAULTS.packageStart);
const packageEnd = ref(DEMO_DEFAULTS.packageEnd);
const completedPosts = ref(DEMO_DEFAULTS.completedPosts);
const completedImages = ref(DEMO_DEFAULTS.completedImages);
const completedVideos = ref(DEMO_DEFAULTS.completedVideos);
const serviceProgress = ref({ ...DEMO_DEFAULTS.serviceProgress });
const scheduleMonth = ref(new Date());
const selectedDate = ref(new Date());
const dailyTasks = ref({ ...DEMO_DEFAULTS.dailyTasks });

const homeSnapshot = ref(null);
const servicesSnapshot = ref(null);
const homeLoading = ref(false);
const servicesLoading = ref(false);
const apiError = ref(null);

export function useAppScreen(screenId) {
  const screen = computed(() => SCREENS[screenId] ?? {});

  const pkg = computed(() => {
    const code =
      homeSnapshot.value?.subscription?.packageCode ||
      servicesSnapshot.value?.activeSubscription?.packageCode ||
      activePackage.value;
    return PACKAGES[code] ?? PACKAGES.PRO_15;
  });

  const tier = computed(() => {
    const fromApi =
      homeSnapshot.value?.subscription?.tier ||
      servicesSnapshot.value?.activeSubscription?.tier;
    if (fromApi) {
      return fromApi;
    }
    return getPackageTier(activePackage.value);
  });

  const subscriptionTitle = computed(() => {
    return (
      homeSnapshot.value?.subscription?.displayTitle ||
      servicesSnapshot.value?.activeSubscription?.displayTitle ||
      null
    );
  });

  const packageOptions = computed(() =>
    PACKAGE_CODES.map((code) => ({
      code,
      labelKey: PACKAGES[code].labelKey,
      tierKey: PACKAGES[code].tierKey
    }))
  );

  function setActivePackage(code) {
    if (!PACKAGE_CODES.includes(code)) return;
    activePackage.value = code;
    saveStoredPackage(code);
  }

  const overallPercent = computed(() => {
    if (homeSnapshot.value?.progress) {
      return homeSnapshot.value.progress.overallPercent ?? 0;
    }
    const p = pkg.value;
    const total = p.posts + p.images + p.videos;
    if (total === 0) return 0;
    const completed =
      Math.min(completedPosts.value, p.posts) +
      Math.min(completedImages.value, p.images) +
      Math.min(completedVideos.value, p.videos);
    return Math.round((completed / total) * 100);
  });

  const overallStatus = computed(() => {
    if (homeSnapshot.value?.progress?.status) {
      return homeSnapshot.value.progress.status;
    }
    if (overallPercent.value >= 100) return "done";
    if (overallPercent.value > 0) return "progress";
    return "pending";
  });

  const services = computed(() => {
    if (homeSnapshot.value?.services?.length) {
      return homeSnapshot.value.services.map((item, index) => ({
        id: item.id,
        index: index + 1,
        icon: item.icon || "doc",
        name: item.name,
        nameKey: null,
        percent: item.percent ?? 0,
        status: item.status
      }));
    }
    if (servicesSnapshot.value?.packageServices?.length) {
      return servicesSnapshot.value.packageServices.map((item, index) => ({
        id: item.id,
        index: index + 1,
        icon: "doc",
        name: item.name,
        nameKey: null,
        desc: item.description,
        descKey: null,
        percent: 0,
        status: "pending"
      }));
    }
    return getServicesForPackage(activePackage.value, serviceProgress.value);
  });

  const implementationTasks = computed(() => {
    if (servicesSnapshot.value?.implementationItems?.length) {
      return servicesSnapshot.value.implementationItems.map((item) => ({
        id: item.id,
        code: item.code,
        title: item.title,
        titleKey: null,
        category: item.category,
        current: item.currentCount,
        total: item.targetCount,
        status: item.status,
        date: item.updatedOn,
        deliverableId: item.deliverableId,
        reviewable: item.reviewable
      }));
    }
    return getImplementationTasksForTier(tier.value);
  });

  const activePackageTitleKey = computed(() => {
    if (subscriptionTitle.value) {
      return null;
    }
    return getActivePackageTitleKey(tier.value);
  });

  const packageStartDisplay = computed(
    () =>
      homeSnapshot.value?.subscription?.startDate ||
      servicesSnapshot.value?.activeSubscription?.startDate ||
      packageStart.value
  );

  const packageEndDisplay = computed(
    () =>
      homeSnapshot.value?.subscription?.endDate ||
      servicesSnapshot.value?.activeSubscription?.endDate ||
      packageEnd.value
  );

  const notifications = computed(() => getNotificationsForTier(tier.value));
  const accountMenus = computed(() => getAccountMenusForRole(getUserRole()));

  const weekDays = computed(() => {
    if (homeSnapshot.value?.schedule?.weekDays?.length) {
      return homeSnapshot.value.schedule.weekDays.map((d) => ({
        labelKey: DOW_KEYS[d.dowIndex ?? 0],
        date: d.dayOfMonth,
        full: parseDate(d.date),
        key: d.date,
        isSelected: d.selected
      }));
    }
    const base = new Date(selectedDate.value);
    const day = base.getDay();
    const mondayOffset = day === 0 ? -6 : 1 - day;
    const monday = new Date(base);
    monday.setDate(base.getDate() + mondayOffset);
    return DOW_KEYS.map((labelKey, i) => {
      const d = new Date(monday);
      d.setDate(monday.getDate() + i);
      return {
        labelKey,
        date: d.getDate(),
        full: d,
        key: dateKey(d),
        isSelected: dateKey(d) === dateKey(selectedDate.value)
      };
    });
  });

  const tasksForSelectedDay = computed(() => {
    if (homeSnapshot.value?.schedule?.tasks) {
      return homeSnapshot.value.schedule.tasks.map((task) => ({
        title: task.title,
        titleKey: null,
        time: task.scheduledTime || ""
      }));
    }
    return dailyTasks.value[dateKey(selectedDate.value)] || [];
  });

  const monthLabel = computed(() => {
    if (homeSnapshot.value?.schedule) {
      return {
        month: homeSnapshot.value.schedule.month,
        year: homeSnapshot.value.schedule.year
      };
    }
    const d = scheduleMonth.value;
    return { month: d.getMonth() + 1, year: d.getFullYear() };
  });

  const progressRing = computed(() => {
    const r = 46;
    const c = 2 * Math.PI * r;
    const offset = c - (overallPercent.value / 100) * c;
    return { r, c, offset };
  });

  async function refreshHome() {
    homeLoading.value = true;
    apiError.value = null;
    try {
      const data = await getMobileHome(dateKey(selectedDate.value));
      homeSnapshot.value = data;
      if (data.subscription?.packageCode) {
        activePackage.value = data.subscription.packageCode;
      }
      if (data.subscription?.startDate) {
        packageStart.value = data.subscription.startDate;
      }
      if (data.subscription?.endDate) {
        packageEnd.value = data.subscription.endDate;
      }
      if (data.schedule?.month && data.schedule?.year) {
        scheduleMonth.value = new Date(data.schedule.year, data.schedule.month - 1, 1);
      }
    } catch (err) {
      apiError.value = err;
      homeSnapshot.value = null;
    } finally {
      homeLoading.value = false;
    }
  }

  async function refreshServices(category = "all") {
    servicesLoading.value = true;
    apiError.value = null;
    try {
      servicesSnapshot.value = await getMobileServices(category);
      const sub = servicesSnapshot.value.activeSubscription;
      if (sub?.packageCode) {
        activePackage.value = sub.packageCode;
      }
      if (sub?.startDate) {
        packageStart.value = sub.startDate;
      }
      if (sub?.endDate) {
        packageEnd.value = sub.endDate;
      }
    } catch (err) {
      apiError.value = err;
      servicesSnapshot.value = null;
    } finally {
      servicesLoading.value = false;
    }
  }

  function selectDay(day) {
    selectedDate.value = new Date(day.full);
    scheduleMonth.value = new Date(day.full.getFullYear(), day.full.getMonth(), 1);
    refreshHome();
  }

  function prevMonth() {
    const d = new Date(scheduleMonth.value);
    d.setMonth(d.getMonth() - 1);
    scheduleMonth.value = d;
  }

  function nextMonth() {
    const d = new Date(scheduleMonth.value);
    d.setMonth(d.getMonth() + 1);
    scheduleMonth.value = d;
  }

  const hasBlock = (blockId) => {
    const blocks = screen.value.blocks ?? [];
    return blocks.includes(blockId);
  };

  return {
    screen,
    screenId,
    activePackage,
    packageOptions,
    setActivePackage,
    pkg,
    tier,
    subscriptionTitle,
    activePackageTitleKey,
    packageStart: packageStartDisplay,
    packageEnd: packageEndDisplay,
    overallPercent,
    overallStatus,
    services,
    implementationTasks,
    notifications,
    accountMenus,
    weekDays,
    tasksForSelectedDay,
    monthLabel,
    progressRing,
    selectDay,
    prevMonth,
    nextMonth,
    hasBlock,
    refreshHome,
    refreshServices,
    homeLoading,
    servicesLoading,
    apiError,
    usingApi: computed(() => Boolean(homeSnapshot.value || servicesSnapshot.value))
  };
}
