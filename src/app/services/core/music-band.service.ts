import {HttpClient, HttpParams} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {environment} from '../../../environments/environment';
import {
  MusicBandFilter
} from '../../model/core/music-band/music-band-filter.model';
import {PageableRequest} from '../../model/pageable-request.model';
import {Observable} from 'rxjs';
import {Page} from '../../model/page.model';
import {MusicBand} from '../../model/core/music-band/music-band.model';
import {MusicBandRequest} from '../../model/core/music-band/music-band.request';

@Injectable({
  providedIn: 'root'
})
export class MusicBandService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/music-bands`;

  getAllMusicBands(
    filter: MusicBandFilter = {},
    config: PageableRequest
  ): Observable<Page<MusicBand>> {
    let params = new HttpParams();

    params = this.setFilterParams(params, filter);
    params = this.setPaginationParams(params, config);

    return this.http.get<Page<MusicBand>>(this.apiUrl, { params });
  }

  private setFilterParams(params: HttpParams, filter: MusicBandFilter): HttpParams {
    const simpleFields: (keyof MusicBandFilter)[] = [
      'name',
      'description',
      'genre',
      'frontManName',
      'bestAlbumName',
      'minParticipants',
      'maxParticipants',
      'minSingles',
      'maxSingles',
      'minAlbumsCount',
      'maxAlbumsCount',
      'minCoordinateX',
      'maxCoordinateX',
      'minCoordinateY',
      'maxCoordinateY',
      'createdBy'
    ];

    simpleFields.forEach(key => {
      const value = filter[key];
      if (value != null && value !== '') {
        params = params.set(key, value.toString());
      }
    });

    if (filter.establishmentDateBefore) {
      params = params.set('establishmentDateBefore', this.formatDate(filter.establishmentDateBefore));
    }
    if (filter.establishmentDateAfter) {
      params = params.set('establishmentDateAfter', this.formatDate(filter.establishmentDateAfter));
    }

    return params;
  }

  private setPaginationParams(params: HttpParams, config: PageableRequest): HttpParams {
    if (config.page != null) {
      params = params.set('page', config.page.toString());
    }
    if (config.size != null) {
      params = params.set('size', config.size.toString());
    }
    if (config.direction) {
      params = params.set('direction', config.direction);
    }
    if (config.sort?.length) {
      config.sort.forEach(sortField => {
        params = params.append('sort', sortField);
      });
    }
    return params;
  }

  private formatDate(date: Date): string {
    return date.toISOString().split('T')[0]; // YYYY-MM-DD
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

  exportToCsv(bands: MusicBand[]): string {
    if (bands.length === 0) return '';

    const headers = [
      // Band
      'Band ID', 'Band Name', 'Genre', 'Participants', 'Singles Count', 'Albums Count', 'Establishment Date',
      // frontman (Person)
      'Front Man ID', 'Front Man Name', 'Front Man Eye Color', 'Front Man Hair Color', 'Front Man Weight', 'Front Man Nationality',
      // frontman Location
      'Front Man Location ID', 'Front Man Location X', 'Front Man Location Y', 'Front Man Location Z',
      // Best Album
      'Best Album ID', 'Best Album Name', 'Best Album Tracks', 'Best Album Sales',
      // Coordinates
      'Coordinates ID', 'Coordinate X', 'Coordinate Y',
      // Band Auditable
      'Created By', 'Created Date', 'Last Modified By', 'Last Modified Date'
    ];

    const rows = bands.map(band => [
      // Band
      band.id,
      band.name,
      band.genre,
      band.numberOfParticipants,
      band.singlesCount,
      band.albumsCount,
      band.establishmentDate ? new Date(band.establishmentDate).toLocaleDateString('en-US') : '',
      // frontman
      band.frontMan?.id || '',
      band.frontMan?.name || '',
      band.frontMan?.eyeColor || '',
      band.frontMan?.hairColor || '',
      band.frontMan?.weight || '',
      band.frontMan?.nationality || '',
      // frontman Location
      band.frontMan?.location?.id || '',
      band.frontMan?.location?.x || '',
      band.frontMan?.location?.y || '',
      band.frontMan?.location?.z || '',
      // Best Album
      band.bestAlbum?.id || '',
      band.bestAlbum?.name || '',
      band.bestAlbum?.tracks || '',
      band.bestAlbum?.sales || '',
      // Coordinates
      band.coordinates?.id || '',
      band.coordinates?.x || '',
      band.coordinates?.y || '',
      // Band Auditable
      band.createdBy,
      new Date(band.createdDate).toLocaleDateString('en-US'),
      band.lastModifiedBy,
      new Date(band.lastModifiedDate).toLocaleDateString('en-US')
    ].map(field => `"${String(field).replace(/"/g, '""')}"`).join(','));

    return [headers.join(','), ...rows].join('\n');
  }

}
