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
/** Quản lý user — yêu cầu role ADMIN. */
export class AdminService {
    /**
     * GET /api/v1/admin/users — Danh sách toàn bộ user.
     * @usedBy views/UsersAdminView.vue, views/SubscriptionsAdminView.vue
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
     * POST /api/v1/admin/users — Tạo user (userName, password, role, enabled).
     * @usedBy views/UsersAdminView.vue
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
     * PUT /api/v1/admin/users/{id} — Sửa role, trạng thái ACTIVE/INACTIVE, đổi mật khẩu.
     * @usedBy views/UsersAdminView.vue
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
