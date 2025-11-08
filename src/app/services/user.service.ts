import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {
  BehaviorSubject,
  catchError,
  filter,
  Observable,
  of,
  switchMap,
  tap,
  throwError
} from 'rxjs';
import {environment} from '../../environments/environment';
import {Role, User} from '../model/user/user.model';
import {PageableRequest} from '../model/pageable-request.model';
import {Page} from '../model/page.model';
import {UserFilter} from '../model/user/user-filter.model';
import {RoleRequest} from '../model/user/request/role.request';
import {UserRequest} from '../model/auth/request/user.request';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/users`;

  private currentUserCache: User | null = null;
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  private currentUserLoadingSubject = new BehaviorSubject<boolean>(false);
  public currentUserLoading$ = this.currentUserLoadingSubject.asObservable();

  getAllUsers(filter: UserFilter = {}, config: PageableRequest = {}): Observable<Page<User>> {
    let params = new HttpParams();
    if (filter.username) params = params.set('username', filter.username);
    if (filter.createdAtAfter) params = params.set('createdAtAfter', filter.createdAtAfter);
    if (filter.createdAtBefore) params = params.set('createdAtBefore', filter.createdAtBefore);
    if (filter.updatedAtAfter) params = params.set('updatedAtAfter', filter.updatedAtAfter);
    if (filter.updatedAtBefore) params = params.set('updatedAtBefore', filter.updatedAtBefore);
    if (config.page) params = params.set('page', config.page.toString());
    if (config.size) params = params.set('size', config.size.toString());
    if (config.sort) {
      config.sort.forEach(sortField => {
        params = params.append('sort', sortField);
      });
    }
    if (config.direction) params = params.set('direction', config.direction);
    return this.http.get<Page<User>>(this.apiUrl, {params});
  }

  getCurrentUser(forceRefresh: boolean = false): Observable<User> {
    if (this.currentUserCache && !forceRefresh) {
      return of(this.currentUserCache);
    }
    if (this.currentUserLoadingSubject.value && !forceRefresh) {
      return this.currentUser$.pipe(
        filter(user => user !== null),
        switchMap(user => user ? of(user) : this.fetchCurrentUser())
      );
    }

    return this.fetchCurrentUser();
  }

  private fetchCurrentUser(): Observable<User> {
    this.currentUserLoadingSubject.next(true);

    return this.http.get<User>(`${this.apiUrl}/me`).pipe(
      tap(user => {
        this.currentUserCache = user;
        this.currentUserSubject.next(user);
        this.currentUserLoadingSubject.next(false);
      }),
      catchError(error => {
        this.currentUserLoadingSubject.next(false);
        this.currentUserCache = null;
        this.currentUserSubject.next(null);
        console.error('Failed to fetch current user:', error);
        return throwError(() => error);
      })
    );
  }

  getCurrentUserSync(): User | null {
    return this.currentUserCache;
  }

  refreshCurrentUser(): Observable<User> {
    this.currentUserCache = null;
    return this.getCurrentUser(true);
  }

  clearCurrentUserCache(): void {
    this.currentUserCache = null;
    this.currentUserSubject.next(null);
    this.currentUserLoadingSubject.next(false);
  }

  getPermissions(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/permissions`);
  }

  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`);
  }

  getUserByUsername(username: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/username/${username}`);
  }

  updateUser(id: number, request: UserRequest): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}`, request).pipe(
      tap(updatedUser => {
        // Если обновили текущего пользователя - обновляем кэш
        if (this.currentUserCache && this.currentUserCache.id === id) {
          this.currentUserCache = updatedUser;
          this.currentUserSubject.next(updatedUser);
        }
      })
    );
  }

  updateUserRoles(id: number, request: RoleRequest): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}/roles`, request).pipe(
      tap(updatedUser => {
        if (this.currentUserCache && this.currentUserCache.id === id) {
          this.currentUserCache = updatedUser;
          this.currentUserSubject.next(updatedUser);
        }
      })
    );
  }

  deleteUser(id: number): Observable<User> {
    return this.http.delete<User>(`${this.apiUrl}/${id}`).pipe(
      tap(() => {
        if (this.currentUserCache && this.currentUserCache.id === id) {
          this.clearCurrentUserCache();
        }
      })
    );
  }

  getUserRoles(userId: number): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/${userId}/roles`);
  }

  hasRole(user: User, role: Role): boolean {
    return user.roles.includes(role);
  }
}
