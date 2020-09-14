import { Injectable } from '@angular/core';
import { StorageService } from './storage.service';
import { CryptoService } from './crypto.service';

@Injectable({
  providedIn: 'root'
})
export class StudentService {

  private _username: string;
  private _password: string;
  private _rememberMe: boolean;
  private _isLoggedIn: boolean;
  private _notificationsForNewGrades: boolean;
  private _notificationsForGeneralUpdates: boolean;

  constructor(
    private storageService: StorageService,
    private cryptoService: CryptoService
  ) { }

  async init() {
    const username = await this.storageService.getUsername();
    this.username = username.value;

    const password = await this.storageService.getPassword();
    if (password.value !== null) {
      this.password = this.cryptoService.decrypt(password.value);
    }

    const rememberMe = await this.storageService.getRememberMe();
    if (rememberMe.value === null) {
      this.rememberMe = null;
      this.isLoggedIn = false;
      this.notificationsForNewGrades = false;
    } else if (rememberMe.value === 'false') {
      this.rememberMe = false;
      this.isLoggedIn = false;
      this.notificationsForNewGrades = false;
    } else if (rememberMe.value === 'true') {
      this.rememberMe = true;
      this.isLoggedIn = true;
      const notificationsForNewGrades = await this.storageService.getNewGradeNotification();
      if (notificationsForNewGrades.value === 'true') {
        this.notificationsForNewGrades = true;
      } else if (notificationsForNewGrades.value === 'false') {
        this.notificationsForNewGrades = false;
      }
    }

    const notificationsForGeneralUpdates = await this.storageService.getUpdatesNotification();
    if (notificationsForGeneralUpdates.value === null) {
      this.notificationsForGeneralUpdates = true;
    } else if (notificationsForGeneralUpdates.value === 'true') {
      this.notificationsForGeneralUpdates = true;
    } else if (notificationsForGeneralUpdates.value === 'false') {
      this.notificationsForGeneralUpdates = false;
    }
  }

  get username(): string {
    return this._username;
  }

  set username(value: string) {
    this._username = value;
  }

  get password(): string {
    return this._password;
  }

  set password(value: string) {
    this._password = value;
  }

  get isLoggedIn(): boolean {
    return this._isLoggedIn;
  }

  set isLoggedIn(value: boolean) {
    this._isLoggedIn = value;
  }

  get rememberMe(): boolean {
    return this._rememberMe;
  }

  set rememberMe(value: boolean) {
    this._rememberMe = value;
  }

  get notificationsForNewGrades(): boolean {
    return this._notificationsForNewGrades;
  }

  set notificationsForNewGrades(value: boolean) {
    this._notificationsForNewGrades = value;
  }

  get notificationsForGeneralUpdates(): boolean {
    return this._notificationsForGeneralUpdates;
  }

  set notificationsForGeneralUpdates(value: boolean) {
    this._notificationsForGeneralUpdates = value;
  }
}
