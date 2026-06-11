/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type PackageServiceInfo = {
    id: string;
    name: string;
    icon?: string;
    description: string;
    trackMode: PackageServiceInfo.trackMode;
    completedCount?: number;
    totalCount?: number;
    percent: number;
    status: PackageServiceInfo.status;
};
export namespace PackageServiceInfo {
    export enum trackMode {
        QUANTITY = 'quantity',
        STATUS = 'status',
    }
    export enum status {
        PENDING = 'pending',
        PROGRESS = 'progress',
        DONE = 'done',
    }
}

