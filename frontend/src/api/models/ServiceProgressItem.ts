/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type ServiceProgressItem = {
    id: string;
    name: string;
    icon?: string;
    /**
     * quantity = hiển thị completed/total; status = chỉ trạng thái
     */
    trackMode: ServiceProgressItem.trackMode;
    /**
     * Chỉ khi trackMode=quantity
     */
    completedCount?: number;
    /**
     * Chỉ khi trackMode=quantity
     */
    totalCount?: number;
    percent: number;
    status: ServiceProgressItem.status;
};
export namespace ServiceProgressItem {
    /**
     * quantity = hiển thị completed/total; status = chỉ trạng thái
     */
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

