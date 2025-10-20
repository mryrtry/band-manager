import { Component, Output, EventEmitter, OnInit, signal, inject, Input, ViewChild, OnChanges, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MusicBandService } from '../../services/music-band.service';
import { MusicBandRequest } from '../../models/requests/music-band-request.model';
import { MusicBand } from '../../models/music-band.model';
import { getMusicGenreOptions } from '../../models/enums/music-genre.model';
import { firstValueFrom } from 'rxjs';
import { PersonSelectorComponent } from '../../selectors/person-selector/person-selector.component';
import { AlbumSelectorComponent } from '../../selectors/album-selector/album-selector.component';
import { CustomSelectComponent } from '../select/select.component';
import {CoordinatesSelectorComponent} from '../../selectors/coordinate-selector/coordinates-selector.component';

interface SelectOption {
  value: any;
  label: string;
}

type FormMode = 'create' | 'edit';

@Component({
  selector: 'app-music-band-form',
  templateUrl: './music-band-form.component.html',
  styleUrls: ['./music-band-form.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    CustomSelectComponent,
    CoordinatesSelectorComponent,
    AlbumSelectorComponent,
    PersonSelectorComponent
  ]
})
export class MusicBandFormComponent implements OnInit, OnChanges {
  @Output() formSubmit = new EventEmitter<MusicBand>();
  @Output() formCancel = new EventEmitter<void>();
  @Input() mode: FormMode = 'create';
  @Input() musicBandId?: number;
  @Input() initialData?: MusicBand;

  private musicBandService = inject(MusicBandService);
  private fb = inject(FormBuilder);

  isLoading = signal(false);
  errorMessage = signal<string | null>(null);
  isInitialized = signal(false);

  // ID выбранных сущностей
  coordinatesId = signal<number | null>(null);
  bestAlbumId = signal<number | null>(null);
  frontManId = signal<number | null>(null);

  genreOptions: SelectOption[] = getMusicGenreOptions();

  musicBandForm: FormGroup;

  @ViewChild(CoordinatesSelectorComponent) coordinatesSelector!: CoordinatesSelectorComponent;
  @ViewChild(PersonSelectorComponent) personSelector!: PersonSelectorComponent;
  @ViewChild(AlbumSelectorComponent) albumSelector!: AlbumSelectorComponent;

  constructor() {
    this.musicBandForm = this.createForm();
  }

  private createForm(): FormGroup {
    return this.fb.group({
      name: ['', [Validators.required]],
      description: ['', [Validators.required]],
      genre: [null, [Validators.required]],
      numberOfParticipants: [null, [Validators.required, Validators.min(1)]],
      singlesCount: [null, [Validators.required, Validators.min(1)]],
      albumsCount: [null, [Validators.required, Validators.min(1)]],
      establishmentDate: ['', [Validators.required]]
    });
  }

  async ngOnInit() {
    if (this.mode === 'edit' && this.musicBandId && !this.initialData) {
      await this.loadMusicBandData();
    } else if (this.initialData) {
      this.populateFormWithData(this.initialData);
    }
    this.isInitialized.set(true);
  }

  async ngOnChanges(changes: SimpleChanges) {
    if (changes['musicBandId'] && this.mode === 'edit' && this.musicBandId && this.isInitialized()) {
      await this.loadMusicBandData();
    }

    if (changes['initialData'] && this.initialData && this.isInitialized()) {
      this.populateFormWithData(this.initialData);
    }
  }

  private async loadMusicBandData(): Promise<void> {
    if (!this.musicBandId) return;

    this.isLoading.set(true);
    try {
      const musicBand = await firstValueFrom(
        this.musicBandService.getMusicBandById(this.musicBandId)
      );
      this.populateFormWithData(musicBand);
    } catch (error) {
      this.errorMessage.set('Ошибка загрузки данных группы');
      console.error('Error loading music band:', error);
    } finally {
      this.isLoading.set(false);
    }
  }

  private populateFormWithData(musicBand: MusicBand): void {
    // Заполняем основные поля формы
    this.musicBandForm.patchValue({
      name: musicBand.name,
      description: musicBand.description,
      genre: musicBand.genre,
      numberOfParticipants: musicBand.numberOfParticipants,
      singlesCount: musicBand.singlesCount,
      albumsCount: musicBand.albumsCount,
      establishmentDate: this.formatDate(musicBand.establishmentDate)
    });

    // Устанавливаем ID из вложенных объектов
    this.coordinatesId.set(musicBand.coordinates.id);
    this.bestAlbumId.set(musicBand.bestAlbum.id);
    this.frontManId.set(musicBand.frontMan.id);

    // Программно выбираем значения в селекторах
    setTimeout(() => {
      if (this.coordinatesSelector) {
        this.coordinatesSelector.selectedCoordinatesId.set(musicBand.coordinates.id);
      }
      if (this.albumSelector) {
        this.albumSelector.selectedAlbumId.set(musicBand.bestAlbum.id);
      }
      if (this.personSelector) {
        this.personSelector.selectedPersonId.set(musicBand.frontMan.id);
      }
    });
  }

