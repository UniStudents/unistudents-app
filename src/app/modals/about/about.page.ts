import { Component, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { Plugins } from '@capacitor/core';

const { Device } = Plugins;

@Component({
  selector: 'app-about',
  templateUrl: './about.page.html',
  styleUrls: ['./about.page.scss'],
})
export class AboutPage implements OnInit {

  device: string;
  appVersion: any;

  constructor(
    private modalController: ModalController
  ) { }

  ngOnInit() {
    Device.getInfo().then(info => {
      this.appVersion = info.appVersion;
      if (info.platform === 'ios') {
        this.device = 'για iPhone';
      } else if (info.platform === 'android') {
        this.device = 'για Android';
      }
    });
  }

  async closeModal() {
    await this.modalController.dismiss();
  }
}
