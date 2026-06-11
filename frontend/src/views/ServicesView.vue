<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import MobileAppShell from "../components/layout/MobileAppShell.vue";
import MobileScreenHeader from "../components/layout/MobileScreenHeader.vue";
import { useAppScreen } from "../composables/useAppScreen";
import { SERVICE_FILTER_IDS } from "../config/appScreenConfig";
import { submitReview } from "../api/mobileApi";

const router = useRouter();
const { t } = useI18n();

const {
  screen,
  pkg,
  tier,
  subscriptionTitle,
  activePackageTitleKey,
  packageStart,
  packageEnd,
  services,
  implementationTasks,
  refreshServices
} = useAppScreen("services");

const activeTab = ref("packageInfo");
const activeFilter = ref("all");
const activeDetailTitle = ref("");
const reviewTarget = ref(null);
const reviewForm = ref({
  score: 9,
  comments: "",
  suggestions: ""
});
const reviewHint = ref("");
const reviewSubmitting = ref(false);
const reviewScores = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];

onMounted(() => {
  refreshServices(activeFilter.value);
});

watch(activeFilter, (category) => {
  refreshServices(category);
});

const packageTitle = computed(
  () => subscriptionTitle.value || pkg.value.label || (activePackageTitleKey.value ? t(activePackageTitleKey.value) : t(pkg.value.labelKey))
);

const taskTitle = (task) => task.title || t(task.titleKey);
const serviceName = (item) => item.name || t(item.nameKey);
const serviceDesc = (item) => item.desc || (item.descKey ? t(item.descKey) : "");
const serviceTrackMode = (item) => String(item.trackMode || "").toLowerCase();
const isQuantityService = (item) => serviceTrackMode(item) === "quantity";

const formatDate = (iso) => {
  if (!iso) return "—";
  const [y, m, d] = iso.split("-");
  return `${d}/${m}/${y}`;
};

const formatDisplayDate = (iso) => {
  if (!iso) return "—";
  const [y, m, d] = iso.split("-");
  return `${d}/${m}/${y}`;
};

const statusLabel = (status) => {
  if (status === "done") return t("home.statusDone");
  if (status === "progress") return t("home.statusProgress");
  if (status === "pending") return t("home.statusPending");
  if (status === "approved") return t("services.status.approved");
  if (status === "waiting_feedback") return t("services.status.waiting");
  return t("services.status.inProgress");
};

const serviceProgressLabel = (item) => {
  if (isQuantityService(item)) {
    return `${item.completedCount ?? 0}/${item.totalCount ?? 0}`;
  }
  return `${item.percent ?? 0}%`;
};

const serviceIcon = (item) => item.icon || "doc";

const filteredTasks = computed(() => implementationTasks.value);
const contentRows = computed(() => implementationTasks.value.filter((task) => task.category === "content"));
const mediaRows = computed(() =>
  implementationTasks.value.filter((task) => task.mediaName || task.previewUrl || task.designScore != null)
);

const sumQuantity = (rows, id, name, icon) => {
  const completedCount = rows.reduce((sum, item) => sum + (item.completedCount ?? 0), 0);
  const totalCount = rows.reduce((sum, item) => sum + (item.totalCount ?? 0), 0);
  const percent = totalCount > 0 ? Math.min(100, Math.round((completedCount / totalCount) * 100)) : 0;
  return {
    id,
    name,
    icon,
    desc: "",
    trackMode: "quantity",
    completedCount,
    totalCount,
    percent,
    status: percent >= 100 ? "done" : percent > 0 ? "progress" : "pending"
  };
};

const statusService = (item) => ({
  ...item,
  trackMode: item.trackMode || "status",
  percent: item.percent ?? 0,
  status: item.status || "pending"
});

