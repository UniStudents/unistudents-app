import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { EnvService } from './env.service';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  fetchedData = false;

  constructor(
      private http: HttpClient,
      private env: EnvService,
  ) { }

  fetchStudent(university: string, username: string, password: string) {
    return this.http.post(this.env.API_URL + '/api/student/' + university, {
      username: username,
      password: password
    });
  }
}
