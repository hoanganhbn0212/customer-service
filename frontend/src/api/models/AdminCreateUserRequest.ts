/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type AdminCreateUserRequest = {
    userName: string;
    password: string;
    role: AdminCreateUserRequest.role;
    enabled: AdminCreateUserRequest.enabled;
};
export namespace AdminCreateUserRequest {
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