const displayServices = computed(() => {
  if (tier.value !== "PRO") {
    return services.value;
  }

  const byId = new Map(services.value.map((item) => [item.id, item]));
  const contentRows = [byId.get("posts")].filter(Boolean);
  const mediaRows = [byId.get("design"), byId.get("video")].filter(Boolean);
  const rows = [];

  if (contentRows.length) {
    rows.push(sumQuantity(contentRows, "content", t("services.proList.content"), "edit"));
  }
  if (mediaRows.length) {
    rows.push(sumQuantity(mediaRows, "media", t("services.proList.media"), "video"));
  }

  ["fanpage", "report", "ads", "cover", "like"].forEach((id) => {
    const item = byId.get(id);
    if (item) rows.push(statusService(item));
  });

  return rows;
});

const visibleFilters = computed(() => {
  if (tier.value === "BASIC") {
    return SERVICE_FILTER_IDS.filter((id) => id === "all" || id === "content");
  }
  return SERVICE_FILTER_IDS;
});

const taskPercent = (task) => {
  if (!task.total) return 0;
  return Math.min(100, Math.round((task.current / task.total) * 100));
};

const onTaskClick = (task) => {
  if (task.reviewable && task.deliverableId) {
    router.push({ name: "service-review", params: { deliverableId: task.deliverableId } });
    return;
  }
  if (task.category === "content") {
    window.alert(t("services.contentDetailHint"));
  }
};

const serviceFilterFor = (item) => {
  if (["media", "design", "video"].includes(item.id)) return "media";
  const contentIds = ["posts", "content", "cover", "fanpage", "like"];
  if (item.id === "ads") return "ads";
  if (item.id === "report") return "report";
  if (contentIds.includes(item.id)) return "content";
  return "all";
};

const onServiceDetail = (item) => {
  const filter = serviceFilterFor(item);
  activeDetailTitle.value = serviceName(item);
  activeFilter.value = filter;
  if (filter === "content") {
    activeTab.value = "contentSheet";
    return;
  }
  if (filter === "media") {
    activeTab.value = "mediaSheet";
    return;
  }
  activeTab.value = "implementation";
};

const goNotifications = () => router.push("/notifications");

const contentStatusLabel = (status) => {
  const key = status || "not_started";
  return t(`services.contentStatus.${key}`);
};

const contentScoreLabel = (score) => {
  if (score == null) return "—";
  return `${Number(score).toLocaleString("vi-VN", { maximumFractionDigits: 1 })}/10`;
};

const emptyCell = (value) => value || "—";

const mediaTypeLabel = (type) => {
  if (type === "video") return t("services.mediaType.video");
  return t("services.mediaType.image");
};

const openReviewForm = (row, reviewType) => {
  if (!row.deliverableId) return;
  reviewTarget.value = {
    row,
    reviewType,
    title: reviewType === "DESIGN_VIDEO"
      ? row.mediaName || taskTitle(row)
      : row.topic || taskTitle(row)
  };
  reviewForm.value = {
    score: reviewType === "DESIGN_VIDEO" ? Math.round(row.designScore || 9) : Math.round(row.contentScore || 9),
    comments: reviewType === "DESIGN_VIDEO" ? row.designCustomerComment || "" : row.customerComment || "",
    suggestions: reviewType === "DESIGN_VIDEO" ? row.designImprovementSuggestion || "" : row.improvementSuggestion || ""
  };
  reviewHint.value = "";
};

const submitInlineReview = async () => {
  if (!reviewTarget.value?.row?.deliverableId || !reviewForm.value.score) return;
  reviewSubmitting.value = true;
  try {
    const saved = await submitReview(reviewTarget.value.row.deliverableId, {
      reviewType: reviewTarget.value.reviewType,
      qualityScore: Number(reviewForm.value.score),
      comments: reviewForm.value.comments,
      suggestions: reviewForm.value.suggestions
    });
    const row = reviewTarget.value.row;
    if (reviewTarget.value.reviewType === "DESIGN_VIDEO") {
      row.designScore = saved.qualityScore;
      row.designCustomerComment = saved.comments;
      row.designImprovementSuggestion = saved.suggestions;
    } else {
      row.contentScore = saved.qualityScore;
      row.customerComment = saved.comments;
      row.improvementSuggestion = saved.suggestions;
    }
    reviewHint.value = t("review.submitSuccess");
  } catch {
    reviewHint.value = t("home.comingSoon");
  } finally {
    reviewSubmitting.value = false;
  }
};
</script>

