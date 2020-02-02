import { Component, OnInit } from '@angular/core';
import { Student } from '../shared/models/student.model';
import { StorageService } from '../shared/services/storage.service';
import { AlertController, ToastController } from '@ionic/angular';
import { AuthService } from '../shared/services/auth.service';
import { Router } from '@angular/router';
import { AppVersion } from '@ionic-native/app-version/ngx';
import { ThemeModeService } from '../shared/services/theme-mode.service';

@Component({
  selector: 'app-tab3',
  templateUrl: 'tab3.page.html',
  styleUrls: ['tab3.page.scss']
})
export class Tab3Page implements OnInit {

  public student: Student;
  version = '1.0.0';
  status = false;

  constructor(
      private storageService: StorageService,
      private authService: AuthService,
      private router: Router,
      public alertController: AlertController,
      private appVersion: AppVersion,
      private themeMode: ThemeModeService
  ) {}

  ngOnInit(): void {
    this.loadStudentInfo();

    this.appVersion.getVersionNumber().then(res => {
      this.version = res;
    }).catch(err => {
      alert(err);
    });

    this.storageService.getThemeMode().then(mode => {
      if (mode === 'light') { this.status = false; }
      else if (mode === 'dark') { this.status = true; }
    });
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

  async logoutAlert() {
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

  toggleThemeMode() {
    this.themeMode.enableDarkMode(this.status);
  }
}
