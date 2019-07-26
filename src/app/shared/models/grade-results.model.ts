import {Semester} from './semester.model';

export class GradeResults {
    totalPassedCourses: number;
    totalAverageGrade: string;
    totalEcts: number;
    semesters: Semester[];
}
