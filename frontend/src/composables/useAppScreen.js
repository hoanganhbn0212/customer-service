import { computed, ref } from "vue";
import {
  PACKAGE_CODES,
  PACKAGES,
  SCREENS,
  DAILY_SCHEDULE_ITEMS,
  getAccountMenusForRole,
  getNotificationsForTier,
  getPackageTier,
  getActivePackageTitleKey,
  loadStoredPackage,
  saveStoredPackage
} from "../config/appScreenConfig";
import { getMobileHome, getMobileServices, listAvailablePackages } from "../api/mobileApi";
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

function formatDisplayDate(iso) {
  if (!iso) return "";
  const [y, m, d] = iso.split("-");
  return `${d}/${m}/${y}`;
}

const activePackage = ref(loadStoredPackage());
const packageStart = ref("");
const packageEnd = ref("");
const scheduleMonth = ref(new Date());
const selectedDate = ref(new Date());

const homeSnapshot = ref(null);
const servicesSnapshot = ref(null);
const packageCatalog = ref([]);
const homeLoading = ref(false);
const servicesLoading = ref(false);
const apiError = ref(null);

const fallbackPackageOptions = PACKAGE_CODES.map((code) => ({
  code,
  label: null,
  labelKey: PACKAGES[code].labelKey,
  tier: PACKAGES[code].tier,
  tierKey: PACKAGES[code].tierKey,
  quotaPosts: PACKAGES[code].posts,
  quotaImages: PACKAGES[code].images,
  quotaVideos: PACKAGES[code].videos
}));

function toPackageOption(item) {
  const fallback = PACKAGES[item.code];
  return {
    code: item.code,
    label: item.label,
    labelKey: fallback?.labelKey ?? null,
    tier: item.tier,
    tierKey: fallback?.tierKey ?? (item.tier === "PRO" ? "home.tierPro" : "home.tierBasic"),
    quotaPosts: item.quotaPosts,
    quotaImages: item.quotaImages,
    quotaVideos: item.quotaVideos
  };
}

