import { Component, OnInit, Renderer, ViewChild } from '@angular/core';
import { AlertController, IonLabel, Platform } from '@ionic/angular';
import { Router } from '@angular/router';
import { StorageService } from '../shared/services/storage.service';
import { ApiService } from '../shared/services/api.service';
import { CryptoService } from '../shared/services/crypto.service';
import { NgForm } from '@angular/forms';
import { Student } from '../shared/models/student.model';
import { Plugins } from '@capacitor/core';
import { StoreService } from '../shared/services/store.service';
import { ToastService } from '../shared/services/toast.service';
import { NotificationService } from '../shared/services/notification.service';
import { AngularFireAnalytics } from '@angular/fire/analytics';
import { NetworkService } from '../shared/services/network.service';
import { UniversityService } from '../shared/services/university.service';
import { StudentService } from '../shared/services/student.service';
import { RoutingService } from '../shared/services/routing.service';
import { PermissionService } from '../shared/services/permission.service';

const { App, ScrapePlugin } = Plugins;
const { Keyboard } = Plugins;

@Component({
  selector: 'app-login',
  templateUrl: './login.page.html',
  styleUrls: ['./login.page.scss'],
})
export class LoginPage implements OnInit {
  @ViewChild('usernameLabel', {static: true}) usernameLabel: IonLabel;
  @ViewChild('passwordLabel', {static: true}) passwordLabel: IonLabel;
  usernameField: string;
  passwordField: string;

  passwordType = 'password';
  passwordIcon = 'eye-off';

  isLoadingFlag = false;
  isSameUser = false;

  private uni = '';
  private uniLogo = '';
  private uniForgetMyPasswordLink = '';
  private subscription;

  constructor(
    private router: Router,
    private storageService: StorageService,
    private apiService: ApiService,
    public toastService: ToastService,
    private cryptoService: CryptoService,
    public alertController: AlertController,
    private storeService: StoreService,
    private platform: Platform,
    private renderer: Renderer,
    private notificationService: NotificationService,
    private angularFireAnalytics: AngularFireAnalytics,
    private networkService: NetworkService,
    private universityService: UniversityService,
    private studentService: StudentService,
    private routingService: RoutingService,
    private permissionService: PermissionService
  ) { }

  ngOnInit() {
    this.setUsernameField();
    this.setUniVariables();
  }

  ionViewWillEnter() {
    if (this.studentService.isLoggedIn) {
      App.exitApp();
    } else {
      this.setUsernameField();
      this.passwordField = '';
      this.routingService.currentPage = '/login';
    }
  }

  async login(form: NgForm, event = null) {
    const username = form.value.username;
    const password = form.value.password;

    // check if the user is the same, in order to compare grades
    const storedUsername = this.studentService.username;
    if (storedUsername === username) {
      this.isSameUser = true;
    }

    this.studentService.username = username;
    this.studentService.password = password;

    if (event !== null) {
      // remove focus from input after "done" btn pressed
      this.renderer.invokeElementMethod(event.target, 'blur');
      Keyboard.hide();
    }

    // check if credentials are valid
    if (!this.usernameIsValid(username) || !this.passwordIsValid(password)) { return; }

    // check connectivity status
    if (this.networkService.networkStatus.connected !== true) { return; }

    // begin loading animation
    await Keyboard.hide().then(() => {
      this.isLoading(true);
    });

    if (this.platform.is('android')) {
      this.subscription = this.platform.backButton.subscribeWithPriority(9999, () => {
      });
      this.scrapeStudent();
      this.angularFireAnalytics.logEvent('login', {screen: 'login-page', device: 'android'});
    } else {
      this.fetchStudent();
      this.angularFireAnalytics.logEvent('login', {screen: 'login-page', device: 'ios'});
    }
  }

  scrapeStudent() {
    ScrapePlugin.getStudent({ university: this.uni,
                              username: this.studentService.username,
                              password: this.studentService.password }).then(res => {

      const student: Student = JSON.parse(res.student);
      this.storeService.setStudents(student);

      if (this.isSameUser) {
        this.storeService.compareGrades();
      }

      // store username
      this.storageService.saveUsername(this.studentService.username);

      // store university
      this.storageService.saveUniversity(this.uni);

      // store student's data
      this.storageService.saveStudent(student);

      this.studentService.isLoggedIn = true;
      this.apiService.fetchedData = true;
      this.router.navigate(['/app/tabs/tab1']).then(() => {
        const rememberMe = this.studentService.rememberMe;
        if (rememberMe === null) {
          this.rememberMeAlert();
        }
      });
      // dismiss loading animation
      this.isLoading(false);
      this.subscription.unsubscribe();
    }, error => {
      this.subscription.unsubscribe();
      this.handleScrapeError(error);
    });
  }

