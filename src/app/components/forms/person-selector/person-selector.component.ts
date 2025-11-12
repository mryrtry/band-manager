import { CommonModule } from '@angular/common';
import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { MessageService } from 'primeng/api';

import { PersonService } from '../../../services/core/person.service';
import { Person } from '../../../model/core/person/person.model';
import { PersonRequest } from '../../../model/core/person/person.request';
import { PersonFormComponent } from './person-form/person-form.component';
import { Button } from 'primeng/button';
import { Select } from 'primeng/select';
import { Role, User } from '../../../model/auth/user.model';
import { DialogModule } from 'primeng/dialog';
import {InputGroup} from 'primeng/inputgroup';
import {InputGroupAddon} from 'primeng/inputgroupaddon';

@Component({
  selector: 'app-person-selector',
  templateUrl: './person-selector.component.html',
  styleUrls: ['../selector.component.scss'],
  imports: [CommonModule, Select, Button, PersonFormComponent, DialogModule, FormsModule, InputGroup, InputGroupAddon],
  standalone: true
})
export class PersonSelectorComponent implements OnInit {
  // Inputs
  @Input() disabled = false;
  @Input() personId?: number;
  @Input() currentUser?: User;

  // Outputs
  @Output() personIdChange = new EventEmitter<number | undefined>();

  // State
  persons: Person[] = [];
  selectedPersonId?: number;  // Changed to store only the ID
  selectedPerson?: Person;
  showForm = false;
  formPerson: Person | null = null;
  isLoading = false;

  // Services
  private personService = inject(PersonService);
  private messageService = inject(MessageService);

  ngOnInit() {
    this.loadPersons();
  }

  // Public methods
  loadPersons(): void {
    this.isLoading = true;

    this.personService.getAllPersons().subscribe({
      next: (persons) => {
        this.persons = persons;
        this.syncSelectedPersonWithInput();
        this.isLoading = false;
      },
      error: (error: HttpErrorResponse) => {
        this.handleError('Персоны не загрузились', 'Сервер недоступен, повторите попытку позже.', error);
        this.isLoading = false;
      }
    });
  }

  showCreateForm(): void {
    this.formPerson = null;
    this.showForm = true;
  }

  showEditForm(): void {
    if (!this.selectedPerson) return;
    this.formPerson = this.selectedPerson;
    this.showForm = true;
  }

  hideForm(): void {
    this.showForm = false;
    this.formPerson = null;
  }

  handlePersonSubmit(personRequest: PersonRequest): void {
    this.formPerson
      ? this.updatePerson(this.formPerson.id, personRequest)
      : this.createPerson(personRequest);
  }

  // Handler for when the selection changes in the dropdown
  onSelectionChange(person: Person | undefined): void {
    this.selectedPerson = person;
    this.selectedPersonId = person?.id;
    this.personIdChange.emit(this.selectedPersonId);
  }

  // Handler for when the clear button is clicked
  onClearSelection(): void {
    this.selectedPerson = undefined;
    this.selectedPersonId = undefined;
    this.personIdChange.emit(undefined);
  }

  canUpdate(): boolean {
    if (!this.currentUser || !this.selectedPerson) return true;
    const isOwner = this.selectedPerson.createdBy === this.currentUser.username;
    const isSystem = this.selectedPerson.createdBy === 'system';
    const isAdmin = this.currentUser.roles.includes(Role.ROLE_ADMIN);
    return isOwner || isSystem || isAdmin;
  }

  // Private methods
  private syncSelectedPersonWithInput(): void {
    if (!this.personId || this.persons.length === 0) {
      this.selectedPerson = undefined;
      this.selectedPersonId = undefined;
      return;
    }

    this.selectedPerson = this.persons.find(person => person.id === this.personId);
    this.selectedPersonId = this.selectedPerson?.id;

    if (!this.selectedPerson) {
      console.warn(`Персона с id ${this.personId} не найдена`);
    }
  }

  private createPerson(personRequest: PersonRequest): void {
    this.isLoading = true;

    this.personService.createPerson(personRequest).subscribe({
      next: (createdPerson) => this.handlePersonCreationSuccess(createdPerson),
      error: (error) => this.handleError('Ошибка создания', 'Ошибка при создании персоны', error)
    });
  }

  private updatePerson(personId: number, personRequest: PersonRequest): void {
    this.isLoading = true;

    this.personService.updatePerson(personId, personRequest).subscribe({
      next: (updatedPerson) => this.handlePersonUpdateSuccess(updatedPerson),
      error: (error) => this.handleError('Ошибка обновления', 'Ошибка при обновлении персоны', error)
    });
  }

  private handlePersonCreationSuccess(createdPerson: Person): void {
    this.isLoading = false;
    this.persons.push(createdPerson);
    this.selectedPerson = createdPerson;
    this.selectedPersonId = createdPerson.id;
    this.hideForm();
    this.personIdChange.emit(this.selectedPersonId);

    this.showSuccessMessage('Персона создана', createdPerson.name);
  }

  private handlePersonUpdateSuccess(updatedPerson: Person): void {
    this.isLoading = false;

    // Update the person in the list
    this.persons = this.persons.map(person =>
      person.id === updatedPerson.id ? updatedPerson : person
    );

    // If the updated person is the currently selected one, update the reference
    if (this.selectedPerson?.id === updatedPerson.id) {
      this.selectedPerson = updatedPerson;
      this.selectedPersonId = updatedPerson.id;
    }

    this.hideForm();
    this.personIdChange.emit(this.selectedPersonId);

    this.showSuccessMessage('Персона обновлена', updatedPerson.name);
  }

  private showSuccessMessage(summary: string, personName: string): void {
    this.messageService.add({
      severity: 'success',
      summary,
      detail: `Персона "${personName}" успешно создана`
    });
  }

  private handleError(summary: string, detail: string, error: HttpErrorResponse): void {
    this.isLoading = false;
    this.messageService.add({ severity: 'error', summary, detail });
    console.error(`${summary}:`, error);
  }
}
