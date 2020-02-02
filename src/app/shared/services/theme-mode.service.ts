import { Injectable } from '@angular/core';
import { StatusBar } from '@ionic-native/status-bar/ngx';
import { StorageService } from './storage.service';

@Injectable({
  providedIn: 'root'
})
export class ThemeModeService {
  private color: string;

  constructor(
      private statusBar: StatusBar,
      private storageService: StorageService
  ) { }

  enableDarkMode(shouldEnable) {
    if (shouldEnable) {
      this.statusBar.styleBlackTranslucent();
      this.storageService.saveThemeMode('dark');
      this.color = '#121212';
    } else {
      this.statusBar.styleDefault();
      this.storageService.saveThemeMode('light');
      this.color = '#ffffff';
    }
    document.body.classList.toggle('dark', shouldEnable);
    this.statusBar.backgroundColorByHexString(this.color);
  }
}
