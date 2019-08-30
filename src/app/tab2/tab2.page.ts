import {Component, OnInit} from '@angular/core';
import {GenericHttpService} from '../shared/services/generic-http.service';
import {GradeResults} from '../shared/models/grade-results.model';
import {LoadingController} from '@ionic/angular';
import {Storage} from '@ionic/storage';
import {LoginForm} from '../shared/models/login-form.model';
import {Student} from '../shared/models/student.model';
import {StorageService} from '../shared/services/storage.service';
import {Grades} from '../shared/models/grades.model';

@Component({
  selector: 'app-tab2',
  templateUrl: 'tab2.page.html',
  styleUrls: ['tab2.page.scss']
})
export class Tab2Page implements OnInit {

  public grades: Grades;
  public error = false;

  loginForm: LoginForm = {
      username: 'e16130',
      password: '21aug1998nikos$'
  };

  constructor(
      private service: GenericHttpService,
      private storageService: StorageService,
      public loadingController: LoadingController
  ) {}

  ngOnInit(): void {
    this.loadGrades();
  }

  async loadGrades() {
      this.storageService.getStudent().then((student) => {
          this.grades = student.grades;
      });
  }

  loadOfflineGrades() {
      this.storageService.getStudent().then((student) => {
          this.grades = student.grades;
      })
      .finally(() => {
          this.error = false;
      });
  }

  refreshData(event) {

    this.service.getGradeResults(this.loginForm).subscribe(
        data => {
          this.grades = data;
          this.error = false;
          // this.storage.set('userData', this.grades);
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
