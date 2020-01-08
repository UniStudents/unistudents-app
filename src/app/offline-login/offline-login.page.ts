import { Component, OnInit } from '@angular/core';
import {Router} from '@angular/router';
import {AuthService} from '../shared/services/auth.service';
import {StorageService} from '../shared/services/storage.service';
import {Student} from '../shared/models/student.model';

@Component({
  selector: 'app-offline-login',
  templateUrl: './offline-login.page.html',
  styleUrls: ['./offline-login.page.scss'],
})
export class OfflineLoginPage implements OnInit {

  userAem = '';

  constructor(
      private router: Router,
      private authService: AuthService,
      private storageService: StorageService
  ) { }

  ngOnInit() {
    this.storageService.getStudent().then((student: Student) => {
      if (student != null) {
        this.userAem = student.info.aem;
      }
    });
  }

  proceedOffline() {
    this.authService.isLoggedIn = true;
    this.router.navigate(['/app/tabs/tab1']);
  }
}
