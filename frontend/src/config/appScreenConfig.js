/**
 * Cấu hình chung cho toàn bộ màn hình mobile.
 * Đổi gói / dịch vụ / nội dung từng màn tại đây (hoặc thay bằng API sau).
 *
 * @see docs/MOBILE_API_AND_DATABASE.md — thiết kế DB + API theo màn
 * @see backend/src/main/resources/openapi/mobile-api.yaml
 * @see backend/src/main/resources/db/schema-mobile-app.sql
 */

/** @typedef {'BASIC_15'|'PRO_15'|'BASIC_30'|'PRO_30'} PackageCode */
/** @typedef {'BASIC'|'PRO'} PackageTier */
/** @typedef {'home'|'services'|'schedule'|'notifications'|'account'} ScreenId */

export const PACKAGE_CODES = ["BASIC_15", "PRO_15", "BASIC_30", "PRO_30"];

export const PACKAGES = {
  BASIC_15: {
    code: "BASIC_15",
    tier: "BASIC",
    labelKey: "home.packageBasic15",
    tierKey: "home.tierBasic",
    posts: 15,
    images: 15,
    videos: 0
  },
  PRO_15: {
    code: "PRO_15",
    tier: "PRO",
    labelKey: "home.packagePro15",
    tierKey: "home.tierPro",
    posts: 15,
    images: 10,
    videos: 5
  },
  BASIC_30: {
    code: "BASIC_30",
    tier: "BASIC",
    labelKey: "home.packageBasic30",
    tierKey: "home.tierBasic",
    posts: 30,
    images: 30,
    videos: 0
  },
  PRO_30: {
    code: "PRO_30",
    tier: "PRO",
    labelKey: "home.packagePro30",
    tierKey: "home.tierPro",
    posts: 30,
    images: 20,
    videos: 10
  }
};

/** Danh mục dịch vụ — id dùng xuyên suốt các màn */
export const SERVICE_CATALOG = {
  fanpage: {
    id: "fanpage",
    icon: "doc",
    nameKey: "home.service.fanpage",
    descKey: "services.desc.fanpage",
    tiers: ["PRO"]
  },
  content: {
    id: "content",
    icon: "edit",
    nameKey: "home.service.content",
    descKey: "services.desc.content",
    tiers: ["BASIC", "PRO"]
  },
  ads: {
    id: "ads",
    icon: "ads",
    nameKey: "home.service.ads",
    descKey: "services.desc.ads",
    tiers: ["PRO"]
  },
  report: {
    id: "report",
    icon: "chart",
    nameKey: "home.service.report",
    descKey: "services.desc.report",
    tiers: ["PRO"]
  },
  posts: {
    id: "posts",
    icon: "edit",
    nameKey: "home.service.posts",
    descKey: "services.desc.posts",
    tiers: ["BASIC"]
  },
  design: {
    id: "design",
    icon: "image",
    nameKey: "home.service.design",
    descKey: "services.desc.design",
    tiers: ["BASIC"]
  },
  cover: {
    id: "cover",
    icon: "image",
    nameKey: "services.extra.cover",
    descKey: "services.desc.cover",
    tiers: ["PRO"]
  },
  like: {
    id: "like",
    icon: "heart",
    nameKey: "services.extra.like",
    descKey: "services.desc.like",
    tiers: ["PRO"]
  }
};

/** Dịch vụ hiển thị theo tier (Basic vs Pro) */
export const PACKAGE_SERVICE_IDS = {
  BASIC: ["posts", "design"],
  PRO: ["fanpage", "content", "ads", "report", "cover", "like"]
};

/** Cấu hình từng màn — bật/tắt block, copy, hành vi */
export const SCREENS = {
  home: {
    id: "home",
    titleKey: "nav.overview",
    showBell: true,
    blocks: ["packageCard", "overallProgress", "serviceList"]
  },
  services: {
    id: "services",
    titleKey: "nav.services",
    showBell: true,
    listMode: "implementation"
  },
  schedule: {
    id: "schedule",
    titleKey: "nav.schedule",
    subtitleKey: "schedule.subtitle"
  },
  notifications: {
    id: "notifications",
    titleKey: "nav.notifications",
    subtitleKey: "notifications.subtitle"
  },
  account: {
    id: "account",
    titleKey: "nav.account",
    showPackageSwitcher: true
  }
};

