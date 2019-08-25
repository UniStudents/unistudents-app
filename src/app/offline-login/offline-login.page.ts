import { Component, OnInit } from '@angular/core';
import {Router} from '@angular/router';
import {AuthService} from '../shared/services/auth.service';

@Component({
  selector: 'app-offline-login',
  templateUrl: './offline-login.page.html',
  styleUrls: ['./offline-login.page.scss'],
})
export class OfflineLoginPage implements OnInit {

  constructor(
      private router: Router,
      private authService: AuthService,
  ) { }

  ngOnInit() {
  }

  proceedOffline() {
    this.authService.isLoggedIn = true;
    this.router.navigate(['/app/tabs/tab1']);
  }
}
