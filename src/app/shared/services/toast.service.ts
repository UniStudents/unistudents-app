import { Injectable } from '@angular/core';
import { AnimationController, Platform, ToastController } from '@ionic/angular';

@Injectable({
  providedIn: 'root'
})
export class ToastService {

  constructor(
      private toastController: ToastController,
      private animationCtrl: AnimationController,
      private platform: Platform
  ) { }

  async presentSimple(msg: string, dur = 2000) {
    const toast = await this.toastController.create({
      message: msg,
      duration: dur,
      mode: 'ios'
    });
    await toast.present();
  }

  async present(msg: string, dur = 2000) {

    if (this.platform.is('ios')) {
      this.presentSimple(msg, dur);
      return;
    }

    const leave = (baseEl: any) => {
      const bottom = `calc(-0px - var(--ion-safe-area-bottom, 0px))`;
      const animation = this.animationCtrl.create()
        // tslint:disable-next-line:no-non-null-assertion
        .addElement(baseEl.querySelector('.toast-wrapper')!)
        .fromTo('transform', `translateY(${bottom})`, 'translateY(100%');

      return this.animationCtrl.create()
        .addElement(baseEl)
        .easing('cubic-bezier(.36,.66,.04,1)')
        .duration(300)
        .addAnimation(animation);
    };

    const enter = (baseEl: any) => {
      const bottom = `calc(-0px - var(--ion-safe-area-bottom, 0px))`;
      const animation = this.animationCtrl.create()
        // tslint:disable-next-line:no-non-null-assertion
        .addElement(baseEl.querySelector('.toast-wrapper')!)
        .fromTo('transform', 'translateY(100%)', `translateY(${bottom})`);

      return this.animationCtrl.create()
        .addElement(baseEl)
        .easing('cubic-bezier(.36,.66,.04,1)')
        .duration(300)
        .addAnimation(animation);
    };

    const toast = await this.toastController.create({
      message: msg,
      duration: dur,
      cssClass: 'custom-toast',
      mode: 'ios',
      leaveAnimation: ((baseEl, opts) => leave(baseEl)),
      enterAnimation: ((baseEl, opts) => (enter(baseEl)))
    });
    await toast.present();
  }
}
