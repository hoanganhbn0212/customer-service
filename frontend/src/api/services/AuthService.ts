/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { LoginRequest } from '../models/LoginRequest';
import type { LoginResponse } from '../models/LoginResponse';
import type { RegisterRequest } from '../models/RegisterRequest';
import type { RegisterResponse } from '../models/RegisterResponse';
import type { UserProfile } from '../models/UserProfile';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
/** Xác thực — token qua `OpenAPI.TOKEN` (main.js). */
export class AuthService {
    /**
     * POST /api/v1/auth/register — Đăng ký tài khoản mới.
     * @usedBy views/LoginView.vue (tab đăng ký)
     */
    public static register(
        requestBody: RegisterRequest,
    ): CancelablePromise<RegisterResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/v1/auth/register',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                409: `Username already exists`,
            },
        });
    }
    /**
     * POST /api/v1/auth/login — Đăng nhập, trả JWT + role (lưu session).
     * @usedBy views/LoginView.vue
     */
    public static login(
        requestBody: LoginRequest,
    ): CancelablePromise<LoginResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/v1/auth/login',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                401: `Invalid credentials`,
            },
        });
    }
    /**
     * GET /api/v1/me — Profile user đang đăng nhập.
     * @usedBy Chưa dùng UI (session lấy từ response login)
     */
    public static getCurrentUser(): CancelablePromise<UserProfile> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/me',
            errors: {
                401: `Unauthorized`,
            },
        });
    }
}
