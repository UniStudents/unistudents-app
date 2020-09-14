import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { StorageService } from '../shared/services/storage.service';
import { StudentService } from '../shared/services/student.service';
import { RoutingService } from '../shared/services/routing.service';

@Component({
  selector: 'app-offline-login',
  templateUrl: './offline-login.page.html',
  styleUrls: ['./offline-login.page.scss'],
})
export class OfflineLoginPage implements OnInit {

  userAem = '';

  constructor(
      private router: Router,
      private storageService: StorageService,
      private studentService: StudentService,
      private routingService: RoutingService
  ) { }

  async ngOnInit() {
    const rememberMe = this.studentService.rememberMe;
    if (rememberMe === true) {
      const stud = await this.storageService.getStudent();
      if (stud.value !== null) {
        const student = JSON.parse(stud.value);
        this.userAem = student.info.aem;
      }
    }
  }

  ionViewWillEnter() {
    this.routingService.currentPage = '/offline-login';
  }

  proceedOffline() {
    this.studentService.isLoggedIn = true;
    this.router.navigate(['/app/tabs/tab1']);
  }
}
