import {Routes} from '@angular/router';
import {LoginComponent} from '../components/auth/login/login.component';

export const routes: Routes = [
  {
    path: '',
    component: LoginComponent,
    title: 'Band Manager - Главная',
  },
  { path: '**', redirectTo: '' }
];
