import { Component } from '@angular/core';

import { Platform } from '@ionic/angular';
import { Plugins, StatusBarStyle } from '@capacitor/core';
import { ThemeModeService } from './shared/services/theme-mode.service';
import { NetworkService } from './shared/services/network.service';
import { UpdateService } from './shared/services/update.service';
import { RoutingService } from './shared/services/routing.service';
import { NotificationService } from './shared/services/notification.service';
import { UniversityService } from './shared/services/university.service';
import { StudentService } from './shared/services/student.service';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss']
})
export class AppComponent {
  constructor(
    private platform: Platform,
    private updateService: UpdateService,
    private routingService: RoutingService,
    private networkService: NetworkService,
    private studentService: StudentService,
    private themeModeService: ThemeModeService,
    private universityService: UniversityService,
    private notificationService: NotificationService
  ) {
    this.initializeApp();
  }

  initializeApp() {
    const { SplashScreen, StatusBar } = Plugins;
    this.platform.ready().then(() => {
      SplashScreen.hide();
      StatusBar.setStyle({ style: StatusBarStyle.Light});

      // clear received notifications (for iOS)
      this.notificationService.removeAllDeliveredNotifications();

      // check for new updates
      this.updateService.checkForUpdates().then(() => {

        // init university values
        this.universityService.init().then(() => {

          // init student data & preferences
          this.studentService.init().then(() => {

            // init network service
            this.networkService.init().then(() => {

              // init routing service
              this.routingService.init();
            });
          });
        });
      });

      // set up theme mode
      this.themeModeService.init();
    });
  }
}
