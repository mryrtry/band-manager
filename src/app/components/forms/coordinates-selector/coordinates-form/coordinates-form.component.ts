import { CommonModule } from '@angular/common';
import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { MessageService } from 'primeng/api';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

import { Button, ButtonDirective, ButtonIcon, ButtonLabel } from 'primeng/button';
import { InputNumber } from 'primeng/inputnumber';
import { Message } from 'primeng/message';

import { Coordinates } from '../../../../model/core/coordinates/coordinates.model';
import { CoordinatesRequest } from '../../../../model/core/coordinates/coordinates.request';

@Component({
  selector: 'app-coordinates-form',
  templateUrl: './coordinates-form.component.html',
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
export class CoordinatesFormComponent implements OnInit {
  // Inputs
  @Input() coordinates: Coordinates | null = null;

  // Outputs
  @Output() coordinatesSubmit = new EventEmitter<CoordinatesRequest>();
  @Output() cancel = new EventEmitter<void>();

  // State
  isLoading = false;
  coordinatesForm: FormGroup;

  // Services
  private messageService = inject(MessageService);
  private fb = inject(FormBuilder);

  constructor() {
    this.coordinatesForm = this.createForm();
  }

  get isEditMode(): boolean {
    return !!this.coordinates?.id;
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
      x: [null, [Validators.required, Validators.min(-146.999999)]], // Matches @NotNull and @DecimalMin(value = "-147", inclusive = false)
      y: [null] // Optional field
    });
  }

  initializeForm(): void {
    if (this.coordinates) {
      this.coordinatesForm.patchValue({
        x: this.coordinates.x,
        y: this.coordinates.y
      });
    } else {
      this.coordinatesForm.reset();
    }
  }

  isInvalid(fieldName: string): boolean {
    const field = this.coordinatesForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  getFieldError(fieldName: string): string {
    const field = this.coordinatesForm.get(fieldName);

    if (!field || !field.errors) return '';

    if (field.errors['required']) {
      return 'Это поле обязательно';
    }
    if (field.errors['min'] && fieldName === 'x') {
      return 'Координата X должна быть больше -147';
    }

    return 'Недопустимое значение';
  }

  onSubmit(): void {
    this.markFormAsTouched();

    if (this.coordinatesForm.invalid) {
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
    this.coordinatesForm.markAllAsTouched();
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

    const coordinatesRequest: CoordinatesRequest = {
      x: this.coordinatesForm.value.x,
      y: this.coordinatesForm.value.y || undefined // Optional field
    };

    this.coordinatesSubmit.emit(coordinatesRequest);
  }

  private resetForm(): void {
    this.coordinatesForm.reset();
    this.isLoading = false;
  }
}
