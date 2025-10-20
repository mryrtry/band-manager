import {Component, computed, EventEmitter, inject, OnInit, Output, signal} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {LocationService} from '../../services/location.service';
import {LocationRequest} from '../../models/requests/location-request.model';
import {Location} from '../../models/location.model';
import {firstValueFrom} from 'rxjs';
import {CustomSelectComponent} from '../../components/select/select.component';

interface SelectOption {
  value: any;
  label: string;
}

interface LocationOption {
  id: number;
  x: number | null;
  y: number;
  z: number;
  displayName: string;
}

type ComponentMode = 'select' | 'create' | 'edit';

@Component({
  selector: 'app-location-selector',
  templateUrl: './location-selector.component.html',
  styleUrls: ['./location-selector.component.scss'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, CustomSelectComponent]
})
export class LocationSelectorComponent implements OnInit {
  @Output() locationSelected = new EventEmitter<number | null>();

  private locationService = inject(LocationService);
  private fb = inject(FormBuilder);

  locations = signal<LocationOption[]>([]);
  locationOptions = signal<SelectOption[]>([]);
  isLoading = signal(false);
  selectedLocationId = signal<number | null>(null);
  editingLocationId = signal<number | null>(null);
  mode = signal<ComponentMode>('select');
  errorMessage = signal<string | null>(null);

  selectedLocation = computed(() => {
    const id = this.selectedLocationId();
    return id ? this.locations().find(loc => loc.id === id) || null : null;
  });

  editingLocation = computed(() => {
    const id = this.editingLocationId();
    return id ? this.locations().find(loc => loc.id === id) || null : null;
  });

  locationForm: FormGroup;

  constructor() {
    this.locationForm = this.createForm();
  }

  private createForm(): FormGroup {
    return this.fb.group({
      x: [null],
      y: [null, [Validators.required]],
      z: [null, [Validators.required]]
    });
  }

  async ngOnInit(): Promise<void> {
    await this.loadLocations();
  }

  public reset(): void {
    this.selectedLocationId.set(null);
    this.editingLocationId.set(null);
    this.mode.set('select');
    this.locationForm.reset();
    this.errorMessage.set(null);
  }

  async loadLocations(): Promise<void> {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    try {
      const locations = await firstValueFrom(this.locationService.getAll());
      const locationOptions = locations.map(loc => ({
        id: loc.id,
        x: loc.x,
        y: loc.y,
        z: loc.z,
        displayName: this.formatLocationDisplay(loc)
      }));

      this.locations.set(locationOptions);
      this.locationOptions.set(
        locationOptions.map(loc => ({
          value: loc.id,
          label: loc.displayName
        }))
      );
    } catch (error) {
      this.errorMessage.set('Ошибка загрузки локаций');
      console.error('Error loading locations:', error);
    } finally {
      this.isLoading.set(false);
    }
  }

  private formatLocationDisplay(location: Location): string {
    const xDisplay = location.x !== null ? `X: ${location.x}` : 'X: не указано';
    return `${xDisplay}, Y: ${location.y}, Z: ${location.z}`;
  }

  switchMode(newMode: ComponentMode, locationId?: number): void {
    this.mode.set(newMode);
    this.errorMessage.set(null);
    this.locationForm.reset();

    if (newMode === 'edit' && locationId) {
      this.editingLocationId.set(locationId);
      this.populateFormForEditing();
    } else {
      this.editingLocationId.set(null);
    }
  }

  private populateFormForEditing(): void {
    const location = this.editingLocation();
    if (!location) return;

    this.locationForm.patchValue({
      x: location.x,
      y: location.y,
      z: location.z
    });
  }

  onLocationSelect(locationId: number): void {
    this.selectedLocationId.set(locationId);
    this.locationSelected.emit(locationId);
  }

  clearSelection(): void {
    this.selectedLocationId.set(null);
    this.locationSelected.emit(null);
  }

  async createLocation(): Promise<void> {
    if (this.locationForm.invalid) {
      this.markFormGroupTouched();
      return;
    }

    await this.saveLocation();
  }

