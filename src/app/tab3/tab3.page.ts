import {Component, OnInit} from '@angular/core';
import {Student} from '../shared/models/student.model';
import {StorageService} from '../shared/services/storage.service';
import {AlertController} from '@ionic/angular';
import {AuthService} from '../shared/services/auth.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-tab3',
  templateUrl: 'tab3.page.html',
  styleUrls: ['tab3.page.scss']
})
export class Tab3Page implements OnInit {

  public student: Student;

  constructor(
      private storageService: StorageService,
      private authService: AuthService,
      private router: Router,
      public alertController: AlertController
  ) {}

  ngOnInit(): void {
    this.loadStudentInfo();
  }

  loadStudentInfo() {
    this.storageService.getStudent().then((student) => {
      this.student = student;
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  async presentAlert() {
    const alert = await this.alertController.create({
      header: 'Αποσύνδεση',
      message: 'Τα δεδομένα σου θα διαγραφτούν απο τη συσκευή!',
      buttons: [
        {
          text: 'ΑΚΥΡΟ',
          role: 'cancel',
          handler: () => {
            alert.dismiss();
          }
        }, {
          text: 'ΝΑΙ',
          handler: () => {
            this.logout();
          }
        }
      ]
    });

    await alert.present();
  }

}
