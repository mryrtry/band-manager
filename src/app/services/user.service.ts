import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {User} from '../models/user.model';
import {PaginatedResponse} from '../models/paginated-response.model';
import {RegisterRequest} from '../models/requests/register-request.model';

export interface UserGetConfig {
  filter: UserFilter;
  pagination: UserPagination;
  sorting?: UserSorting;
}

export interface UserFilter {
  id?: number | null;
  username?: string | null;
  createdAt?: Date | null;
  updatedAt?: Date | null;
}

export interface UserPagination {
  page: number;
  size?: number;
}

export interface UserSorting {
  sort: string[];
  direction?: 'asc' | 'desc';
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/users`;

  getUsers(config: UserGetConfig): Observable<PaginatedResponse<User>> {
    let params = new HttpParams();

    const {filter} = config;
    Object.keys(filter).forEach(key => {
      const value = filter[key as keyof UserFilter];
      if (value !== undefined && value !== null && value !== '') {
        params = params.set(key, value.toString());
      }
    });

    const {pagination} = config;
    params = params.set('page', pagination.page.toString());
    if (pagination.size !== undefined && pagination.size !== null) {
      params = params.set('size', pagination.size.toString());
    }

    const {sorting} = config;
    if (sorting && sorting.sort && sorting.sort.length > 0) {
      sorting.sort.forEach(sortField => {
        params = params.append('sort', sortField);
      });
    }
    if (sorting && sorting.direction !== undefined && sorting.direction !== null) {
      params = params.append('direction', sorting.direction.toString());
    }

    return this.http.get<PaginatedResponse<User>>(this.apiUrl, {params});
  }

  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/me`);
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

  updateUser(id: number, userRequest: RegisterRequest): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}`, userRequest);
  }

  deleteUser(id: number): Observable<User> {
    return this.http.delete<User>(`${this.apiUrl}/${id}`);
  }

}
