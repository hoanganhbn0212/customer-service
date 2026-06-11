/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ScheduleDay } from './ScheduleDay';
import type { ScheduleTask } from './ScheduleTask';
export type MobileHomeSchedule = {
    month?: number;
    year?: number;
    weekDays?: Array<ScheduleDay>;
    tasks?: Array<ScheduleTask>;
};

