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
export class CustomerService {
    /**
     * List all customers
     * @returns Customer Customer list
     * @throws ApiError
     */
    public static listCustomers(): CancelablePromise<Array<Customer>> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/customers',
        });
    }
    /**
     * Create a customer
     * @param requestBody
     * @returns Customer Created
     * @throws ApiError
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
     * Get customer by id
     * @param id
     * @returns Customer Customer found
     * @throws ApiError
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
     * Update customer
     * @param id
     * @param requestBody
     * @returns Customer Updated
     * @throws ApiError
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
     * Delete customer
     * @param id
     * @returns void
     * @throws ApiError
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
