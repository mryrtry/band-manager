import {inject, Injectable} from '@angular/core';
import {CanActivate, Router} from '@angular/router';
import {AuthService} from '../services/auth.service';
import {Observable, combineLatest, filter, map, take} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  private authService = inject(AuthService);
  private router = inject(Router);

  canActivate(): Observable<boolean> {
    // Комбинируем: ждём пока завершится checkAuthStatus, потом смотрим isAuthenticated
    return combineLatest([
      this.authService.authChecked$,
      this.authService.isAuthenticated$
    ]).pipe(
      // ждём пока authChecked === true
      filter(([authChecked]) => authChecked),
      take(1),
      map(([_, isAuthenticated]) => {
        if (isAuthenticated) {
          return true;
        } else {
          this.router.navigate(['/login']).then();
          return false;
        }
      })
    );
  }
}
