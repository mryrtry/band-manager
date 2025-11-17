import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../environments/environment';
import {BehaviorSubject, Observable, tap} from 'rxjs';
import {UserRequest} from '../model/auth/request/user.request';
import {LoginResponse} from '../model/auth/response/login.response';
import {Tokens} from '../model/auth/response/tokens.response';
import {LoginRequest} from '../model/auth/request/login.request';
import {TokenRequest} from '../model/auth/request/token.request';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/auth`;
  private tokenSubject = new BehaviorSubject<string | null>(this.getAccessToken());
  public token$ = this.tokenSubject.asObservable();

  register(userData: UserRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/register`, userData).pipe(
      tap(response => this.setTokens(response.tokens)),
    );
  }

  login(loginData: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, loginData).pipe(
      tap(response => this.setTokens(response.tokens)),
    );
  }

  refreshToken(): Observable<Tokens> {
    const refreshToken = this.getRefreshToken();
    return this.http.post<Tokens>(`${this.apiUrl}/refresh`, { token: refreshToken }).pipe(
      tap(tokens => this.setTokens(tokens))
    );
  }

  logout(): Observable<void> {
    const response: Observable<void> = this.http.post<void>(`${this.apiUrl}/logout`, {}).pipe(
      tap(() => this.clearTokens())
    );
    this.clearTokens();
    return response;
  }

  validateToken(request: TokenRequest): Observable<{ valid: boolean; username: string }> {
    return this.http.post<{ valid: boolean; username: string }>(
      `${this.apiUrl}/validate`,
      { token: request.token },
    );
  }

  private setTokens(tokens: Tokens): void {
    localStorage.setItem('accessToken', tokens.accessToken);
    localStorage.setItem('refreshToken', tokens.refreshToken);
    this.tokenSubject.next(tokens.accessToken);
  }

  getAccessToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  getRefreshToken(): string | null {
    return localStorage.getItem('refreshToken');
  }

  public clearTokens(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    this.tokenSubject.next(null);
  }

  isAuthenticated(): boolean {
    return !!this.getAccessToken();
  }

  get authState$(): Observable<string | null> {
    return this.tokenSubject.asObservable();
  }

}
