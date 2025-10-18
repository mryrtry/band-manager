import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Coordinates} from '../models/coordinates.model';
import {CoordinatesRequest} from '../models/requests/coordinates-request.model';

@Injectable({
  providedIn: 'root'
})
export class CoordinatesService {
  private baseUrl = '/coordinates';

  constructor(private http: HttpClient) {
  }

  getAll(): Observable<Coordinates[]> {
    return this.http.get<Coordinates[]>(this.baseUrl);
  }

  get(id: number): Observable<Coordinates> {
    return this.http.get<Coordinates>(`${this.baseUrl}/${id}`);
  }

  create(request: CoordinatesRequest): Observable<Coordinates> {
    return this.http.post<Coordinates>(this.baseUrl, request);
  }

  update(id: number, request: CoordinatesRequest): Observable<Coordinates> {
    return this.http.put<Coordinates>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<Coordinates> {
    return this.http.delete<Coordinates>(`${this.baseUrl}/${id}`);
  }

}
