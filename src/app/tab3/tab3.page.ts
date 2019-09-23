import {Component, OnInit} from '@angular/core';
import {Student} from '../shared/models/student.model';
import {StorageService} from '../shared/services/storage.service';

@Component({
  selector: 'app-tab3',
  templateUrl: 'tab3.page.html',
  styleUrls: ['tab3.page.scss']
})
export class Tab3Page implements OnInit {

  public student: Student;

  constructor(
      private storageService: StorageService
  ) {}

  ngOnInit(): void {
    this.loadStudentInfo();
  }

  loadStudentInfo() {
    this.storageService.getStudent().then((student) => {
      this.student = student;
    });
  }

}
