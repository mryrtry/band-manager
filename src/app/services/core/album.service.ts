import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {environment} from '../../../environments/environment';
import {Album} from '../../model/core/album/album.model';
import {AlbumRequest} from '../../model/core/album/album.request';

@Injectable({
  providedIn: 'root'
})
export class AlbumService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/albums`;

  getAllAlbums(): Observable<Album[]> {
    return this.http.get<Album[]>(this.apiUrl);
  }

  getAlbum(id: number): Observable<Album> {
    return this.http.get<Album>(`${this.apiUrl}/${id}`);
  }

  createAlbum(request: AlbumRequest): Observable<Album> {
    return this.http.post<Album>(this.apiUrl, request);
  }

  updateAlbum(id: number, request: AlbumRequest): Observable<Album> {
    return this.http.put<Album>(`${this.apiUrl}/${id}`, request);
  }

  deleteAlbum(id: number): Observable<Album> {
    return this.http.delete<Album>(`${this.apiUrl}/${id}`);
  }

  exportToCsv(albums: Album[]): string {
    if (albums.length === 0) return '';

    const headers = [
      'ID', 'Name', 'Tracks', 'Sales', 'Created By', 'Created Date', 'Last Modified By', 'Last Modified Date'
    ];

    const rows = albums.map(album => [
      album.id,
      album.name,
      album.tracks,
      album.sales,
      album.createdBy,
      new Date(album.createdDate).toLocaleDateString('en-US'),
      album.lastModifiedBy,
      new Date(album.lastModifiedDate).toLocaleDateString('en-US')
    ].map(field => `"${String(field).replace(/"/g, '""')}"`).join(','));

    return [headers.join(','), ...rows].join('\n');
  }

}