/** Thông báo mẫu — lọc theo tiers */
export const NOTIFICATION_ITEMS = [
  {
    id: "n1",
    title: "Bài viết số 12 đã được hoàn thiện",
    body: "Đội ngũ đã hoàn thiện bài viết số 12. Anh/chị vui lòng kiểm tra và phản hồi nếu cần chỉnh sửa.",
    createdAt: "2026-06-04T09:30:00+07:00",
    type: "CONTENT_DONE",
    read: false,
    tiers: ["BASIC", "PRO"]
  },
  {
    id: "n2",
    title: "Ảnh cho bài viết số 12 đã hoàn thiện",
    body: "File thiết kế ảnh đã sẵn sàng. Anh/chị vui lòng xem trước và phản hồi nếu cần điều chỉnh.",
    createdAt: "2026-06-04T14:15:00+07:00",
    type: "MEDIA_DONE",
    read: false,
    tiers: ["BASIC", "PRO"]
  },
  {
    id: "n3",
    title: "Cần duyệt nội dung bài viết số 13",
    body: "Nội dung bài viết số 13 đang chờ anh/chị duyệt trước khi chuyển sang bước thiết kế.",
    createdAt: "2026-06-05T08:45:00+07:00",
    type: "APPROVAL_REQUIRED",
    read: false,
    tiers: ["BASIC", "PRO"]
  },
  {
    id: "n4",
    title: "Phản hồi về góp ý của anh/chị",
    body: "Đội ngũ đã tiếp nhận feedback và cập nhật lại nội dung theo góp ý của anh/chị.",
    createdAt: "2026-06-05T16:20:00+07:00",
    type: "FEEDBACK_REPLY",
    read: true,
    tiers: ["BASIC", "PRO"]
  },
  {
    id: "n5",
    title: "Ưu đãi nâng cấp gói dịch vụ",
    body: "Khách hàng đang sử dụng gói hiện tại được ưu đãi khi nâng cấp lên gói Pro trong tháng này.",
    createdAt: "2026-06-06T10:00:00+07:00",
    type: "OFFER",
    read: true,
    tiers: ["BASIC", "PRO"]
  },
  {
    id: "n6",
    title: "Khuyến mại thiết kế thêm ảnh",
    body: "Đăng ký thêm hạng mục thiết kế trong tuần này để nhận mức giá khuyến mại.",
    createdAt: "2026-06-06T11:30:00+07:00",
    type: "PROMOTION",
    read: true,
    tiers: ["BASIC", "PRO"]
  },
  {
    id: "n7",
    title: "Gợi ý nhắc lịch duyệt bài",
    body: "Anh/chị có thể kiểm tra nội dung đang chờ duyệt để kịp tiến độ đăng bài.",
    createdAt: "2026-06-07T09:00:00+07:00",
    type: "SCHEDULE_REMINDER",
    read: false,
    tiers: ["BASIC", "PRO"]
  },
  {
    id: "n8",
    title: "Nhắc kiểm tra tiến độ gói dịch vụ",
    body: "Tiến độ triển khai đã được cập nhật. Anh/chị vui lòng kiểm tra các hạng mục đã hoàn thành.",
    createdAt: "2026-06-07T15:00:00+07:00",
    type: "PROGRESS_REMINDER",
    read: true,
    tiers: ["BASIC", "PRO"]
  }
];

/**
 * Hạng mục triển khai trên màn Dịch vụ (demo — thay API sau).
 * category: content | ads | report
 * status: approved | in_progress | waiting_feedback
 */