  fetchStudent() {
    this.apiService.fetchStudent(this.uni, this.studentService.username, this.studentService.password).subscribe((student: Student) => {
      this.storeService.setStudents(student);

      if (this.isSameUser) {
        this.storeService.compareGrades();
      }

      // store username
      this.storageService.saveUsername(this.studentService.username);

      // store university
      this.storageService.saveUniversity(this.uni);

      // store student's data
      this.storageService.saveStudent(student);

      this.studentService.isLoggedIn = true;
      this.apiService.fetchedData = true;
      this.router.navigate(['/app/tabs/tab1']).then(() => {
        const rememberMe = this.studentService.rememberMe;
        if (rememberMe === null) {
          this.rememberMeAlert();
        }
      });
    }, error => {
      this.handleApiError(error);
    }, () => {
      // dismiss loading animation
      this.isLoading(false);
    });
  }

  handleScrapeError(error) {
    // dismiss loading animation
    this.isLoading(false);
    if (error.code === '401') {
      this.passwordField = '';
      this.passwordLabel.color = 'danger';
      const item = document.getElementById('passwordItem');
      item.classList.add('invalid-password');
    } else if (error.code === '408') {
      this.toastService.presentSimple('Το σύστημα της σχολής σου δεν ανταποκρίνεται.');
    } else if (error.code === '500') {
      this.toastService.presentSimple('Κάτι πήγε λάθος! Δοκίμασε ξανά σε λίγο.');
    } else {
      this.toastService.presentSimple('Κάτι πήγε λάθος! Δοκίμασε ξανά σε λίγο.');
    }
  }

  handleApiError(error) {
    // dismiss loading animation
    this.isLoading(false);
    if (error.status === 401) {
      this.passwordField = '';
      this.passwordLabel.color = 'danger';
      const item = document.getElementById('passwordItem');
      item.classList.add('invalid-password');
    } else if (error.status === 408) {
      this.toastService.presentSimple('Το σύστημα της σχολής σου δεν ανταποκρίνεται.');
    } else if (error.status === 500) {
      this.toastService.presentSimple('Κάτι πήγε λάθος! Δοκίμασε ξανά σε λίγο.');
    } else {
      this.toastService.presentSimple('Κάτι πήγε λάθος! Δοκίμασε ξανά σε λίγο.');
    }
  }

  setUsernameField() {
    this.usernameField = this.studentService.username;
  }

  setUniVariables() {
    this.uni = this.universityService.uni;
    this.uniLogo = this.universityService.uniLogo;
    this.uniForgetMyPasswordLink = this.universityService.uniForgotMyPasswordLink;
  }

  usernameIsValid(username: string): boolean {
    if (username === null || username === undefined || username === '') {
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

  togglePasswordIcon() {
    this.passwordType = this.passwordType === 'text' ? 'password' : 'text';
    this.passwordIcon = this.passwordIcon === 'eye-off' ? 'eye' : 'eye-off';
  }

  async rememberMeAlert() {
    const alert = await this.alertController.create({
      header: 'Να με θυμάσαι',
      message: 'Θέλεις να αποθηκευτεί ο κωδικός στη συσκευή σου;',
      buttons: [
        {
          text: 'ΟΧΙ',
          role: 'cancel',
          handler: () => {
            this.studentService.rememberMe = false;
            this.studentService.notificationsForNewGrades = false;
            this.storageService.saveRememberMe('false');
            this.storageService.saveNewGradeNotification('false');
            alert.dismiss();
          }
        }, {
          text: 'ΝΑΙ',
          handler: () => {
            this.studentService.rememberMe = true;
            this.studentService.notificationsForNewGrades = true;
            this.storageService.saveRememberMe('true');
            this.storageService.saveNewGradeNotification('true');
            const password = this.cryptoService.encrypt(this.studentService.password);
            this.storageService.savePassword(password);
            this.notificationService.subscribeToTopics();
            setTimeout(() => {
              this.permissionService.isXiaomi();
            }, 2000);
          }
        }
      ],
      backdropDismiss: false
    });
    await alert.present();
  }

  isLoading(status: boolean) {
    this.isLoadingFlag = status;
    this.routingService.isLoadingFlag = status;
    const text = document.getElementById('inner-text');
    const spinner = document.getElementById('spinner');
    if (status === true) {
      spinner.classList.add('spinner-transition');
    } else {
      spinner.classList.remove('spinner-transition');
    }
  }
}
