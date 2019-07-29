import {Semester} from './semester.model';
import {Student} from './student.model';

export class GradeResults {
    student: Student;
    totalPassedCourses: number;
    totalAverageGrade: string;
    totalEcts: number;
    semesters: Semester[];
}
