import {Routes} from '@angular/router';
import {MainPage} from './pages/home/home.page';
import {SpecialOperationsPage} from './pages/special/special.page'
import {AwardPage} from './pages/award/award.page';

export const routes: Routes = [
  {path: '', component: MainPage, title: 'Band Manager - Главная'},
  {path: 'special', component: SpecialOperationsPage, title: 'Band Manager - Специальные операции'},
  {path: 'award', component: AwardPage, title: 'Band Manager - Музыкальные награды'},
  { path: '**', redirectTo: '' }
];
