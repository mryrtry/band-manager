import {HttpClient} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {LocationRequest} from '../../model/core/location/location.request';
import {Location} from '../../model/core/location/location.model';

@Injectable({
  providedIn: 'root'
})
export class LocationService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/locations`;

  getAllLocations(): Observable<Location[]> {
    return this.http.get<Location[]>(this.apiUrl);
  }

  getLocation(id: number): Observable<Location> {
    return this.http.get<Location>(`${this.apiUrl}/${id}`);
  }

  createLocation(request: LocationRequest): Observable<Location> {
    return this.http.post<Location>(this.apiUrl, request);
  }

  updateLocation(id: number, request: LocationRequest): Observable<Location> {
    return this.http.put<Location>(`${this.apiUrl}/${id}`, request);
  }

  deleteLocation(id: number): Observable<Location> {
    return this.http.delete<Location>(`${this.apiUrl}/${id}`);
  }

  exportToCsv(locations: Location[]): string {
    if (locations.length === 0) return '';

    const headers = ['ID', 'X', 'Y', 'Z', 'Created By', 'Created Date', 'Last Modified By', 'Last Modified Date'];

    const rows = locations.map(loc => [
      loc.id,
      loc.x,
      loc.y,
      loc.z,
      loc.createdBy,
      new Date(loc.createdDate).toLocaleDateString('en-US'),
      loc.lastModifiedBy,
      new Date(loc.lastModifiedDate).toLocaleDateString('en-US')
    ].map(field => `"${String(field).replace(/"/g, '""')}"`).join(','));

    return [headers.join(','), ...rows].join('\n');
  }

}
