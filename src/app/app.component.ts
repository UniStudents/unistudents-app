import { Component } from '@angular/core';

import { Platform } from '@ionic/angular';
import { SplashScreen } from '@ionic-native/splash-screen/ngx';
import { StatusBar } from '@ionic-native/status-bar/ngx';
import { Router } from '@angular/router';
import { NetworkService } from './shared/services/network.service';
import { StorageService } from './shared/services/storage.service';
import { ThemeModeService } from './shared/services/theme-mode.service';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html'
})
export class AppComponent {
  constructor(
    private platform: Platform,
    private networkService: NetworkService,
    private router: Router,
    private splashScreen: SplashScreen,
    private statusBar: StatusBar,
    private storageService: StorageService,
    private themeModeService: ThemeModeService
  ) {
    this.initializeApp();
  }

  initializeApp() {
    this.platform.ready().then(() => {
      this.statusBar.styleDefault();
      this.statusBar.backgroundColorByHexString('#ffffff');
      this.splashScreen.hide();

      if (this.networkService.isConnected()) {
        this.router.navigateByUrl('/login');
      } else {
        this.router.navigateByUrl('/offline-login');
      }

      this.storageService.getThemeMode().then(mode => {
        if (mode === null) {
          this.storageService.saveThemeMode('light');
        } else if (mode === 'dark') {
          this.themeModeService.enableDarkMode(true);
        }
      });

      this.platform.resume.subscribe(() => {
        this.router.navigate(['/app/tabs/tab1']);
      });

      this.platform.backButton.subscribe(async () => {
        if ((this.router.isActive('/login', true) && this.router.url === '/login') ||
            ((this.router.isActive('/offline-login', true) && this.router.url === '/offline-login'))) {
          navigator['app'].exitApp();
        }
      });
    });
  }
}
