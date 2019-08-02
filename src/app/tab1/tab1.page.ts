import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import { Chart } from 'chart.js';
import {GradeResults} from '../shared/models/grade-results.model';
import {Storage} from '@ionic/storage';

@Component({
  selector: 'app-tab1',
  templateUrl: 'tab1.page.html',
  styleUrls: ['tab1.page.scss']
})
export class Tab1Page implements OnInit {
  @ViewChild('lineCanvas') lineCanvas: ElementRef;

  private lineChart: Chart;
  public grades: GradeResults;

  constructor(
      private storage: Storage
  ) {}

  ngOnInit(): void {
    this.loadStudentInfo();
  }

  loadStudentInfo() {
    this.storage.get('gradesObj')
        .then(
            (grades) => {
              this.grades = grades;
              this.lineChart = this.getChart();
            }
        )
        .catch(
            error => console.log(error)
        )
        .finally(
        () => {
        }
    );
  }

  getChart(): Chart {
    return new Chart(this.lineCanvas.nativeElement, {
      type: 'line',
      data: {
        labels: this.getSemesters(),
        datasets: [
          {
            label: 'GPA',
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
            data: this.getGrades(),
            spanGaps: false
          }
        ]
      },
      options: {
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

  private getGrades() {
    const grades: Array<number> = [];

    for (let i = 0; i < this.grades.semesters.length; i++) {
      grades.push(Number(this.grades.semesters[i].gradeAverage));
    }

    return grades;
  }

  private getSemesters() {
    const semesters: Array<number> = [];

    for (let i = 0; i < this.grades.semesters.length; i++) {
      semesters.push(this.grades.semesters[i].id);
    }

    return semesters;
  }
}
