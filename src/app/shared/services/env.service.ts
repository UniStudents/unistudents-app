import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class EnvService {

  API_URL = 'https://unipi-students-api.herokuapp.com';

  constructor() { }
}
