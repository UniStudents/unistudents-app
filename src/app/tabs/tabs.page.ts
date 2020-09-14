import { Component } from '@angular/core';
import { StorageService } from '../shared/services/storage.service';

@Component({
  selector: 'app-tabs',
  templateUrl: 'tabs.page.html',
  styleUrls: ['tabs.page.scss']
})
export class TabsPage {

  private newGrades;

  constructor(
      private storageService: StorageService
  ) {}

  ngOnInit(): void {
    this.newGrades = this.storageService.newGrades;
  }

  newGradesExist(): boolean {
      this.newGrades = this.storageService.newGrades;
      return this.newGrades > 0;
  }
}
