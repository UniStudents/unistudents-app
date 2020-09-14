import { Injectable } from '@angular/core';
import { Plugins, StatusBarStyle } from '@capacitor/core';
import { StorageService } from './storage.service';


const { StatusBar } = Plugins;

@Injectable({
  providedIn: 'root'
})
export class ThemeModeService {
  private color: string;
  darkMode = false;

  constructor(
      private storageService: StorageService
  ) { }

  init() {
    this.storageService.getThemeMode().then(mode => {
      if (mode === null) {
        this.storageService.saveThemeMode('light');
      } else if (mode.value === 'dark') {
        this.enableDarkMode(true);
      }
    });
  }

  enableDarkMode(shouldEnable) {
    document.body.classList.toggle('dark', shouldEnable);
    if (shouldEnable) {
      StatusBar.setStyle({ style: StatusBarStyle.Dark });
      this.storageService.saveThemeMode('dark');
      this.color = '#121212';
      this.darkMode = true;
    } else {
      StatusBar.setStyle({ style: StatusBarStyle.Light });
      this.storageService.saveThemeMode('light');
      this.color = '#ffffff';
      this.darkMode = false;
    }
  }
}
