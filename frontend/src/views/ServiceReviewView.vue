<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import MobileAppShell from "../components/layout/MobileAppShell.vue";
import {
  getDeliverableReview,
  saveReviewDraft,
  submitReview
} from "../api/mobileApi";

const route = useRoute();
const router = useRouter();
const { t } = useI18n();

const deliverableId = computed(() => String(route.params.deliverableId ?? ""));
const loading = ref(true);
const reviewData = ref(null);

const qualityScore = ref(9);
const comments = ref("");
const suggestions = ref("");
const submitted = ref(false);
const saveHint = ref("");

const scores = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];

const deliverable = computed(() => reviewData.value?.deliverable);
const existingReview = computed(() => reviewData.value?.review);

const teamContentPercent = computed(() => {
  const score = deliverable.value?.teamContentScore ?? 0;
  return (score / 10) * 100;
});

const teamDesignPercent = computed(() => {
  const score = deliverable.value?.teamDesignScore ?? 0;
  return (score / 10) * 100;
});

const responseLabel = computed(() => {
  if (deliverable.value?.companyResponseStatus === "responded") {
    return t("review.responseStatus.responded");
  }
  return t("review.responseStatus.pending");
});

const applyReviewToForm = (review) => {
  if (!review) return;
  if (review.qualityScore) {
    qualityScore.value = review.qualityScore;
  }
  comments.value = review.comments || "";
  suggestions.value = review.suggestions || "";
  if (review.status === "SUBMITTED") {
    submitted.value = true;
  }
};

const loadReview = async () => {
  loading.value = true;
  try {
    const data = await getDeliverableReview(deliverableId.value);
    reviewData.value = data;
    applyReviewToForm(data.review);
    if (!data.review) {
      qualityScore.value = 9;
      comments.value = "";
      suggestions.value = "";
    }
  } catch {
    router.replace("/services");
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  if (!deliverableId.value) {
    router.replace("/services");
    return;
  }
  loadReview();
});

const goBack = () => router.push("/services");
const goNotifications = () => router.push("/notifications");

const saveDraft = async () => {
  try {
    const saved = await saveReviewDraft(deliverableId.value, {
      qualityScore: qualityScore.value,
      comments: comments.value,
      suggestions: suggestions.value
    });
    applyReviewToForm(saved);
    saveHint.value = t("review.draftSaved");
    window.setTimeout(() => {
      saveHint.value = "";
    }, 2500);
  } catch {
    saveHint.value = t("home.comingSoon");
  }
};

const submitReviewForm = async () => {
  if (!qualityScore.value) return;
  try {
    await submitReview(deliverableId.value, {
      qualityScore: qualityScore.value,
      comments: comments.value,
      suggestions: suggestions.value
    });
    submitted.value = true;
    saveHint.value = t("review.submitSuccess");
  } catch {
    saveHint.value = t("home.comingSoon");
  }
};
</script>

