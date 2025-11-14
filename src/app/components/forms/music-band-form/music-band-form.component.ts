// Updated form component with change detection
import {CommonModule} from '@angular/common';
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
import {MessageService} from 'primeng/api';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {Button} from 'primeng/button';
import {InputText} from 'primeng/inputtext';
import {InputNumber} from 'primeng/inputnumber';
import {Message} from 'primeng/message';
import {Select} from 'primeng/select';
import {Textarea} from 'primeng/textarea';
import {DatePicker} from 'primeng/datepicker';

import {
  CoordinatesSelectorComponent
} from '../coordinates-selector/coordinates-selector.component';
import {
  AlbumSelectorComponent
} from '../album-selector/album-selector.component';
import {
  PersonSelectorComponent
} from '../person-selector/person-selector.component';
import {MusicBand} from '../../../model/core/music-band/music-band.model';
import {Role, User} from '../../../model/auth/user.model';
import {
  MusicBandRequest
} from '../../../model/core/music-band/music-band.request';
import {MusicGenre} from '../../../model/core/music-genre.enum';
import {HttpErrorResponse} from '@angular/common/http';
import {MusicBandService} from '../../../services/core/music-band.service';
import {Tooltip} from 'primeng/tooltip';

@Component({
  selector: 'app-music-band-form',
  templateUrl: './music-band-form.component.html',
  styleUrls: ['../form.component.scss', '../selector.component.scss'],
  imports: [CommonModule, FormsModule, ReactiveFormsModule, InputText, InputNumber, Select, Message, Button, Textarea, DatePicker, CoordinatesSelectorComponent, AlbumSelectorComponent, PersonSelectorComponent, Tooltip],
  standalone: true
})
export class MusicBandFormComponent implements OnInit, OnChanges {
  // Inputs
  @Input() musicBand: MusicBand | null = null;
  @Input() currentUser?: User;
  @Input() disabled = false;
  @Input() isEditMode = false;

  // Outputs
  @Output() musicBandSubmit = new EventEmitter<MusicBandRequest>();
  @Output() cancel = new EventEmitter<void>();
  @Output() modeChange = new EventEmitter<'show' | 'edit' | 'create'>(); // Mode change emitter
  @Output() changesOccurred = new EventEmitter<boolean>(); // New output to indicate changes

  // Form data
  genres = Object.values(MusicGenre);
  today = new Date();

  // State
  isLoading = false;
  musicBandForm: FormGroup;

  // Services
  private messageService = inject(MessageService);
  private fb = inject(FormBuilder);
  private musicBandService = inject(MusicBandService);

  constructor() {
    this.musicBandForm = this.createForm();
  }

  get isViewMode(): boolean {
    return !this.isEditMode && !!this.musicBand?.id;
  }

  get isCreateMode(): boolean {
    return !this.musicBand?.id;
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
    if (changes['musicBand'] || changes['disabled']) {
      this.updateFormState();
    }
  }

