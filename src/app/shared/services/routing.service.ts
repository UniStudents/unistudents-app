import { Injectable } from '@angular/core';
import { Platform } from '@ionic/angular';
import { Router } from '@angular/router';
import { Plugins } from '@capacitor/core';
import { StudentService } from './student.service';
import { NetworkService } from './network.service';
import { UpdateService } from './update.service';

const { App } = Plugins;

@Injectable({
  providedIn: 'root'
})
export class RoutingService {

  currentPage: string;
  isAnyModalPageOpened = false;
  isLoadingFlag = false;

  constructor(
    private router: Router,
    private platform: Platform,
    private networkService: NetworkService,
    private studentService: StudentService,
    private updateService: UpdateService
  ) { }

  init() {
    if (this.networkService.networkStatus.connected === true) {
      const rememberMe = this.studentService.rememberMe;
      if (rememberMe === null && this.updateService.isFirstTime) {
        this.router.navigateByUrl('/welcome');
      } else if (rememberMe === null && !this.updateService.isFirstTime) {
        this.router.navigateByUrl('/universities');
      } else if (rememberMe === false) {
        this.router.navigateByUrl('/login');
      } else if (rememberMe === true) {
        this.router.navigate(['/app/tabs/tab1']);
      }
    } else if (this.networkService.networkStatus.connected === false) {
      this.router.navigateByUrl('/offline-login');
    }

    this.platform.resume.subscribe(() => {
      this.router.navigate([this.currentPage]);
    });

    this.platform.backButton.subscribe(async () => {
      if ((this.router.isActive('/welcome', true) && this.router.url === '/welcome') ||
        (this.router.isActive('/universities', true) && this.router.url === '/universities') ||
        ((this.router.isActive('/offline-login', true) && this.router.url === '/offline-login')) ||
        ((this.router.isActive('/app/tabs/tab1', true) && this.router.url === '/app/tabs/tab1')) ||
        ((this.router.isActive('/app/tabs/tab2', true) && this.router.url === '/app/tabs/tab2')) ||
        ((this.router.isActive('/app/tabs/tab3', true) && this.router.url === '/app/tabs/tab3') && !this.isAnyModalPageOpened)) {
        App.exitApp();
      } else if ((this.router.isActive('/login', true) && this.router.url === '/login') && !this.isLoadingFlag) {
        this.router.navigate(['/universities']);
      }
    });
  }
}
