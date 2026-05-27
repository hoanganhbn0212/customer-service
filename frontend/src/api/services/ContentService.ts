/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { LoginThemeResponse } from '../models/LoginThemeResponse';
import type { SaveLoginThemeRequest } from '../models/SaveLoginThemeRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
/** Nội dung tĩnh — theme màn đăng nhập. */
export class ContentService {
    /**
     * GET /api/v1/content — Lấy URL ảnh nền header/body màn login (public).
     * @usedBy views/LoginView.vue, views/LoginThemeAdminView.vue
     */
    public static getLoginTheme(): CancelablePromise<LoginThemeResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/content',
        });
    }
    /**
     * POST /api/v1/content — Lưu ảnh nền login (admin / DEVELOP có quyền sửa trang).
     * @usedBy views/LoginThemeAdminView.vue
     */
    public static saveLoginTheme(
        requestBody: SaveLoginThemeRequest,
    ): CancelablePromise<LoginThemeResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/v1/content',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
            },
        });
    }
}
