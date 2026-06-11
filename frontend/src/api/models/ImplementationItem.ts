/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type ImplementationItem = {
    id: string;
    code: string;
    title: string;
    category: ImplementationItem.category;
    currentCount: number;
    targetCount: number;
    status: ImplementationItem.status;
    updatedOn: string;
    deliverableId?: string;
    reviewable?: boolean;
    plannedPublishDate?: string;
    topic?: string;
    ideaFrame?: string;
    postContent?: string;
    contentStatus?: ImplementationItem.contentStatus;
    attachmentUrl?: string;
    contentScore?: number;
    customerComment?: string;
    improvementSuggestion?: string;
    completedOn?: string;
    mediaName?: string;
    mediaType?: ImplementationItem.mediaType;
    previewUrl?: string;
    designScore?: number;
    designCustomerComment?: string;
    designImprovementSuggestion?: string;
};
export namespace ImplementationItem {
    export enum category {
        CONTENT = 'content',
        ADS = 'ads',
        REPORT = 'report',
    }
    export enum status {
        APPROVED = 'approved',
        IN_PROGRESS = 'in_progress',
        WAITING_FEEDBACK = 'waiting_feedback',
    }
    export enum contentStatus {
        NOT_STARTED = 'not_started',
        DOING = 'doing',
        WAITING_CUSTOMER = 'waiting_customer',
        NEEDS_REVISION = 'needs_revision',
        COMPLETED = 'completed',
        PUBLISHED = 'published',
    }
    export enum mediaType {
        IMAGE = 'image',
        VIDEO = 'video',
    }
}

