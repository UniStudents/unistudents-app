import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class EnvService {

  API_URL = 'https://unistudents.herokuapp.com';

  constructor() { }
}
