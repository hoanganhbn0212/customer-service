/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type UserProfile = {
    id: string;
    userName: string;
    role: UserProfile.role;
    enabled: UserProfile.enabled;
};
export namespace UserProfile {
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

