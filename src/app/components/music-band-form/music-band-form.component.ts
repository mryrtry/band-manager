import { Component, Output, EventEmitter, OnInit, signal, inject, Input } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MusicBandService } from '../../services/music-band.service';
import { MusicBandRequest } from '../../models/requests/music-band-request.model';
import { MusicBand } from '../../models/music-band.model';
import { getMusicGenreOptions, MusicGenre } from '../../models/enums/music-genre.model';
import { firstValueFrom } from 'rxjs';
import { PersonSelectorComponent } from '../../selectors/person-selector/person-selector.component';
import { AlbumSelectorComponent } from '../../selectors/album-selector/album-selector.component';
import { CoordinatesSelectorComponent } from '../../selectors/coordinate-selector/coordinates-selector.component';
import { CustomSelectComponent } from '../select/select.component';

interface SelectOption {
  value: any;
  label: string;
}

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
export class MusicBandFormComponent implements OnInit {
  @Output() formSubmit = new EventEmitter<MusicBand>();
  @Output() formCancel = new EventEmitter<void>();
  @Input() initialData?: MusicBand;

  private musicBandService = inject(MusicBandService);
  private fb = inject(FormBuilder);

  isLoading = signal(false);
  errorMessage = signal<string | null>(null);

  coordinatesId = signal<number | null>(null);
  bestAlbumId = signal<number | null>(null);
  frontManId = signal<number | null>(null);

  // Сигналы для начальных значений селекторов
  initialCoordinatesId = signal<number | null>(null);
  initialAlbumId = signal<number | null>(null);
  initialPersonId = signal<number | null>(null);

  genreOptions: SelectOption[] = getMusicGenreOptions();

  musicBandForm: FormGroup;

  constructor() {
    this.musicBandForm = this.fb.group({
      name: ['', [Validators.required]],
      description: ['', [Validators.required]],
      genre: [null, [Validators.required]],
      numberOfParticipants: [null, [Validators.required, Validators.min(1)]],
      singlesCount: [null, [Validators.required, Validators.min(1)]],
      albumsCount: [null, [Validators.required, Validators.min(1)]],
      establishmentDate: ['', [Validators.required]]
    });
  }

  ngOnInit() {
    if (this.initialData) {
      this.patchFormWithInitialData();
    }
  }

  private patchFormWithInitialData() {
    console.log('Patching form with initial data:', this.initialData);

    // Заполняем основные поля формы
    this.musicBandForm.patchValue({
      name: this.initialData!.name,
      description: this.initialData!.description,
      genre: this.initialData!.genre,
      numberOfParticipants: this.initialData!.numberOfParticipants,
      singlesCount: this.initialData!.singlesCount,
      albumsCount: this.initialData!.albumsCount,
      establishmentDate: this.formatDate(this.initialData!.establishmentDate)
    });

    // Устанавливаем ID из вложенных объектов
    const coordsId = this.initialData!.coordinates.id;
    const albumId = this.initialData!.bestAlbum.id;
    const personId = this.initialData!.frontMan.id;

    console.log('Setting IDs:', { coordsId, albumId, personId });

    this.coordinatesId.set(coordsId);
    this.bestAlbumId.set(albumId);
    this.frontManId.set(personId);

    // Устанавливаем начальные значения для селекторов
    this.initialCoordinatesId.set(coordsId);
    this.initialAlbumId.set(albumId);
    this.initialPersonId.set(personId);
  }

  private formatDate(dateString: string): string {
    return dateString.split('T')[0];
  }

  // Обработчики вложенных селекторов
  onCoordinatesSelected(coordinatesId: number | null) {
    this.coordinatesId.set(coordinatesId);
  }

  onAlbumSelected(albumId: number | null) {
    this.bestAlbumId.set(albumId);
  }

  onPersonSelected(personId: number | null) {
    this.frontManId.set(personId);
  }

  async onSubmit() {
    if (this.musicBandForm.invalid || !this.coordinatesId() || !this.bestAlbumId() || !this.frontManId()) {
      this.markFormGroupTouched();
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

      if (this.initialData) {
        // PUT запрос для редактирования
        result = await firstValueFrom(
          this.musicBandService.updateMusicBand(this.initialData.id, musicBandRequest)
        );
      } else {
        // POST запрос для создания
        result = await firstValueFrom(
          this.musicBandService.createMusicBand(musicBandRequest)
        );
      }

      this.formSubmit.emit(result);
      this.resetForm();

    } catch (error: any) {
      if (error?.error?.details) {
        const backendErrors = error.error.details;
        this.handleBackendErrors(backendErrors);
      } else {
        this.errorMessage.set(`Ошибка ${this.initialData ? 'сохранения' : 'создания'} музыкальной группы`);
      }
      console.error('Error saving music band:', error);
    } finally {
      this.isLoading.set(false);
    }
  }

  onCancel() {
    this.formCancel.emit();
    this.resetForm();
  }

  private resetForm() {
    this.musicBandForm.reset();
    this.coordinatesId.set(null);
    this.bestAlbumId.set(null);
    this.frontManId.set(null);
    this.initialCoordinatesId.set(null);
    this.initialAlbumId.set(null);
    this.initialPersonId.set(null);
    this.errorMessage.set(null);
  }

  private markFormGroupTouched() {
    Object.keys(this.musicBandForm.controls).forEach(key => {
      const control = this.musicBandForm.get(key);
      control?.markAsTouched();
    });
  }

  private handleBackendErrors(errors: any[]) {
    Object.keys(this.musicBandForm.controls).forEach(key => {
      const control = this.musicBandForm.get(key);
      if (control?.errors?.['serverError']) {
        const { serverError, ...otherErrors } = control.errors;
        control.setErrors(Object.keys(otherErrors).length > 0 ? otherErrors : null);
      }
    });

    let hasGlobalError = false;

    errors.forEach(error => {
      const field = error.field.toLowerCase();
      const control = this.musicBandForm.get(field);

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

  get isEditMode(): boolean {
    return !!this.initialData;
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
}
