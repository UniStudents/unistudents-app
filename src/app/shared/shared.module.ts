import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {GenericHttpService} from './services/generic-http.service';
import {HttpClientModule} from '@angular/common/http';
import {NetworkService} from './services/network.service';

@NgModule({
  declarations: [],
  imports: [
    CommonModule
  ],
  exports: [
    HttpClientModule
  ],
  providers: [
      GenericHttpService,
      NetworkService
  ]
})
export class SharedModule { }
