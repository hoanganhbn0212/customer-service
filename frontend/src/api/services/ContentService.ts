/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { LoginThemeResponse } from '../models/LoginThemeResponse';
import type { SaveLoginThemeRequest } from '../models/SaveLoginThemeRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class ContentService {
    /**
     * Get login theme backgrounds (header and body)
     * @returns LoginThemeResponse Theme backgrounds
     * @throws ApiError
     */
    public static getLoginTheme(): CancelablePromise<LoginThemeResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/content',
        });
    }
    /**
     * Save login theme backgrounds (admin only)
     * @param requestBody
     * @returns LoginThemeResponse Saved
     * @throws ApiError
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
