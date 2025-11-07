import {Routes} from '@angular/router';
import {MainPage} from '../pages/main/main.page';
import {LoginComponent} from '../components/auth/login/login.component';

export const routes: Routes = [
  {
    path: '',
    component: MainPage,
    title: 'Band Manager - Главная',
  },
  {
    path: 'login',
    component: LoginComponent,
    title: 'Band Manager - Логин',
  },
  { path: '**', redirectTo: '' }
];
