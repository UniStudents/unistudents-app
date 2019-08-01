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
    this.lineChart = this.getChart();
    this.loadStudentInfo();
  }

  loadStudentInfo() {
    this.storage.get('gradesObj')
        .then(
            (grades) => {
              this.grades = grades;
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
        labels: ['2016', '2017', '2018', '2019'],
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
            data: [6.5, 5.9, 8, 7.3],
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
}
