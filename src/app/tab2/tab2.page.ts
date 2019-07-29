import {Component, OnInit} from '@angular/core';
import {GenericHttpService} from '../shared/services/generic-http.service';
import {GradeResults} from '../shared/models/grade-results.model';
import {LoadingController} from '@ionic/angular';
import {Storage} from '@ionic/storage';

@Component({
  selector: 'app-tab2',
  templateUrl: 'tab2.page.html',
  styleUrls: ['tab2.page.scss']
})
export class Tab2Page implements OnInit {

  public grades: GradeResults;
  public error = false;

  constructor(
      private service: GenericHttpService,
      private storage: Storage,
      public loadingController: LoadingController) {}

  ngOnInit(): void {
    this.loadGrades();
  }

  async loadGrades() {

    const loading = await this.loadingController.create({
      message: 'Please wait..'
    });

    await loading.present();

    this.service.getGradeResults()
        .subscribe(
        data => {
          this.grades = data;
          this.error = false;
          this.storage.set('gradesObj', this.grades);
        },
        error => {
          loading.dismiss();
          this.error = true;
        },
        () => {
          loading.dismiss();
        }
    );
  }

  loadOfflineGrades() {
      this.storage.get('gradesObj')
          .then(
          (grades) => {
              this.grades = grades;
          }
      ).finally(
          () => {
              this.error = false;
          }
      );
  }

  refreshData(event) {

    this.service.getGradeResults().subscribe(
        data => {
          this.grades = data;
          this.error = false;
          this.storage.set('gradesObj', this.grades);
        },
        error => {
          event.target.complete();
          this.error = true;
        },
        () => {
          event.target.complete();
        }
    );
  }
}
