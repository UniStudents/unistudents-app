import { Injectable } from '@angular/core';
import { Storage } from '@ionic/storage';
import { Student } from '../models/student.model';
import { Grades } from '../models/grades.model';

@Injectable({
  providedIn: 'root'
})
export class StorageService {

  public newGrades = 0;
  public newGradesList: Array<string> = [];
  private STUDENT_KEY = 'student';
  private USERNAME_KEY = 'username';
  private PASSWORD_KEY = 'password';
  private THEME_MODE_KEY = 'theme_mode';
  private REMEMBER_ME_KEY = 'remember_me';

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

  getStudent() {
    return this.storage.get(this.STUDENT_KEY);
  }

  removeStudent() {
    return this.storage.remove(this.STUDENT_KEY);
  }

  compareGrades(grades: Grades, oldGrades: Grades) {
    this.newGrades = 0;
    this.newGradesList = [];

    let diffs = 0;
    for (let i = 0; i < oldGrades.semesters.length; i++) {
      for (let j = 0; j < oldGrades.semesters[i].courses.length; j++) {
        if (oldGrades.semesters[i].courses[j].examPeriod !== grades.semesters[i].courses[j].examPeriod) {
          diffs++;
          this.newGradesList.push(grades.semesters[i].courses[j].id);
        }
      }
    }
    this.newGrades = diffs;
  }

  saveUsername(username: string) {
    return this.storage.set(this.USERNAME_KEY, username);
  }

  getUsername() {
    return this.storage.get(this.USERNAME_KEY);
  }

  removeUsername() {
    return this.storage.remove(this.USERNAME_KEY);
  }

  savePassword(password: string) {
    return this.storage.set(this.PASSWORD_KEY, password);
  }

  getPassword() {
    return this.storage.get(this.PASSWORD_KEY);
  }

  removePassword() {
    return this.storage.remove(this.PASSWORD_KEY);
  }

  saveThemeMode(mode: string) {
    return this.storage.set(this.THEME_MODE_KEY, mode);
  }

  getThemeMode() {
    return this.storage.get(this.THEME_MODE_KEY);
  }

  saveRememberMe(rememberMe: string) {
    return this.storage.set(this.REMEMBER_ME_KEY, rememberMe);
  }

  getRememberMe() {
    return this.storage.get(this.REMEMBER_ME_KEY);
  }

  removeRememberMe() {
    return this.storage.remove(this.REMEMBER_ME_KEY);
  }
}
