import {Component, OnInit} from '@angular/core';
import {GenericHttpService} from '../shared/services/generic-http.service';
import {GradeResults} from '../shared/models/grade-results.model';
import {LoadingController} from '@ionic/angular';

@Component({
  selector: 'app-tab2',
  templateUrl: 'tab2.page.html',
  styleUrls: ['tab2.page.scss']
})
export class Tab2Page implements OnInit {

  public grades: GradeResults;

  constructor(
      private service: GenericHttpService,
      public loadingController: LoadingController) {}

  ngOnInit(): void {
    this.loadGrades();
  }

  async loadGrades() {

    const loading = await this.loadingController.create({
      message: 'Please wait..'
    });

    await loading.present();

    this.service.getGradeResults().subscribe(
        data => {
          this.grades = data;
        },
        error => {
          loading.dismiss();
          this.grades = null;
        },
        () => {
          loading.dismiss();
        }
    );
  }

  refreshData(event) {
    this.service.getGradeResults().subscribe(
        data => {
          this.grades = data;
        },
        error => {
          event.target.complete();
          this.grades = null;
        },
        () => {
          event.target.complete();
        }
    );
  }
}