export function useAppScreen(screenId) {
  const screen = computed(() => SCREENS[screenId] ?? {});

  const pkg = computed(() => {
    const code =
      homeSnapshot.value?.subscription?.packageCode ||
      servicesSnapshot.value?.activeSubscription?.packageCode ||
      activePackage.value;
    return (
      packageCatalog.value.find((item) => item.code === code) ||
      fallbackPackageOptions.find((item) => item.code === code) ||
      fallbackPackageOptions[1]
    );
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
    packageCatalog.value.length ? packageCatalog.value : fallbackPackageOptions
  );

  function setActivePackage(code) {
    if (!packageOptions.value.some((item) => item.code === code)) return;
    activePackage.value = code;
    saveStoredPackage(code);
  }

  const overallPercent = computed(() => {
    if (homeSnapshot.value?.progress) {
      return homeSnapshot.value.progress.overallPercent ?? 0;
    }
    return 0;
  });

  const overallStatus = computed(() => {
    if (homeSnapshot.value?.progress?.status) {
      return homeSnapshot.value.progress.status;
    }
    if (overallPercent.value >= 100) return "done";
    if (overallPercent.value > 0) return "progress";
    return "pending";
  });

  const overallCompleted = computed(() => {
    if (homeSnapshot.value?.progress?.completedItems != null) {
      return homeSnapshot.value.progress.completedItems;
    }
    return 0;
  });

  const overallTotal = computed(() => {
    if (homeSnapshot.value?.progress?.totalItems != null) {
      return homeSnapshot.value.progress.totalItems;
    }
    return 0;
  });

  const progressBreakdown = computed(() => {
    const progress = homeSnapshot.value?.progress;
    if (!progress) {
      return [];
    }
    const rows = [
      {
        id: "posts",
        labelKey: "home.progressPosts",
        completed: progress.completedPosts ?? 0,
        total: progress.quotaPosts ?? 0
      },
      {
        id: "images",
        labelKey: "home.progressImages",
        completed: progress.completedImages ?? 0,
        total: progress.quotaImages ?? 0
      },
      {
        id: "videos",
        labelKey: "home.progressVideos",
        completed: progress.completedVideos ?? 0,
        total: progress.quotaVideos ?? 0
      }
    ];
    return rows
      .filter((row) => row.total > 0)
      .map((row) => ({
        ...row,
        percent: row.total === 0 ? 0 : Math.min(100, Math.round((row.completed / row.total) * 100))
      }));
  });

  const subscriptionStatus = computed(
    () =>
      homeSnapshot.value?.subscription?.status ||
      servicesSnapshot.value?.activeSubscription?.status ||
      "ACTIVE"
  );

  const deploymentStatus = computed(
    () =>
      homeSnapshot.value?.subscription?.deploymentStatus ||
      servicesSnapshot.value?.activeSubscription?.deploymentStatus ||
      null
  );

  const dashboardStatus = computed(() => {
    if (subscriptionStatus.value !== "ACTIVE") {
      return "paused";
    }
    if (deploymentStatus.value === "PAUSED") {
      return "paused";
    }
    if (deploymentStatus.value === "COMPLETED") {
      return "done";
    }
    return overallStatus.value === "done" ? "done" : "progress";
  });

  const services = computed(() => {
    if (homeSnapshot.value?.services?.length) {
      return homeSnapshot.value.services.map((item, index) => ({
        id: item.id,
        index: index + 1,
        icon: item.icon || "doc",
        name: item.name,
        nameKey: null,
        trackMode: item.trackMode || "status",
        completedCount: item.completedCount ?? null,
        totalCount: item.totalCount ?? null,
        percent: item.percent ?? 0,
        status: item.status
      }));
    }
    if (servicesSnapshot.value?.packageServices?.length) {
      return servicesSnapshot.value.packageServices.map((item, index) => ({
        id: item.id,
        index: index + 1,
        icon: item.icon || "doc",
        name: item.name,
        nameKey: null,
        desc: item.description,
        descKey: null,
        trackMode: item.trackMode || "status",
        completedCount: item.completedCount ?? null,
        totalCount: item.totalCount ?? null,
        percent: item.percent ?? 0,
        status: item.status || "pending"
      }));
    }
    return [];
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
        reviewable: item.reviewable,
        plannedPublishDate: item.plannedPublishDate,
        topic: item.topic,
        ideaFrame: item.ideaFrame,
        postContent: item.postContent,
        contentStatus: item.contentStatus,
        attachmentUrl: item.attachmentUrl,
        contentScore: item.contentScore,
        customerComment: item.customerComment,
        improvementSuggestion: item.improvementSuggestion,
        completedOn: item.completedOn,
        mediaName: item.mediaName,
        mediaType: item.mediaType,
        previewUrl: item.previewUrl,
        designScore: item.designScore,
        designCustomerComment: item.designCustomerComment,
        designImprovementSuggestion: item.designImprovementSuggestion
      }));
    }
    return [];
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
    return [];
  });

  const dailyScheduleItems = computed(() => {
    const apiTasks = homeSnapshot.value?.schedule?.tasks;
    if (apiTasks?.length) {
      const selected = dateKey(selectedDate.value);
      return apiTasks.map((task) => ({
        date: formatDisplayDate(task.date || task.taskDate || selected),
        task: task.task || task.title || "",
        service: task.service || task.relatedService || "—",
        status: task.status || "—",
        note: task.note || task.remark || ""
      }));
    }
    return DAILY_SCHEDULE_ITEMS.map((item) => ({
      ...item,
      date: formatDisplayDate(item.date)
    }));
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
      await refreshPackageCatalog();
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
      await refreshPackageCatalog();
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

  async function refreshPackageCatalog() {
    if (packageCatalog.value.length) {
      return;
    }
    try {
      const rows = await listAvailablePackages();
      packageCatalog.value = rows.map(toPackageOption);
    } catch {
      packageCatalog.value = [];
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
    overallCompleted,
    overallTotal,
    progressBreakdown,
    overallStatus,
    subscriptionStatus,
    deploymentStatus,
    dashboardStatus,
    services,
    implementationTasks,
    notifications,
    accountMenus,
    weekDays,
    tasksForSelectedDay,
    dailyScheduleItems,
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
