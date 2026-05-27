/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { AdminCreateUserRequest } from '../models/AdminCreateUserRequest';
import type { AdminUpdateUserRequest } from '../models/AdminUpdateUserRequest';
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
}
