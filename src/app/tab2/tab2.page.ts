import { Component } from '@angular/core';
import { Grades } from '../shared/models/grades.model';
import { Platform } from '@ionic/angular';
import { StoreService } from '../shared/services/store.service';
import { Observable } from 'rxjs';
import { Student } from '../shared/models/student.model';
import { RoutingService } from '../shared/services/routing.service';
import { AngularFireAnalytics } from '@angular/fire/analytics';
import { StorageService } from '../shared/services/storage.service';
import { ApiService } from '../shared/services/api.service';
import { NetworkService } from '../shared/services/network.service';

@Component({
  selector: 'app-tab2',
  templateUrl: 'tab2.page.html',
  styleUrls: ['tab2.page.scss']
})
export class Tab2Page {

  public grades: Grades = null;
  private studentObservable: Observable<Student[]>;

  constructor(
    private storeService: StoreService,
    private storageService: StorageService,
    private apiService: ApiService,
    private platform: Platform,
    private routingService: RoutingService,
    private networkService: NetworkService,
    private angularFireAnalytics: AngularFireAnalytics
  ) {}

  ngOnInit(): void {
    this.studentObservable = this.storeService.students;
    this.studentObservable.subscribe(res => {
      if (res.length !== 0) {
        this.grades = res[0].grades;
      }
    });
  }

  ionViewWillEnter() {
    this.routingService.currentPage = '/app/tabs/tab2';
  }

  refreshGrades(event) {
    this.angularFireAnalytics.logEvent('refresh_grades', {screen: 'tab2'});
    if (this.networkService.networkStatus.connected !== true) {
      event.target.complete();
      return;
    }
    setTimeout(() => {
      this.storeService.fetchStudent(event);
    }, 500);
  }

  getGradeColor(gradeString: string) {
    if (gradeString.includes('-')) {
      return '#657BFF';
    }

    const grade = Number(gradeString.replace(',', '.'));

    if (grade >= 5) {
      return '#657BFF';
    } else {
      return '#f25454';
    }
  }

  getCourseLength(courseLength) {
    if (courseLength === 0) {
      return 11;
    } else if (courseLength >= 3) {
      return 9;
    } else {
      return 10;
    }
  }
}
