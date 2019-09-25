import { Injectable } from '@angular/core';
import {Storage} from '@ionic/storage';
import {Student} from '../models/student.model';
import {Grades} from '../models/grades.model';
import {Info} from '../models/info.model';

@Injectable({
  providedIn: 'root'
})
export class StorageService {

  public newGrades = 0;
  private STUDENT_KEY = 'student';

  constructor(
      private storage: Storage
  ) { }

  saveStudent(student: Student) {
    return this.storage.set(this.STUDENT_KEY, student);
  }

  saveGrades(grades: Grades) {
    this.getStudent().then((student) => {
      student.grades = grades;
      return this.storage.set(this.STUDENT_KEY, student);
    });
  }

  saveInfo(info: Info) {
    this.getStudent().then((student) => {
      student.info = info;
      return this.storage.set(this.STUDENT_KEY, student);
    });
  }

  getStudent() {
    return this.storage.get(this.STUDENT_KEY);
  }

  removeStudent() {
    return this.storage.remove(this.STUDENT_KEY);
  }

  compareGrades(grades: Grades, oldGrades: Grades) {
    let diffs = 0;
    for (let i = 0; i < oldGrades.semesters.length; i++) {
      for (let j = 0; j < oldGrades.semesters[i].courses.length; j++) {
        if (oldGrades.semesters[i].courses[j].examPeriod !== grades.semesters[i].courses[j].examPeriod) {
          diffs++;
        }
      }
    }
    this.newGrades = diffs;
  }
}
