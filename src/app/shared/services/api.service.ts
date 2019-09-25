import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Storage} from '@ionic/storage';
import {EnvService} from './env.service';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  public username: string;
  public password: string;

  constructor(
      private http: HttpClient,
      private storage: Storage,
      private env: EnvService
  ) { }

  getGrades() {
    return this.http.post(this.env.API_URL + '/api/grades', {
      username: this.username,
      password: this.password
    });
  }

  getInfo() {
    return this.http.post(this.env.API_URL + '/api/info', {
      username: this.username,
      password: this.password
    });
  }
}
