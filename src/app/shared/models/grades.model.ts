import {Semester} from './semester.model';

export class Grades {
    totalPassedCourses: number;
    totalAverageGrade: string;
    totalEcts: number;
    semesters: Semester[];
}
