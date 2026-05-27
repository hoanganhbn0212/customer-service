/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type LoginResponse = {
    accessToken: string;
    tokenType: string;
    expiresIn: number;
    userName: string;
    role: LoginResponse.role;
};
export namespace LoginResponse {
    export enum role {
        ADMIN = 'ADMIN',
        DEVELOP = 'DEVELOP',
        USER = 'USER',
    }
}

