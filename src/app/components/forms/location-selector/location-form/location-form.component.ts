import { CommonModule } from '@angular/common';
import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { MessageService } from 'primeng/api';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

import { Button, ButtonDirective, ButtonIcon, ButtonLabel } from 'primeng/button';
import { InputNumber } from 'primeng/inputnumber';
import { Message } from 'primeng/message';

import { Location } from '../../../../model/core/location/location.model';
import { LocationRequest } from '../../../../model/core/location/location.request';

@Component({
  selector: 'app-location-form',
  templateUrl: './location-form.component.html',
  styleUrls: ['../../form.component.scss'],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    InputNumber,
    Message,
    Button,
    ButtonDirective,
    ButtonIcon,
    ButtonLabel
  ],
  standalone: true
})
export class LocationFormComponent implements OnInit {
  // Inputs
  @Input() location: Location | null = null;

  // Outputs
  @Output() locationSubmit = new EventEmitter<LocationRequest>();
  @Output() cancel = new EventEmitter<void>();

  // State
  isLoading = false;
  locationForm: FormGroup;

  // Services
  private messageService = inject(MessageService);
  private fb = inject(FormBuilder);

  constructor() {
    this.locationForm = this.createForm();
  }

  get isEditMode(): boolean {
    return !!this.location?.id;
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

  // Form methods
  private createForm(): FormGroup {
    return this.fb.group({
      x: [null],
      y: [null, [Validators.required]],
      z: [null, [Validators.required]]
    });
  }

  initializeForm(): void {
    if (this.location) {
      this.locationForm.patchValue({
        x: this.location.x,
        y: this.location.y,
        z: this.location.z
      });
    } else {
      this.locationForm.reset();
    }
  }

  isInvalid(fieldName: string): boolean {
    const field = this.locationForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  getFieldError(fieldName: string): string {
    const field = this.locationForm.get(fieldName);

    if (!field || !field.errors) return '';

    if (field.errors['required']) {
      return 'Это поле обязательно';
    }

    return 'Недопустимое значение';
  }

  onSubmit(): void {
    this.markFormAsTouched();

    if (this.locationForm.invalid) {
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
    this.locationForm.markAllAsTouched();
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

    const locationRequest: LocationRequest = {
      x: this.locationForm.value.x || undefined,
      y: this.locationForm.value.y,
      z: this.locationForm.value.z
    };

    this.locationSubmit.emit(locationRequest);
  }

  private resetForm(): void {
    this.locationForm.reset();
    this.isLoading = false;
  }

}
