/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ImplementationItem } from './ImplementationItem';
import type { PackageServiceInfo } from './PackageServiceInfo';
import type { SubscriptionSummary } from './SubscriptionSummary';
export type MobileServicesResponse = {
    activeSubscription: SubscriptionSummary;
    implementationItems: Array<ImplementationItem>;
    packageServices: Array<PackageServiceInfo>;
};

