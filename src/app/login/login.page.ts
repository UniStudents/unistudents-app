import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {AuthService} from '../shared/services/auth.service';
import {IonItem, IonLabel, LoadingController} from '@ionic/angular';
import {Storage} from '@ionic/storage';
import {Router} from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.page.html',
  styleUrls: ['./login.page.scss'],
})
export class LoginPage implements OnInit {
    @ViewChild('passwordLabel') passwordLabel: IonLabel;
    passwordField: string;

  constructor(
      private router: Router,
      private storage: Storage,
      private authService: AuthService,
      public loadingController: LoadingController
  ) { }

  ngOnInit() {
  }

  async login(form: NgForm) {

    const loading = await this.loadingController.create({
      message: 'Please wait..'
    });

    await loading.present();

    this.authService.login(form.value.username, form.value.password)
        .subscribe(
            (response) => {
              this.storage.set('userData', response).then( () => {
                  this.authService.isLoggedIn = true;
                  this.router.navigate(['/app/tabs/tab1']);
              });
            },
            (error) => {
              loading.dismiss();
              if (error.status === 401) {

                this.passwordField = '';

                const item = document.getElementById('passwordItem');
                item.classList.add('invalid-password');

                this.passwordLabel.color = 'danger';
              } else {

              }
            },
            () => {
              loading.dismiss();
            }
        );
  }
}
