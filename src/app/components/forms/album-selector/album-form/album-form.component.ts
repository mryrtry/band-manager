import { CommonModule } from '@angular/common';
import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { MessageService } from 'primeng/api';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

import { Button, ButtonDirective, ButtonIcon, ButtonLabel } from 'primeng/button';
import { InputNumber } from 'primeng/inputnumber';
import { Message } from 'primeng/message';
import { InputText } from 'primeng/inputtext';

import { Album } from '../../../../model/core/album/album.model';
import { AlbumRequest } from '../../../../model/core/album/album.request';

@Component({
  selector: 'app-album-form',
  templateUrl: './album-form.component.html',
  styleUrls: ['../../form.component.scss'],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    InputText,
    InputNumber,
    Message,
    Button,
    ButtonDirective,
    ButtonIcon,
    ButtonLabel
  ],
  standalone: true
})
export class AlbumFormComponent implements OnInit {
  // Inputs
  @Input() album: Album | null = null;

  // Outputs
  @Output() albumSubmit = new EventEmitter<AlbumRequest>();
  @Output() cancel = new EventEmitter<void>();

  // State
  isLoading = false;
  albumForm: FormGroup;

  // Services
  private messageService = inject(MessageService);
  private fb = inject(FormBuilder);

  constructor() {
    this.albumForm = this.createForm();
  }

  get isEditMode(): boolean {
    return !!this.album?.id;
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
      name: ['', [Validators.required, Validators.minLength(1)]], // Matches @NotBlank
      tracks: [null, [Validators.required, Validators.min(0.1)]], // Matches @NotNull and @DecimalMin(value = "0", inclusive = false)
      sales: [null, [Validators.min(0.1)]] // Matches @DecimalMin(value = "0", inclusive = false), optional
    });
  }

  initializeForm(): void {
    if (this.album) {
      this.albumForm.patchValue({
        name: this.album.name,
        tracks: this.album.tracks,
        sales: this.album.sales
      });
    } else {
      this.albumForm.reset();
    }
  }

  isInvalid(fieldName: string): boolean {
    const field = this.albumForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  getFieldError(fieldName: string): string {
    const field = this.albumForm.get(fieldName);

    if (!field || !field.errors) return '';

    if (field.errors['required']) {
      return 'Это поле обязательно';
    }
    if (field.errors['minlength'] && fieldName === 'name') {
      return 'Название не может быть пустым';
    }
    if (field.errors['min']) {
      if (fieldName === 'tracks') {
        return 'Количество треков должно быть > 0';
      }
      if (fieldName === 'sales') {
        return 'Продажи должны быть > 0';
      }
    }

    return 'Недопустимое значение';
  }

  onSubmit(): void {
    this.markFormAsTouched();

    if (this.albumForm.invalid) {
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
    this.albumForm.markAllAsTouched();
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

    const albumRequest: AlbumRequest = {
      name: this.albumForm.value.name.trim(), // Ensure no leading/trailing whitespace
      tracks: this.albumForm.value.tracks,
      sales: this.albumForm.value.sales || undefined // Optional field
    };

    this.albumSubmit.emit(albumRequest);
  }

  private resetForm(): void {
    this.albumForm.reset();
    this.isLoading = false;
  }
}
