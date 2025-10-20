import { Component, Output, EventEmitter, OnInit, signal, computed, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AlbumService } from '../../services/album.service';
import { AlbumRequest } from '../../models/requests/album-request.model';
import { firstValueFrom } from 'rxjs';
import { CustomSelectComponent } from '../../components/select/select.component';

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

type ComponentMode = 'select' | 'create' | 'edit';

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
  editingAlbumId = signal<number | null>(null);
  mode = signal<ComponentMode>('select');
  errorMessage = signal<string | null>(null);

  selectedAlbum = computed(() => {
    const id = this.selectedAlbumId();
    return id ? this.albums().find(album => album.id === id) || null : null;
  });

  editingAlbum = computed(() => {
    const id = this.editingAlbumId();
    return id ? this.albums().find(album => album.id === id) || null : null;
  });

  albumForm: FormGroup;

  public reset() {
    this.selectedAlbumId.set(null);
    this.editingAlbumId.set(null);
    this.mode.set('select');
    this.albumForm.reset();
    this.errorMessage.set(null);
  }

  constructor() {
    this.albumForm = this.createForm();
  }

  private createForm(): FormGroup {
    return this.fb.group({
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

  switchMode(newMode: ComponentMode, albumId?: number): void {
    this.mode.set(newMode);
    this.errorMessage.set(null);
    this.albumForm.reset();

    if (newMode === 'edit' && albumId) {
      this.editingAlbumId.set(albumId);
      this.populateFormForEditing();
    } else {
      this.editingAlbumId.set(null);
    }
  }

  private populateFormForEditing(): void {
    const album = this.editingAlbum();
    if (!album) return;

    this.albumForm.patchValue({
      name: album.name,
      tracks: album.tracks,
      sales: album.sales
    });
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

    await this.saveAlbum();
  }

  async updateAlbum() {
    if (this.albumForm.invalid) {
      this.markFormGroupTouched();
      return;
    }

    await this.saveAlbum(true);
  }

  private async saveAlbum(isUpdate: boolean = false): Promise<void> {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    try {
      const formData = this.albumForm.value;
      const albumData: AlbumRequest = {
        name: formData.name,
        tracks: Number(formData.tracks),
        sales: Number(formData.sales)
      };

      let savedAlbum;

      if (isUpdate && this.editingAlbumId()) {
        savedAlbum = await firstValueFrom(
          this.albumService.update(this.editingAlbumId()!, albumData)
        );
      } else {
        savedAlbum = await firstValueFrom(
          this.albumService.create(albumData)
        );
      }

      await this.handleSaveSuccess(savedAlbum, isUpdate);

    } catch (error: any) {
      this.handleSaveError(error);
    } finally {
      this.isLoading.set(false);
    }
  }

  private async handleSaveSuccess(album: any, isUpdate: boolean): Promise<void> {
    if (isUpdate) {
      await this.loadAlbums(); // Reload to get updated list
    } else {
      this.addAlbumToLists(album);
    }

    this.selectAlbum(album);
    this.switchMode('select');
    this.albumForm.reset();
  }

  private addAlbumToLists(album: any): void {
    const newAlbum: AlbumOption = {
      id: album.id,
      name: album.name,
      tracks: album.tracks,
      sales: album.sales,
      displayName: `${album.name} (${album.tracks} треков, ${album.sales} продаж)`
    };

    this.albums.update(albums => [...albums, newAlbum]);
    this.albumOptions.update(options => [
      ...options,
      { value: newAlbum.id, label: newAlbum.displayName }
    ]);
  }

  private selectAlbum(album: any): void {
    this.selectedAlbumId.set(album.id);
    this.albumSelected.emit(album.id);
  }

  private handleSaveError(error: any): void {
    if (error?.error?.details) {
      const backendErrors = error.error.details;
      this.handleBackendErrors(backendErrors);
    } else {
      this.errorMessage.set(`Ошибка ${this.isEditMode ? 'обновления' : 'создания'} альбома`);
    }
    console.error(`Error ${this.isEditMode ? 'updating' : 'creating'} album:`, error);
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
      const field = error.field?.toLowerCase();
      const control = field ? this.albumForm.get(field) : null;

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
    return this.albumForm.valid;
  }

  get submitButtonText(): string {
    if (this.isLoading()) {
      return this.isEditMode ? 'Обновление...' : 'Создание...';
    }
    return this.isEditMode ? 'Обновить альбом' : 'Создать альбом';
  }

  get formTitle(): string {
    return this.isEditMode ? 'Редактирование альбома' : 'Создание альбома';
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