<template>
  <MobileAppShell active-tab="services">
    <MobileScreenHeader
      :title="t(screen.titleKey)"
      :show-bell="screen.showBell"
      @bell="goNotifications"
    />

    <div class="mobile-page-body services-page">
      <article class="card package-card active-package-card">
        <div class="package-inner static">
          <span class="crown-wrap" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M4 18h16l-2-9-4 4-3-5-3 5-4-4-2 9zm2.2-2l1.4-6.3 3.2 3.2 3.4-5.7 3.4 5.7 3.2-3.2 1.4 6.3H6.2z"
              />
            </svg>
          </span>
          <span class="package-text">
            <span class="package-row">
              <strong>{{ packageTitle }}</strong>
              <span class="tier-pill">{{ t(pkg.tierKey) }}</span>
            </span>
            <span class="package-date">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="4" width="18" height="18" rx="2" />
                <path d="M16 2v4M8 2v4M3 10h18" />
              </svg>
              {{ formatDate(packageStart) }} – {{ formatDate(packageEnd) }}
            </span>
          </span>
        </div>
      </article>

      <div class="screen-tabs" role="tablist">
        <button
          type="button"
          role="tab"
          class="screen-tab"
          :class="{ active: activeTab === 'packageInfo' }"
          :aria-selected="activeTab === 'packageInfo'"
          @click="activeTab = 'packageInfo'"
        >
          {{ t("services.tab.packageInfo") }}
        </button>
        <button
          type="button"
          role="tab"
          class="screen-tab"
          :class="{ active: activeTab === 'implementation' }"
          :aria-selected="activeTab === 'implementation'"
          @click="activeTab = 'implementation'"
        >
          {{ t("services.tab.implementation") }}
        </button>
      </div>

      <template v-if="activeTab === 'contentSheet'">
        <section class="card content-sheet-card">
          <div class="sheet-head">
            <div>
              <p class="sheet-eyebrow">{{ t("services.contentSheet.eyebrow") }}</p>
              <h2 class="section-title">{{ activeDetailTitle || t("services.proList.content") }}</h2>
            </div>
            <button type="button" class="detail-btn ghost" @click="activeTab = 'packageInfo'">
              {{ t("services.contentSheet.back") }}
            </button>
          </div>

          <div v-if="contentRows.length" class="sheet-scroll" role="region" :aria-label="t('services.contentSheet.title')">
            <table class="content-sheet">
              <thead>
                <tr>
                  <th>{{ t("services.contentSheet.no") }}</th>
                  <th>{{ t("services.contentSheet.publishDate") }}</th>
                  <th>{{ t("services.contentSheet.topic") }}</th>
                  <th>{{ t("services.contentSheet.ideaFrame") }}</th>
                  <th>{{ t("services.contentSheet.postContent") }}</th>
                  <th>{{ t("services.contentSheet.status") }}</th>
                  <th>{{ t("services.contentSheet.attachment") }}</th>
                  <th>{{ t("services.contentSheet.contentScore") }}</th>
                  <th>{{ t("services.contentSheet.customerComment") }}</th>
                  <th>{{ t("services.contentSheet.improvement") }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, index) in contentRows" :key="row.id" @click="onTaskClick(row)">
                  <td class="sheet-index">{{ index + 1 }}</td>
                  <td>{{ formatDisplayDate(row.plannedPublishDate || row.date) }}</td>
                  <td>{{ emptyCell(row.topic || taskTitle(row)) }}</td>
                  <td class="wide-cell">{{ emptyCell(row.ideaFrame) }}</td>
                  <td class="wide-cell">{{ emptyCell(row.postContent) }}</td>
                  <td>
                    <span class="content-status" :class="row.contentStatus || 'not_started'">
                      {{ contentStatusLabel(row.contentStatus) }}
                    </span>
                  </td>
                  <td>
                    <a
                      v-if="row.attachmentUrl"
                      :href="row.attachmentUrl"
                      target="_blank"
                      rel="noreferrer"
                      class="sheet-link"
                      @click.stop
                    >
                      {{ t("services.contentSheet.openFile") }}
                    </a>
                    <span v-else>—</span>
                  </td>
                  <td>{{ contentScoreLabel(row.contentScore) }}</td>
                  <td class="wide-cell">{{ emptyCell(row.customerComment) }}</td>
                  <td class="wide-cell">{{ emptyCell(row.improvementSuggestion) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <p v-else class="hint-box">{{ t("services.emptyTasks") }}</p>
          <div v-if="contentRows.length" class="review-actions-row">
            <button
              v-for="row in contentRows"
              :key="`content-review-${row.id}`"
              type="button"
              class="detail-btn"
              :disabled="!row.deliverableId"
              @click="openReviewForm(row, 'CONTENT')"
            >
              {{ t("review.openForm") }}: {{ row.topic || taskTitle(row) }}
            </button>
          </div>
        </section>
      </template>

      <template v-else-if="activeTab === 'mediaSheet'">
        <section class="card content-sheet-card">
          <div class="sheet-head">
            <div>
              <p class="sheet-eyebrow">{{ t("services.mediaSheet.eyebrow") }}</p>
              <h2 class="section-title">{{ activeDetailTitle || t("services.proList.media") }}</h2>
            </div>
            <button type="button" class="detail-btn ghost" @click="activeTab = 'packageInfo'">
              {{ t("services.contentSheet.back") }}
            </button>
          </div>

          <div v-if="mediaRows.length" class="sheet-scroll" role="region" :aria-label="t('services.mediaSheet.title')">
            <table class="content-sheet media-sheet">
              <thead>
                <tr>
                  <th>{{ t("services.contentSheet.no") }}</th>
                  <th>{{ t("services.mediaSheet.completedDate") }}</th>
                  <th>{{ t("services.mediaSheet.name") }}</th>
                  <th>{{ t("services.mediaSheet.type") }}</th>
                  <th>{{ t("services.mediaSheet.preview") }}</th>
                  <th>{{ t("services.contentSheet.status") }}</th>
                  <th>{{ t("services.mediaSheet.designScore") }}</th>
                  <th>{{ t("services.contentSheet.customerComment") }}</th>
                  <th>{{ t("services.contentSheet.improvement") }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, index) in mediaRows" :key="row.id" @click="onTaskClick(row)">
                  <td class="sheet-index">{{ index + 1 }}</td>
                  <td>{{ formatDisplayDate(row.completedOn || row.date) }}</td>
                  <td>{{ emptyCell(row.mediaName || taskTitle(row)) }}</td>
                  <td>{{ mediaTypeLabel(row.mediaType) }}</td>
                  <td class="preview-cell">
                    <template v-if="row.previewUrl">
                      <video
                        v-if="row.mediaType === 'video'"
                        :src="row.previewUrl"
                        controls
                        class="media-preview"
                        @click.stop
                      />
                      <img v-else :src="row.previewUrl" :alt="row.mediaName || taskTitle(row)" class="media-preview" />
                      <a :href="row.previewUrl" target="_blank" rel="noreferrer" class="sheet-link" @click.stop>
                        {{ t("services.mediaSheet.openPreview") }}
                      </a>
                    </template>
                    <span v-else>—</span>
                  </td>
                  <td>
                    <span class="content-status" :class="row.contentStatus || 'not_started'">
                      {{ contentStatusLabel(row.contentStatus) }}
                    </span>
                  </td>
                  <td>{{ contentScoreLabel(row.designScore) }}</td>
                  <td class="wide-cell">{{ emptyCell(row.designCustomerComment) }}</td>
                  <td class="wide-cell">{{ emptyCell(row.designImprovementSuggestion) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <p v-else class="hint-box">{{ t("services.emptyTasks") }}</p>
          <div v-if="mediaRows.length" class="review-actions-row">
            <button
              v-for="row in mediaRows"
              :key="`media-review-${row.id}`"
              type="button"
              class="detail-btn"
              :disabled="!row.deliverableId"
              @click="openReviewForm(row, 'DESIGN_VIDEO')"
            >
              {{ t("review.openForm") }}: {{ row.mediaName || taskTitle(row) }}
            </button>
          </div>
        </section>
      </template>

      <template v-else-if="activeTab === 'implementation'">
        <div class="filter-chips" role="group" :aria-label="t('services.filterLabel')">
          <button
            v-for="filterId in visibleFilters"
            :key="filterId"
            type="button"
            class="chip"
            :class="{ active: activeFilter === filterId }"
            @click="activeFilter = filterId"
          >
            {{ t(`services.filter.${filterId}`) }}
          </button>
        </div>

        <p v-if="!filteredTasks.length" class="hint-box">{{ t("services.emptyTasks") }}</p>

        <article
          v-for="task in filteredTasks"
          :key="task.id"
          class="card impl-card"
          role="button"
          tabindex="0"
          @click="onTaskClick(task)"
          @keydown.enter="onTaskClick(task)"
        >
          <div class="impl-head">
            <strong>{{ taskTitle(task) }}</strong>
            <span class="status-pill" :class="task.status">{{ statusLabel(task.status) }}</span>
          </div>
          <div class="impl-progress-row">
            <span class="impl-count">{{ task.current }}/{{ task.total }}</span>
            <div class="bar-track compact">
              <div class="bar-fill" :style="{ width: `${taskPercent(task)}%` }" />
            </div>
          </div>
          <p class="impl-date">{{ formatDisplayDate(task.date) }}</p>
        </article>
      </template>

      <template v-else>
        <p class="package-info-intro">{{ t("services.packageInfoIntro") }}</p>
        <article
          v-for="item in displayServices"
          :key="item.id"
          class="card info-card service-card clickable"
          role="button"
          tabindex="0"
          @click="onServiceDetail(item)"
          @keydown.enter="onServiceDetail(item)"
        >
          <span class="svc-icon service-card-icon" :data-icon="serviceIcon(item)" />
          <div class="service-card-body">
            <div class="service-card-head">
              <strong>{{ serviceName(item) }}</strong>
              <span class="status-pill mini" :class="item.status">{{ statusLabel(item.status) }}</span>
            </div>
            <p v-if="serviceDesc(item)">{{ serviceDesc(item) }}</p>
            <div class="service-progress-line">
              <span>{{ t("services.progress") }}: {{ serviceProgressLabel(item) }}</span>
              <span>{{ item.percent ?? 0 }}%</span>
            </div>
            <div class="bar-track compact">
              <div class="bar-fill" :style="{ width: `${item.percent ?? 0}%` }" />
            </div>
            <button type="button" class="detail-btn" @click.stop="onServiceDetail(item)">
              {{ t("services.viewDetail") }}
            </button>
          </div>
        </article>
        <p v-if="!displayServices.length" class="hint-box">{{ t("services.empty") }}</p>
      </template>

      <section v-if="reviewTarget" class="card inline-review-card">
        <div class="sheet-head">
          <div>
            <p class="sheet-eyebrow">
              {{ reviewTarget.reviewType === "DESIGN_VIDEO" ? t("review.designVideoTitle") : t("review.contentTitle") }}
            </p>
            <h2 class="section-title">{{ reviewTarget.title }}</h2>
          </div>
          <button type="button" class="detail-btn ghost" @click="reviewTarget = null">
            {{ t("review.close") }}
          </button>
        </div>

        <label class="review-label" for="inline-review-score">{{ t("review.scoreDropdown") }}</label>
        <select id="inline-review-score" v-model="reviewForm.score" class="review-select">
          <option v-for="score in reviewScores" :key="score" :value="score">{{ score }}/10</option>
        </select>

        <label class="review-label" for="inline-review-comments">{{ t("review.commentsLabel") }}</label>
        <textarea
          id="inline-review-comments"
          v-model="reviewForm.comments"
          class="review-textarea"
          rows="4"
          :placeholder="t('review.commentsPlaceholder')"
        />

        <label class="review-label" for="inline-review-suggestions">{{ t("review.suggestionsLabel") }}</label>
        <textarea
          id="inline-review-suggestions"
          v-model="reviewForm.suggestions"
          class="review-textarea"
          rows="3"
          :placeholder="t('review.suggestionsPlaceholder')"
        />

        <button type="button" class="submit-review-btn" :disabled="reviewSubmitting" @click="submitInlineReview">
          {{ t("review.submit") }}
        </button>
        <p v-if="reviewHint" class="review-hint">{{ reviewHint }}</p>
      </section>
    </div>
  </MobileAppShell>
</template>

<style scoped>
@import "../styles/mobile-page.css";

.services-page {
  gap: 12px;
}

.active-package-card {
  margin-top: -4px;
}

.package-inner.static {
  cursor: default;
}

.screen-tabs {
  display: flex;
  gap: 20px;
  border-bottom: 2px solid #e8eef5;
  padding: 0 4px 0;
  margin-top: 4px;
}

.screen-tab {
  border: none;
  background: transparent;
  padding: 10px 2px 12px;
  font-size: 0.88rem;
  font-weight: 600;
  color: #94a3b8;
  cursor: pointer;
  position: relative;
  font-family: inherit;
}

.screen-tab.active {
  color: #1a6dff;
}

.screen-tab.active::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: -2px;
  height: 2px;
  background: #1a6dff;
  border-radius: 2px 2px 0 0;
}

.filter-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 4px 0 2px;
}

.chip {
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #64748b;
  font-size: 0.8rem;
  font-weight: 600;
  padding: 8px 14px;
  border-radius: 999px;
  cursor: pointer;
  font-family: inherit;
}

.chip.active {
  background: #1a6dff;
  border-color: #1a6dff;
  color: #fff;
}

.impl-card {
  padding: 16px;
  cursor: pointer;
  transition: box-shadow 0.15s ease;
}

.impl-card:active {
  box-shadow: 0 4px 16px rgba(15, 50, 100, 0.1);
}

.impl-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}

.impl-head strong {
  font-size: 0.92rem;
  color: #0f172a;
  line-height: 1.35;
}

.impl-progress-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.impl-count {
  font-size: 0.82rem;
  font-weight: 700;
  color: #475569;
  min-width: 42px;
}

.bar-track.compact {
  flex: 1;
  margin: 0;
  height: 6px;
}

.impl-date {
  margin: 0;
  font-size: 0.78rem;
  font-weight: 600;
  color: #94a3b8;
}

.status-pill.approved {
  background: #ecfdf3;
  color: #16a34a;
}

.status-pill.in_progress {
  background: #eff6ff;
  color: #1a6dff;
}

.status-pill.waiting_feedback {
  background: #fff7ed;
  color: #ea580c;
}

.package-info-intro {
  margin: 0;
  font-size: 0.85rem;
  color: #64748b;
  line-height: 1.5;
}

.info-card strong {
  display: block;
  font-size: 0.92rem;
  color: #0f172a;
}

.info-card p {
  margin: 0;
  font-size: 0.82rem;
  color: #64748b;
  line-height: 1.45;
}

.service-card {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.service-card.clickable {
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.service-card.clickable:active {
  transform: translateY(1px);
  box-shadow: 0 4px 16px rgba(15, 50, 100, 0.1);
}

.service-card-icon {
  margin-top: 2px;
}

.service-card-body {
  flex: 1;
  min-width: 0;
}

.service-card-head {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: center;
  margin-bottom: 6px;
}

.service-progress-line {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  margin: 10px 0 6px;
  font-size: 0.8rem;
  font-weight: 700;
  color: #1e40af;
}

.detail-btn {
  margin-top: 10px;
  border: 1px solid #bfdbfe;
  background: #eff6ff;
  color: #1d4ed8;
  border-radius: 10px;
  padding: 8px 12px;
  font-size: 0.78rem;
  font-weight: 800;
  font-family: inherit;
  cursor: pointer;
}

.detail-btn.ghost {
  margin-top: 0;
  background: #fff;
  white-space: nowrap;
}

.status-pill.mini {
  font-size: 0.68rem;
  padding: 2px 8px;
}

.content-sheet-card {
  padding: 14px;
}

.sheet-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 12px;
}

.sheet-head .section-title {
  margin: 0;
}

.sheet-eyebrow {
  margin: 0 0 4px;
  font-size: 0.72rem;
  font-weight: 800;
  color: #1d4ed8;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.sheet-scroll {
  overflow-x: auto;
  border: 1px solid #dbeafe;
  border-radius: 14px;
  background: #fff;
  -webkit-overflow-scrolling: touch;
}

.content-sheet {
  width: 1280px;
  border-collapse: separate;
  border-spacing: 0;
  font-size: 0.78rem;
}

.content-sheet.media-sheet {
  width: 1120px;
}

.content-sheet th,
.content-sheet td {
  padding: 10px 12px;
  text-align: left;
  vertical-align: top;
  border-right: 1px solid #e8eef5;
  border-bottom: 1px solid #e8eef5;
}

.content-sheet th {
  position: sticky;
  top: 0;
  z-index: 1;
  background: #eff6ff;
  color: #1e3a8a;
  font-weight: 800;
  white-space: nowrap;
}

.content-sheet tr {
  cursor: pointer;
}

.content-sheet tbody tr:hover {
  background: #f8fbff;
}

.content-sheet th:last-child,
.content-sheet td:last-child {
  border-right: none;
}

.content-sheet tbody tr:last-child td {
  border-bottom: none;
}

.sheet-index {
  font-weight: 800;
  color: #1d4ed8;
  text-align: center !important;
}

.wide-cell {
  min-width: 180px;
  line-height: 1.45;
}

.sheet-link {
  display: inline-block;
  margin-top: 6px;
  color: #1d4ed8;
  font-weight: 800;
  text-decoration: none;
}

.preview-cell {
  min-width: 160px;
}

.media-preview {
  display: block;
  width: 150px;
  height: 90px;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid #dbeafe;
  background: #f8fafc;
}

.review-actions-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
}

.review-actions-row .detail-btn {
  width: 100%;
  text-align: left;
}

.inline-review-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  border-color: #bfdbfe;
  background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
}

