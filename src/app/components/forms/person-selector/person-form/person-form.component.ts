import { CommonModule } from '@angular/common';
import { Component, EventEmitter, inject, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { MessageService } from 'primeng/api';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

import { Button, ButtonDirective, ButtonIcon, ButtonLabel } from 'primeng/button';
import { InputText } from 'primeng/inputtext';
import { InputNumber } from 'primeng/inputnumber';
import { Message } from 'primeng/message';
import { Select } from 'primeng/select';

import { Person } from '../../../../model/core/person/person.model';
import { PersonRequest } from '../../../../model/core/person/person.request';
import { Color } from '../../../../model/core/color.enum';
import { Country } from '../../../../model/core/country.enum';
import { LocationSelectorComponent } from '../../location-selector/location-selector.component';
import { User } from '../../../../model/auth/user.model';

@Component({
  selector: 'app-person-form',
  templateUrl: './person-form.component.html',
  styleUrls: ['../../form.component.scss'],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    InputText,
    InputNumber,
    Select,
    Message,
    Button,
    LocationSelectorComponent,
    ButtonLabel,
    ButtonIcon,
    ButtonDirective
  ],
  standalone: true
})
export class PersonFormComponent implements OnInit, OnChanges {
  // Inputs
  @Input() person: Person | null = null;
  @Input() currentUser?: User;

  // Outputs
  @Output() personSubmit = new EventEmitter<PersonRequest>();
  @Output() cancel = new EventEmitter<void>();

  // Form data
  colors = Object.values(Color);
  countries = Object.values(Country);

  // State
  isLoading = false;
  personForm: FormGroup;

  // Services
  private messageService = inject(MessageService);
  private fb = inject(FormBuilder);

  constructor() {
    this.personForm = this.createForm();
  }

  get isEditMode(): boolean {
    return !!this.person?.id;
  }

  get submitButtonText(): string {
    return this.isEditMode ? 'Обновить' : 'Создать';
  }

  get submitButtonIcon(): string {
    return this.isEditMode ? 'pi pi-pencil' : 'pi pi-plus';
  }

  ngOnInit(): void {
    this.initializeForm();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['person']) {
      this.initializeForm();
    }
  }

  // Form methods
  private createForm(): FormGroup {
    return this.fb.group({
      name: ['', [Validators.required, Validators.minLength(1)]],
      eyeColor: [null, [Validators.required]],
      hairColor: [null, [Validators.required]],
      locationId: [null, [Validators.required]],
      weight: [null, [Validators.required, Validators.min(0.000001)]],
      nationality: [null, [Validators.required]]
    });
  }

  initializeForm(): void {
    if (this.person) {
      this.personForm.patchValue({
        name: this.person.name,
        eyeColor: this.person.eyeColor,
        hairColor: this.person.hairColor,
        locationId: this.person.location?.id || null,
        weight: this.person.weight,
        nationality: this.person.nationality
      });
    } else {
      this.personForm.reset();
    }
  }

  isInvalid(fieldName: string): boolean {
    const field = this.personForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  getFieldError(fieldName: string): string {
    const field = this.personForm.get(fieldName);

    if (!field || !field.errors) return '';

    if (field.errors['required']) {
      return 'Это поле обязательно';
    }

    if (field.errors['min']) {
      return 'Должно быть больше 0';
    }

    return 'Недопустимое значение';
  }

  onLocationChange(locationId: number | undefined): void {
    this.personForm.get('locationId')?.setValue(locationId);
    this.personForm.get('locationId')?.markAsTouched();
  }

  onSubmit(): void {
    this.markFormAsTouched();

    if (this.personForm.invalid) {
      this.showValidationError();
      return;
    }

    this.submitForm();
  }

  onCancel(): void {
    this.resetForm();
    this.cancel.emit();
  }

  // Private methods
  private markFormAsTouched(): void {
    this.personForm.markAllAsTouched();
  }

  private showValidationError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Ошибка валидации',
      detail: 'Пожалуйста, исправьте ошибки в форме'
    });
  }

  private submitForm(): void {
    this.isLoading = true;

    const personRequest: PersonRequest = {
      name: this.personForm.value.name,
      eyeColor: this.personForm.value.eyeColor,
      hairColor: this.personForm.value.hairColor,
      locationId: this.personForm.value.locationId,
      weight: this.personForm.value.weight,
      nationality: this.personForm.value.nationality
    };

    this.personSubmit.emit(personRequest);
  }

  private resetForm(): void {
    this.personForm.reset();
    this.isLoading = false;
  }
}
