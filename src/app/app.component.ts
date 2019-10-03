import { Component } from '@angular/core';

import {Platform} from '@ionic/angular';
import { SplashScreen } from '@ionic-native/splash-screen/ngx';
import { StatusBar } from '@ionic-native/status-bar/ngx';
import {Router} from '@angular/router';
import {NetworkService} from './shared/services/network.service';

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
    private statusBar: StatusBar
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

      this.platform.resume.subscribe(() => {
        this.router.navigate(['/app/tabs/tab1']);
      });
    });
  }
}
