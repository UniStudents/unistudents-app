import { Component, OnInit, ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import { AuthService } from '../shared/services/auth.service';
import { AlertController, IonLabel, LoadingController, ToastController } from '@ionic/angular';
import { Router } from '@angular/router';
import { StorageService } from '../shared/services/storage.service';
import { Student } from '../shared/models/student.model';
import { AppMinimize } from '@ionic-native/app-minimize/ngx';
import { ApiService } from '../shared/services/api.service';
import { Grades } from '../shared/models/grades.model';

@Component({
  selector: 'app-login',
  templateUrl: './login.page.html',
  styleUrls: ['./login.page.scss'],
})
export class LoginPage implements OnInit {
  @ViewChild('usernameLabel') usernameLabel: IonLabel;
  @ViewChild('passwordLabel') passwordLabel: IonLabel;
  passwordField: string;
  usernameField: string;

  passwordType = 'password';
  passwordIcon = 'eye-off';

  loading: any;
  rememberMeOption: any;

  constructor(
      private router: Router,
      private storageService: StorageService,
      private authService: AuthService,
      public loadingController: LoadingController,
      private appMinimize: AppMinimize,
      private apiService: ApiService,
      public toastController: ToastController,
      // private fingerAuth: FingerprintAIO,
      private cryptoService: CryptoService,
      public alertController: AlertController,
      private firebase: Firebase
  ) {
      this.firebase.setScreenName('LoginScreen');
  }

  ngOnInit() {
      this.setUsernameField();

      this.getRememberMeOption().then(rememberMe => {
          if (rememberMe === null) {
              this.rememberMeOption = 'null';
          } else if (rememberMe === 'true') {
              this.rememberMeOption = 'true';
          } else if (rememberMe === 'false') {
              this.rememberMeOption = 'false';
          }

          if (this.rememberMeOption === 'true') {
              this.setPasswordField();
          } else {
              this.passwordField = '';
          }
      });
  }

  setUsernameField() {
      this.storageService.getUsername().then((username) => {
          if (username !== null) {
              this.usernameField = username;
          } else {
              this.usernameField = '';
          }
      });
  }

  setPasswordField() {
      this.storageService.getPassword().then((password) => {
          if (password !== null) {
              password = this.cryptoService.decrypt(password);
              this.passwordField = password;
          } else {
              this.passwordField = '';
          }
      });
  }

  getRememberMeOption() {
      return this.storageService.getRememberMe();
  }

  setRememberMeOption(option: string) {
      this.storageService.saveRememberMe(option);
  }

  ionViewWillEnter() {
      if (this.authService.isLoggedIn) {
        this.appMinimize.minimize();
      } else {
          this.setUsernameField();

          this.getRememberMeOption().then(rememberMe => {
              if (rememberMe === null) {
                  this.rememberMeOption = 'null';
              } else if (rememberMe === 'true') {
                  this.rememberMeOption = 'true';
              } else if (rememberMe === 'false') {
                  this.rememberMeOption = 'false';
              }

              if (this.rememberMeOption === 'true') {
                  this.setPasswordField();
              } else {
                  this.passwordField = '';
              }
          });
      }
  }

  async login(form: NgForm) {

    this.loading = await this.loadingController.create({
      message: 'Παρακαλώ περιμένετε..'
    });
    await this.loading.present();

    // check if credentials are valid
    if (!this.usernameIsValid(form.value.username) || !this.passwordIsValid(form.value.password)) {
        this.loading.dismiss();
        return;
    }

    // check for stored data
    this.storageService.getUsername().then(storedUsername => {
        if (storedUsername === null) {
            this.authenticateRequest(form, false);
        } else if (form.value.username === storedUsername) {
            this.authenticateRequest(form, true);
        }
    });
  }

  authenticateRequest(form: NgForm, userStored: boolean) {
      const username = form.value.username;
      const password = form.value.password;

      if (userStored) {
          this.authService.loginGrades(username, password).subscribe((grades: Grades) => {
              // compare grades
              this.compareGrades(grades);

              // save & store username locally
              this.saveUsernameLocally(username);

              // encrypt & save password locally
              this.savePasswordLocally(password);

              // save fetched data locally & navigate to home screen
              this.storageService.saveGrades(grades);
              this.authService.isLoggedIn = true;
              this.router.navigate(['/app/tabs/tab1']);
          }, (error) => {
              this.handleLoginError(error);
          }, () => {
              this.loading.dismiss();
          });
      } else {
          this.authService.login(username, password).subscribe((student: Student) => {
              // compare grades
              this.compareGrades(student.grades);

              // save & store username locally
              this.saveUsernameLocally(username);

              // encrypt & save password locally
              this.savePasswordLocally(password);

              // save fetched data locally & navigate to home screen
              this.storageService.saveStudent(student).then(() => {
                  this.authService.isLoggedIn = true;
                  this.router.navigate(['/app/tabs/tab1']).then(navigate => {
                      if (this.rememberMeOption === 'null') {
                          this.rememberMeAlert(form);
                      }
                  });
              });
          }, (error) => {
              this.handleLoginError(error);
          }, () => {
              this.loading.dismiss();
          });
      }
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
      if (password === undefined || password === '') {
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

  compareGrades(grades: Grades) {
      this.storageService.getStudent().then((oldStudent) => {
          if (oldStudent) {
              this.storageService.compareGrades(grades, oldStudent.grades);
          }
      });
  }

  saveUsernameLocally(username: string) {
      this.apiService.username = username;
      this.storageService.saveUsername(username);
  }

  savePasswordLocally(password: string) {
      if (this.rememberMeOption === 'true') {
          // encrypt, save & store password locally
          this.apiService.password = password;
          password = this.cryptoService.encrypt(password);
          this.storageService.savePassword(password);
      } else if (this.rememberMeOption === 'false') {
          // save temporary
          this.apiService.password = password;
      }
  }

  handleLoginError(error) {
      this.loading.dismiss();
      if (error.status === 401) {
          this.passwordField = '';
          this.passwordLabel.color = 'danger';

          const item = document.getElementById('passwordItem');
          item.classList.add('invalid-password');
      } else if (error.status === 500) {
          this.presentToast('Το students είναι προσωρινά εκτός λειτουργίας!', 5000);
      } else {
          this.presentToast('Κάτη πήγε λάθος! Δοκίμασε ξανά σε λίγο.');
      }
  }

  async presentToast(msg: string, dur = 2000) {
      const toast = await this.toastController.create({
          message: msg,
          duration: dur
      });
      await toast.present();
  }

  hideShowPassword() {
      this.passwordType = this.passwordType === 'text' ? 'password' : 'text';
      this.passwordIcon = this.passwordIcon === 'eye-off' ? 'eye' : 'eye-off';
  }

  async rememberMeAlert(form: NgForm) {
      const alert = await this.alertController.create({
          header: 'Να με θυμάσαι',
          message: 'Θέλεις να αποθηκευτεί ο κωδικός στη συσκευή σου;',
          buttons: [
              {
                  text: 'ΟΧΙ',
                  role: 'cancel',
                  handler: () => {
                      this.setRememberMeOption('false');
                      this.rememberMeOption = 'false';
                      this.apiService.password = form.value.password;
                      alert.dismiss();
                  }
              }, {
                  text: 'ΝΑΙ',
                  handler: () => {
                      this.setRememberMeOption('true');
                      this.rememberMeOption = 'true';
                      // encrypt, save & store password locally
                      this.apiService.password = form.value.password;
                      const password = this.cryptoService.encrypt(form.value.password);
                      this.storageService.savePassword(password);
                  }
              }
          ]
      });
      await alert.present();
  }

  public showFingerprintAuthDlg() {
        this.fingerprintOptions = {
            title: 'Biometric Authentication', // (Android Only) | optional | Default: "<APP_NAME> Biometric Sign On"
            subtitle: 'Coolest Plugin ever', // (Android Only) | optional | Default: null
            description: 'Please authenticate', // optional | Default: null
            fallbackButtonTitle: 'Use Backup', // optional | When disableBackup is false defaults to "Use Pin".
            // When disableBackup is true defaults to "Cancel"
            disableBackup: false  // optional | default: false
        };
        this.fingerAuth.show(this.fingerprintOptions)
            .then((result1: any) => this.presentToast(result1))
            .catch((error: any) => this.presentToast(error));
    }
}
