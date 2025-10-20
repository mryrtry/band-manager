// person-selector.component.ts
import { Component, Output, EventEmitter, OnInit, signal, computed, inject, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { PersonService } from '../../services/person.service';
import { PersonRequest } from '../../models/requests/person-request.model';
import { Color } from '../../models/enums/color.model';
import { Country } from '../../models/enums/country.model';
import { LocationSelectorComponent } from '../location-selector/location-selector.component';
import { firstValueFrom } from 'rxjs';
import { CustomSelectComponent } from '../../components/select/select.component';

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
  locationId: number;
  displayName: string;
}

type ComponentMode = 'select' | 'create' | 'edit';

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
  editingPersonId = signal<number | null>(null);
  mode = signal<ComponentMode>('select');
  errorMessage = signal<string | null>(null);
  locationId = signal<number | null>(null);

  // Опции для кастомных селектов
  eyeColorOptions: SelectOption[] = [];
  hairColorOptions: SelectOption[] = [];
  nationalityOptions: SelectOption[] = [];
  personOptions: SelectOption[] = [];

  selectedPerson = computed(() => {
    const id = this.selectedPersonId();
    return id ? this.persons().find(person => person.id === id) || null : null;
  });

  editingPerson = computed(() => {
    const id = this.editingPersonId();
    return id ? this.persons().find(person => person.id === id) || null : null;
  });

  personForm: FormGroup;

  @ViewChild(LocationSelectorComponent) locationSelector!: LocationSelectorComponent;

  public reset() {
    this.selectedPersonId.set(null);
    this.editingPersonId.set(null);
    this.mode.set('select');
    this.personForm.reset();
    this.errorMessage.set(null);
    this.locationId.set(null);
    if (this.locationSelector) {
      this.locationSelector.reset();
    }
  }

  constructor() {
    this.initializeOptions();
    this.personForm = this.createForm();
  }

  private createForm(): FormGroup {
    return this.fb.group({
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
        locationId: person.location.id,
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

  switchMode(newMode: ComponentMode, personId?: number): void {
    this.mode.set(newMode);
    this.errorMessage.set(null);
    this.personForm.reset();
    this.locationId.set(null);

    if (this.locationSelector) {
      this.locationSelector.reset();
    }

    if (newMode === 'edit' && personId) {
      this.editingPersonId.set(personId);
      this.populateFormForEditing();
    } else {
      this.editingPersonId.set(null);
    }
  }

  private populateFormForEditing(): void {
    const person = this.editingPerson();
    if (!person) return;

    this.personForm.patchValue({
      name: person.name,
      eyeColor: person.eyeColor,
      hairColor: person.hairColor,
      weight: person.weight,
      nationality: person.nationality
    });

    this.locationId.set(person.locationId);

    // Программно выбираем локацию в дочернем компоненте
    setTimeout(() => {
      if (this.locationSelector) {
        this.locationSelector.selectedLocationId.set(person.locationId);
      }
    });
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

    await this.savePerson();
  }

  async updatePerson() {
    if (this.personForm.invalid || !this.locationId()) {
      this.markFormGroupTouched();
      if (!this.locationId()) {
        this.errorMessage.set('Необходимо выбрать локацию');
      }
      return;
    }

    await this.savePerson(true);
  }

  private async savePerson(isUpdate: boolean = false): Promise<void> {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    try {
      const formData = this.personForm.value;
      const personData: PersonRequest = {
        ...formData,
        weight: Number(formData.weight),
        locationId: this.locationId()!
      };

      let savedPerson;

      if (isUpdate && this.editingPersonId()) {
        savedPerson = await firstValueFrom(
          this.personService.update(this.editingPersonId()!, personData)
        );
      } else {
        savedPerson = await firstValueFrom(
          this.personService.create(personData)
        );
      }

      await this.handleSaveSuccess(savedPerson, isUpdate);

    } catch (error: any) {
      this.handleSaveError(error);
    } finally {
      this.isLoading.set(false);
    }
  }

  private async handleSaveSuccess(person: any, isUpdate: boolean): Promise<void> {
    if (isUpdate) {
      await this.loadPersons(); // Reload to get updated list
    } else {
      this.addPersonToLists(person);
    }

    this.selectPerson(person);
    this.switchMode('select');
    this.personForm.reset();
    this.locationId.set(null);
  }

  private addPersonToLists(person: any): void {
    const newPerson: PersonOption = {
      id: person.id,
      name: person.name,
      eyeColor: person.eyeColor,
      hairColor: person.hairColor,
      weight: person.weight,
      nationality: person.nationality,
      locationId: person.location.id,
      displayName: `${person.name} (${person.nationality})`
    };

    this.persons.update(persons => [...persons, newPerson]);
    this.personOptions = [...this.personOptions, {
      value: newPerson.id,
      label: newPerson.displayName
    }];
  }

  private selectPerson(person: any): void {
    this.selectedPersonId.set(person.id);
    this.personSelected.emit(person.id);
  }

  private handleSaveError(error: any): void {
    if (error?.error?.details) {
      const backendErrors = error.error.details;
      this.handleBackendErrors(backendErrors);
    } else {
      this.errorMessage.set(`Ошибка ${this.isEditMode ? 'обновления' : 'создания'} персоны`);
    }
    console.error(`Error ${this.isEditMode ? 'updating' : 'creating'} person:`, error);
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
      const field = error.field?.toLowerCase();
      const control = field ? this.personForm.get(field) : null;

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

  // Computed properties for template
  get isSelectMode(): boolean {
    return this.mode() === 'select';
  }

  get isCreateMode(): boolean {
    return this.mode() === 'create';
  }

  get isEditMode(): boolean {
    return this.mode() === 'edit';
  }

  get isFormValid(): boolean {
    return this.personForm.valid && this.locationId() !== null;
  }

  get submitButtonText(): string {
    if (this.isLoading()) {
      return this.isEditMode ? 'Обновление...' : 'Создание...';
    }
    return this.isEditMode ? 'Обновить персону' : 'Создать персону';
  }

  get formTitle(): string {
    return this.isEditMode ? 'Редактирование персоны' : 'Создание персоны';
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