  async updateLocation(): Promise<void> {
    if (this.locationForm.invalid) {
      this.markFormGroupTouched();
      return;
    }

    await this.saveLocation(true);
  }

  private async saveLocation(isUpdate: boolean = false): Promise<void> {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    try {
      const formData = this.locationForm.value;
      const locationData: LocationRequest = {
        x: this.parseCoordinate(formData.x),
        y: Number(formData.y),
        z: Number(formData.z)
      };

      let savedLocation: Location;

      if (isUpdate && this.editingLocationId()) {
        savedLocation = await firstValueFrom(
          this.locationService.update(this.editingLocationId()!, locationData)
        );
      } else {
        savedLocation = await firstValueFrom(
          this.locationService.create(locationData)
        );
      }

      await this.handleSaveSuccess(savedLocation, isUpdate);

    } catch (error: any) {
      this.handleSaveError(error);
    } finally {
      this.isLoading.set(false);
    }
  }

  private parseCoordinate(value: any): number | null {
    return value !== null && value !== '' && !isNaN(Number(value))
      ? Number(value)
      : null;
  }

  private async handleSaveSuccess(location: Location, isUpdate: boolean): Promise<void> {
    if (isUpdate) {
      await this.loadLocations(); // Reload to get updated list
    } else {
      this.addLocationToLists(location);
    }

    this.selectLocation(location);
    this.switchMode('select');
    this.locationForm.reset();
  }

  private addLocationToLists(location: Location): void {
    const newLocation: LocationOption = {
      id: location.id,
      x: location.x,
      y: location.y,
      z: location.z,
      displayName: this.formatLocationDisplay(location)
    };

    this.locations.update(locations => [...locations, newLocation]);
    this.locationOptions.update(options => [
      ...options,
      { value: newLocation.id, label: newLocation.displayName }
    ]);
  }

  private selectLocation(location: Location): void {
    this.selectedLocationId.set(location.id);
    this.locationSelected.emit(location.id);
  }

  private handleSaveError(error: any): void {
    if (error?.error?.details) {
      const backendErrors = error.error.details;
      this.handleBackendErrors(backendErrors);
    } else {
      this.errorMessage.set(`Ошибка ${this.isEditMode ? 'обновления' : 'создания'} локации`);
    }
    console.error(`Error ${this.isEditMode ? 'updating' : 'creating'} location:`, error);
  }

  private markFormGroupTouched(): void {
    Object.keys(this.locationForm.controls).forEach(key => {
      this.locationForm.get(key)?.markAsTouched();
    });
  }

  private handleBackendErrors(errors: any[]): void {
    // Clear previous server errors
    Object.keys(this.locationForm.controls).forEach(key => {
      const control = this.locationForm.get(key);
      if (control?.errors?.['serverError']) {
        const { serverError, ...otherErrors } = control.errors;
        control.setErrors(Object.keys(otherErrors).length > 0 ? otherErrors : null);
      }
    });

    let hasGlobalError = false;

    errors.forEach(error => {
      const field = error.field?.toLowerCase();
      const control = field ? this.locationForm.get(field) : null;

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
    return this.locationForm.valid;
  }

  get submitButtonText(): string {
    if (this.isLoading()) {
      return this.isEditMode ? 'Обновление...' : 'Создание...';
    }
    return this.isEditMode ? 'Обновить локацию' : 'Создать локацию';
  }

  get formTitle(): string {
    return this.isEditMode ? 'Редактирование локации' : 'Создание локации';
  }

  getFieldError(fieldName: string): string {
    const control = this.locationForm.get(fieldName);
    if (!control) return '';

    if (control.errors?.['serverError']) {
      return control.errors['serverError'];
    }
    if (control.errors?.['required'] && (control.touched || control.dirty)) {
      return 'Это поле обязательно';
    }
    if (control.errors?.['invalidNumber']) {
      return 'Введите корректное число';
    }

    return '';
  }

  hasFieldError(fieldName: string): boolean {
    const control = this.locationForm.get(fieldName);
    return !!(control?.invalid && (control.touched || control.dirty));
  }
}
