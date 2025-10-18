import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Album} from '../models/album.model';
import {AlbumRequest} from '../models/requests/album-request.model';

@Injectable({
  providedIn: 'root'
})
export class AlbumService {
  private baseUrl = '/albums';

  constructor(private http: HttpClient) {
  }

  getAll(): Observable<Album[]> {
    return this.http.get<Album[]>(this.baseUrl);
  }

  get(id: number): Observable<Album> {
    return this.http.get<Album>(`${this.baseUrl}/${id}`);
  }

  create(request: AlbumRequest): Observable<Album> {
    return this.http.post<Album>(this.baseUrl, request);
  }

  update(id: number, request: AlbumRequest): Observable<Album> {
    return this.http.put<Album>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<Album> {
    return this.http.delete<Album>(`${this.baseUrl}/${id}`);
  }

}
