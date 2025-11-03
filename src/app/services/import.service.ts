import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {PaginatedResponse} from '../models/paginated-response.model';

export interface ImportOperation {
  id: number;
  filename: string;
  status: ImportStatus;
  createdEntitiesCount?: number;
  errorMessage?: string;
  startedAt: Date;
  completedAt?: Date;
  createdBy: string;
  createdDate: Date;
  lastModifiedBy?: string;
  lastModifiedDate?: Date;
}

export enum ImportStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  VALIDATION_FAILED = 'VALIDATION_FAILED'
}

export interface ImportGetConfig {
  pagination: ImportPagination;
  sorting?: ImportSorting;
}

export interface ImportPagination {
  page: number;
  size?: number;
}

export interface ImportSorting {
  sort: string[];
  direction?: 'asc' | 'desc';
}

@Injectable({
  providedIn: 'root'
})
export class ImportService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/import`;

  importMusicBands(file: File): Observable<ImportOperation> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<ImportOperation>(`${this.apiUrl}/music-bands`, formData);
  }

  getImportOperations(config: ImportGetConfig): Observable<PaginatedResponse<ImportOperation>> {
    let params = new HttpParams();

    const {pagination} = config;
    params = params.set('page', pagination.page.toString());
    if (pagination.size !== undefined && pagination.size !== null) {
      params = params.set('size', pagination.size.toString());
    }

    const {sorting} = config;
    if (sorting && sorting.sort && sorting.sort.length > 0) {
      sorting.sort.forEach(sortField => {
        params = params.append('sort', sortField);
      });
    }
    if (sorting && sorting.direction !== undefined && sorting.direction !== null) {
      params = params.append('direction', sorting.direction.toString());
    }

    return this.http.get<PaginatedResponse<ImportOperation>>(`${this.apiUrl}/operations`, {params});
  }

  getImportOperation(id: number): Observable<ImportOperation> {
    return this.http.get<ImportOperation>(`${this.apiUrl}/operations/${id}`);
  }

  getSupportedFormats(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/supported-formats`);
  }
}
