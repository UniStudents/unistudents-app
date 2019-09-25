import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {AuthService} from '../shared/services/auth.service';
import {IonItem, IonLabel, LoadingController} from '@ionic/angular';
import {Storage} from '@ionic/storage';
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
    @ViewChild('passwordLabel') passwordLabel: IonLabel;
    passwordField: string;
    entered = false;

  constructor(
      private router: Router,
      private storageService: StorageService,
      private authService: AuthService,
      public loadingController: LoadingController,
      private appMinimize: AppMinimize,
      private apiService: ApiService
  ) { }

  ngOnInit() {
  }

  ionViewWillEnter() {
      if (this.entered) {
        this.appMinimize.minimize();
      }
  }

  async login(form: NgForm) {

    const loading = await this.loadingController.create({
      message: 'Παρακαλώ περιμένετε..'
    });

    await loading.present();

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
            this.entered = true;
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
            // display server error msg
        }
    }, () => {
        loading.dismiss();
    });
  }
}
