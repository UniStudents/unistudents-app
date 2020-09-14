import { Injectable } from '@angular/core';
import { NetworkStatus, Plugins } from '@capacitor/core';
import { Router } from '@angular/router';
import { ToastService } from './toast.service';
import { StudentService } from './student.service';

const { Network } = Plugins;

@Injectable({
  providedIn: 'root'
})
export class NetworkService {

  private _networkStatus: NetworkStatus;

  constructor(
      private router: Router,
      private toastService: ToastService,
      private studentService: StudentService
  ) {}

  async init() {
    this.networkStatus = await Network.getStatus();
    return Network.addListener('networkStatusChange', (status) => {
      if (status.connected !== this.networkStatus.connected) {
        this.networkStatus = status;
        if (status.connected === false) {
          if ((this.router.isActive('/login', true) && this.router.url === '/login') ||
              ((this.router.isActive('/universities', true) && this.router.url === '/universities')) ||
              ((this.router.isActive('/welcome', true) && this.router.url === '/welcome'))) {
            this.toastService.presentSimple('Είσαι offline!');
          } else {
            this.toastService.present('Είσαι offline!');
          }
        } else if (status.connected === true) {
          if (this.router.isActive('/offline-login', true) && this.router.url === '/offline-login') {
            const rememberMe = this.studentService.rememberMe;
            if (rememberMe === null) {
              this.router.navigateByUrl('/universities');
              this.toastService.presentSimple('Είσαι πάλι online!');
            } else if (rememberMe === false) {
              this.router.navigateByUrl('/login');
              this.toastService.presentSimple('Είσαι πάλι online!');
            } else if (rememberMe === true) {
              this.router.navigate(['/app/tabs/tab1']);
              this.toastService.present('Είσαι πάλι online!');
            }
          } else if ((this.router.isActive('/universities', true) && this.router.url === '/universities') ||
                     (this.router.isActive('/login', true) && this.router.url === '/login') ||
                     (this.router.isActive('/welcome', true) && this.router.url === '/welcome')) {
            this.toastService.presentSimple('Είσαι πάλι online!');
          } else {
            this.toastService.present('Είσαι πάλι online!');
          }
        }
      }
    });
  }

  get networkStatus(): NetworkStatus {
    return this._networkStatus;
  }

  set networkStatus(value: NetworkStatus) {
    this._networkStatus = value;
  }
}
