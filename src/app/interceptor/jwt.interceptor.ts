import {
  HttpErrorResponse,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpRequest
} from '@angular/common/http';
import {inject} from '@angular/core';
import {
  BehaviorSubject,
  catchError,
  filter,
  switchMap,
  take,
  throwError
} from 'rxjs';
import {AuthService} from '../services/auth/auth.service';
import {Router} from '@angular/router';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (shouldIntercept(req.url)) {
    const accessToken = authService.getAccessToken();
    if (accessToken) {
      req = addToken(req, accessToken);
    }
    return next(req).pipe(
      catchError((error) => {
        if (error instanceof HttpErrorResponse && error.status === 401) {
          return handle401Error(req, next, authService, router);
        }
        return throwError(() => error);
      })
    );
  }
  return next(req);
};

const excludedPaths = [
  '/auth/login',
  '/auth/register',
  '/auth/refresh',
];

const shouldIntercept = (url: string): boolean => {
  const isExcluded = excludedPaths.some(path => url.includes(path));
  return !isExcluded;
};

const addToken = (request: HttpRequest<unknown>, token: string): HttpRequest<unknown> => {
  return request.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });
};

let isRefreshing = false;
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

const handle401Error = (
  request: HttpRequest<unknown>,
  next: HttpHandlerFn,
  authService: AuthService,
  router: Router
) => {
  if (!isRefreshing) {
    isRefreshing = true;
    refreshTokenSubject.next(null);
    const refreshToken = authService.getRefreshToken();
    if (refreshToken) {
      return authService.refreshToken().pipe(
        switchMap((tokens) => {
          isRefreshing = false;
          refreshTokenSubject.next(tokens.accessToken);
          return next(addToken(request, tokens.accessToken));
        }),
        catchError((error) => {
          isRefreshing = false;
          authService.logout();
          router.navigate(['/login']).then();
          return throwError(() => error);
        })
      );
    } else {
      isRefreshing = false;
      authService.logout();
      router.navigate(['/login']).then();
      return throwError(() => new Error('No refresh token'));
    }
  } else {
    return refreshTokenSubject.pipe(
      filter(token => token !== null),
      take(1),
      switchMap(token => next(addToken(request, token!)))
    );
  }
};
