/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { MobileHomeSchedule } from './MobileHomeSchedule';
import type { ProgressSummary } from './ProgressSummary';
import type { ServiceProgressItem } from './ServiceProgressItem';
import type { SubscriptionSummary } from './SubscriptionSummary';
export type MobileHomeResponse = {
    subscription: SubscriptionSummary;
    progress: ProgressSummary;
    services: Array<ServiceProgressItem>;
    schedule: MobileHomeSchedule;
};

