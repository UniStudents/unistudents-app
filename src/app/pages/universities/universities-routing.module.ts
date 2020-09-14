import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { UniversitiesPage } from './universities.page';

const routes: Routes = [
  {
    path: '',
    component: UniversitiesPage
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class UniversitiesPageRoutingModule {}
