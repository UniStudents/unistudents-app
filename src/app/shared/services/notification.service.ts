import { Injectable } from '@angular/core';
import { Plugins, PushNotification, PushNotificationActionPerformed, PushNotificationToken } from '@capacitor/core';
import { AlertController } from '@ionic/angular';
import { AngularFireAnalytics } from '@angular/fire/analytics';
import { ApiService } from './api.service';
import { Course } from '../models/course.model';

const { PushNotifications, FCMPlugin } = Plugins;

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  subscribedTopics: string[] = [];

  constructor(
    private apiService: ApiService,
    public alertController: AlertController,
    private angularFireAnalytics: AngularFireAnalytics
  ) { }

  async initNotificationService() {
    // Request permission to use push notifications
    // iOS will prompt user and return if they granted permission or not
    // Android will just grant without prompting
    PushNotifications.requestPermission().then(result => {
      if (result.granted) {
        // Register with Apple / Google to receive push via APNS/FCM
        PushNotifications.register();
      }
    });

    // On success, we should be able to receive notifications
    PushNotifications.addListener('registration',
      (token: PushNotificationToken) => {
        console.log('Push registration success, token: ' + token.value);
      }
    );

    // Some issue with our setup and push will not work
    PushNotifications.addListener('registrationError',
      (error: any) => {
        console.log('Error on registration: ' + JSON.stringify(error));
      }
    );

    // Show us the notification payload if the app is open on our device
    PushNotifications.addListener('pushNotificationReceived',
      (notification: PushNotification) => {
        this.alert(notification.title, notification.body);
      }
    );

    // Method called when tapping on a notification
    PushNotifications.addListener('pushNotificationActionPerformed',
      (notification: PushNotificationActionPerformed) => {
        this.angularFireAnalytics.logEvent('open_notification',
          {title: notification.notification.title, body: notification.notification.body});
      }
    );
  }

  subscribeToTopics() {
    this.subscribedTopics.forEach(topic => {
      this.subscribeToTopic(topic);
    });
  }

  subscribeToTopic(fTopic: string) {
    fTopic = encodeURIComponent(fTopic);
    FCMPlugin.subscribeTo({ topic: fTopic }).then(() => {
    }, err => {
      console.error(err);
    });
  }

  unsubscribeFromTopic(fTopic: string) {
    this.removeTopic(fTopic);
    fTopic = encodeURIComponent(fTopic);
    FCMPlugin.unsubscribeFrom({ topic: fTopic }).then(() => {
    }, error => {
      console.error(error);
    });
  }

  unsubscribeFromAllTopics() {
    this.removeAllTopics();
    PushNotifications.removeAllListeners();
    FCMPlugin.deleteInstance();
  }

  notifyNewGrade(university: string, course: Course, semester: number, department: string) {
    this.unsubscribeFromTopic(university + '.' + course.id);
    setTimeout(() => {
      this.apiService.notifyForNewCourse();
    }, 3000);
  }

  addTopic(topic: string) {
    if (!this.subscribedTopics.includes(topic)) {
      this.subscribedTopics.push(topic);
    }
  }

  removeTopic(topic: string) {
    const index: number = this.subscribedTopics.indexOf(topic);
    if (index !== -1) {
      this.subscribedTopics.splice(index, 1);
    }
  }

  removeAllTopics() {
    this.subscribedTopics = [];
  }

  async alert(title: string, body: string) {
    const alert = await this.alertController.create({
      header: title,
      message: body,
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

  removeAllDeliveredNotifications() {
    PushNotifications.removeAllDeliveredNotifications();
  }
}
