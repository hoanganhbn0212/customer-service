/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ContentReviewDto } from '../models/ContentReviewDto';
import type { DeliverableReviewResponse } from '../models/DeliverableReviewResponse';
import type { MobileAccountResponse } from '../models/MobileAccountResponse';
import type { MobileHomeResponse } from '../models/MobileHomeResponse';
import type { MobileServicesResponse } from '../models/MobileServicesResponse';
import type { NotificationListResponse } from '../models/NotificationListResponse';
import type { PackageCatalogItem } from '../models/PackageCatalogItem';
import type { PackageUpgradeRequest } from '../models/PackageUpgradeRequest';
import type { PackageUpgradeResponse } from '../models/PackageUpgradeResponse';
import type { SaveReviewDraftRequest } from '../models/SaveReviewDraftRequest';
import type { SubmitReviewRequest } from '../models/SubmitReviewRequest';
import type { UnreadCountResponse } from '../models/UnreadCountResponse';
import type { UpdateAccountRequest } from '../models/UpdateAccountRequest';
import type { UserVoucherDto } from '../models/UserVoucherDto';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class MobileService {
    /**
     * Mobile home overview
     * @param selectedDate
     * @returns MobileHomeResponse OK
     * @throws ApiError
     */
    public static getMobileHome(
        selectedDate?: string,
    ): CancelablePromise<MobileHomeResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/mobile/home',
            query: {
                'selectedDate': selectedDate,
            },
            errors: {
                401: `Unauthorized`,
            },
        });
    }
    /**
     * Mobile services screen
     * @param category
     * @returns MobileServicesResponse OK
     * @throws ApiError
     */
    public static getMobileServices(
        category: 'all' | 'content' | 'ads' | 'report' = 'all',
    ): CancelablePromise<MobileServicesResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/mobile/services',
            query: {
                'category': category,
            },
            errors: {
                401: `Unauthorized`,
            },
        });
    }
    /**
     * @param deliverableId
     * @returns DeliverableReviewResponse OK
     * @throws ApiError
     */
    public static getDeliverableReview(
        deliverableId: string,
    ): CancelablePromise<DeliverableReviewResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/mobile/deliverables/{deliverableId}/review',
            path: {
                'deliverableId': deliverableId,
            },
            errors: {
                401: `Unauthorized`,
                404: `Not found`,
            },
        });
    }
    /**
     * @param deliverableId
     * @param requestBody
     * @returns ContentReviewDto Saved
     * @throws ApiError
     */
    public static saveReviewDraft(
        deliverableId: string,
        requestBody: SaveReviewDraftRequest,
    ): CancelablePromise<ContentReviewDto> {
        return __request(OpenAPI, {
            method: 'PUT',
            url: '/api/v1/mobile/deliverables/{deliverableId}/reviews/draft',
            path: {
                'deliverableId': deliverableId,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                401: `Unauthorized`,
            },
        });
    }
    /**
     * @param deliverableId
     * @param requestBody
     * @returns ContentReviewDto Submitted
     * @throws ApiError
     */
    public static submitReview(
        deliverableId: string,
        requestBody: SubmitReviewRequest,
    ): CancelablePromise<ContentReviewDto> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/v1/mobile/deliverables/{deliverableId}/reviews',
            path: {
                'deliverableId': deliverableId,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                401: `Unauthorized`,
            },
        });
    }
    /**
     * @param page
     * @param size
     * @param unreadOnly
     * @returns NotificationListResponse OK
     * @throws ApiError
     */
    public static listNotifications(
        page?: number,
        size: number = 20,
        unreadOnly: boolean = false,
    ): CancelablePromise<NotificationListResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/mobile/notifications',
            query: {
                'page': page,
                'size': size,
                'unreadOnly': unreadOnly,
            },
            errors: {
                401: `Unauthorized`,
            },
        });
    }
    /**
     * @returns UnreadCountResponse OK
     * @throws ApiError
     */
    public static getUnreadNotificationCount(): CancelablePromise<UnreadCountResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/mobile/notifications/unread-count',
            errors: {
                401: `Unauthorized`,
            },
        });
    }
    /**
     * @param id
     * @returns void
     * @throws ApiError
     */
    public static markNotificationRead(
        id: string,
    ): CancelablePromise<void> {
        return __request(OpenAPI, {
            method: 'PATCH',
            url: '/api/v1/mobile/notifications/{id}/read',
            path: {
                'id': id,
            },
            errors: {
                401: `Unauthorized`,
            },
        });
    }
    /**
     * @returns MobileAccountResponse OK
     * @throws ApiError
     */
    public static getMobileAccount(): CancelablePromise<MobileAccountResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/mobile/account',
            errors: {
                401: `Unauthorized`,
            },
        });
    }
    /**
     * @param requestBody
     * @returns MobileAccountResponse Updated
     * @throws ApiError
     */
    public static updateMobileAccount(
        requestBody: UpdateAccountRequest,
    ): CancelablePromise<MobileAccountResponse> {
        return __request(OpenAPI, {
            method: 'PATCH',
            url: '/api/v1/mobile/account',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                401: `Unauthorized`,
            },
        });
    }
    /**
     * @returns PackageCatalogItem OK
     * @throws ApiError
     */
    public static listAvailablePackages(): CancelablePromise<Array<PackageCatalogItem>> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/mobile/packages',
            errors: {
                401: `Unauthorized`,
            },
        });
    }
    /**
     * @param requestBody
     * @returns PackageUpgradeResponse Created
     * @throws ApiError
     */
    public static requestPackageUpgrade(
        requestBody: PackageUpgradeRequest,
    ): CancelablePromise<PackageUpgradeResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/v1/mobile/package-upgrade-requests',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                401: `Unauthorized`,
            },
        });
    }
    /**
     * @returns UserVoucherDto OK
     * @throws ApiError
     */
    public static listMyVouchers(): CancelablePromise<Array<UserVoucherDto>> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/mobile/vouchers',
            errors: {
                401: `Unauthorized`,
            },
        });
    }
}
