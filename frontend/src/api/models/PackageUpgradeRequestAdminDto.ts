/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type PackageUpgradeRequestAdminDto = {
    id: string;
    userId: string;
    userName: string;
    fromPackageCode: string;
    toPackageCode: string;
    status: PackageUpgradeRequestAdminDto.status;
    note?: string;
    adminNote?: string;
    createdAt: string;
    reviewedAt?: string;
};
export namespace PackageUpgradeRequestAdminDto {
    export enum status {
        PENDING = 'PENDING',
        APPROVED = 'APPROVED',
        REJECTED = 'REJECTED',
    }
}

