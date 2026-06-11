/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type PackageCatalogItem = {
    code: string;
    tier: PackageCatalogItem.tier;
    label: string;
    quotaPosts: number;
    quotaImages: number;
    quotaVideos: number;
};
export namespace PackageCatalogItem {
    export enum tier {
        BASIC = 'BASIC',
        PRO = 'PRO',
    }
}

