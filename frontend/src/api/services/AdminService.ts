/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { AdminAssignSubscriptionRequest } from '../models/AdminAssignSubscriptionRequest';
import type { AdminCreateUserRequest } from '../models/AdminCreateUserRequest';
import type { AdminUpdateUserRequest } from '../models/AdminUpdateUserRequest';
import type { PackageCatalogItem } from '../models/PackageCatalogItem';
import type { PackageUpgradeRequestAdminDto } from '../models/PackageUpgradeRequestAdminDto';
import type { ReviewPackageUpgradeRequest } from '../models/ReviewPackageUpgradeRequest';
import type { SubscriptionSummary } from '../models/SubscriptionSummary';
import type { UserAccountResponse } from '../models/UserAccountResponse';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class AdminService {
    /**
     * List all users (admin only)
     * @returns UserAccountResponse User list
     * @throws ApiError
     */
    public static listUsers(): CancelablePromise<Array<UserAccountResponse>> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/admin/users',
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
            },
        });
    }
    /**
     * Create user (admin only)
     * @param requestBody
     * @returns UserAccountResponse Created
     * @throws ApiError
     */
    public static createUser(
        requestBody: AdminCreateUserRequest,
    ): CancelablePromise<UserAccountResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/v1/admin/users',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                409: `Username exists`,
            },
        });
    }
    /**
     * Update user role/status (admin only)
     * @param id
     * @param requestBody
     * @returns UserAccountResponse Updated
     * @throws ApiError
     */
    public static updateUser(
        id: string,
        requestBody: AdminUpdateUserRequest,
    ): CancelablePromise<UserAccountResponse> {
        return __request(OpenAPI, {
            method: 'PUT',
            url: '/api/v1/admin/users/{id}',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
            },
        });
    }
    /**
     * Assign active package to user (admin)
     * @param id
     * @param requestBody
     * @returns SubscriptionSummary Created
     * @throws ApiError
     */
    public static assignUserSubscription(
        id: string,
        requestBody: AdminAssignSubscriptionRequest,
    ): CancelablePromise<SubscriptionSummary> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/v1/admin/users/{id}/subscription',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
            },
        });
    }
    /**
     * List service packages (admin)
     * @returns PackageCatalogItem OK
     * @throws ApiError
     */
    public static listAdminPackages(): CancelablePromise<Array<PackageCatalogItem>> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/admin/packages',
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
            },
        });
    }
    /**
     * List package upgrade requests (admin)
     * @param status
     * @returns PackageUpgradeRequestAdminDto OK
     * @throws ApiError
     */
    public static listPackageUpgradeRequests(
        status?: 'PENDING' | 'APPROVED' | 'REJECTED',
    ): CancelablePromise<Array<PackageUpgradeRequestAdminDto>> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/admin/package-upgrade-requests',
            query: {
                'status': status,
            },
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
            },
        });
    }
    /**
     * Approve or reject upgrade request (admin)
     * @param id
     * @param requestBody
     * @returns PackageUpgradeRequestAdminDto Updated
     * @throws ApiError
     */
    public static reviewPackageUpgradeRequest(
        id: string,
        requestBody: ReviewPackageUpgradeRequest,
    ): CancelablePromise<PackageUpgradeRequestAdminDto> {
        return __request(OpenAPI, {
            method: 'PATCH',
            url: '/api/v1/admin/package-upgrade-requests/{id}',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Bad request`,
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not found`,
            },
        });
    }
}