export const IMPLEMENTATION_TASKS = [
  {
    id: "design_post",
    titleKey: "services.tasks.designPost",
    category: "content",
    current: 12,
    total: 24,
    status: "approved",
    date: "2024-06-12",
    tiers: ["PRO", "BASIC"]
  },
  {
    id: "seo_content",
    titleKey: "services.tasks.seoContent",
    category: "content",
    current: 8,
    total: 16,
    status: "in_progress",
    date: "2024-06-11",
    tiers: ["PRO"]
  },
  {
    id: "run_ads",
    titleKey: "services.tasks.runAds",
    category: "ads",
    current: 2,
    total: 5,
    status: "waiting_feedback",
    date: "2024-06-10",
    tiers: ["PRO"]
  },
  {
    id: "weekly_report",
    titleKey: "services.tasks.weeklyReport",
    category: "report",
    current: 23,
    total: 24,
    status: "approved",
    date: "2024-06-09",
    tiers: ["PRO"]
  },
  {
    id: "write_posts",
    titleKey: "services.tasks.writePosts",
    category: "content",
    current: 10,
    total: 15,
    status: "in_progress",
    date: "2024-06-12",
    tiers: ["BASIC"]
  },
  {
    id: "design_images",
    titleKey: "services.tasks.designImages",
    category: "content",
    current: 12,
    total: 15,
    status: "approved",
    date: "2024-06-11",
    tiers: ["BASIC"]
  }
];

export const SERVICE_FILTER_IDS = ["all", "content", "ads", "report"];

/** Demo: lịch trình giai đoạn 1, chỉ hiển thị theo từng ngày. */
export const DAILY_SCHEDULE_ITEMS = [
  {
    date: "2026-06-05",
    task: "Hoàn thiện bài viết số 5",
    service: "Viết bài",
    status: "Đang làm",
    note: ""
  },
  {
    date: "2026-06-06",
    task: "Thiết kế ảnh cho bài viết số 5",
    service: "Thiết kế hình ảnh",
    status: "Chờ duyệt",
    note: ""
  }
];

/** Demo: màn đánh giá khi khách xem bài / hình (theo task id) */
export const REVIEW_BY_TASK = {
  design_post: {
    postNumber: "1256",
    responseStatus: "responded",
    thumbnailUrl:
      "https://images.unsplash.com/photo-1556228578-0d85b1a4d571?w=160&h=160&fit=crop",
    teamContent: 8.5,
    teamDesign: 9.0,
    defaultScore: 9,
    sampleCommentKey: "review.sampleComment.designPost"
  },
  seo_content: {
    postNumber: "1240",
    responseStatus: "responded",
    thumbnailUrl:
      "https://images.unsplash.com/photo-1570172619644-dfdcef6e0e0c?w=160&h=160&fit=crop",
    teamContent: 8.0,
    teamDesign: 8.5,
    defaultScore: 8,
    sampleCommentKey: "review.sampleComment.seo"
  },
  write_posts: {
    postNumber: "1180",
    responseStatus: "pending",
    thumbnailUrl:
      "https://images.unsplash.com/photo-1612817288484-6f933f271cf2?w=160&h=160&fit=crop",
    teamContent: 7.5,
    teamDesign: 8.0,
    defaultScore: 8,
    sampleCommentKey: "review.sampleComment.writePosts"
  },
  design_images: {
    postNumber: "1175",
    responseStatus: "responded",
    thumbnailUrl:
      "https://images.unsplash.com/photo-1596462502278-27bfdc403348?w=160&h=160&fit=crop",
    teamContent: 8.2,
    teamDesign: 9.2,
    defaultScore: 9,
    sampleCommentKey: "review.sampleComment.designImages"
  }
};

export function getReviewForTask(taskId) {
  return REVIEW_BY_TASK[taskId] ?? null;
}

export function isTaskReviewable(taskId) {
  return Boolean(REVIEW_BY_TASK[taskId]);
}

export function reviewDraftKey(taskId) {
  return `layla_review_draft_${taskId}`;
}

