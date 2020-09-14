import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Student } from '../models/student.model';
import { StorageService } from './storage.service';
import { ApiService } from './api.service';
import { Router } from '@angular/router';
import { ToastService } from './toast.service';
import { Platform } from '@ionic/angular';
import { Plugins } from '@capacitor/core';
import { NotificationService } from './notification.service';
import { UniversityService } from './university.service';
import { StudentService } from './student.service';
import { ThemeModeService } from './theme-mode.service';

const { ScrapePlugin } = Plugins;

@Injectable({
  providedIn: 'root'
})
export class StoreService {

  private _students: BehaviorSubject<Student[]> = new BehaviorSubject<Student[]>([]);
  firstSync = false;
  refreshGrades = false;

  constructor(
      private router: Router,
      private storageService: StorageService,
      private apiService: ApiService,
      private toastService: ToastService,
      private platform: Platform,
      private notificationService: NotificationService,
      private universityService: UniversityService,
      private studentService: StudentService,
      private themeMode: ThemeModeService
  ) {}

  get students(): Observable<Student[]> {
    return new Observable(fn => this._students.subscribe(fn));
  }

  setStudents(student) {
    this._students.next([student]);
  }

  loadStoredStudent() {
    return this.storageService.getStudent().then(student => {
      setTimeout(() => {
        this._students.next([JSON.parse(student.value)]);
      }, 200);
    });
  }

  async fetchStudent(event = null) {
    if (event === null) {
      this.firstSync = true;
    }

    let newStudent: Student;
    if (this._students.getValue().length === 0) {
      this.storageService.getStudent().then(student => {
        newStudent = JSON.parse(student.value);
      });
    } else {
      newStudent = this._students.getValue()[0];
    }

    /* if platform is ios fetch data from api
    *  else
    *  fetch data using scrape plugin */
    if (this.platform.is('android')) {
        ScrapePlugin.getStudent({ university: this.universityService.uni,
                                  username: this.studentService.username,
                                  password: this.studentService.password }).then(res => {
        newStudent = JSON.parse(res.student);
        this._students.next([newStudent]);

        this.compareGrades();

        this.storageService.saveStudent(newStudent);

        this.firstSync = false;
        if (event) { event.target.complete(); }
      }, error => {
        this.handleScrapeError(error);
        this.firstSync = false;
        if (event) { event.target.complete(); }
      });
    } else {
      // if Platform is 'ios' or PWA, fetch data from api
      if (event !== null) { this.refreshGrades = true; }
      await this.apiService.fetchStudent(this.universityService.uni,
                                         this.studentService.username,
                                         this.studentService.password).subscribe((student: Student) => {
        newStudent = student;
        this._students.next([newStudent]);

        this.compareGrades();

        this.storageService.saveGrades(newStudent.grades);

        this.firstSync = false;
        if (event) { event.target.complete(); this.refreshGrades = false; }
      }, error => {
        this.handleApiError(error);
        this.firstSync = false;
        if (event) { event.target.complete(); this.refreshGrades = false; }
      }, () => {
        this.firstSync = false;
        if (event) { event.target.complete(); this.refreshGrades = false; }
      });
    }
  }

  async logout() {
    await this.storageService.clear();
    this.studentService.username = '';
    this.studentService.password = '';
    this.studentService.rememberMe = null;
    this.studentService.isLoggedIn = false;
    this.studentService.notificationsForNewGrades = false;
    this._students.next([]);
    this.notificationService.unsubscribeFromAllTopics();
    this.themeMode.enableDarkMode(false);
    this.storageService.saveFirstTime('0');
  }

  compareGrades() {
    this.storageService.getStudent().then((oldStudent) => {
      this.storageService.compareGrades(this._students.getValue()[0].grades, JSON.parse(oldStudent.value).grades,
        this.universityService.uni, this._students.getValue()[0].info.department);
    });
  }

  handleApiError(error) {
    if (error.status === 401) {
      this.logout();
      this.router.navigate(['/universities']);
      this.toastService.presentSimple('Λάθος όνομα ή κωδικός! Έλεγξε τα στοιχεία σου ξανά.');
    } else if (error.status === 408) {
      this.toastService.present('Το σύστημα της σχολής σου δεν ανταποκρίνεται.');
    } else if (error.status === 500) {
      this.toastService.present('Κάτι πήγε λάθος! Δοκίμασε ξανά σε λίγο.');
    } else {
      this.toastService.present('Κάτι πήγε λάθος! Δοκίμασε ξανά σε λίγο.');
    }
  }

  handleScrapeError(error) {
    if (error.code === '401') {
      this.logout();
      this.router.navigate(['/universities']);
      this.toastService.presentSimple('Λάθος όνομα ή κωδικός! Έλεγξε τα στοιχεία σου ξανά.');
    } else if (error.code === '408') {
      this.toastService.present('Το σύστημα της σχολής σου δεν ανταποκρίνεται.');
    } else if (error.code === '500') {
      this.toastService.present('Κάτι πήγε λάθος! Δοκίμασε ξανά σε λίγο.');
    } else {
      this.toastService.present('Κάτι πήγε λάθος! Δοκίμασε ξανά σε λίγο.');
    }
  }
}
