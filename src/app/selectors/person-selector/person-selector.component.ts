// person-selector.component.ts
import {Component, Output, EventEmitter, OnInit, signal, computed, inject, HostListener} from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { PersonService } from '../../services/person.service';
import { PersonRequest } from '../../models/requests/person-request.model';
import { Color } from '../../models/enums/color.model';
import { LocationSelectorComponent } from '../location-selector/location-selector.component';
import { firstValueFrom } from 'rxjs';
import {CustomSelectComponent} from '../../components/select/select.component';
import {Country} from '../../models/enums/country.model';

interface SelectOption {
  value: any;
  label: string;
}

interface PersonOption {
  id: number;
  name: string;
  eyeColor: Color;
  hairColor: Color;
  weight: number;
  nationality: Country;
  displayName: string;
}

@Component({
  selector: 'app-person-selector',
  templateUrl: './person-selector.component.html',
  styleUrls: ['./person-selector.component.scss'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LocationSelectorComponent, CustomSelectComponent]
})
export class PersonSelectorComponent implements OnInit {
  @Output() personSelected = new EventEmitter<number | null>();

  private personService = inject(PersonService);
  private fb = inject(FormBuilder);

  persons = signal<PersonOption[]>([]);
  isLoading = signal(false);
  selectedPersonId = signal<number | null>(null);
  mode = signal<'select' | 'create'>('select');
  errorMessage = signal<string | null>(null);
  locationId = signal<number | null>(null);

  // Опции для кастомных селектов
  eyeColorOptions: SelectOption[] = [];
  hairColorOptions: SelectOption[] = [];
  nationalityOptions: SelectOption[] = [];
  personOptions: SelectOption[] = [];

  selectedPerson = computed(() => {
    const id = this.selectedPersonId();
    if (id === null) return null;
    return this.persons().find(person => person.id === id) || null;
  });

  personForm: FormGroup;

  constructor() {
    this.initializeOptions();
    this.personForm = this.fb.group({
      name: ['', [Validators.required]],
      eyeColor: [null, [Validators.required]],
      hairColor: [null, [Validators.required]],
      weight: [null, [Validators.required, Validators.min(0.1)]],
      nationality: [null, [Validators.required]]
    });
  }

  private initializeOptions() {
    this.eyeColorOptions = Object.values(Color).map(color => ({
      value: color,
      label: color
    }));

    this.hairColorOptions = Object.values(Color).map(color => ({
      value: color,
      label: color
    }));

    this.nationalityOptions = Object.values(Country).map(country => ({
      value: country,
      label: country
    }));
  }

  async ngOnInit() {
    await this.loadPersons();
  }

  async loadPersons() {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    try {
      const persons = await firstValueFrom(this.personService.getAll());
      const personOptions = persons.map(person => ({
        id: person.id,
        name: person.name,
        eyeColor: person.eyeColor,
        hairColor: person.hairColor,
        weight: person.weight,
        nationality: person.nationality,
        displayName: `${person.name} (${person.nationality})`
      }));

      this.persons.set(personOptions);
      this.personOptions = personOptions.map(person => ({
        value: person.id,
        label: person.displayName
      }));
    } catch (error) {
      this.errorMessage.set('Ошибка загрузки персон');
      console.error('Error loading persons:', error);
    } finally {
      this.isLoading.set(false);
    }
  }

  switchMode(newMode: 'select' | 'create') {
    this.mode.set(newMode);
    this.errorMessage.set(null);
    this.personForm.reset();
    this.locationId.set(null);
  }

  onPersonSelect(personId: number) {
    this.selectedPersonId.set(personId);
    this.personSelected.emit(personId);
  }

  clearSelection() {
    this.selectedPersonId.set(null);
    this.personSelected.emit(null);
  }

  onLocationSelected(locationId: number | null) {
    this.locationId.set(locationId);
  }

  async createPerson() {
    if (this.personForm.invalid || !this.locationId()) {
      this.markFormGroupTouched();
      if (!this.locationId()) {
        this.errorMessage.set('Необходимо выбрать локацию');
      }
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    try {
      const formData = this.personForm.value;
      const personData: PersonRequest = {
        ...formData,
        weight: Number(formData.weight),
        locationId: this.locationId()!
      };

      const createdPerson = await firstValueFrom(this.personService.create(personData));

      const newPerson: PersonOption = {
        id: createdPerson.id,
        name: createdPerson.name,
        eyeColor: createdPerson.eyeColor,
        hairColor: createdPerson.hairColor,
        weight: createdPerson.weight,
        nationality: createdPerson.nationality,
        displayName: `${createdPerson.name} (${createdPerson.nationality})`
      };

      // Обновляем списки
      this.persons.update(persons => [...persons, newPerson]);
      this.personOptions = [...this.personOptions, {
        value: newPerson.id,
        label: newPerson.displayName
      }];

      this.selectPerson(newPerson);
      this.mode.set('select');
      this.personForm.reset();
      this.locationId.set(null);

    } catch (error: any) {
      if (error?.error?.details) {
        const backendErrors = error.error.details;
        this.handleBackendErrors(backendErrors);
      } else {
        this.errorMessage.set('Ошибка создания персоны');
      }
      console.error('Error creating person:', error);
    } finally {
      this.isLoading.set(false);
    }
  }

  private selectPerson(person: PersonOption) {
    this.selectedPersonId.set(person.id);
    this.personSelected.emit(person.id);
  }

  private markFormGroupTouched() {
    Object.keys(this.personForm.controls).forEach(key => {
      const control = this.personForm.get(key);
      control?.markAsTouched();
    });
  }

  private handleBackendErrors(errors: any[]) {
    Object.keys(this.personForm.controls).forEach(key => {
      const control = this.personForm.get(key);
      if (control?.errors?.['serverError']) {
        const { serverError, ...otherErrors } = control.errors;
        control.setErrors(Object.keys(otherErrors).length > 0 ? otherErrors : null);
      }
    });

    let hasGlobalError = false;

    errors.forEach(error => {
      const field = error.field.toLowerCase();
      const control = this.personForm.get(field);

      if (control) {
        const currentErrors = control.errors || {};
        control.setErrors({
          ...currentErrors,
          serverError: error.message
        });
        control.markAsTouched();
      } else {
        hasGlobalError = true;
        this.errorMessage.set(error.message);
      }
    });
  }

  get isSelectMode(): boolean {
    return this.mode() === 'select';
  }

  get isCreateMode(): boolean {
    return this.mode() === 'create';
  }

  get isFormValid(): boolean {
    return this.personForm.valid && this.locationId() !== null;
  }

  getFieldError(fieldName: string): string {
    const control = this.personForm.get(fieldName);

    if (!control) return '';

    if (control.errors?.['serverError']) {
      return control.errors['serverError'];
    }

    if (control.errors?.['required'] && (control.touched || control.dirty)) {
      return 'Это поле обязательно';
    }

    if (control.errors?.['min'] && (control.touched || control.dirty)) {
      return `Значение должно быть больше ${control.errors['min'].min}`;
    }

    return '';
  }

  hasFieldError(fieldName: string): boolean {
    const control = this.personForm.get(fieldName);
    return !!(control?.invalid && (control.touched || control.dirty));
  }

}
