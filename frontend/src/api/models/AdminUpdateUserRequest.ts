/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type AdminUpdateUserRequest = {
    role?: AdminUpdateUserRequest.role;
    enabled?: AdminUpdateUserRequest.enabled;
    password?: string;
};
export namespace AdminUpdateUserRequest {
    export enum role {
        ADMIN = 'ADMIN',
        DEVELOP = 'DEVELOP',
        USER = 'USER',
    }
    export enum enabled {
        ACTIVE = 'ACTIVE',
        INACTIVE = 'INACTIVE',
    }
}

