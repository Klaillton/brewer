import { Routes } from '@angular/router';
import { CervejaListComponent } from './components/cerveja/cerveja-list.component';

export const routes: Routes = [
  { path: '', redirectTo: '/cervejas', pathMatch: 'full' },
  { path: 'cervejas', component: CervejaListComponent }
];
