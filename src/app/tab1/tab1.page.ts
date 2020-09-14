import { Component, ElementRef, ViewChild } from '@angular/core';
import { Chart } from 'chart.js';
import { Student } from '../shared/models/student.model';
import { Router } from '@angular/router';
import { ApiService } from '../shared/services/api.service';
import { StoreService } from '../shared/services/store.service';
import { Observable } from 'rxjs';
import { AngularFireAnalytics } from '@angular/fire/analytics';
import { NotificationService } from '../shared/services/notification.service';
import { RoutingService } from '../shared/services/routing.service';
import { NetworkService } from '../shared/services/network.service';
import { StudentService } from '../shared/services/student.service';
import { UniversityService } from '../shared/services/university.service';

@Component({
  selector: 'app-tab1',
  templateUrl: 'tab1.page.html',
  styleUrls: ['tab1.page.scss']
})
export class Tab1Page {
  @ViewChild('lineCanvas', {static: true}) lineCanvas: ElementRef;
  @ViewChild('doughnutCanvas', {static: true}) doughnutCanvas: ElementRef;

  initGraphs = false;
  flag = false;
  public student: Student = null;
  private lineChart: Chart;
  private doughnutChart: Chart;
  private studentObservable: Observable<Student[]>;

  constructor(
    private apiService: ApiService,
    private router: Router,
    private storeService: StoreService,
    private firebaseAnalytics: AngularFireAnalytics,
    private notificationService: NotificationService,
    private routingService: RoutingService,
    private networkService: NetworkService,
    private studentService: StudentService,
    private universityService: UniversityService
  ) {}

  ngOnInit(): void {
    this.studentObservable = this.storeService.students;

    this.studentObservable.subscribe(res => {
      // after login-page screen
      if (this.apiService.fetchedData && !this.flag) {
        setTimeout(() => {
          this.student = res[0];
          this.flag = true;
          this.lineChart = this.getLineChart();
          this.doughnutChart = this.getDoughnutChart();
          this.initGraphs = true;
          this.setFirebase();
        }, 400);
      } else {
        // after home-page screen
        if (res.length !== 0) {
          this.student = res[0];
          this.apiService.fetchedData = true;
          this.flag = true;
          if (this.initGraphs) {
            this.updateLineChart();
            this.updateDoughnutChart();
          } else {
            this.lineChart = this.getLineChart();
            this.doughnutChart = this.getDoughnutChart();
            this.initGraphs = true;
            this.setFirebase();
          }
          // after subscribedTopics filled
          // subscribe to them
          if (this.studentService.notificationsForNewGrades) {
            this.notificationService.subscribeToTopics();
          }
          this.firebaseAnalytics.logEvent('refresh_grades', {screen: 'tab1'});
        }
      }
    });

    // start fetching student's data
    if (!this.apiService.fetchedData) {
      this.fetchStudent();
    }
  }

  ionViewWillEnter() {
    this.routingService.currentPage = '/app/tabs/tab1';
  }

  async fetchStudent() {
    // print stored student to front-end
    await this.storeService.loadStoredStudent();

    // if connected fetch new data
    const status = this.networkService.networkStatus;
    if (status.connected === true) {
      setTimeout(() => {
        this.storeService.fetchStudent();
      }, 800);
    }
  }

  setFirebase() {
    this.firebaseAnalytics.setUserProperties({
      Semester: this.student.info.semester,
      Department: this.student.info.department,
      University: this.universityService.uni
    });
    this.notificationService.initNotificationService();
    this.notificationService.addTopic(this.universityService.uni);
  }

