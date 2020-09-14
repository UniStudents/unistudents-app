import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { UniversitiesPageRoutingModule } from './universities-routing.module';

import { UniversitiesPage } from './universities.page';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    UniversitiesPageRoutingModule
  ],
  declarations: [UniversitiesPage]
})
export class UniversitiesPageModule {}
