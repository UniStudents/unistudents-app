import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {LoginForm} from '../models/login-form.model';
import {Observable} from 'rxjs';
import {GradeResults} from '../models/grade-results.model';
import {catchError} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class GenericHttpService {

  constructor(private http: HttpClient) { }

  apiUrl = 'http://192.168.3.116:8080/api/grades';

  loginForm: LoginForm = {
    username: 'e16130',
    password: '21aug1998nikos$'
  };

  getGradeResults(): Observable<GradeResults> {
    return this.http.post<GradeResults>(this.apiUrl, this.loginForm);
  }
}
