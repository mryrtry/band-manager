import { Component, Output, EventEmitter, OnInit, signal, computed, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AlbumService } from '../../services/album.service';
import { AlbumRequest } from '../../models/requests/album-request.model';
import { firstValueFrom } from 'rxjs';
import {CustomSelectComponent} from '../../components/select/select.component';

interface SelectOption {
  value: any;
  label: string;
}

interface AlbumOption {
  id: number;
  name: string;
  tracks: number;
  sales: number;
  displayName: string;
}

@Component({
  selector: 'app-album-selector',
  templateUrl: './album-selector.component.html',
  styleUrls: ['./album-selector.component.scss'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, CustomSelectComponent]
})
export class AlbumSelectorComponent implements OnInit {
  @Output() albumSelected = new EventEmitter<number | null>();

  private albumService = inject(AlbumService);
  private fb = inject(FormBuilder);

  albums = signal<AlbumOption[]>([]);
  albumOptions = signal<SelectOption[]>([]);
  isLoading = signal(false);
  selectedAlbumId = signal<number | null>(null);
  mode = signal<'select' | 'create'>('select');
  errorMessage = signal<string | null>(null);

  selectedAlbum = computed(() => {
    const id = this.selectedAlbumId();
    if (id === null) return null;
    return this.albums().find(album => album.id === id) || null;
  });

  albumForm: FormGroup;

  constructor() {
    this.albumForm = this.fb.group({
      name: ['', [Validators.required]],
      tracks: [null, [Validators.required, Validators.min(1)]],
      sales: [null, [Validators.required, Validators.min(0)]]
    });
  }

  async ngOnInit() {
    await this.loadAlbums();
  }

  async loadAlbums() {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    try {
      const albums = await firstValueFrom(this.albumService.getAll());
      const albumOptions = albums.map(album => ({
        id: album.id,
        name: album.name,
        tracks: album.tracks,
        sales: album.sales,
        displayName: `${album.name} (${album.tracks} треков, ${album.sales} продаж)`
      }));

      this.albums.set(albumOptions);
      this.albumOptions.set(
        albumOptions.map(album => ({
          value: album.id,
          label: album.displayName
        }))
      );
    } catch (error) {
      this.errorMessage.set('Ошибка загрузки альбомов');
      console.error('Error loading albums:', error);
    } finally {
      this.isLoading.set(false);
    }
  }

  switchMode(newMode: 'select' | 'create') {
    this.mode.set(newMode);
    this.errorMessage.set(null);
    this.albumForm.reset();
  }

  onAlbumSelect(albumId: number) {
    this.selectedAlbumId.set(albumId);
    this.albumSelected.emit(albumId);
  }

  clearSelection() {
    this.selectedAlbumId.set(null);
    this.albumSelected.emit(null);
  }

  async createAlbum() {
    if (this.albumForm.invalid) {
      this.markFormGroupTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    try {
      const formData = this.albumForm.value;

      const albumData: AlbumRequest = {
        name: formData.name,
        tracks: Number(formData.tracks),
        sales: Number(formData.sales)
      };

      const createdAlbum = await firstValueFrom(this.albumService.create(albumData));

      const newAlbum: AlbumOption = {
        id: createdAlbum.id,
        name: createdAlbum.name,
        tracks: createdAlbum.tracks,
        sales: createdAlbum.sales,
        displayName: `${createdAlbum.name} (${createdAlbum.tracks} треков, ${createdAlbum.sales} продаж)`
      };

      this.albums.update(albums => [...albums, newAlbum]);
      this.albumOptions.update(options => [
        ...options,
        { value: newAlbum.id, label: newAlbum.displayName }
      ]);

      this.selectAlbum(newAlbum);
      this.mode.set('select');
      this.albumForm.reset();

    } catch (error: any) {
      if (error?.error?.details) {
        const backendErrors = error.error.details;
        this.handleBackendErrors(backendErrors);
      } else {
        this.errorMessage.set('Ошибка создания альбома');
      }
      console.error('Error creating album:', error);
    } finally {
      this.isLoading.set(false);
    }
  }

  private selectAlbum(album: AlbumOption) {
    this.selectedAlbumId.set(album.id);
    this.albumSelected.emit(album.id);
  }

  private markFormGroupTouched() {
    Object.keys(this.albumForm.controls).forEach(key => {
      const control = this.albumForm.get(key);
      control?.markAsTouched();
    });
  }

  private handleBackendErrors(errors: any[]) {
    Object.keys(this.albumForm.controls).forEach(key => {
      const control = this.albumForm.get(key);
      if (control?.errors?.['serverError']) {
        const { serverError, ...otherErrors } = control.errors;
        control.setErrors(Object.keys(otherErrors).length > 0 ? otherErrors : null);
      }
    });

    let hasGlobalError = false;

    errors.forEach(error => {
      const field = error.field.toLowerCase();
      const control = this.albumForm.get(field);

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
    return this.albumForm.valid;
  }

  getFieldError(fieldName: string): string {
    const control = this.albumForm.get(fieldName);
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
    const control = this.albumForm.get(fieldName);
    return !!(control?.invalid && (control.touched || control.dirty));
  }
}
