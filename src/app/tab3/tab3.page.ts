import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AlertController, ModalController } from '@ionic/angular';
import { ThemeModeService } from '../shared/services/theme-mode.service';
import { StoreService } from '../shared/services/store.service';
import { RoutingService } from '../shared/services/routing.service';
import { AboutPage } from '../modals/about/about.page';
import { FaqPage } from '../modals/faq/faq.page';
import { SettingsPage } from '../modals/settings/settings.page';
import { AngularFireAnalytics } from '@angular/fire/analytics';
import { Info } from '../shared/models/info.model';
import { Observable } from 'rxjs';
import { Student } from '../shared/models/student.model';

@Component({
  selector: 'app-tab3',
  templateUrl: 'tab3.page.html',
  styleUrls: ['tab3.page.scss']
})
export class Tab3Page {

  public info: Info = null;
  private studentObservable: Observable<Student[]>;
  darkMode = false;

  constructor(
    public alertController: AlertController,
    private router: Router,
    private themeMode: ThemeModeService,
    private storeService: StoreService,
    private routingService: RoutingService,
    private modalController: ModalController,
    private angularFireAnalytics: AngularFireAnalytics
  ) { }

  ngOnInit(): void {
    this.studentObservable = this.storeService.students;
    this.studentObservable.subscribe(res => {
      if (res.length !== 0) {
        this.info = res[0].info;
      }
    });
    this.darkMode = this.themeMode.darkMode;
  }

  ionViewWillEnter() {
    this.routingService.currentPage = '/app/tabs/tab3';
  }

  async logout() {
    this.angularFireAnalytics.logEvent('logout', {screen: 'tab3'});
    await this.storeService.logout();
    this.router.navigate(['/universities']);
  }

  async logoutAlert() {
    const alert = await this.alertController.create({
      header: 'Αποσύνδεση',
      message: 'Τα δεδομένα σου θα διαγραφούν απο τη συσκευή!',
      buttons: [
        {
          text: 'ΑΚΥΡΟ',
          role: 'cancel',
          handler: () => {
            alert.dismiss();
          }
        }, {
          text: 'ΝΑΙ',
          handler: () => {
            this.logout();
          }
        }
      ]
    });

    await alert.present();
  }

  toggleThemeMode() {
    this.themeMode.enableDarkMode(this.darkMode);
    this.angularFireAnalytics.setUserProperties({DarkMode: this.darkMode});
  }

  async openFaqModal() {
    this.routingService.isAnyModalPageOpened = true;
    const modal = await this.modalController.create({
      component: FaqPage,
      cssClass: 'modal',
      swipeToClose: true
    });
    modal.onDidDismiss().then(() => this.routingService.isAnyModalPageOpened = false);
    return await modal.present();
  }

  async openSettingsModal() {
    this.routingService.isAnyModalPageOpened = true;
    const modal = await this.modalController.create({
      component: SettingsPage,
      cssClass: 'modal',
      swipeToClose: true
    });
    modal.onDidDismiss().then(() => this.routingService.isAnyModalPageOpened = false);
    return await modal.present();
  }

  async openAboutModal() {
    this.routingService.isAnyModalPageOpened = true;
    const modal = await this.modalController.create({
      component: AboutPage,
      cssClass: 'modal',
      swipeToClose: true
    });
    modal.onDidDismiss().then(() => this.routingService.isAnyModalPageOpened = false);
    return await modal.present();
  }
}
