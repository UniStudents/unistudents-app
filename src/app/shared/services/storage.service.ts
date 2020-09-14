import { Injectable } from '@angular/core';
import { Student } from '../models/student.model';
import { Grades } from '../models/grades.model';
import { ToastService } from './toast.service';
import { Plugins } from '@capacitor/core';
import { NotificationService } from './notification.service';
import { Course } from '../models/course.model';

const { Storage } = Plugins;

@Injectable({
  providedIn: 'root'
})
export class StorageService {

  public newGrades = 0;
  public newGradesList: Array<string> = [];
  private STUDENT_KEY = 'student';
  private USERNAME_KEY = 'username';
  private PASSWORD_KEY = 'password';
  private UNIVERSITY_KEY = 'university';
  private THEME_MODE_KEY = 'theme_mode';
  private REMEMBER_ME_KEY = 'remember_me';
  private NEW_GRADE_NOTIFICATION = 'new_grade_notification';
  private UPDATES_NOTIFICATION = 'updates_notification';
  private APP_VERSION_KEY = 'app_version';
  private IS_FIRST_TIME = 'is_first_time';

  constructor(
      private toastService: ToastService,
      private notificationService: NotificationService
  ) { }

  saveStudent(student: Student) {
    return Storage.set({ key: this.STUDENT_KEY, value: JSON.stringify({
        info: student.info,
        grades: student.grades
      }) });
  }

  saveGrades(grades: Grades) {
    this.getStudent().then((student) => {
      const stud: Student = JSON.parse(student.value);
      stud.grades = grades;
      return Storage.set({ key: this.STUDENT_KEY, value: JSON.stringify({
          info: stud.info,
          grades: stud.grades
        }) });
    });
  }

  getStudent() {
    return Storage.get({ key: this.STUDENT_KEY });
  }

  removeStudent() {
    return Storage.remove({ key: this.STUDENT_KEY });
  }

  async compareGrades(newGrades: Grades, oldGrades: Grades, university: string, department: string) {
    this.newGrades = 0;
    this.newGradesList = [];

    const oldCourses: Array<Course> = [];
    oldGrades.semesters.forEach(semester => {
      semester.courses.forEach(course => {
        oldCourses.push(course);
      });
    });

    for (const semester of newGrades.semesters) {
      for (const course of semester.courses) {
        let i = 0;
        while (oldCourses.length > 0) {
          if (course.id === oldCourses[i].id) {
            if (course.examPeriod !== oldCourses[i].examPeriod) {
              this.newGrades++;
              this.newGradesList.push(course.id);

              if (course.grade !== '-') {
                if (Number(course.grade) >= 5) {
                  await this.notificationService.unsubscribeFromTopic(university + '.' + course.id);
                }
              }

              this.notificationService.notifyNewGrade(university, course, semester.id, department);
            }
            oldCourses.splice(i, 1);
            break;
          } else {
            i++;
          }
        }
      }
    }

    setTimeout(() => {
      this.printNewGradesMsg();
    }, 500);
  }

  printNewGradesMsg() {
    if (this.newGrades > 1) {
      this.toastService.present('Έχεις ' + this.newGrades + ' νέους βαθμούς!');
    } else if (this.newGrades === 1) {
      this.toastService.present('Έχεις 1 νέο βαθμό!');
    } else {
      this.toastService.present('Δεν έχεις νέους βαθμούς!');
    }
  }

  saveUsername(username: string) {
    return Storage.set({ key: this.USERNAME_KEY, value: username });
  }

  getUsername() {
    return Storage.get({ key: this.USERNAME_KEY });
  }

  removeUsername() {
    return Storage.remove({ key: this.USERNAME_KEY });
  }

  savePassword(password: string) {
    return Storage.set({ key: this.PASSWORD_KEY, value: password });
  }

  getPassword() {
    return Storage.get({ key: this.PASSWORD_KEY });
  }

  removePassword() {
    return Storage.remove({ key: this.PASSWORD_KEY });
  }

  saveUniversity(university: string) {
    return Storage.set({ key: this.UNIVERSITY_KEY, value: university });
  }

  getUniversity() {
    return Storage.get({ key: this.UNIVERSITY_KEY });
  }

  saveThemeMode(mode: string) {
    return Storage.set({ key: this.THEME_MODE_KEY, value: mode });
  }

  getThemeMode() {
    return Storage.get({ key: this.THEME_MODE_KEY });
  }

  saveRememberMe(rememberMe: string) {
    return Storage.set({ key: this.REMEMBER_ME_KEY, value: rememberMe });
  }

  getRememberMe() {
    return Storage.get({ key: this.REMEMBER_ME_KEY });
  }

  removeRememberMe() {
    return Storage.remove({ key: this.REMEMBER_ME_KEY });
  }

  getNewGradeNotification() {
    return Storage.get({ key: this.NEW_GRADE_NOTIFICATION });
  }

  saveNewGradeNotification(newGradeNotification: string) {
    return Storage.set({ key: this.NEW_GRADE_NOTIFICATION, value: newGradeNotification });
  }

  getUpdatesNotification() {
    return Storage.get({ key: this.UPDATES_NOTIFICATION });
  }

  saveUpdatesNotification(updatesNotification: string) {
    return Storage.set({ key: this.UPDATES_NOTIFICATION, value: updatesNotification });
  }

  saveAppVersion(appVersion: string) {
    return Storage.set({ key: this.APP_VERSION_KEY, value: appVersion });
  }

  getAppVersion() {
    return Storage.get({ key: this.APP_VERSION_KEY });
  }

  saveFirstTime(isFirstTime: string) {
    return Storage.set({ key: this.IS_FIRST_TIME, value: isFirstTime });
  }

  getFirstTime() {
    return Storage.get({ key: this.IS_FIRST_TIME });
  }

  clear() {
    return Storage.clear();
  }
}
