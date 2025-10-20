import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {BestBandAward} from '../models/best-band-award.model';
import {BestBandAwardRequest} from '../models/requests/best-band-award-request.model';
import {MusicGenre} from '../models/enums/music-genre.model';
import {PaginatedResponse} from '../models/paginated-response.model';
import {environment} from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class BestBandAwardService {
  private baseUrl = `${environment.apiUrl}/best-band-awards`;

  constructor(private http: HttpClient) {
  }

  getAll(
    genre?: MusicGenre,
    bandId?: number | null,
    page: number = 0,
    size: number = 10,
    sort: string = 'id',
    direction: 'asc' | 'desc' = 'desc'
  ): Observable<PaginatedResponse<BestBandAward>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort == 'bandName' ? 'id' : sort)
      .set('direction', direction);

    if (genre) params = params.set('genre', genre);
    if (bandId != null) params = params.set('bandId', bandId.toString());

    return this.http.get<PaginatedResponse<BestBandAward>>(this.baseUrl, {params});
  }

  get(id: number): Observable<BestBandAward> {
    return this.http.get<BestBandAward>(`${this.baseUrl}/${id}`);
  }

  create(request: BestBandAwardRequest): Observable<BestBandAward> {
    return this.http.post<BestBandAward>(this.baseUrl, request);
  }

  update(id: number, request: BestBandAwardRequest): Observable<BestBandAward> {
    return this.http.put<BestBandAward>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<BestBandAward> {
    return this.http.delete<BestBandAward>(`${this.baseUrl}/${id}`);
  }

}
