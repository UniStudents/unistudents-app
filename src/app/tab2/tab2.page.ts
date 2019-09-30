import {Component, OnInit} from '@angular/core';
import {StorageService} from '../shared/services/storage.service';
import {Grades} from '../shared/models/grades.model';
import {ApiService} from '../shared/services/api.service';
import {ToastController} from '@ionic/angular';

@Component({
  selector: 'app-tab2',
  templateUrl: 'tab2.page.html',
  styleUrls: ['tab2.page.scss']
})
export class Tab2Page implements OnInit {

  public grades: Grades;

  constructor(
      private apiService: ApiService,
      private storageService: StorageService,
      public toastController: ToastController
  ) {}

  ngOnInit(): void {
      this.loadGrades();
  }

  loadGrades() {
      this.storageService.getStudent().then((student) => {
        this.grades = student.grades;
      });
  }

  refreshGrades(event) {

    if (this.apiService.username === undefined) {
        this.presentToast('Είσαι offline. Δοκίμασε να συνδεθείς ξανά.');
        event.target.complete();
        return;
    }

    this.apiService.getGrades().subscribe((grades: Grades) => {

      this.storageService.getStudent().then((oldStudent) => {
        // compare grades
        this.storageService.compareGrades(grades, oldStudent.grades);

        // print message about new grades
        this.printNewGradesMsg();
      });

      // save new grades
      this.grades = grades;
      this.storageService.saveGrades(grades);
    },
    error => {
      event.target.complete();
      this.presentToast('Κάτι πήγε λάθος! Δοκίμασε ξανά αργότερα.');
    },
    () => {
      event.target.complete();
    });
  }

  printNewGradesMsg() {
      if (this.storageService.newGrades > 1) {
          this.presentToast('Έχεις ' + this.storageService.newGrades + ' νέους βαθμούς!');
      } else if (this.storageService.newGrades === 1) {
          this.presentToast('Έχεις 1 νέο βαθμό!');
      } else {
          this.presentToast('Δεν έχεις νέους βαθμούς!');
      }
  }

  async presentToast(msg: string) {
    const toast = await this.toastController.create({
        message: msg,
        duration: 2000
    });
    await toast.present();
  }
}
