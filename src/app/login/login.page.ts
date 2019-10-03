import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {AuthService} from '../shared/services/auth.service';
import {IonLabel, LoadingController, ToastController} from '@ionic/angular';
import {Router} from '@angular/router';
import {StorageService} from '../shared/services/storage.service';
import {Student} from '../shared/models/student.model';
import {AppMinimize} from '@ionic-native/app-minimize/ngx';
import {ApiService} from '../shared/services/api.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.page.html',
  styleUrls: ['./login.page.scss'],
})
export class LoginPage implements OnInit {
  @ViewChild('usernameLabel') usernameLabel: IonLabel;
  @ViewChild('passwordLabel') passwordLabel: IonLabel;
  passwordField: string;

  constructor(
      private router: Router,
      private storageService: StorageService,
      private authService: AuthService,
      public loadingController: LoadingController,
      private appMinimize: AppMinimize,
      private apiService: ApiService,
      public toastController: ToastController
  ) { }

  ngOnInit() {
  }

  ionViewWillEnter() {
      if (this.authService.isLoggedIn) {
        this.appMinimize.minimize();
      }
  }

  async login(form: NgForm) {

    const loading = await this.loadingController.create({
      message: 'Παρακαλώ περιμένετε..'
    });
    await loading.present();

    // check if credentials are valid
    if (!this.usernameIsValid(form.value.username) || !this.passwordIsValid(form.value.password)) {
        loading.dismiss();
        return;
    }

    this.authService.login(form.value.username, form.value.password).subscribe((student: Student) => {
        // compare grades
        this.storageService.getStudent().then((oldStudent) => {
            if (oldStudent) {
                this.storageService.compareGrades(student.grades, oldStudent.grades);
            }
        });

        // save credentials temporary for refresh data
        this.apiService.username = form.value.username;
        this.apiService.password = form.value.password;

        // save fetched data locally & navigate to home screen
        this.storageService.saveStudent(student).then(() => {
            this.authService.isLoggedIn = true;
            this.router.navigate(['/app/tabs/tab1']);
        });
    }, (error) => {
        loading.dismiss();
        if (error.status === 401) {
            this.passwordField = '';
            this.passwordLabel.color = 'danger';

            const item = document.getElementById('passwordItem');
            item.classList.add('invalid-password');
        } else {
            this.presentToast('Κάτη πήγε λάθος! Δοκίμασε ξανά σε λίγο.');
        }
    }, () => {
        loading.dismiss();
    });
  }

  usernameIsValid(username: string): boolean {
      if (username === '') {
          this.usernameLabel.color = 'danger';
          const item = document.getElementById('usernameItem');
          item.classList.add('invalid-password');
          return false;
      } else {
          this.usernameLabel.color = '';
          const item = document.getElementById('usernameItem');
          item.classList.remove('invalid-password');
          return true;
      }
  }

  passwordIsValid(password: string): boolean {
      if (password === undefined) {
          this.passwordLabel.color = 'danger';
          const item = document.getElementById('passwordItem');
          item.classList.add('invalid-password');
          return false;
      } else {
          this.passwordLabel.color = '';
          const item = document.getElementById('passwordItem');
          item.classList.remove('invalid-password');
          return true;
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