  canUpdate(): boolean {
    if (!this.currentUser || !this.musicBand) return false;
    const isOwner = this.musicBand.createdBy === this.currentUser.username;
    const isSystem = this.musicBand.createdBy === 'system';
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
    const field = this.musicBandForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  getFieldError(fieldName: string): string {
    const field = this.musicBandForm.get(fieldName);

    if (!field || !field.errors) return '';

    if (field.errors['required']) return 'Это поле обязательно';
    if (field.errors['min']) {
      if (fieldName === 'numberOfParticipants') return 'Количество участников должно быть >= 1';
      if (fieldName === 'singlesCount' || fieldName === 'albumsCount') return 'Значение должно быть >= 0';
    }
    if (fieldName === 'description' && field.errors['minlength']) return 'Описание не может быть пустым';
    return 'Недопустимое значение';
  }

  onCoordinatesChange(coordinatesId: number | undefined): void {
    if (!this.isViewMode) {
      this.musicBandForm.get('coordinatesId')?.setValue(coordinatesId);
      this.musicBandForm.get('coordinatesId')?.markAsTouched();
    }
  }

  onBestAlbumChange(albumId: number | undefined): void {
    if (!this.isViewMode) {
      this.musicBandForm.get('bestAlbumId')?.setValue(albumId);
      this.musicBandForm.get('bestAlbumId')?.markAsTouched();
    }
  }

  onFrontManChange(personId: number | undefined): void {
    if (!this.isViewMode) {
      this.musicBandForm.get('frontManId')?.setValue(personId);
      this.musicBandForm.get('frontManId')?.markAsTouched();
    }
  }

  onSubmit(): void {
    if (this.isViewMode) return;

    this.markFormAsTouched();

    if (this.musicBandForm.invalid) {
      this.showValidationError();
      return;
    }

    this.submitForm();
  }

  onCancel(): void {
    // Only emit changes if form has been modified
    const hasChanges = this.musicBandForm.dirty && this.musicBandForm.touched;
    this.changesOccurred.emit(hasChanges);
    this.resetForm();
    this.cancel.emit();
  }

  private createForm(): FormGroup {
    return this.fb.group({
      name: ['', [Validators.required, Validators.minLength(1)]],
      coordinatesId: [null, [Validators.required]],
      genre: [null, [Validators.required]],
      numberOfParticipants: [null, [Validators.required, Validators.min(1)]],
      singlesCount: [null, [Validators.required, Validators.min(0)]],
      description: ['', [Validators.required, Validators.minLength(1)]],
      bestAlbumId: [null, [Validators.required]],
      albumsCount: [null, [Validators.required, Validators.min(0)]],
      establishmentDate: [null, [Validators.required]],
      frontManId: [null, [Validators.required]]
    });
  }

  private updateFormState(): void {
    this.patchFormValues();

    if (this.isViewMode) {
      this.musicBandForm.disable({emitEvent: false});
    } else {
      this.musicBandForm.enable({emitEvent: false});
    }
  }

  private patchFormValues(): void {
    if (this.musicBand) {
      let establishmentDateValue: Date | null = null;
      const dateStr = this.musicBand.establishmentDate;

      if (dateStr) {
        const parsedDate = new Date(dateStr);
        if (!isNaN(parsedDate.getTime())) {
          establishmentDateValue = parsedDate;
        } else {
          console.warn('Некорректная дата основания:', dateStr);
        }
      }

      this.musicBandForm.patchValue({
        name: this.musicBand.name,
        coordinatesId: this.musicBand.coordinates?.id || null,
        genre: this.musicBand.genre,
        numberOfParticipants: this.musicBand.numberOfParticipants,
        singlesCount: this.musicBand.singlesCount,
        description: this.musicBand.description,
        bestAlbumId: this.musicBand.bestAlbum?.id || null,
        albumsCount: this.musicBand.albumsCount,
        establishmentDate: establishmentDateValue,
        frontManId: this.musicBand.frontMan?.id || null
      });
    } else {
      if (!this.isViewMode) {
        this.musicBandForm.reset();
      }
    }
  }

  private markFormAsTouched(): void {
    this.musicBandForm.markAllAsTouched();
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
    const musicBandRequest: MusicBandRequest = {
      name: this.musicBandForm.value.name,
      coordinatesId: this.musicBandForm.value.coordinatesId,
      genre: this.musicBandForm.value.genre,
      numberOfParticipants: this.musicBandForm.value.numberOfParticipants,
      singlesCount: this.musicBandForm.value.singlesCount,
      description: this.musicBandForm.value.description,
      bestAlbumId: this.musicBandForm.value.bestAlbumId,
      albumsCount: this.musicBandForm.value.albumsCount,
      establishmentDate: this.musicBandForm.value.establishmentDate,
      frontManId: this.musicBandForm.value.frontManId
    };

    if (this.musicBand?.id) {
      this.updateMusicBand(this.musicBand.id, musicBandRequest);
    } else {
      this.createMusicBand(musicBandRequest);
    }
  }

  private createMusicBand(request: MusicBandRequest): void {
    this.musicBandService.createMusicBand(request).subscribe({
      next: (createdMusicBand) => {
        this.handleMusicBandCreationSuccess(createdMusicBand);
      },
      error: (error) => this.handleError('Ошибка создания', 'Ошибка при создании музыкальной группы', error)
    });
  }

  private updateMusicBand(id: number, request: MusicBandRequest): void {
    this.musicBandService.updateMusicBand(id, request).subscribe({
      next: (updatedMusicBand) => {
        this.handleMusicBandUpdateSuccess(updatedMusicBand);
      },
      error: (error) => this.handleError('Ошибка обновления', 'Ошибка при обновлении музыкальной группы', error)
    });
  }

  private handleMusicBandCreationSuccess(createdMusicBand: MusicBand): void {
    this.musicBand = createdMusicBand;
    this.isLoading = false;
    this.changesOccurred.emit(true); // Emit that changes occurred
    this.showSuccessMessage('Музыкальная группа создана', createdMusicBand);
    this.enterViewMode();
  }

  private handleMusicBandUpdateSuccess(updatedMusicBand: MusicBand): void {
    this.musicBand = updatedMusicBand;
    this.isLoading = false;
    this.changesOccurred.emit(true); // Emit that changes occurred
    this.exitEditMode(); // Switch back to view mode
    this.showSuccessMessage('Музыкальная группа обновлена', updatedMusicBand);
  }

  private showSuccessMessage(summary: string, musicBand: MusicBand): void {
    this.messageService.add({
      severity: 'success',
      summary,
      detail: `Музыкальная группа "${musicBand.name}" успешно ${this.isCreateMode ? 'создана' : 'обновлена'}`
    });
  }

  private handleError(summary: string, detail: string, error: HttpErrorResponse): void {
    this.isLoading = false;
    this.messageService.add({severity: 'error', summary, detail});
    console.error(`${summary}:`, error);
  }

  private resetForm(): void {
    this.musicBandForm.reset();
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
