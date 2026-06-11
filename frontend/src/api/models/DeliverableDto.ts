/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type DeliverableDto = {
    id: string;
    postNumber: string;
    thumbnailUrl?: string;
    taskTitle?: string;
    companyResponseStatus: DeliverableDto.companyResponseStatus;
    teamContentScore?: number;
    teamDesignScore?: number;
};
export namespace DeliverableDto {
    export enum companyResponseStatus {
        RESPONDED = 'responded',
        PENDING = 'pending',
    }
}

