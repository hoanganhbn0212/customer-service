/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type SubmitReviewRequest = {
    qualityScore: number;
    reviewType?: SubmitReviewRequest.reviewType;
    comments?: string;
    suggestions?: string;
};
export namespace SubmitReviewRequest {
    export enum reviewType {
        CONTENT = 'CONTENT',
        DESIGN_VIDEO = 'DESIGN_VIDEO',
    }
}

