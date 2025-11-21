// best-band-award-form.component.ts
import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import { MessageService } from 'primeng/api';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import { Button } from 'primeng/button';
import { InputNumber } from 'primeng/inputnumber';
import { Message } from 'primeng/message';
import { Select } from 'primeng/select';
import { Tooltip } from 'primeng/tooltip';

import { BestBandAward } from '../../../model/core/best-band-award/best-band-award.model';
import { Role, User } from '../../../model/auth/user.model';
import { BestBandAwardRequest } from '../../../model/core/best-band-award/best-band-award.request';
import { HttpErrorResponse } from '@angular/common/http';
import { BestBandAwardService } from '../../../services/core/best-band-award.service';
import { MusicGenre } from '../../../model/core/music-genre.enum';
import { ErrorDetail } from '../../../model/error-response.model'; // Убедитесь, что этот интерфейс определён

@Component({
  selector: 'app-best-band-award-form',
  templateUrl: './best-band-award-form.component.html',
  styleUrls: ['../form.component.scss', '../selector.component.scss'],
  imports: [CommonModule, FormsModule, ReactiveFormsModule, InputNumber, Select, Message, Button, Tooltip],
  standalone: true
})
export class BestBandAwardFormComponent implements OnInit, OnChanges {
  // Inputs
  @Input() award: BestBandAward | null = null;
  @Input() currentUser?: User;
  @Input() isEditMode = false;

  // Outputs
  @Output() bestBandAwardSubmit = new EventEmitter<BestBandAwardRequest>();
  @Output() cancel = new EventEmitter<void>();
  @Output() modeChange = new EventEmitter<'show' | 'edit' | 'create'>(); // Mode change emitter
  @Output() changesOccurred = new EventEmitter<boolean>(); // New output to indicate changes

  // Form data
  genres = Object.values(MusicGenre);

  // State
  isLoading = false;
  awardForm: FormGroup;

  // Services
  private messageService = inject(MessageService);
  private fb = inject(FormBuilder);
  private awardService = inject(BestBandAwardService);

  constructor() {
    this.awardForm = this.createForm();
  }

  get isViewMode(): boolean {
    return !this.isEditMode && !!this.award?.id;
  }

  get isCreateMode(): boolean {
    return !this.award?.id;
  }

  get submitButtonText(): string {
    return this.isEditMode ? 'Обновить' : 'Создать';
  }

  get submitButtonIcon(): string {
    return this.isEditMode ? 'pi pi-pencil' : 'pi pi-plus';
  }

  ngOnInit(): void {
    this.updateFormState();
    // Emit initial mode based on the current state
    if (this.isCreateMode) {
      this.modeChange.emit('create');
    } else if (this.isViewMode) {
      this.modeChange.emit('show');
    } else {
      this.modeChange.emit('edit');
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['award']) {
      this.updateFormState();
    }
  }

  canUpdate(): boolean {
    if (!this.currentUser || !this.award) return false;
    const isOwner = this.award.createdBy === this.currentUser.username;
    const isSystem = this.award.createdBy === 'system';
    const isAdmin = this.currentUser.roles.includes(Role.ROLE_ADMIN);
    return isOwner || isSystem || isAdmin;
  }

  enterEditMode(): void {
    if (this.canUpdate()) {
      this.isEditMode = true;
      this.updateFormState();
      this.modeChange.emit('edit'); // Emit mode change
    }
  }

  exitEditMode(): void {
    this.isEditMode = false;
    this.updateFormState();
    this.modeChange.emit('show'); // Emit mode change
  }

  enterCreateMode(): void {
    this.isEditMode = false;
    this.updateFormState();
    this.modeChange.emit('create'); // Emit mode change
  }

