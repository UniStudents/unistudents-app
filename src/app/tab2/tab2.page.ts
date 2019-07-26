import {Component, OnInit} from '@angular/core';
import {GenericHttpService} from '../shared/services/generic-http.service';
import {Observable} from 'rxjs';
import {GradeResults} from '../shared/models/grade-results.model';

@Component({
  selector: 'app-tab2',
  templateUrl: 'tab2.page.html',
  styleUrls: ['tab2.page.scss']
})
export class Tab2Page implements OnInit {

  public grades: GradeResults;

  constructor(private service: GenericHttpService) {}

  ngOnInit(): void {

    this.service.getGradeResults().subscribe(
        data => {
          this.grades = data;
        },
        error => {
          console.log(error);
        }
    );
  }
}
