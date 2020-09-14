import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { UniversityService } from '../../shared/services/university.service';
import { RoutingService } from '../../shared/services/routing.service';
import { AlertController, Platform } from '@ionic/angular';
import { UpdateService } from '../../shared/services/update.service';

@Component({
  selector: 'app-universities',
  templateUrl: './universities.page.html',
  styleUrls: ['./universities.page.scss'],
})
export class UniversitiesPage implements OnInit {

  isAndroid: boolean;

  constructor(
    private router: Router,
    private universityService: UniversityService,
    private routingService: RoutingService,
    private platform: Platform,
    private updateService: UpdateService,
    private alertController: AlertController
  ) { }

  ngOnInit() {
    this.isAndroid = this.platform.is('android');
    if (this.updateService.isFirstTime) {
      setTimeout(() => {
        this.presentAlert();
      }, 500);
    }
  }

  ionViewWillEnter() {
    this.routingService.currentPage = '/universities';
  }

  action(uni: string) {
    this.universityService.uni = uni;
    this.router.navigateByUrl('/login');
  }

  async presentAlert() {
    const alert = await this.alertController.create({
      header: 'Σημαντική Ενημέρωση',
      message: 'Τα ιδρύματα <b>ΕΚΠΑ</b>, <b>ΠΑΔΑ</b> και <b>ΠΑΝΤΕΙΟ</b> βρίσκονται σε δοκιμαστική φάση. ' +
        'Σε περίπτωση που αντιμετωπίσεις κάποιο πρόβλημα παρακαλούμε να επικοινωνήσεις μαζί μας στο info@unistudents.gr',
      buttons: ['ΤΟ ΚΑΤΑΛΑΒΑ'],
      backdropDismiss: false
    });

    await alert.present();
  }
}