<template>
  <MobileAppShell v-if="!loading && deliverable" active-tab="services">
    <header class="review-top">
      <button type="button" class="icon-btn" :aria-label="t('review.back')" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M15 18l-6-6 6-6" />
        </svg>
      </button>
      <h1>{{ t("review.title") }}</h1>
      <button type="button" class="icon-btn bell" :aria-label="t('nav.notifications')" @click="goNotifications">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 22a2 2 0 002-2H10a2 2 0 002 2z" />
          <path d="M18 10a6 6 0 10-12 0c0 5-2 6-6 6-6h12s-2 1-6 6z" />
        </svg>
      </button>
    </header>

    <div class="mobile-page-body review-page">
      <article class="card post-card">
        <div class="post-head">
          <div>
            <p class="post-label">{{ t("review.postLabel", { number: deliverable.postNumber }) }}</p>
            <span class="response-pill">{{ responseLabel }}</span>
          </div>
          <img
            v-if="deliverable.thumbnailUrl"
            :src="deliverable.thumbnailUrl"
            alt=""
            class="post-thumb"
            width="56"
            height="56"
          />
        </div>
        <p v-if="deliverable.taskTitle" class="task-ref">{{ deliverable.taskTitle }}</p>
      </article>

      <section class="card team-card">
        <h2 class="block-title">{{ t("review.teamPerformance") }}</h2>
        <div class="team-row">
          <div class="team-meta">
            <span>{{ t("review.teamContent") }}</span>
            <strong>{{ deliverable.teamContentScore }}/10</strong>
          </div>
          <div class="bar-track compact">
            <div class="bar-fill" :style="{ width: `${teamContentPercent}%` }" />
          </div>
        </div>
        <div class="team-row">
          <div class="team-meta">
            <span>{{ t("review.teamDesign") }}</span>
            <strong>{{ deliverable.teamDesignScore }}/10</strong>
          </div>
          <div class="bar-track compact">
            <div class="bar-fill design" :style="{ width: `${teamDesignPercent}%` }" />
          </div>
        </div>
      </section>

      <section class="card score-card">
        <h2 class="block-title">{{ t("review.qualityScore") }}</h2>
        <div class="score-grid" role="group" :aria-label="t('review.qualityScore')">
          <button
            v-for="n in scores"
            :key="n"
            type="button"
            class="score-btn"
            :class="{ active: qualityScore === n }"
            :aria-pressed="qualityScore === n"
            :disabled="submitted"
            @click="qualityScore = n"
          >
            {{ n }}
          </button>
        </div>
      </section>

      <section class="card field-card">
        <label class="field-label" for="review-comments">{{ t("review.commentsLabel") }}</label>
        <textarea
          id="review-comments"
          v-model="comments"
          class="field-textarea"
          rows="5"
          :disabled="submitted"
          :placeholder="t('review.commentsPlaceholder')"
        />
      </section>

      <section class="card field-card">
        <label class="field-label" for="review-suggestions">{{ t("review.suggestionsLabel") }}</label>
        <input
          id="review-suggestions"
          v-model="suggestions"
          type="text"
          class="field-input"
          :disabled="submitted"
          :placeholder="t('review.suggestionsPlaceholder')"
        />
      </section>

      <p v-if="saveHint" class="save-hint" role="status">{{ saveHint }}</p>

      <div class="actions">
        <button type="button" class="btn primary" :disabled="submitted" @click="submitReviewForm">
          {{ t("review.submit") }}
        </button>
        <button type="button" class="btn secondary" :disabled="submitted" @click="saveDraft">
          {{ t("review.saveDraft") }}
        </button>
      </div>
    </div>
  </MobileAppShell>
</template>

<style scoped>
@import "../styles/mobile-page.css";

.review-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: calc(12px + env(safe-area-inset-top, 0)) 12px 12px;
  background: #f3f6fb;
  flex-shrink: 0;
}

.review-top h1 {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  color: #0f172a;
}

.icon-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 12px;
  background: #fff;
  color: #334155;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(15, 40, 80, 0.08);
}

.icon-btn svg {
  width: 22px;
  height: 22px;
}

.review-page {
  padding-top: 4px;
  gap: 12px;
  padding-bottom: 24px;
}

.post-card {
  padding: 16px;
}

.post-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.post-label {
  margin: 0 0 8px;
  font-size: 0.95rem;
  font-weight: 700;
  color: #0f172a;
}

.response-pill {
  display: inline-block;
  font-size: 0.68rem;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 999px;
  background: #ecfdf3;
  color: #16a34a;
}

.post-thumb {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  object-fit: cover;
  flex-shrink: 0;
  border: 1px solid #e8eef5;
}

.task-ref {
  margin: 12px 0 0;
  font-size: 0.8rem;
  color: #64748b;
  font-weight: 500;
}

.block-title {
  margin: 0 0 14px;
  font-size: 0.9rem;
  font-weight: 700;
  color: #0f172a;
}

.team-row + .team-row {
  margin-top: 14px;
}

.team-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 0.82rem;
  color: #475569;
  font-weight: 600;
}

.team-meta strong {
  color: #1a6dff;
  font-size: 0.85rem;
}

.bar-fill.design {
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
}

.score-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
}

.score-btn {
  aspect-ratio: 1;
  border: 2px solid #e2e8f0;
  border-radius: 50%;
  background: #fff;
  font-size: 0.95rem;
  font-weight: 700;
  color: #64748b;
  cursor: pointer;
  font-family: inherit;
}

.score-btn.active {
  border-color: #1a6dff;
  background: #1a6dff;
  color: #fff;
  box-shadow: 0 4px 14px rgba(26, 109, 255, 0.35);
}

.field-label {
  display: block;
  font-size: 0.88rem;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 10px;
}

.field-textarea,
.field-input {
  width: 100%;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 12px 14px;
  font-size: 0.88rem;
  font-family: inherit;
  color: #0f172a;
  background: #f8fafc;
  box-sizing: border-box;
}

.field-textarea {
  resize: vertical;
  min-height: 120px;
  line-height: 1.5;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 4px;
}

.btn {
  width: 100%;
  padding: 14px;
  border-radius: 14px;
  font-size: 0.95rem;
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

.btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.save-hint {
  margin: 0;
  text-align: center;
  font-size: 0.82rem;
  font-weight: 600;
  color: #16a34a;
}
</style>
