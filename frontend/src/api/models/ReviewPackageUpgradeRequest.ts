/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type ReviewPackageUpgradeRequest = {
    status: ReviewPackageUpgradeRequest.status;
    adminNote?: string;
};
export namespace ReviewPackageUpgradeRequest {
    export enum status {
        APPROVED = 'APPROVED',
        REJECTED = 'REJECTED',
    }
}

