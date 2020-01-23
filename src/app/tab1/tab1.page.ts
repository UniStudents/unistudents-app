import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import { Chart } from 'chart.js';
import {GradeResults} from '../shared/models/grade-results.model';
import {Student} from '../shared/models/student.model';
import {StorageService} from '../shared/services/storage.service';

@Component({
  selector: 'app-tab1',
  templateUrl: 'tab1.page.html',
  styleUrls: ['tab1.page.scss']
})
export class Tab1Page implements OnInit {
  @ViewChild('lineCanvas') lineCanvas: ElementRef;
  @ViewChild('pieCanvas') pieCanvas: ElementRef;
  @ViewChild('doughnutCanvas') doughnutCanvas: ElementRef;

  private lineChart: Chart;
  private pieChart: Chart;
  private doughnutChart: Chart;
  public grades: GradeResults;
  public student: Student;

  constructor(
      private storageService: StorageService
  ) {}

  ngOnInit(): void {
    this.loadStudentInfo();
  }

  loadStudentInfo() {
    this.storageService.getStudent().then((student) => {
      this.student = student;
      this.lineChart = this.getLineChart();
      // this.pieChart = this.getPieChart();
      this.doughnutChart = this.getDoughnutChart();
    });
  }

  getDoughnutChart(): Chart {
    const dataset: Array<number> = [0, 0, 0];
    dataset[0] = Number(this.student.grades.totalPassedCourses);

    for (const semester of this.student.grades.semesters) {
      for (const course of semester.courses) {
        if (course.grade === '-') {
          dataset[2]++;
        } else if (Number(course.grade) < 5) {
          dataset[1]++;
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
          data: dataset
        }]
      },
      options: {
        legend: {
          onClick: null
        }
      }
    });
  }

  // getPieChart(): Chart {
  //   const gradesQuantity: Array<number> = [0, 0, 0];
  //
  //   for (const semester of this.student.grades.semesters) {
  //     for (const course of semester.courses) {
  //       if (course.grade !== '-') {
  //         if (Number(course.grade) > 8) {
  //           gradesQuantity[0]++;
  //         } else if (Number(course.grade) > 6) {
  //           gradesQuantity[1]++;
  //         } else if (Number(course.grade) > 4) {
  //           gradesQuantity[2]++;
  //         } else {
  //           continue;
  //         }
  //       }
  //     }
  //   }
  //
  //   return new Chart(this.pieCanvas.nativeElement, {
  //     type: 'pie',
  //     data: {
  //       labels: ['Άριστα', 'Λίαν Καλώς', 'Καλώς'],
  //       datasets: [{
  //         backgroundColor: [
  //           '#657BFF',
  //           'rgba(101,123,255,0.8)',
  //           'rgba(101,123,255,0.6)'
  //         ],
  //         data: gradesQuantity
  //       }]
  //     },
  //     options: {
  //       legend: {
  //         onClick: null
  //       }
  //     }
  //   });
  // }

  getLineChart(): Chart {
    const grades: Array<number> = [];
    const semesters: Array<number> = [];

    for (let i = 0; i < this.student.grades.semesters.length; i++) {
      if (this.student.grades.semesters[i].gradeAverage === '-') { continue; }
      semesters.push(this.student.grades.semesters[i].id);
      grades.push(Number(this.student.grades.semesters[i].gradeAverage.replace('-', '')));
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
}
