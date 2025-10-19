import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {delay, Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {MusicBand} from '../models/music-band.model';
import {MusicBandRequest} from '../models/requests/music-band-request.model';
import {PaginatedResponse} from '../models/paginated-response.model';

export interface MusicBandGetConfig {
  filter: MusicBandFilter;
  sorting: MusicBandSorting;
  pagination: MusicBandPagination;
}

export interface MusicBandFilter {
  name?: string | null;
  description?: string | null;
  genre?: string | null;
  frontManName?: string | null;
  bestAlbumName?: string | null;
  minParticipants?: number | null;
  maxParticipants?: number | null;
  minSingles?: number | null;
  maxSingles?: number | null;
  minAlbumsCount?: number | null;
  maxAlbumsCount?: number | null;
  minCoordinateX?: number | null;
  maxCoordinateX?: number | null;
  minCoordinateY?: number | null;
  maxCoordinateY?: number | null;
}

export interface MusicBandPagination {
  page: number;
  size?: number;
}

export interface MusicBandSorting {
  sort: string[];
  direction?: 'asc' | 'desc';
}

@Injectable({
  providedIn: 'root'
})
export class MusicBandService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/music-bands`;

  getMusicBands(config: MusicBandGetConfig): Observable<PaginatedResponse<MusicBand>> {
    let params = new HttpParams();
    const { filter } = config;
    Object.keys(filter).forEach(key => {
      const value = filter[key as keyof MusicBandFilter];
      if (value !== undefined && value !== null && value !== '') {
        params = params.set(key, value.toString());
      }
    });
    const { pagination } = config;
    params = params.set('page', pagination.page.toString());
    if (pagination.size !== undefined && pagination.size !== null) {
      params = params.set('size', pagination.size.toString());
    }
    const { sorting } = config;
    if (sorting.sort && sorting.sort.length > 0) {
      sorting.sort.forEach(sortField => {
        params = params.append('sort', sortField);
      });
    }
    if (sorting.direction !== undefined && sorting.direction !== null) {
      params = params.append('direction', sorting.direction.toString());
    }
    return this.http.get<PaginatedResponse<MusicBand>>(this.apiUrl, { params }).pipe(delay(0));
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

  deleteMusicBands(ids: number[]): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}`, {body: ids});
  }
}