  isInvalid(fieldName: string): boolean {
    if (this.isViewMode) return false;
    const field = this.awardForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  getFieldError(fieldName: string): string {
    const field = this.awardForm.get(fieldName);

    if (!field || !field.errors) return '';

    if (field.errors['required']) return 'Это поле обязательно';
    if (field.errors['min']) {
      if (fieldName === 'musicBandId') return 'ID музыкальной группы должен быть >= 1';
    }
    // Обработка ошибки сервера в универсальной функции не подходит, если она используется для других полей
    // Лучше использовать специфическую логику в шаблоне, как показано в HTML
    return 'Недопустимое значение';
  }

  onSubmit(): void {
    if (this.isViewMode) return;

    this.markFormAsTouched();

    if (this.awardForm.invalid) {
      this.showValidationError();
      return;
    }

    this.submitForm();
  }

  onCancel(): void {
    // Only emit changes if form has been modified
    const hasChanges = this.awardForm.dirty && this.awardForm.touched;
    this.changesOccurred.emit(hasChanges);
    this.resetForm();
    this.cancel.emit();
  }

  private createForm(): FormGroup {
    return this.fb.group({
      musicBandId: [null, [Validators.required, Validators.min(1)]],
      genre: [null, [Validators.required]]
    });
  }

  private updateFormState(): void {
    this.patchFormValues();

    if (this.isViewMode) {
      this.awardForm.disable({ emitEvent: false });
    } else {
      this.awardForm.enable({ emitEvent: false });
    }
  }

  private patchFormValues(): void {
    if (this.award) {
      this.awardForm.patchValue({
        musicBandId: this.award.bandId,
        genre: this.award.genre
      });
    } else {
      if (!this.isViewMode) {
        this.awardForm.reset();
      }
    }
  }

  private markFormAsTouched(): void {
    this.awardForm.markAllAsTouched();
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
    const awardRequest: BestBandAwardRequest = {
      musicBandId: this.awardForm.value.musicBandId,
      genre: this.awardForm.value.genre
    };

    // Очищаем старые ошибки сервера перед отправкой
    this.awardForm.get('musicBandId')?.setErrors(null);

    if (this.award?.id) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Обновление не поддерживается',
        detail: 'API не поддерживает обновление BestBandAward'
      });
      this.isLoading = false;
      this.exitEditMode();
    } else {
      this.createAward(awardRequest);
    }
  }

  private createAward(request: BestBandAwardRequest): void {
    this.awardService.createBestBandAward(request).subscribe({
      next: (createdAward) => {
        this.handleAwardCreationSuccess(createdAward);
      },
      error: (error) => this.handleError('Ошибка создания', 'Ошибка при создании награды', error)
    });
  }

  private handleAwardCreationSuccess(createdAward: BestBandAward): void {
    this.award = createdAward;
    this.isLoading = false;
    this.changesOccurred.emit(true); // Emit that changes occurred
    this.showSuccessMessage('Награда создана', createdAward);
    this.enterViewMode();
  }

  private showSuccessMessage(summary: string, award: BestBandAward): void {
    this.messageService.add({
      severity: 'success',
      summary,
      detail: `Награда "${award.genre}" за группу "${award.bandName}" успешно создана`
    });
  }

  private handleError(summary: string, detail: string, error: HttpErrorResponse): void {
    this.isLoading = false;
    let errorMessage = detail;
    if (error.status === 400 && error.error && error.error.message) {
      errorMessage = error.error.message; // Используем сообщение из DTO
    }
    // Обработка ошибки 404 и структуры details
    if (error.status === 404 && error.error && error.error.details) {
      const details = error.error.details as ErrorDetail[];
      details.forEach(detail => {
        if (detail.field === 'service') { // Предполагаем, что ошибка группы приходит под полем 'service'
          // Устанавливаем ошибку на конкретное поле формы, например, musicBandId
          this.awardForm.get('musicBandId')?.setErrors({ serverError: detail.message });
          // Также можно показать сообщение в MessageService
          this.messageService.add({
            severity: 'error',
            summary: 'Ошибка валидации',
            detail: detail.message
          });
        }
      });
      return; // Выходим, чтобы не перезаписать ошибку общим сообщением
    }
    this.messageService.add({ severity: 'error', summary, detail: errorMessage });
    console.error(`${summary}:`, error);
  }

  private resetForm(): void {
    this.awardForm.reset();
    this.isLoading = false;
    this.enterCreateMode();
    this.updateFormState();
  }

  enterViewMode(): void {
    this.isEditMode = false;
    this.updateFormState();
    this.modeChange.emit('show');
  }
}
