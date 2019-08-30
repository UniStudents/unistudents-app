import { Injectable } from '@angular/core';
import {Storage} from '@ionic/storage';
import {Student} from '../models/student.model';
import {Grades} from '../models/grades.model';
import {Info} from '../models/info.model';

@Injectable({
  providedIn: 'root'
})
export class StorageService {

  private STUDENT_KEY = 'student';
  private student: Student;

  constructor(
      private storage: Storage
  ) { }

  saveStudent(student: Student) {
    return this.storage.set(this.STUDENT_KEY, student);
  }

  saveGrades(grades: Grades) {
    this.storage.get(this.STUDENT_KEY).then((student) => {
      student.grades = grades;
      return this.storage.set(this.STUDENT_KEY, student);
    });
  }

  saveInfo(info: Info) {
    this.storage.get(this.STUDENT_KEY).then((student) => {
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
}
