import { Component, OnInit } from '@angular/core';
import { Student } from '../shared/models/student.model';
import { StorageService } from '../shared/services/storage.service';
import { AlertController, ToastController } from '@ionic/angular';
import { AuthService } from '../shared/services/auth.service';
import { Router } from '@angular/router';
import { AppVersion } from '@ionic-native/app-version/ngx';
import { ThemeModeService } from '../shared/services/theme-mode.service';
import { ApiService } from '../shared/services/api.service';

@Component({
  selector: 'app-tab3',
  templateUrl: 'tab3.page.html',
  styleUrls: ['tab3.page.scss']
})
export class Tab3Page implements OnInit {

  public student: Student;
  version = '1.0.0';
  status = false;
  rememberMe = false;
  onInit = false;

  constructor(
      private storageService: StorageService,
      private authService: AuthService,
      private router: Router,
      public alertController: AlertController,
      private appVersion: AppVersion,
      private themeMode: ThemeModeService,
      private apiService: ApiService,
      private toastController: ToastController
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

    this.storageService.getRememberMe().then(rememberMe => {
      if (rememberMe === 'true') {
          this.onInit = true;
          this.rememberMe = true;
      } else if (rememberMe === 'false') {
          this.rememberMe = false;
      }
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
      message: 'Τα δεδομένα σου θα διαγραφούν απο τη συσκευή!',
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

  toggleRememberMe() {
    if (this.rememberMe && this.onInit) {
        this.onInit = false;
        return;
    }

    if (this.rememberMe === true) {
      this.storageService.saveRememberMe('true').then(rememberMe => {
        // encrypt, save & store password locally
        console.log(this.apiService.password);
        const password = this.cryptoService.encrypt(this.apiService.password);
        this.storageService.savePassword(password).then(result => {
          this.presentToast('Ο κωδικός σου αποθηκεύτηκε επιτυχώς!');
        });
      });
    } else {
      this.storageService.saveRememberMe('false').then(rememberMe => {
        this.storageService.removePassword().then(result => {
            this.presentToast('Ο κωδικός σου διαγράφηκε επιτυχώς!');
        });
      });
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
