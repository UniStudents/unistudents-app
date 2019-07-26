import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {LoginForm} from '../models/login-form.model';

@Injectable({
  providedIn: 'root'
})
export class GenericHttpService {

  constructor(private http: HttpClient) { }

  apiUrl = 'http://192.168.3.116:8080/api/grades';

  loginForm: LoginForm = {
    username: 'username',
    password: 'password'
  };

  getGradeResults() {
    return this.http.post(this.apiUrl, this.loginForm);
  }
}
