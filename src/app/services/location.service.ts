import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Location} from '../models/location.model';
import {LocationRequest} from '../models/requests/location-request.model';

@Injectable({
  providedIn: 'root'
})
export class LocationService {
  private baseUrl = '/locations';

  constructor(private http: HttpClient) {
  }

  getAll(): Observable<Location[]> {
    return this.http.get<Location[]>(this.baseUrl);
  }

  get(id: number): Observable<Location> {
    return this.http.get<Location>(`${this.baseUrl}/${id}`);
  }

  create(request: LocationRequest): Observable<Location> {
    return this.http.post<Location>(this.baseUrl, request);
  }

  update(id: number, request: LocationRequest): Observable<Location> {
    return this.http.put<Location>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<Location> {
    return this.http.delete<Location>(`${this.baseUrl}/${id}`);
  }

}
