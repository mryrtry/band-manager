import {HttpClient, HttpParams} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {environment} from '../../environments/environment';
import {
  MusicBandFilter
} from '../model/core/music-band/music-band-filter.model';
import {PageableRequest} from '../model/pageable-request.model';
import {Observable} from 'rxjs';
import {Page} from '../model/page.model';
import {MusicBand} from '../model/core/music-band/music-band.model';
import {MusicBandRequest} from '../model/core/music-band/music-band.request';

@Injectable({
  providedIn: 'root'
})
export class MusicBandService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/music-bands`;

  getAllMusicBands(filter: MusicBandFilter = {}, config: PageableRequest): Observable<Page<MusicBand>> {
    let params = new HttpParams();
    if (filter.name) params = params.set('name', filter.name);
    if (filter.description) params = params.set('description', filter.description);
    if (filter.genre) params = params.set('genre', filter.genre);
    if (filter.frontManName) params = params.set('frontManName', filter.frontManName);
    if (filter.bestAlbumName) params = params.set('bestAlbumName', filter.bestAlbumName);
    if (filter.minParticipants) params = params.set('minParticipants', filter.minParticipants.toString());
    if (filter.maxParticipants) params = params.set('maxParticipants', filter.maxParticipants.toString());
    if (filter.minSingles) params = params.set('minSingles', filter.minSingles.toString());
    if (filter.maxSingles) params = params.set('maxSingles', filter.maxSingles.toString());
    if (filter.minAlbumsCount) params = params.set('minAlbumsCount', filter.minAlbumsCount.toString());
    if (filter.maxAlbumsCount) params = params.set('maxAlbumsCount', filter.maxAlbumsCount.toString());
    if (filter.minCoordinateX) params = params.set('minCoordinateX', filter.minCoordinateX.toString());
    if (filter.maxCoordinateX) params = params.set('maxCoordinateX', filter.maxCoordinateX.toString());
    if (filter.minCoordinateY) params = params.set('minCoordinateY', filter.minCoordinateY.toString());
    if (filter.maxCoordinateY) params = params.set('maxCoordinateY', filter.maxCoordinateY.toString());
    if (filter.establishmentDateBefore) params = params.set('establishmentDateBefore', filter.establishmentDateBefore.toISOString().split('T')[0]);
    if (filter.establishmentDateAfter) params = params.set('establishmentDateAfter', filter.establishmentDateAfter.toISOString().split('T')[0]);
    if (filter.createdBy) params = params.set('createdBy', filter.createdBy);

    if (config.page) params = params.set('page', config.page.toString());
    if (config.size) params = params.set('size', config.size.toString());
    if (config.sort) {
      config.sort.forEach(sortField => {
        params = params.append('sort', sortField);
      });
    }
    if (config.direction) params = params.set('direction', config.direction);

    return this.http.get<Page<MusicBand>>(this.apiUrl, {params});
  }

  getMusicBand(id: number): Observable<MusicBand> {
    return this.http.get<MusicBand>(`${this.apiUrl}/${id}`);
  }

  getMaxCoordinates(): Observable<MusicBand> {
    return this.http.get<MusicBand>(`${this.apiUrl}/max-coordinates`);
  }

  getBandsEstablishedBefore(date: Date): Observable<MusicBand[]> {
    const params = new HttpParams().set('date', date.toISOString().split('T')[0]);
    return this.http.get<MusicBand[]>(`${this.apiUrl}/established-before`, {params});
  }

  getUniqueAlbumsCount(): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/unique-albums-count`);
  }

  createMusicBand(request: MusicBandRequest): Observable<MusicBand> {
    return this.http.post<MusicBand>(this.apiUrl, request);
  }

  updateMusicBand(id: number, request: MusicBandRequest): Observable<MusicBand> {
    return this.http.put<MusicBand>(`${this.apiUrl}/${id}`, request);
  }

  removeParticipant(id: number): Observable<MusicBand> {
    return this.http.put<MusicBand>(`${this.apiUrl}/${id}/remove-participant`, {});
  }

  deleteMusicBand(id: number): Observable<MusicBand> {
    return this.http.delete<MusicBand>(`${this.apiUrl}/${id}`);
  }

  deleteMusicBands(ids: number[]): Observable<MusicBand[]> {
    return this.http.delete<MusicBand[]>(this.apiUrl, {body: ids});
  }
}
