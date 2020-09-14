import { Injectable } from '@angular/core';
import { StorageService } from './storage.service';

@Injectable({
  providedIn: 'root'
})
export class UniversityService {

  private _uni: string;
  private _uniLogo: string;
  private _uniForgotMyPasswordLink: string;

  constructor(
    private storageService: StorageService
  ) { }

  async init() {
    const university = await this.storageService.getUniversity();
    if (university.value !== null) {
      this.uni = university.value;
    }
  }

  get uni() {
    return this._uni;
  }

  set uni(uni: string) {
    this._uni = uni;
    this.uniLogo = uni;
    this.uniForgotMyPasswordLink = uni;
  }

  get uniLogo() {
    return this._uniLogo;
  }

  set uniLogo(uni: string) {
    switch (uni) {
      case 'UNIPI':
        this._uniLogo = '/assets/unipi-logo.png';
        break;
      case 'UNIWA':
        this._uniLogo = '/assets/uniwa-logo.png';
        break;
      case 'UOA':
        this._uniLogo = '/assets/ekpa-logo.png';
        break;
      case 'PANTEION':
        this._uniLogo = '/assets/panteion-logo.png';
        break;
    }
  }

  get uniForgotMyPasswordLink() {
    return this._uniForgotMyPasswordLink;
  }

  set uniForgotMyPasswordLink(uni: string) {
    switch (uni) {
      case 'UNIPI':
        this._uniForgotMyPasswordLink = 'https://mypassword.unipi.gr';
        break;
      case 'UNIWA':
        this._uniForgotMyPasswordLink = 'https://my.uniwa.gr/recovery.php';
        break;
      case 'UOA':
        this._uniForgotMyPasswordLink = 'http://www.noc.uoa.gr/ypostiri3h-xrhston/syxnes-erwtiseis.html#forgottenpassword';
        break;
      case 'PANTEION':
        this._uniForgotMyPasswordLink = 'https://mypassword.panteion.gr/';
        break;
    }
  }
}
