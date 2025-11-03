// interceptors/auth.interceptor.ts (расширенная версия)
import {HttpErrorResponse, HttpInterceptorFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {BehaviorSubject, Observable, throwError} from 'rxjs';
import {catchError, filter, switchMap, take} from 'rxjs/operators';
import {AuthService} from '../services/auth.service';
import {environment} from '../../environments/environment';

let isRefreshing = false;
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  if (req.url.startsWith(`${environment.apiUrl}/auth/`)) {
    return next(req);
  }

  const accessToken = authService.getAccessToken();
  if (accessToken) {
    req = addToken(req, accessToken);
  }

  return next(req).pipe(
    catchError(error => {
      if (error instanceof HttpErrorResponse && error.status === 401 && !req.url.includes('/auth/refresh')) {
        return handle401Error(req, next, authService);
      }
      return throwError(() => error);
    })
  );
};

const addToken = (request: any, token: string): any => {
  return request.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });
};

const handle401Error = (request: any, next: any, authService: AuthService): Observable<any> => {
  if (!isRefreshing) {
    isRefreshing = true;
    refreshTokenSubject.next(null);

    return authService.refreshTokens().pipe(
      switchMap((tokens: any) => {
        isRefreshing = false;
        refreshTokenSubject.next(tokens.accessToken);
        return next(addToken(request, tokens.accessToken));
      }),
      catchError((error) => {
        isRefreshing = false;
        authService.logout().subscribe();
        return throwError(() => error);
      })
    );
  } else {
    return refreshTokenSubject.pipe(
      filter(token => token !== null),
      take(1),
      switchMap(token => {
        return next(addToken(request, token!));
      })
    );
  }
};
