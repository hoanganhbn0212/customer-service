/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type SubscriptionSummary = {
    id: string;
    packageCode: string;
    tier: SubscriptionSummary.tier;
    displayTitle: string;
    startDate: string;
    endDate: string;
    status: SubscriptionSummary.status;
    deploymentStatus: SubscriptionSummary.deploymentStatus;
};
export namespace SubscriptionSummary {
    export enum tier {
        BASIC = 'BASIC',
        PRO = 'PRO',
    }
    export enum status {
        ACTIVE = 'ACTIVE',
        EXPIRED = 'EXPIRED',
        CANCELLED = 'CANCELLED',
    }
    export enum deploymentStatus {
        IN_PROGRESS = 'IN_PROGRESS',
        COMPLETED = 'COMPLETED',
        PAUSED = 'PAUSED',
    }
}

