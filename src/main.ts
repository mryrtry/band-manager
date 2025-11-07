import {bootstrapApplication} from '@angular/platform-browser';
import {AppComponent} from './app/app';
import {provideRouter} from '@angular/router';
import {provideHttpClient} from '@angular/common/http';
import {routes} from './app/router/app.routes';
import {provideAnimations} from '@angular/platform-browser/animations';
import {providePrimeNG} from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import {definePreset} from '@primeuix/themes';
import {MessageService} from 'primeng/api';

const MyPreset = definePreset(Aura, {
  semantic: {
  }
});

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(),
    provideAnimations(),
    providePrimeNG({
      theme: {
        preset: MyPreset,
        options: {
          darkModeSelector: '.my-app-dark'
        }
      }
    }),
    MessageService
  ]
}).catch(err => console.error(err));
