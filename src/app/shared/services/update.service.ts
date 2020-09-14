import { Injectable } from '@angular/core';
import { StorageService } from './storage.service';
import { StoreService } from './store.service';

@Injectable({
  providedIn: 'root'
})
export class UpdateService {
  isFirstTime: boolean;

  constructor(
    private storageService: StorageService,
    private storeService: StoreService
  ) { }

  checkForUpdates() {
    return this.storageService.getFirstTime().then(firstTime => {
      if (firstTime.value === null) {
        this.storeService.logout();
        this.isFirstTime = true;
      } else {
        this.isFirstTime = false;
      }
    });
  }
}
