/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CreateCustomerRequest } from '../models/CreateCustomerRequest';
import type { Customer } from '../models/Customer';
import type { UpdateCustomerRequest } from '../models/UpdateCustomerRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
/**
 * CRUD khách hàng mẫu (demo ban đầu) — không thuộc luồng app Layla/mobile.
 * @usedBy views/DashboardView.vue (/customers)
 */
export class CustomerService {
    /**
     * GET /api/customers — Liệt kê khách hàng demo.
     */
    public static listCustomers(): CancelablePromise<Array<Customer>> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/customers',
        });
    }
    /**
     * POST /api/customers — Tạo khách hàng demo (name, email).
     */
    public static createCustomer(
        requestBody: CreateCustomerRequest,
    ): CancelablePromise<Customer> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/customers',
            body: requestBody,
            mediaType: 'application/json',
        });
    }
    /**
     * GET /api/customers/{id} — Chi tiết một khách (chưa gọi từ UI hiện tại).
     */
    public static getCustomerById(
        id: number,
    ): CancelablePromise<Customer> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/customers/{id}',
            path: {
                'id': id,
            },
            errors: {
                404: `Not found`,
            },
        });
    }
    /**
     * PUT /api/customers/{id} — Cập nhật khách (chưa gọi từ UI hiện tại).
     */
    public static updateCustomer(
        id: number,
        requestBody: UpdateCustomerRequest,
    ): CancelablePromise<Customer> {
        return __request(OpenAPI, {
            method: 'PUT',
            url: '/api/customers/{id}',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                404: `Not found`,
            },
        });
    }
    /**
     * DELETE /api/customers/{id} — Xóa khách demo.
     */
    public static deleteCustomer(
        id: number,
    ): CancelablePromise<void> {
        return __request(OpenAPI, {
            method: 'DELETE',
            url: '/api/customers/{id}',
            path: {
                'id': id,
            },
            errors: {
                404: `Not found`,
            },
        });
    }
}
