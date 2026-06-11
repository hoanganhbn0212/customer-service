import mockData from "../data/mockAppData.json";

const clone = (value) => JSON.parse(JSON.stringify(value));
const wait = (value) => Promise.resolve(clone(value));

let accountState = clone(mockData.account);
let notificationsState = clone(mockData.notifications);
let reviewState = clone(mockData.reviews);

export function getMobileHome(selectedDate) {
  const home = clone(mockData.home);
  if (selectedDate) {
    home.schedule.tasks = mockData.dailyScheduleItems
      .filter((item) => item.date === selectedDate)
      .map((item, index) => ({
        id: `schedule-${index + 1}`,
        title: item.task,
        task: item.task,
        service: item.service,
        status: item.status,
        note: item.note,
        date: item.date,
        scheduledTime: ""
      }));
  }
  return wait(home);
}

export function getMobileServices(category = "all") {
  const services = clone(mockData.services);
  if (category && category !== "all") {
    services.implementationItems = services.implementationItems.filter((item) => item.category === category);
  }
  return wait(services);
}

export function getDeliverableReview(deliverableId) {
  return wait(reviewState[deliverableId] || reviewState[Object.keys(reviewState)[0]]);
}

export function saveReviewDraft(deliverableId, body) {
  const saved = {
    id: `draft-${deliverableId}`,
    status: "DRAFT",
    qualityScore: body.qualityScore,
    comments: body.comments || "",
    suggestions: body.suggestions || ""
  };
  reviewState[deliverableId] = {
    ...(reviewState[deliverableId] || reviewState[Object.keys(reviewState)[0]]),
    review: saved
  };
  return wait(saved);
}

export function submitReview(deliverableId, body) {
  const saved = {
    id: `review-${deliverableId}`,
    status: "SUBMITTED",
    reviewType: body.reviewType || "CONTENT",
    qualityScore: body.qualityScore,
    comments: body.comments || "",
    suggestions: body.suggestions || ""
  };
  reviewState[deliverableId] = {
    ...(reviewState[deliverableId] || reviewState[Object.keys(reviewState)[0]]),
    review: saved
  };
  return wait(saved);
}

export function listNotifications(page = 0, size = 20, unreadOnly = false) {
  const rows = notificationsState.filter((item) => !unreadOnly || !item.read);
  const start = page * size;
  return wait({
    items: rows.slice(start, start + size),
    page,
    size,
    total: rows.length
  });
}

export function getUnreadNotificationCount() {
  return wait({ count: notificationsState.filter((item) => !item.read).length });
}

export function markNotificationRead(id) {
  notificationsState = notificationsState.map((item) =>
    item.id === id ? { ...item, read: true } : item
  );
  return Promise.resolve(null);
}

export function getMobileAccount() {
  return wait(accountState);
}

export function updateMobileAccount(body) {
  accountState = { ...accountState, ...body };
  return wait(accountState);
}

export function listAvailablePackages() {
  return wait(mockData.packages);
}

export function requestPackageUpgrade(body) {
  return wait({
    id: `upgrade-${Date.now()}`,
    toPackageCode: body.toPackageCode,
    note: body.note || "",
    status: "PENDING"
  });
}

export function listMyVouchers() {
  return wait(mockData.vouchers);
}
