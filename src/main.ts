import {bootstrapApplication} from '@angular/platform-browser';
import {AppComponent} from './app/app';
import {provideRouter} from '@angular/router';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {routes} from './app/router/app.routes';
import {provideAnimations} from '@angular/platform-browser/animations';
import {providePrimeNG} from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import {definePreset} from '@primeuix/themes';
import {ConfirmationService, MessageService} from 'primeng/api';
import {jwtInterceptor} from './app/interceptor/jwt.interceptor';

//todo: История импорта
//todo: Таблица пользователей (admin)
//todo: Фикс ошибок в транзакциях с бэкенда

const MyPreset = definePreset(Aura, {
  semantic: {
  }
});

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes), provideHttpClient(withInterceptors([jwtInterceptor])),
    provideAnimations(),
    providePrimeNG({
      theme: {
        preset: MyPreset,
        options: {
          darkModeSelector: '.my-app-dark'
        }
      }
    }),
    MessageService,
    ConfirmationService
  ]
}).catch(err => console.error(err));
