import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {Person} from '../../model/core/person/person.model';
import {environment} from '../../../environments/environment';
import {PersonRequest} from '../../model/core/person/person.request';
import {Location} from '../../model/core/location/location.model';

@Injectable({
  providedIn: 'root'
})
export class PersonService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/persons`;

  getAllPersons(): Observable<Person[]> {
    return this.http.get<Person[]>(this.apiUrl);
  }

  getPerson(id: number): Observable<Person> {
    return this.http.get<Person>(`${this.apiUrl}/${id}`);
  }

  createPerson(request: PersonRequest): Observable<Person> {
    return this.http.post<Person>(this.apiUrl, request);
  }

  updatePerson(id: number, request: PersonRequest): Observable<Person> {
    return this.http.put<Person>(`${this.apiUrl}/${id}`, request);
  }

  deletePerson(id: number): Observable<Person> {
    return this.http.delete<Person>(`${this.apiUrl}/${id}`);
  }

  exportToCsv(persons: Person[]): string {
    if (persons.length === 0) return '';

    const headers = [
      'ID', 'Name', 'Eye Color', 'Hair Color', 'Location ID', 'X', 'Y', 'Z', 'Weight', 'Nationality',
      'Created By', 'Created Date', 'Last Modified By', 'Last Modified Date'
    ];

    const rows = persons.map(person => {
      const loc: Location = person.location;
      return [
        person.id,
        person.name,
        person.eyeColor,
        person.hairColor,
        loc?.id || '',
        loc?.x || '',
        loc?.y || '',
        loc?.z || '',
        person.weight,
        person.nationality,
        person.createdBy,
        new Date(person.createdDate).toLocaleDateString('en-US'),
        person.lastModifiedBy,
        new Date(person.lastModifiedDate).toLocaleDateString('en-US')
      ].map(field => `"${String(field).replace(/"/g, '""')}"`).join(',');
    });

    return [headers.join(','), ...rows].join('\n');
  }

}