  getDoughnutChart(): Chart {
    const dataset: Array<number> = [0, 0, 0];
    dataset[0] = Number(this.student.grades.totalPassedCourses);

    for (const semester of this.student.grades.semesters) {
      for (const course of semester.courses) {
        if (course.grade === '') { continue; }
        if (course.grade === '-') {
          dataset[2]++;
          this.notificationService.addTopic(this.universityService.uni + '.' + course.id);
        } else if (Number(course.grade) < 5) {
          dataset[1]++;
          this.notificationService.addTopic(this.universityService.uni + '.' + course.id);
        }
      }
    }

    return new Chart(this.doughnutCanvas.nativeElement, {
      type: 'doughnut',
      data: {
        labels: ['Πέρασες', 'Κόπηκες', 'Δεν έχεις δώσει'],
        datasets: [{
          backgroundColor: [
            '#657BFF',
            'rgba(101,123,255,0.6)',
            'rgba(101,123,255,0.3)'
          ],
          data: dataset,
          borderWidth: 0,
        }]
      },
      options: {
        legend: {
          onClick: null
        }
      }
    });
  }

  getLineChart(): Chart {
    const grades: Array<number> = [];
    const semesters: Array<number> = [];

    for (let i = 0; i < this.student.grades.semesters.length; i++) {
      if (this.student.grades.semesters[i].gradeAverage === '-') { continue; }
      semesters.push(this.student.grades.semesters[i].id);
      grades.push(Number(this.student.grades.semesters[i].gradeAverage.replace('-', '').replace(',', '.')));
    }

    return new Chart(this.lineCanvas.nativeElement, {
      type: 'line',
      data: {
        labels: semesters,
        datasets: [
          {
            label: 'Μέσος Όρος',
            fill: true,
            lineTension: 0.4,
            backgroundColor: 'rgba(101,123,255,0.41)',
            borderColor: '#657BFF',
            borderCapStyle: 'butt',
            borderDash: [],
            borderDashOffset: 0.0,
            borderJoinStyle: 'miter',
            pointBorderColor: 'rgba(75,192,192,1)',
            pointBackgroundColor: '#fff',
            pointBorderWidth: 1,
            pointHoverRadius: 5,
            pointHoverBackgroundColor: 'rgba(75,192,192,1)',
            pointHoverBorderColor: 'rgba(220,220,220,1)',
            pointHoverBorderWidth: 2,
            pointRadius: 1,
            pointHitRadius: 10,
            data: grades,
            spanGaps: false,
            duration: 4000,
            easing: 'easeInQuart'
          }
        ]
      },
      options: {
        legend: {
          onClick: null,
          labels: {
            boxWidth: 0
          }
        },
        scales: {
          xAxes: [{
            gridLines: {
              display: true
            }
          }],
          yAxes: [{
            gridLines: {
              drawBorder: false,
              display: false
            },
            ticks: {
              maxTicksLimit: 4
            }
          }]
        }
      },
    });
  }

  updateDoughnutChart() {
    const dataset: Array<number> = [0, 0, 0];
    dataset[0] = Number(this.student.grades.totalPassedCourses);

    for (const semester of this.student.grades.semesters) {
      for (const course of semester.courses) {
        if (course.grade === '') { continue; }
        if (course.grade === '-') {
          dataset[2]++;
          this.notificationService.addTopic(this.universityService.uni + '.' + course.id);
        } else if (Number(course.grade) < 5) {
          dataset[1]++;
          this.notificationService.addTopic(this.universityService.uni + '.' + course.id);
        }
      }
    }

    this.doughnutChart.data.datasets[0].data = dataset;
    this.doughnutChart.update();
  }

  updateLineChart() {
    const grades: Array<number> = [];
    const semesters: Array<number> = [];

    for (let i = 0; i < this.student.grades.semesters.length; i++) {
      if (this.student.grades.semesters[i].gradeAverage === '-') { continue; }
      semesters.push(this.student.grades.semesters[i].id);
      grades.push(Number(this.student.grades.semesters[i].gradeAverage.replace('-', '').replace(',', '.')));
    }

    this.lineChart.data.datasets[0].data = grades;
    this.lineChart.data.labels = semesters;

    this.lineChart.update();
  }
}
