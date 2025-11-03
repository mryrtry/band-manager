// app.routes.ts
import {Routes} from '@angular/router';
import {MainPage} from './pages/home/home.page';
import {SpecialOperationsPage} from './pages/special/special.page';
import {AwardPage} from './pages/award/award.page';
import {LoginComponent} from './components/login/login.component';
import {RegisterComponent} from './components/register/register.component';
import {GuestGuard} from './guard/guest.guard';
import {AuthGuard} from './guard/auth.guard';
import {AdminGuard} from './guard/admin.guard';
import {UsersPage} from './pages/users/users.page';
import {ImportHistoryPage} from './pages/import-history/import-history.page';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    title: 'Band Manager - Вход',
    canActivate: [GuestGuard]
  },
  {
    path: 'register',
    component: RegisterComponent,
    title: 'Band Manager - Регистрация',
    canActivate: [GuestGuard]
  },
  {
    path: '',
    component: MainPage,
    title: 'Band Manager - Главная',
    canActivate: [AuthGuard]
  },
  {
    path: 'special',
    component: SpecialOperationsPage,
    title: 'Band Manager - Специальные операции',
    canActivate: [AuthGuard]
  },
  {
    path: 'award',
    component: AwardPage,
    title: 'Band Manager - Музыкальные награды',
    canActivate: [AuthGuard]
  },
  {
    path: 'users',
    component: UsersPage,
    title: 'Band Manager - Управление пользователями',
    canActivate: [AuthGuard, AdminGuard]
  },
  {
    path: 'import-history',
    component: ImportHistoryPage,
    title: 'Band Manager - История импорта',
    canActivate: [AuthGuard]
  },
  { path: '**', redirectTo: '' }
];
