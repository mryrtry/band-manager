import {HttpClient, HttpParams} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {environment} from '../../../environments/environment';
import {
  BestBandAwardFilter
} from '../../model/core/best-band-award/best-band-award-filter.model';
import {PageableRequest} from '../../model/pageable-request.model';
import {Observable} from 'rxjs';
import {Page} from '../../model/page.model';
import {
  BestBandAward
} from '../../model/core/best-band-award/best-band-award.model';
import {
  BestBandAwardRequest
} from '../../model/core/best-band-award/best-band-award.request';

@Injectable({
  providedIn: 'root'
})
export class BestBandAwardService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/best-band-awards`;

  getAllBestBandAwards(filter: BestBandAwardFilter = {}, config: PageableRequest): Observable<Page<BestBandAward>> {
    let params = new HttpParams();
    params = this.setFilterParams(params, filter);
    params = this.setPaginationParams(params, config);
    return this.http.get<Page<BestBandAward>>(this.apiUrl, {params});
  }

  getBestBandAward(id: number): Observable<BestBandAward> {
    return this.http.get<BestBandAward>(`${this.apiUrl}/${id}`);
  }

  createBestBandAward(request: BestBandAwardRequest): Observable<BestBandAward> {
    return this.http.post<BestBandAward>(this.apiUrl, request);
  }

  updateBestBandAward(id: number, request: BestBandAwardRequest): Observable<BestBandAward> {
    return this.http.put<BestBandAward>(`${this.apiUrl}/${id}`, request);
  }

  deleteBestBandAward(id: number): Observable<BestBandAward> {
    return this.http.delete<BestBandAward>(`${this.apiUrl}/${id}`);
  }

  private setFilterParams(params: HttpParams, filter: BestBandAwardFilter): HttpParams {
    const simpleFields: (keyof BestBandAwardFilter)[] = ['bandId', 'bandName', 'genre'];

    simpleFields.forEach(key => {
      const value = filter[key];
      if (value != null && value !== '') {
        params = params.set(key, value.toString());
      }
    });

    if (filter.createdAtBefore) {
      params = params.set('createdAtBefore', this.formatDate(filter.createdAtBefore));
    }
    if (filter.createdAtAfter) {
      params = params.set('createdAtAfter', this.formatDate(filter.createdAtAfter));
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
    return date.toISOString().split('T')[0];
  }

}
