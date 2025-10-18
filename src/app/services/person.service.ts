import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Person} from '../models/person.model';
import {PersonRequest} from '../models/requests/person-request.model';

@Injectable({
  providedIn: 'root'
})
export class PersonService {
  private baseUrl = '/persons';

  constructor(private http: HttpClient) {
  }

  getAll(): Observable<Person[]> {
    return this.http.get<Person[]>(this.baseUrl);
  }

  get(id: number): Observable<Person> {
    return this.http.get<Person>(`${this.baseUrl}/${id}`);
  }

  create(request: PersonRequest): Observable<Person> {
    return this.http.post<Person>(this.baseUrl, request);
  }

  update(id: number, request: PersonRequest): Observable<Person> {
    return this.http.put<Person>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<Person> {
    return this.http.delete<Person>(`${this.baseUrl}/${id}`);
  }

}
