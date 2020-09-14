import { Course } from './course.model';

export class Semester {
    id: number;
    passedCourses: number;
    gradeAverage: string;
    ects: string;
    courses: Course[];
}
