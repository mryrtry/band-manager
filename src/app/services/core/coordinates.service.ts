import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {environment} from '../../../environments/environment';
import {Coordinates} from '../../model/core/coordinates/coordinates.model';
import {
  CoordinatesRequest
} from '../../model/core/coordinates/coordinates.request';

@Injectable({
  providedIn: 'root'
})
export class CoordinatesService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/coordinates`;

  getAllCoordinates(): Observable<Coordinates[]> {
    return this.http.get<Coordinates[]>(this.apiUrl);
  }

  getCoordinates(id: number): Observable<Coordinates> {
    return this.http.get<Coordinates>(`${this.apiUrl}/${id}`);
  }

  createCoordinates(request: CoordinatesRequest): Observable<Coordinates> {
    return this.http.post<Coordinates>(this.apiUrl, request);
  }

  updateCoordinates(id: number, request: CoordinatesRequest): Observable<Coordinates> {
    return this.http.put<Coordinates>(`${this.apiUrl}/${id}`, request);
  }

  deleteCoordinates(id: number): Observable<Coordinates> {
    return this.http.delete<Coordinates>(`${this.apiUrl}/${id}`);
  }

  exportToCsv(coordinates: Coordinates[]): string {
    if (coordinates.length === 0) return '';

    const headers = ['ID', 'X', 'Y', 'Created By', 'Created Date', 'Last Modified By', 'Last Modified Date'];

    const rows = coordinates.map(coord => [
      coord.id,
      coord.x,
      coord.y,
      coord.createdBy,
      new Date(coord.createdDate).toLocaleDateString('en-US'),
      coord.lastModifiedBy,
      new Date(coord.lastModifiedDate).toLocaleDateString('en-US')
    ].map(field => `"${String(field).replace(/"/g, '""')}"`).join(','));

    return [headers.join(','), ...rows].join('\n');
  }

}
