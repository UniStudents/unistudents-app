import {Course} from './course.model';

export class Semester {
    id: number;
    passedCourses: number;
    gradeAverage: string;
    ects: number;
    courses: Course[];
}
