import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, catchError, map, Observable, of, tap} from 'rxjs';
import {environment} from '../../environments/environment';
import {RegisterRequest} from '../models/requests/register-request.model';
import {AuthResponse, TokenPair, TokenValidationResponse} from '../models/responses/auth-response.model';
import {Permission, Role, RolePermissions, User} from '../models/user.model';
import {LoginRequest} from '../models/requests/login-request.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/auth`;
  private userUrl = `${environment.apiUrl}/users`;

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  private userPermissionsSubject = new BehaviorSubject<string[]>([]);
  public userPermissions$ = this.userPermissionsSubject.asObservable();

  private authCheckedSubject = new BehaviorSubject<boolean>(false);
  public authChecked$ = this.authCheckedSubject.asObservable();

  constructor() {
    this.checkAuthStatus();
  }

  private hasToken(): boolean {
    return !!this.getAccessToken();
  }

  login(loginRequest: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, loginRequest)
      .pipe(tap(response => {
        console.log(response.tokens)
        this.setTokens(response.tokens);
        this.currentUserSubject.next(response.user);
        this.isAuthenticatedSubject.next(true);
        this.loadUserPermissions(response.user);
        this.authCheckedSubject.next(true);
      }));
  }

  register(registerRequest: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, registerRequest)
      .pipe(tap(response => {
        this.setTokens(response.tokens);
        this.currentUserSubject.next(response.user);
        this.isAuthenticatedSubject.next(true);
        this.loadUserPermissions(response.user);
        this.authCheckedSubject.next(true);
      }));
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/logout`, {})
      .pipe(tap(() => {
        this.clearAuthData();
      }), catchError((error) => {
        // Даже если запрос не удался, очищаем данные на клиенте
        this.clearAuthData();
        return of(void 0);
      }));
  }

  refreshTokens(): Observable<TokenPair> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    const refreshRequest = {refreshToken};

    return this.http.post<TokenPair>(`${this.apiUrl}/refresh`, refreshRequest)
      .pipe(tap(tokens => {
        this.setTokens(tokens);
      }), catchError(error => {
        this.clearAuthData();
        throw error;
      }));
  }

  validateToken(token: string): Observable<TokenValidationResponse> {
    const tokenRequest = {token};
    return this.http.post<TokenValidationResponse>(`${this.apiUrl}/validate`, tokenRequest);
  }

  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${this.userUrl}/me`)
      .pipe(tap(user => {
        this.currentUserSubject.next(user);
        this.loadUserPermissions(user);
      }), catchError(error => {
        this.clearAuthData();
        throw error;
      }));
  }

  getPermissions(): Observable<string[]> {
    return this.http.get<string[]>(`${this.userUrl}/permissions`)
      .pipe(tap(permissions => {
        this.userPermissionsSubject.next(permissions);
      }));
  }

  private loadUserPermissions(user: User): void {
    let permissions: string[] = [];

    user.roles.forEach(role => {
      const rolePermissions = RolePermissions[role as Role];
      if (rolePermissions) {
        permissions.push(...rolePermissions);
      }
    });

    permissions = [...new Set(permissions)];
    this.userPermissionsSubject.next(permissions);

    // Дополнительно загружаем permissions с сервера
    this.getPermissions().subscribe({
      error: () => {
        console.warn('Failed to load permissions from server, using role-based permissions');
      }
    });
  }

  private checkAuthStatus(): void {
    const token = this.getAccessToken();

    if (!token) {
      this.clearAuthData();
      this.authCheckedSubject.next(true);
      return;
    }

    this.validateToken(token).subscribe({
      next: (response) => {
        if (response.valid) {
          this.isAuthenticatedSubject.next(true);
          this.getCurrentUser().subscribe({
            next: () => {
              this.authCheckedSubject.next(true);
            }, error: () => {
              this.refreshTokens().subscribe({
                next: () => {
                  this.getCurrentUser().subscribe({
                    next: () => {
                      this.authCheckedSubject.next(true);
                    }, error: () => {
                      this.clearAuthData();
                      this.authCheckedSubject.next(true);
                    }
                  });
                }, error: () => {
                  this.clearAuthData();
                  this.authCheckedSubject.next(true);
                }
              });
            }
          });
        } else {
          this.clearAuthData();
          this.authCheckedSubject.next(true);
        }
      }, error: () => {
        this.clearAuthData();
        this.authCheckedSubject.next(true);
      }
    });
  }

  private setTokens(tokens: TokenPair): void {
    if (tokens.access_token) {
      localStorage.setItem('access_token', tokens.access_token);
    }
    if (tokens.refresh_token) {
      localStorage.setItem('refresh_token', tokens.refresh_token);
    }
  }

  getAccessToken(): string | null {
    return localStorage.getItem('access_token');
  }

  private getRefreshToken(): string | null {
    return localStorage.getItem('refresh_token');
  }

  private clearAuthData(): void {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
    this.userPermissionsSubject.next([]);
    this.authCheckedSubject.next(true);
  }

  hasPermission(permission: Permission | string): boolean {
    const currentPermissions = this.userPermissionsSubject.value;
    return currentPermissions.includes(permission);
  }

  isAdmin(): boolean {
    const user = this.currentUserSubject.value;
    return user ? user.roles.includes(Role.ROLE_ADMIN) : false;
  }

  getUserPermissions(): string[] {
    return [...this.userPermissionsSubject.value];
  }

  waitForAuthCheck(): Observable<boolean> {
    return this.authChecked$.pipe(map(checked => checked), tap(checked => {
      if (!checked) {
        console.log('Waiting for auth check...');
      }
    }));
  }
}