.review-label {
  font-size: 0.82rem;
  font-weight: 800;
  color: #0f172a;
}

.review-select,
.review-textarea {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid #dbeafe;
  border-radius: 12px;
  background: #fff;
  color: #0f172a;
  font-family: inherit;
  font-size: 0.86rem;
  padding: 10px 12px;
}

.review-textarea {
  resize: vertical;
  line-height: 1.45;
}

.submit-review-btn {
  margin-top: 4px;
  border: none;
  border-radius: 12px;
  padding: 12px 14px;
  background: linear-gradient(135deg, #1a6dff, #3d8cff);
  color: #fff;
  font-family: inherit;
  font-size: 0.9rem;
  font-weight: 800;
  cursor: pointer;
}

.submit-review-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.review-hint {
  margin: 0;
  font-size: 0.82rem;
  font-weight: 700;
  color: #16a34a;
}

.content-status {
  display: inline-flex;
  padding: 4px 8px;
  border-radius: 999px;
  font-size: 0.7rem;
  font-weight: 800;
  white-space: nowrap;
}

.content-status.not_started {
  background: #f1f5f9;
  color: #64748b;
}

.content-status.doing {
  background: #eff6ff;
  color: #1d4ed8;
}

.content-status.waiting_customer {
  background: #fff7ed;
  color: #ea580c;
}

.content-status.needs_revision {
  background: #fef2f2;
  color: #dc2626;
}

.content-status.completed {
  background: #ecfdf3;
  color: #16a34a;
}

.content-status.published {
  background: #f0fdf4;
  color: #15803d;
}
</style>
