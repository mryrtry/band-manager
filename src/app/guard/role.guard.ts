import {CanActivateFn, Router} from '@angular/router';
import {inject} from '@angular/core';
import {catchError, map, of} from 'rxjs';
import {AuthService} from '../services/auth/auth.service';
import {UserService} from '../services/auth/user.service';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const userService = inject(UserService);
  const router = inject(Router);
  const requiredRoles = route.data['roles'] as string[];
  if (!authService.isAuthenticated()) {
    router.navigate(['/login']);
    return false;
  }
  return userService.getCurrentUser().pipe(map(user => {
    const userRoles = user.roles.map(role => role.toString());
    if (requiredRoles && !requiredRoles.some(role => userRoles.includes(role))) {
      router.navigate(['/access-denied'], {
        state: {
          returnUrl: state.url,
          requiredRole: requiredRoles,
        }
      }).then();
      return false;
    }
    return true;
  }), catchError(() => {
    router.navigate(['/login']);
    return of(false);
  }));
};