  private formatDate(dateString: string): string {
    return dateString.split('T')[0];
  }

  // Обработчики вложенных селекторов
  onCoordinatesSelected(coordinatesId: number | null): void {
    this.coordinatesId.set(coordinatesId);
  }

  onAlbumSelected(albumId: number | null): void {
    this.bestAlbumId.set(albumId);
  }

  onPersonSelected(personId: number | null): void {
    this.frontManId.set(personId);
  }

  async onSubmit(): Promise<void> {
    if (this.musicBandForm.invalid || !this.coordinatesId() || !this.bestAlbumId() || !this.frontManId()) {
      this.markFormGroupTouched();

      // Показываем ошибки для селекторов
      if (!this.coordinatesId()) {
        this.errorMessage.set('Необходимо выбрать координаты');
      } else if (!this.bestAlbumId()) {
        this.errorMessage.set('Необходимо выбрать лучший альбом');
      } else if (!this.frontManId()) {
        this.errorMessage.set('Необходимо выбрать лидера группы');
      }

      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    try {
      const formData = this.musicBandForm.value;

      const musicBandRequest: MusicBandRequest = {
        name: formData.name,
        description: formData.description,
        genre: formData.genre,
        numberOfParticipants: Number(formData.numberOfParticipants),
        singlesCount: Number(formData.singlesCount),
        albumsCount: Number(formData.albumsCount),
        establishmentDate: formData.establishmentDate,
        coordinatesId: this.coordinatesId()!,
        bestAlbumId: this.bestAlbumId()!,
        frontManId: this.frontManId()!
      };

      let result: MusicBand;

      if (this.mode === 'edit' && this.musicBandId) {
        result = await firstValueFrom(
          this.musicBandService.updateMusicBand(this.musicBandId, musicBandRequest)
        );
      } else {
        result = await firstValueFrom(
          this.musicBandService.createMusicBand(musicBandRequest)
        );
      }

      this.formSubmit.emit(result);
      this.resetForm();

    } catch (error: any) {
      this.handleSubmitError(error);
    } finally {
      this.isLoading.set(false);
    }
  }

  private handleSubmitError(error: any): void {
    if (error?.error?.details) {
      const backendErrors = error.error.details;
      this.handleBackendErrors(backendErrors);
    } else {
      this.errorMessage.set(`Ошибка ${this.mode === 'edit' ? 'сохранения' : 'создания'} музыкальной группы`);
    }
    console.error(`Error ${this.mode === 'edit' ? 'updating' : 'creating'} music band:`, error);
  }

  onCancel(): void {
    this.formCancel.emit();
    this.resetForm();
  }

  private resetForm(): void {
    this.musicBandForm.reset();
    this.coordinatesId.set(null);
    this.bestAlbumId.set(null);
    this.frontManId.set(null);
    this.errorMessage.set(null);

    // Сбрасываем дочерние селекторы
    if (this.coordinatesSelector) {
      this.coordinatesSelector.reset();
    }
    if (this.albumSelector) {
      this.albumSelector.reset();
    }
    if (this.personSelector) {
      this.personSelector.reset();
    }
  }

  private markFormGroupTouched(): void {
    Object.keys(this.musicBandForm.controls).forEach(key => {
      this.musicBandForm.get(key)?.markAsTouched();
    });
  }

  private handleBackendErrors(errors: any[]): void {
    // Очищаем предыдущие серверные ошибки
    Object.keys(this.musicBandForm.controls).forEach(key => {
      const control = this.musicBandForm.get(key);
      if (control?.errors?.['serverError']) {
        const { serverError, ...otherErrors } = control.errors;
        control.setErrors(Object.keys(otherErrors).length > 0 ? otherErrors : null);
      }
    });

    let hasGlobalError = false;

    errors.forEach(error => {
      const field = error.field?.toLowerCase();
      const control = field ? this.musicBandForm.get(field) : null;

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
  get formTitle(): string {
    return this.mode === 'edit' ? 'Редактирование музыкальной группы' : 'Создание музыкальной группы';
  }

  get submitButtonText(): string {
    if (this.isLoading()) {
      return this.mode === 'edit' ? 'Сохранение...' : 'Создание...';
    }
    return this.mode === 'edit' ? 'Сохранить изменения' : 'Создать группу';
  }

  get isFormValid(): boolean {
    return this.musicBandForm.valid &&
      this.coordinatesId() !== null &&
      this.bestAlbumId() !== null &&
      this.frontManId() !== null;
  }

  getFieldError(fieldName: string): string {
    const control = this.musicBandForm.get(fieldName);
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
    const control = this.musicBandForm.get(fieldName);
    return !!(control?.invalid && (control.touched || control.dirty));
  }

  hasSelectorError(selectorId: number | null): boolean {
    return selectorId === null && (this.musicBandForm.touched || this.musicBandForm.dirty);
  }
}
