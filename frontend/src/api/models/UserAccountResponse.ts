/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type UserAccountResponse = {
    id: string;
    userName: string;
    role: UserAccountResponse.role;
    enabled: UserAccountResponse.enabled;
};
export namespace UserAccountResponse {
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

