import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {GenericHttpService} from './services/generic-http.service';
import {HttpClientModule} from '@angular/common/http';
import {NetworkService} from './services/network.service';
import {StorageService} from './services/storage.service';

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
      NetworkService,
      StorageService
  ]
})
export class SharedModule { }
