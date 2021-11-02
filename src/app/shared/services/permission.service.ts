import { Injectable } from '@angular/core';
import { Plugins } from '@capacitor/core';
import { AlertController } from '@ionic/angular';
import { UpdateService } from './update.service';

const { PermissionPlugin } = Plugins;

@Injectable({
  providedIn: 'root'
})
export class PermissionService {

  constructor(
    private alertController: AlertController,
    private updateService: UpdateService
  ) { }

  isXiaomi() {
    if (this.updateService.isFirstTime) {
      PermissionPlugin.isXiaomi().then(res => {
        if (res.isXiaomi === true) {
          this.presentPermissionAlert();
        }
      });
    }
  }

  async presentPermissionAlert() {
    const alert = await this.alertController.create({
      header: 'Ωπ, Xiaomi εε;',
      message: 'Προκειμένου να μπορούμε να σου παρέχουμε ειδοποιήσεις νέων βαθμών σε πραγματικό χρόνο ' +
        'χρειάζεται να ενεργοποιήσεις την λειτουργία "Αυτόματη έναρξη".',
      buttons: [
        {
          text: 'ΟΧΙ ΤΩΡΑ',
          role: 'cancel'
        }, {
          text: 'ΕΝΕΡΓΟΠΟΙΗΣΗ',
          handler: () => {
            PermissionPlugin.openAutoStartPermission();
          }
        }
      ],
      backdropDismiss: false
    });

    await alert.present();
  }
}
