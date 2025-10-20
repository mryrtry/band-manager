import { Component, Output, EventEmitter, OnInit, signal, computed, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { CoordinatesService } from '../../services/coordinates.service';
import { CoordinatesRequest } from '../../models/requests/coordinates-request.model';
import { firstValueFrom } from 'rxjs';
import { CustomSelectComponent } from '../../components/select/select.component';

interface SelectOption {
  value: any;
  label: string;
}

interface CoordinatesOption {
  id: number;
  x: number;
  y: number | null;
  displayName: string;
}

type ComponentMode = 'select' | 'create' | 'edit';

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
  editingCoordinatesId = signal<number | null>(null);
  mode = signal<ComponentMode>('select');
  errorMessage = signal<string | null>(null);

  selectedCoordinates = computed(() => {
    const id = this.selectedCoordinatesId();
    return id ? this.coordinates().find(coord => coord.id === id) || null : null;
  });

  editingCoordinates = computed(() => {
    const id = this.editingCoordinatesId();
    return id ? this.coordinates().find(coord => coord.id === id) || null : null;
  });

  coordinatesForm: FormGroup;

  public reset() {
    this.selectedCoordinatesId.set(null);
    this.editingCoordinatesId.set(null);
    this.mode.set('select');
    this.coordinatesForm.reset();
    this.errorMessage.set(null);
  }

  constructor() {
    this.coordinatesForm = this.createForm();
  }

  private createForm(): FormGroup {
    return this.fb.group({
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
        displayName: this.formatCoordinatesDisplay(coord)
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

  private formatCoordinatesDisplay(coordinates: any): string {
    const yDisplay = coordinates.y !== null ? `Y: ${coordinates.y}` : 'Y: не указано';
    return `X: ${coordinates.x}, ${yDisplay}`;
  }

  switchMode(newMode: ComponentMode, coordinatesId?: number): void {
    this.mode.set(newMode);
    this.errorMessage.set(null);
    this.coordinatesForm.reset();

    if (newMode === 'edit' && coordinatesId) {
      this.editingCoordinatesId.set(coordinatesId);
      this.populateFormForEditing();
    } else {
      this.editingCoordinatesId.set(null);
    }
  }

  private populateFormForEditing(): void {
    const coordinates = this.editingCoordinates();
    if (!coordinates) return;

    this.coordinatesForm.patchValue({
      x: coordinates.x,
      y: coordinates.y
    });
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

    await this.saveCoordinates();
  }

  async updateCoordinates() {
    if (this.coordinatesForm.invalid) {
      this.markFormGroupTouched();
      return;
    }

    await this.saveCoordinates(true);
  }

  private async saveCoordinates(isUpdate: boolean = false): Promise<void> {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    try {
      const formData = this.coordinatesForm.value;
      const coordinatesData: CoordinatesRequest = {
        x: Number(formData.x),
        y: this.parseYCoordinate(formData.y)
      };

      let savedCoordinates;

      if (isUpdate && this.editingCoordinatesId()) {
        savedCoordinates = await firstValueFrom(
          this.coordinatesService.update(this.editingCoordinatesId()!, coordinatesData)
        );
      } else {
        savedCoordinates = await firstValueFrom(
          this.coordinatesService.create(coordinatesData)
        );
      }

      await this.handleSaveSuccess(savedCoordinates, isUpdate);

    } catch (error: any) {
      this.handleSaveError(error);
    } finally {
      this.isLoading.set(false);
    }
  }

  private parseYCoordinate(value: any): number | null {
    return value !== null && value !== '' && !isNaN(Number(value)) ? Number(value) : null;
  }

  private async handleSaveSuccess(coordinates: any, isUpdate: boolean): Promise<void> {
    if (isUpdate) {
      await this.loadCoordinates(); // Reload to get updated list
    } else {
      this.addCoordinatesToLists(coordinates);
    }

    this.selectCoordinates(coordinates);
    this.switchMode('select');
    this.coordinatesForm.reset();
  }

  private addCoordinatesToLists(coordinates: any): void {
    const newCoordinates: CoordinatesOption = {
      id: coordinates.id,
      x: coordinates.x,
      y: coordinates.y,
      displayName: this.formatCoordinatesDisplay(coordinates)
    };

    this.coordinates.update(coords => [...coords, newCoordinates]);
    this.coordinatesOptions.update(options => [
      ...options,
      { value: newCoordinates.id, label: newCoordinates.displayName }
    ]);
  }

  private selectCoordinates(coordinates: any): void {
    this.selectedCoordinatesId.set(coordinates.id);
    this.coordinatesSelected.emit(coordinates.id);
  }

  private handleSaveError(error: any): void {
    if (error?.error?.details) {
      const backendErrors = error.error.details;
      this.handleBackendErrors(backendErrors);
    } else {
      this.errorMessage.set(`Ошибка ${this.isEditMode ? 'обновления' : 'создания'} координат`);
    }
    console.error(`Error ${this.isEditMode ? 'updating' : 'creating'} coordinates:`, error);
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
      const field = error.field?.toLowerCase();
      const control = field ? this.coordinatesForm.get(field) : null;

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
    return this.coordinatesForm.valid;
  }

  get submitButtonText(): string {
    if (this.isLoading()) {
      return this.isEditMode ? 'Обновление...' : 'Создание...';
    }
    return this.isEditMode ? 'Обновить координаты' : 'Создать координаты';
  }

  get formTitle(): string {
    return this.isEditMode ? 'Редактирование координат' : 'Создание координат';
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
