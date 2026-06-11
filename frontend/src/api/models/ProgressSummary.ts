/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type ProgressSummary = {
    overallPercent: number;
    /**
     * Tổng hạng mục đã hoàn thành (bài + ảnh + video)
     */
    completedItems: number;
    /**
     * Tổng hạng mục cần thực hiện trong gói
     */
    totalItems: number;
    completedPosts: number;
    completedImages: number;
    completedVideos: number;
    quotaPosts: number;
    quotaImages: number;
    quotaVideos: number;
    status: ProgressSummary.status;
};
export namespace ProgressSummary {
    export enum status {
        PENDING = 'pending',
        PROGRESS = 'progress',
        DONE = 'done',
    }
}

