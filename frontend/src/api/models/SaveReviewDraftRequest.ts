/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type SaveReviewDraftRequest = {
    qualityScore: number;
    reviewType?: SaveReviewDraftRequest.reviewType;
    comments?: string;
    suggestions?: string;
};
export namespace SaveReviewDraftRequest {
    export enum reviewType {
        CONTENT = 'CONTENT',
        DESIGN_VIDEO = 'DESIGN_VIDEO',
    }
}

