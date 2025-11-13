import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PageableRequest } from '../model/pageable-request.model';
import { Page } from '../model/page.model';
import {ImportOperation} from '../model/import/import.model';

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

  getImportOperations(config: PageableRequest): Observable<Page<ImportOperation>> {
    let params = new HttpParams();

    if (config.page) params = params.set('page', config.page.toString());
    if (config.size) params = params.set('size', config.size.toString());
    if (config.sort) {
      config.sort.forEach(sortField => {
        params = params.append('sort', sortField);
      });
    }

    return this.http.get<Page<ImportOperation>>(`${this.apiUrl}/operations`, { params });
  }

  getImportOperation(id: number): Observable<ImportOperation> {
    return this.http.get<ImportOperation>(`${this.apiUrl}/operations/${id}`);
  }

  getSupportedFormats(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/supported-formats`);
  }
}
