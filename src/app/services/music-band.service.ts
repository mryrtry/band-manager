import {Injectable, inject} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {MusicBand} from '../models/music-band.model';
import {MusicBandRequest} from '../models/requests/music-band-request.model';
import {MusicGenre} from '../models/enums/music-genre.model';

export interface MusicBandFilter {
  name?: string;
  description?: string;
  genre?: MusicGenre;
  frontManName?: string;
  bestAlbumName?: string;
  minParticipants?: number;
  maxParticipants?: number;
  minSingles?: number;
  maxSingles?: number;
  minAlbumsCount?: number;
  maxAlbumsCount?: number;
  minCoordinateX?: number;
  maxCoordinateX?: number;
  minCoordinateY?: number;
  maxCoordinateY?: number;
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'asc' | 'desc';
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class MusicBandService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/music-bands`;

  getMusicBands(filter: MusicBandFilter = {}): Observable<PaginatedResponse<MusicBand>> {
    let params = new HttpParams();
    Object.keys(filter).forEach(key => {
      const value = filter[key as keyof MusicBandFilter];
      if (value !== undefined && value !== null && value !== '') {
        params = params.set(key, value.toString());
      }
    });
    return this.http.get<PaginatedResponse<MusicBand>>(this.apiUrl, {params});
  }

  getMusicBandById(id: number): Observable<MusicBand> {
    return this.http.get<MusicBand>(`${this.apiUrl}/${id}`);
  }

  getMusicBandWithMaxCoordinates(): Observable<MusicBand> {
    return this.http.get<MusicBand>(`${this.apiUrl}/max-coordinates`);
  }

  getBandsEstablishedBefore(date: string): Observable<MusicBand[]> {
    const params = new HttpParams().set('date', date);
    return this.http.get<MusicBand[]>(`${this.apiUrl}/established-before`, {params});
  }

  getUniqueAlbumsCount(): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/unique-albums-count`);
  }

  createMusicBand(bandRequest: MusicBandRequest): Observable<MusicBand> {
    return this.http.post<MusicBand>(this.apiUrl, bandRequest);
  }

  updateMusicBand(id: number, bandRequest: MusicBandRequest): Observable<MusicBand> {
    return this.http.put<MusicBand>(`${this.apiUrl}/${id}`, bandRequest);
  }

  removeParticipant(id: number): Observable<MusicBand> {
    return this.http.put<MusicBand>(`${this.apiUrl}/${id}/remove-participant`, {});
  }

  deleteMusicBand(id: number): Observable<MusicBand> {
    return this.http.delete<MusicBand>(`${this.apiUrl}/${id}`);
  }

}
