/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type NotificationDto = {
    id: string;
    type: NotificationDto.type;
    title: string;
    body?: string;
    read: boolean;
    createdAt: string;
    referenceType?: string;
    referenceId?: string;
};
export namespace NotificationDto {
    export enum type {
        FEEDBACK_REPLY = 'FEEDBACK_REPLY',
        PROMOTION = 'PROMOTION',
        SCHEDULE = 'SCHEDULE',
    }
}

