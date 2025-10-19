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
  mode = signal<'select' | 'create'>('select');
  errorMessage = signal<string | null>(null);

  selectedLocation = computed(() => {
    const id = this.selectedLocationId();
    if (id === null) return null;
    return this.locations().find(loc => loc.id === id) || null;
  });

  locationForm: FormGroup;

  constructor() {
    this.locationForm = this.fb.group({
      x: [null],
      y: [null, [Validators.required]],
      z: [null, [Validators.required]]
    });
  }

  async ngOnInit() {
    await this.loadLocations();
  }

  async loadLocations() {
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
    } finally {
      this.isLoading.set(false);
    }
  }

  private formatLocationDisplay(location: Location): string {
    const xDisplay = location.x !== null ? `X: ${location.x}` : 'X: не указано';
    return `${xDisplay}, Y: ${location.y}, Z: ${location.z}`;
  }

  switchMode(newMode: 'select' | 'create') {
    this.mode.set(newMode);
    this.errorMessage.set(null);
    this.locationForm.reset();
  }

  onLocationSelect(locationId: number) {
    this.selectedLocationId.set(locationId);
    this.locationSelected.emit(locationId);
  }

  clearSelection() {
    this.selectedLocationId.set(null);
    this.locationSelected.emit(null);
  }

  async createLocation() {
    if (this.locationForm.invalid) {
      this.markFormGroupTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    try {
      const formData = this.locationForm.value;

      const locationData: LocationRequest = {
        x: formData.x !== null && formData.x !== '' && !isNaN(Number(formData.x)) ? Number(formData.x) : null,
        y: Number(formData.y),
        z: Number(formData.z)
      };

      const createdLocation = await firstValueFrom(this.locationService.create(locationData));

      const newLocation: LocationOption = {
        id: createdLocation.id,
        x: createdLocation.x,
        y: createdLocation.y,
        z: createdLocation.z,
        displayName: this.formatLocationDisplay(createdLocation)
      };

      this.locations.update(locations => [...locations, newLocation]);
      this.locationOptions.update(options => [
        ...options,
        {value: newLocation.id, label: newLocation.displayName}
      ]);

      this.selectLocation(newLocation);
      this.mode.set('select');
      this.locationForm.reset();

    } catch (error: any) {
      if (error?.error?.details) {
        const backendErrors = error.error.details;
        this.handleBackendErrors(backendErrors);
      } else {
        this.errorMessage.set('Ошибка создания локации');
      }
      console.error('Error creating location:', error);
    } finally {
      this.isLoading.set(false);
    }
  }

  private selectLocation(location: LocationOption) {
    this.selectedLocationId.set(location.id);
    this.locationSelected.emit(location.id);
  }

  private markFormGroupTouched() {
    Object.keys(this.locationForm.controls).forEach(key => {
      const control = this.locationForm.get(key);
      control?.markAsTouched();
    });
  }

  private handleBackendErrors(errors: any[]) {
    Object.keys(this.locationForm.controls).forEach(key => {
      const control = this.locationForm.get(key);
      if (control?.errors?.['serverError']) {
        const {serverError, ...otherErrors} = control.errors;
        control.setErrors(Object.keys(otherErrors).length > 0 ? otherErrors : null);
      }
    });

    let hasGlobalError = false;

    errors.forEach(error => {
      const field = error.field.toLowerCase();
      const control = this.locationForm.get(field);

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
    return this.locationForm.valid;
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
