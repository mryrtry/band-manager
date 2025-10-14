import { Routes } from '@angular/router';
import { MainPageComponent } from './pages/home/home.page';
import { SpecialOperationsComponent } from './pages/special/special.page'

export const routes: Routes = [
  { path: '', component: MainPageComponent, title: 'Band Manager - Главная' },
  { path: 'special', component: SpecialOperationsComponent, title: 'Band Manager - Специальные операции' },
  { path: '**', redirectTo: '' }
];
