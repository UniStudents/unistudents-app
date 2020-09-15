import { Component, OnInit } from '@angular/core';
import { AlertController, ModalController } from '@ionic/angular';
import { StorageService } from '../../shared/services/storage.service';
import { CryptoService } from '../../shared/services/crypto.service';
import { NotificationService } from '../../shared/services/notification.service';
import { AngularFireAnalytics } from '@angular/fire/analytics';
import { StudentService } from '../../shared/services/student.service';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.page.html',
  styleUrls: ['./settings.page.scss'],
})
export class SettingsPage implements OnInit {

  rememberMe = false;
  newGrade = false;
  appUpdates = false;

  onInit: boolean[] = [false, false, false];

  constructor(
    public alertController: AlertController,
    private modalController: ModalController,
    private storageService: StorageService,
    private cryptoService: CryptoService,
    private notificationService: NotificationService,
    private studentService: StudentService,
    private angularFireAnalytics: AngularFireAnalytics
  ) { }

  ngOnInit() {
    if (this.studentService.rememberMe === true) {
      this.onInit[0] = true;
      this.rememberMe = true;
      if (this.studentService.notificationsForNewGrades === true) {
        this.onInit[1] = true;
        this.newGrade = true;
      }
    }

    if (this.studentService.notificationsForGeneralUpdates === true) {
      this.onInit[2] = true;
      this.appUpdates = true;
    }
  }

  async closeModal() {
    await this.modalController.dismiss();
  }

  toggleRememberMe() {
    if (this.onInit[0]) {
      this.onInit[0] = false;
      return;
    }

    if (this.rememberMe === true) {
      this.storageService.saveRememberMe('true').then(() => {
        // encrypt, save & store password locally
        const password = this.cryptoService.encrypt(this.studentService.password);
        this.storageService.savePassword(password).then(() => {
          this.studentService.rememberMe = true;
        });
      });
    } else if (this.rememberMe === false) {
      if (this.newGrade === true) {
        this.newGrade = false;
        this.alert();
      }
      this.storageService.saveRememberMe('false').then(() => {
        this.storageService.removePassword().then(() => {
          this.studentService.rememberMe = false;
          this.studentService.notificationsForNewGrades = false;
        });
      });
    }
    this.angularFireAnalytics.logEvent('toggle_remember_me_notification', {active: this.rememberMe});
  }

  toggleNewGrade() {
    if (this.onInit[1]) {
      this.onInit[1] = false;
      return;
    }

    if (this.newGrade === true) {
      this.storageService.saveNewGradeNotification('true').then(() => {
        // sub to all topics
        this.studentService.notificationsForNewGrades = true;
        this.notificationService.subscribeToTopics();
      });
    } else {
      this.storageService.saveNewGradeNotification('false').then(() => {
        // unsub from all topics
        this.studentService.notificationsForNewGrades = false;
        this.notificationService.subscribedTopics.forEach(topic => {
          this.notificationService.unsubscribeFromTopic(topic);
        });
      });
    }
    this.angularFireAnalytics.logEvent('toggle_new_grade_notification', {active: this.newGrade});
  }

  toggleAppUpdates() {
    if (this.onInit[2]) {
      this.onInit[2] = false;
      return;
    }

    if (this.appUpdates === true) {
      this.storageService.saveUpdatesNotification('true').then(() => {
        // sub to "UniStudents" topic
        this.notificationService.subscribeToTopic('unistudents');
        this.studentService.notificationsForGeneralUpdates = true;
      });
    } else {
      this.storageService.saveUpdatesNotification('false').then(() => {
        // unsub from UniStudents topic
        this.notificationService.unsubscribeFromTopic('unistudents');
        this.studentService.notificationsForGeneralUpdates = false;
      });
    }
    this.angularFireAnalytics.logEvent('toggle_updates_notification', {active: this.appUpdates});
  }

  async alert() {
    const alert = await this.alertController.create({
      header: 'Quick reminder',
      cssClass: 'alert',
      message: 'Απενεργοποιώντας τη λειτουργία αυτή δεν θα μπορείς να λαμβάνεις ειδοποιήσεις για νέους βαθμούς',
      buttons: [
        {
          text: 'OKAY',
          role: 'cancel',
          handler: () => {
            alert.dismiss();
          }
        }
      ]
    });

    await alert.present();
  }
}
