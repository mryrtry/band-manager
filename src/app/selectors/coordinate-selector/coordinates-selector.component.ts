import { Component, Output, EventEmitter, OnInit, signal, computed, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { CoordinatesService } from '../../services/coordinates.service';
import { CoordinatesRequest } from '../../models/requests/coordinates-request.model';
import { firstValueFrom } from 'rxjs';
import {CustomSelectComponent} from '../../components/select/select.component';

interface SelectOption {
  value: any;
  label: string;
}

interface CoordinatesOption {
  id: number;
  x: number;
  y: number;
  displayName: string;
}

@Component({
  selector: 'app-coordinates-selector',
  templateUrl: './coordinates-selector.component.html',
  styleUrls: ['./coordinates-selector.component.scss'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, CustomSelectComponent]
})
export class CoordinatesSelectorComponent implements OnInit {
  @Output() coordinatesSelected = new EventEmitter<number | null>();

  private coordinatesService = inject(CoordinatesService);
  private fb = inject(FormBuilder);

  coordinates = signal<CoordinatesOption[]>([]);
  coordinatesOptions = signal<SelectOption[]>([]);
  isLoading = signal(false);
  selectedCoordinatesId = signal<number | null>(null);
  mode = signal<'select' | 'create'>('select');
  errorMessage = signal<string | null>(null);

  selectedCoordinates = computed(() => {
    const id = this.selectedCoordinatesId();
    if (id === null) return null;
    return this.coordinates().find(coord => coord.id === id) || null;
  });

  coordinatesForm: FormGroup;

  constructor() {
    this.coordinatesForm = this.fb.group({
      x: [null, [Validators.required, Validators.min(-146)]],
      y: [null]
    });
  }

  async ngOnInit() {
    await this.loadCoordinates();
  }

  async loadCoordinates() {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    try {
      const coordinates = await firstValueFrom(this.coordinatesService.getAll());
      const coordinatesOptions = coordinates.map(coord => ({
        id: coord.id,
        x: coord.x,
        y: coord.y,
        displayName: `X: ${coord.x}, Y: ${coord.y}`
      }));

      this.coordinates.set(coordinatesOptions);
      this.coordinatesOptions.set(
        coordinatesOptions.map(coord => ({
          value: coord.id,
          label: coord.displayName
        }))
      );
    } catch (error) {
      this.errorMessage.set('Ошибка загрузки координат');
      console.error('Error loading coordinates:', error);
    } finally {
      this.isLoading.set(false);
    }
  }

  switchMode(newMode: 'select' | 'create') {
    this.mode.set(newMode);
    this.errorMessage.set(null);
    this.coordinatesForm.reset();
  }

  onCoordinatesSelect(coordinatesId: number) {
    this.selectedCoordinatesId.set(coordinatesId);
    this.coordinatesSelected.emit(coordinatesId);
  }

  clearSelection() {
    this.selectedCoordinatesId.set(null);
    this.coordinatesSelected.emit(null);
  }

  async createCoordinates() {
    if (this.coordinatesForm.invalid) {
      this.markFormGroupTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    try {
      const formData = this.coordinatesForm.value;

      const coordinatesData: CoordinatesRequest = {
        x: Number(formData.x),
        y: formData.y !== null && formData.y !== '' && !isNaN(Number(formData.y)) ? Number(formData.y) : null
      };

      const createdCoordinates = await firstValueFrom(this.coordinatesService.create(coordinatesData));

      const newCoordinates: CoordinatesOption = {
        id: createdCoordinates.id,
        x: createdCoordinates.x,
        y: createdCoordinates.y,
        displayName: `X: ${createdCoordinates.x}, Y: ${createdCoordinates.y}`
      };

      this.coordinates.update(coords => [...coords, newCoordinates]);
      this.coordinatesOptions.update(options => [
        ...options,
        { value: newCoordinates.id, label: newCoordinates.displayName }
      ]);

      this.selectCoordinates(newCoordinates);
      this.mode.set('select');
      this.coordinatesForm.reset();

    } catch (error: any) {
      if (error?.error?.details) {
        const backendErrors = error.error.details;
        this.handleBackendErrors(backendErrors);
      } else {
        this.errorMessage.set('Ошибка создания координат');
      }
      console.error('Error creating coordinates:', error);
    } finally {
      this.isLoading.set(false);
    }
  }

  private selectCoordinates(coordinates: CoordinatesOption) {
    this.selectedCoordinatesId.set(coordinates.id);
    this.coordinatesSelected.emit(coordinates.id);
  }

  private markFormGroupTouched() {
    Object.keys(this.coordinatesForm.controls).forEach(key => {
      const control = this.coordinatesForm.get(key);
      control?.markAsTouched();
    });
  }

  private handleBackendErrors(errors: any[]) {
    Object.keys(this.coordinatesForm.controls).forEach(key => {
      const control = this.coordinatesForm.get(key);
      if (control?.errors?.['serverError']) {
        const { serverError, ...otherErrors } = control.errors;
        control.setErrors(Object.keys(otherErrors).length > 0 ? otherErrors : null);
      }
    });

    let hasGlobalError = false;

    errors.forEach(error => {
      const field = error.field.toLowerCase();
      const control = this.coordinatesForm.get(field);

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
    return this.coordinatesForm.valid;
  }

  getFieldError(fieldName: string): string {
    const control = this.coordinatesForm.get(fieldName);
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
    const control = this.coordinatesForm.get(fieldName);
    return !!(control?.invalid && (control.touched || control.dirty));
  }
}
