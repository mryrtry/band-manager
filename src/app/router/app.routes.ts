import {Routes} from '@angular/router';
import {MainPage} from '../pages/main/main.page';
import {authGuard} from '../guard/auth.guard';
import {AccessDeniedPage} from '../pages/access-denied/access-denied.page';
import {AuthComponent} from '../components/auth/auth.component';

export const routes: Routes = [
  {
    path: '',
    component: MainPage,
    title: 'Band Manager - Главная',
    canActivate: [authGuard]
  },
  {
    path: 'auth',
    component: AuthComponent,
    title: 'Band Manager - Авторизация',
  }, {
    path: 'access-denied',
    component: AccessDeniedPage,
    title: 'Band Manager - Доступ запрещен'
  },
  { path: '**', redirectTo: '' }
];
