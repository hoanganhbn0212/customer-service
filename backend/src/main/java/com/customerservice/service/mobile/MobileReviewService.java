package com.customerservice.service.mobile;

import com.customerservice.entity.mobile.ContentReviewEntity;
import com.customerservice.entity.mobile.DeliverableEntity;
import com.customerservice.entity.mobile.ImplementationItemEntity;
import com.customerservice.model.ContentReviewDto;
import com.customerservice.model.DeliverableReviewResponse;
import com.customerservice.model.SaveReviewDraftRequest;
import com.customerservice.model.SubmitReviewRequest;
import com.customerservice.repository.mobile.ContentReviewRepository;
import com.customerservice.repository.mobile.DeliverableRepository;
import com.customerservice.repository.mobile.ImplementationItemRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MobileReviewService {

    private final MobileSubscriptionAccess subscriptionAccess;
    private final DeliverableRepository deliverableRepository;
    private final ImplementationItemRepository implementationItemRepository;
    private final ContentReviewRepository contentReviewRepository;

    public MobileReviewService(
            MobileSubscriptionAccess subscriptionAccess,
            DeliverableRepository deliverableRepository,
            ImplementationItemRepository implementationItemRepository,
            ContentReviewRepository contentReviewRepository
    ) {
        this.subscriptionAccess = subscriptionAccess;
        this.deliverableRepository = deliverableRepository;
        this.implementationItemRepository = implementationItemRepository;
        this.contentReviewRepository = contentReviewRepository;
    }

    @Transactional(readOnly = true)
    public DeliverableReviewResponse getReview(UUID deliverableId) {
        DeliverableEntity deliverable = requireOwnedDeliverable(deliverableId);
        String taskTitle = resolveTaskTitle(deliverable);
        String userId = subscriptionAccess.currentUserId();

        ContentReviewEntity review = contentReviewRepository
                .findFirstByDeliverableIdAndUserIdOrderByUpdatedAtDesc(deliverableId, userId)
                .orElse(null);

        return MobileDtoMapper.toDeliverableReviewResponse(deliverable, taskTitle, review);
    }

    @Transactional
    public ContentReviewDto saveDraft(UUID deliverableId, SaveReviewDraftRequest request) {
        DeliverableEntity deliverable = requireOwnedDeliverable(deliverableId);
        String userId = subscriptionAccess.currentUserId();

        ContentReviewEntity review = contentReviewRepository
                .findByDeliverableIdAndUserIdAndStatus(deliverableId, userId, "DRAFT")
                .orElseGet(() -> newReview(deliverableId, userId));

        if ("SUBMITTED".equals(review.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "REVIEW_ALREADY_SUBMITTED");
        }

        applyRequest(review, request.getQualityScore(), request.getComments(), request.getSuggestions());
        review.setStatus("DRAFT");
        return MobileDtoMapper.toContentReviewDto(contentReviewRepository.save(review));
    }

    @Transactional
    public ContentReviewDto submitReview(UUID deliverableId, SubmitReviewRequest request) {
        requireOwnedDeliverable(deliverableId);
        String userId = subscriptionAccess.currentUserId();

        contentReviewRepository
                .findByDeliverableIdAndUserIdAndStatus(deliverableId, userId, "SUBMITTED")
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "REVIEW_ALREADY_SUBMITTED");
                });

        ContentReviewEntity review = contentReviewRepository
                .findByDeliverableIdAndUserIdAndStatus(deliverableId, userId, "DRAFT")
                .orElseGet(() -> newReview(deliverableId, userId));

        applyRequest(review, request.getQualityScore(), request.getComments(), request.getSuggestions());
        review.setStatus("SUBMITTED");
        review.setSubmittedAt(LocalDateTime.now());

        contentReviewRepository.findByDeliverableIdAndUserIdAndStatus(deliverableId, userId, "DRAFT")
                .filter(draft -> !draft.getId().equals(review.getId()))
                .ifPresent(contentReviewRepository::delete);

        return MobileDtoMapper.toContentReviewDto(contentReviewRepository.save(review));
    }

    private DeliverableEntity requireOwnedDeliverable(UUID deliverableId) {
        var sub = subscriptionAccess.requireActiveSubscription();
        DeliverableEntity deliverable = deliverableRepository.findById(deliverableId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DELIVERABLE_NOT_FOUND"));
        if (!sub.getId().equals(deliverable.getSubscriptionId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "FORBIDDEN");
        }
        return deliverable;
    }

    private String resolveTaskTitle(DeliverableEntity deliverable) {
        if (deliverable.getImplementationItemId() == null) {
            return "";
        }
        return implementationItemRepository.findById(deliverable.getImplementationItemId())
                .map(ImplementationItemEntity::getTitle)
                .orElse("");
    }

    private ContentReviewEntity newReview(UUID deliverableId, String userId) {
        ContentReviewEntity review = new ContentReviewEntity();
        review.setDeliverableId(deliverableId);
        review.setUserId(userId);
        return review;
    }

    private void applyRequest(ContentReviewEntity review, Integer score, String comments, String suggestions) {
        review.setQualityScore(score);
        review.setComments(comments);
        review.setSuggestions(suggestions);
    }
}
