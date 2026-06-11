/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type ContentReviewDto = {
    id?: string;
    qualityScore?: number;
    reviewType?: ContentReviewDto.reviewType;
    comments?: string;
    suggestions?: string;
    status?: ContentReviewDto.status;
    submittedAt?: string;
};
export namespace ContentReviewDto {
    export enum reviewType {
        CONTENT = 'CONTENT',
        DESIGN_VIDEO = 'DESIGN_VIDEO',
    }
    export enum status {
        DRAFT = 'DRAFT',
        SUBMITTED = 'SUBMITTED',
    }
}

