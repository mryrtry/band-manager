import {inject, Injectable} from '@angular/core';
import {CanActivate, Router} from '@angular/router';
import {AuthService} from '../services/auth.service';
import {combineLatest, filter, map, Observable, take} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {
  private authService = inject(AuthService);
  private router = inject(Router);

  canActivate(): Observable<boolean> {
    return combineLatest([
      this.authService.authChecked$,
      this.authService.isAuthenticated$,
      this.authService.currentUser$
    ]).pipe(
      filter(([authChecked]) => authChecked),
      take(1),
      map(([_, isAuthenticated, currentUser]) => {
        if (isAuthenticated && currentUser && this.authService.isAdmin()) {
          return true;
        } else {
          this.router.navigate(['/']).then();
          return false;
        }
      })
    );
  }
}
