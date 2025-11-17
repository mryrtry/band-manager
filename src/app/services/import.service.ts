import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PageableRequest } from '../model/pageable-request.model';
import { Page } from '../model/page.model';
import {ImportOperation} from '../model/import/import.model';
import {ImportFilter} from '../model/import/import-filter.model';

@Injectable({
  providedIn: 'root'
})
export class ImportService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/import`;

  importMusicBands(file: File): Observable<ImportOperation> {
    const formData = new FormData();
    formData.append('file', file, file.name);

    return this.http.post<ImportOperation>(`${this.apiUrl}/music-bands`, formData);
  }

  getImportOperations(filter: ImportFilter = {}, config: PageableRequest): Observable<Page<ImportOperation>> {
    let params = new HttpParams();

    params = this.setFilterParams(params, filter);
    params = this.setPaginationParams(params, config);

    return this.http.get<Page<ImportOperation>>(`${this.apiUrl}/operations`, { params });
  }

  private setFilterParams(params: HttpParams, filter: ImportFilter): HttpParams {
    const simpleFields: (keyof ImportFilter)[] = [
      'username',
      'filename',
      'importStatus',
      'createdEntitiesFrom',
      'createdEntitiesTo'
    ];

    simpleFields.forEach(key => {
      const value = filter[key];
      if (value != null && value !== '') {
        params = params.set(key, value.toString());
      }
    });

    if (filter.startedBefore) {
      params = params.set('startedBefore', this.formatDate(filter.startedBefore));
    }
    if (filter.startedAfter) {
      params = params.set('startedAfter', this.formatDate(filter.startedAfter));
    }
    if (filter.completedBefore) {
      params = params.set('completedBefore', this.formatDate(filter.completedBefore));
    }
    if (filter.completedAfter) {
      params = params.set('completedAfter', this.formatDate(filter.completedAfter));
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

  getImportOperation(id: number): Observable<ImportOperation> {
    return this.http.get<ImportOperation>(`${this.apiUrl}/operations/${id}`);
  }

  getSupportedFormats(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/supported-formats`);
  }

  private formatDate(date: Date): string {
    if (!date) return '';

    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');

    return `${year}-${month}-${day}`;
  }
}
