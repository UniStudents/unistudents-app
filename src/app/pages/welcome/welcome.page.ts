import { Component, OnInit } from '@angular/core';
import { RoutingService } from '../../shared/services/routing.service';

@Component({
  selector: 'app-welcome',
  templateUrl: './welcome.page.html',
  styleUrls: ['./welcome.page.scss'],
})
export class WelcomePage implements OnInit {

  constructor(
    private routingService: RoutingService
  ) { }

  ngOnInit() {
  }

  ionViewWillEnter() {
    this.routingService.currentPage = '/welcome';
  }

}