/** Menu tài khoản */
export const ACCOUNT_MENUS = [
  {
    id: "logout",
    type: "logout",
    labelKey: "account.logout",
    style: "danger"
  },
  {
    id: "admin-group",
    type: "group",
    labelKey: "account.adminSection",
    roles: ["ADMIN", "DEVELOP"],
    items: [
      { id: "admin-users", labelKey: "admin.nav", route: "/admin/users", roles: ["ADMIN"] },
      { id: "admin-subs", labelKey: "adminSubs.nav", route: "/admin/subscriptions", roles: ["ADMIN"] },
      { id: "admin-progress", labelKey: "adminProgress.nav", route: "/admin/progress", roles: ["ADMIN", "DEVELOP"] },
      { id: "admin-theme", labelKey: "theme.nav", route: "/admin/theme", roles: ["ADMIN", "DEVELOP"] },
      { id: "admin-customers", labelKey: "dashboard.title", route: "/customers", roles: ["ADMIN"] }
    ]
  }
];

const STORAGE_PACKAGE_KEY = "layla_active_package";

/** Demo state — thay API sau */
export const DEMO_DEFAULTS = {
  activePackage: "PRO_15",
  packageStart: "2024-05-01",
  packageEnd: "2024-08-01",
  completedPosts: 13,
  completedImages: 10,
  completedVideos: 4,
  serviceProgress: {
    fanpage: 100,
    content: 70,
    ads: 60,
    report: 100,
    posts: 80,
    design: 65,
    cover: 50,
    like: 40
  },
  dailyTasks: {
    "2024-06-10": [{ titleKey: "home.taskPost", time: "09:00" }],
    "2024-06-11": [{ titleKey: "home.taskDesign", time: "14:00" }],
    "2024-06-12": [
      { titleKey: "home.taskPost", time: "09:00" },
      { titleKey: "home.taskReview", time: "15:30" }
    ],
    "2024-06-13": [{ titleKey: "home.taskAds", time: "10:00" }],
    "2024-06-14": [{ titleKey: "home.taskReport", time: "16:00" }]
  }
};

export function loadStoredPackage() {
  const saved = localStorage.getItem(STORAGE_PACKAGE_KEY);
  if (saved && PACKAGE_CODES.includes(saved)) {
    return saved;
  }
  return DEMO_DEFAULTS.activePackage;
}

export function saveStoredPackage(code) {
  localStorage.setItem(STORAGE_PACKAGE_KEY, code);
}

export function getPackageTier(code) {
  return PACKAGES[code]?.tier ?? "BASIC";
}

export function getServiceIdsForPackage(code) {
  const tier = getPackageTier(code);
  return PACKAGE_SERVICE_IDS[tier] ?? PACKAGE_SERVICE_IDS.BASIC;
}

export function getServicesForPackage(code, progressMap = {}) {
  return getServiceIdsForPackage(code).map((id, index) => {
    const def = SERVICE_CATALOG[id];
    const percent = progressMap[id] ?? 0;
    let status = "progress";
    if (percent >= 100) status = "done";
    else if (percent === 0) status = "pending";
    return {
      id,
      index: index + 1,
      icon: def?.icon ?? "doc",
      nameKey: def?.nameKey ?? id,
      descKey: def?.descKey,
      percent,
      status
    };
  });
}

export function getNotificationsForTier(tier) {
  return NOTIFICATION_ITEMS.filter((n) => n.tiers.includes(tier));
}

export function getImplementationTasksForTier(tier) {
  return IMPLEMENTATION_TASKS.filter((task) => task.tiers.includes(tier));
}

export function getActivePackageTitleKey(tier) {
  return tier === "PRO" ? "services.activeTitle.pro" : "services.activeTitle.basic";
}

export function getImplementationTaskById(taskId) {
  return IMPLEMENTATION_TASKS.find((task) => task.id === taskId);
}

export function getAccountMenusForRole(userRole) {
  const role = userRole || "USER";
  return ACCOUNT_MENUS.filter((item) => {
    if (!item.roles) return true;
    return item.roles.includes(role);
  }).map((item) => {
    if (item.type !== "group" || !item.items) return item;
    return {
      ...item,
      items: item.items.filter((sub) => !sub.roles || sub.roles.includes(role))
    };
  });
}
