import { CommonModule } from '@angular/common';
import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { MessageService } from 'primeng/api';

import { CoordinatesService } from '../../../services/core/coordinates.service';
import { Coordinates } from '../../../model/core/coordinates/coordinates.model';
import { CoordinatesRequest } from '../../../model/core/coordinates/coordinates.request';
import { CoordinatesFormComponent } from './coordinates-form/coordinates-form.component';
import { Button } from 'primeng/button';
import { Select } from 'primeng/select';
import { Role, User } from '../../../model/auth/user.model';
import { DialogModule } from 'primeng/dialog';
import { FormsModule } from '@angular/forms';
import {InputGroupAddon} from 'primeng/inputgroupaddon';
import {InputGroup} from 'primeng/inputgroup';
import {Tooltip} from 'primeng/tooltip';

@Component({
  selector: 'app-coordinates-selector',
  templateUrl: './coordinates-selector.component.html',
  styleUrls: ['../selector.component.scss'],
  imports: [CommonModule, Select, Button, CoordinatesFormComponent, DialogModule, FormsModule, InputGroupAddon, InputGroup, Tooltip],
  standalone: true
})
export class CoordinatesSelectorComponent implements OnInit {
  // Inputs
  @Input() disabled = false;
  @Input() coordinatesId?: number;
  @Input() currentUser?: User;

  // Outputs
  @Output() coordinatesIdChange = new EventEmitter<number | undefined>();

  // State
  coordinatesList: Coordinates[] = [];
  selectedCoordinates?: Coordinates;
  showForm = false;
  formCoordinates: Coordinates | null = null;
  isLoading = false;

  // Services
  private coordinatesService = inject(CoordinatesService);
  private messageService = inject(MessageService);

  ngOnInit() {
    this.loadCoordinates();
  }

  // Public methods
  loadCoordinates(): void {
    this.isLoading = true;

    this.coordinatesService.getAllCoordinates().subscribe({
      next: (coordinates) => {
        this.coordinatesList = coordinates;
        this.syncSelectedCoordinatesWithInput();
        this.isLoading = false;
      },
      error: (error: HttpErrorResponse) => {
        this.handleError('Координаты не загрузились', 'Сервер недоступен, повторите попытку позже.', error);
        this.isLoading = false;
      }
    });
  }

  showCreateForm(): void {
    this.formCoordinates = null;
    this.showForm = true;
  }

  showEditForm(): void {
    if (!this.selectedCoordinates) return;
    this.formCoordinates = this.selectedCoordinates;
    this.showForm = true;
  }

  hideForm(): void {
    this.showForm = false;
    this.formCoordinates = null;
  }

  handleCoordinatesSubmit(coordinatesRequest: CoordinatesRequest): void {
    this.formCoordinates
      ? this.updateCoordinates(this.formCoordinates.id, coordinatesRequest)
      : this.createCoordinates(coordinatesRequest);
  }

  emitCoordinatesChange(event: any): void {
    this.coordinatesIdChange.emit(event.id);
  }

  canUpdate(): boolean {
    if (!this.currentUser || !this.selectedCoordinates) return true;
    const isOwner = this.selectedCoordinates.createdBy === this.currentUser.username;
    const isSystem = this.selectedCoordinates.createdBy === 'system';
    const isAdmin = this.currentUser.roles.includes(Role.ROLE_ADMIN);
    return isOwner || isSystem || isAdmin;
  }

  // Private methods
  private syncSelectedCoordinatesWithInput(): void {
    if (!this.coordinatesId || this.coordinatesList.length === 0) {
      this.selectedCoordinates = undefined;
      return;
    }

    this.selectedCoordinates = this.coordinatesList.find(coordinates => coordinates.id === this.coordinatesId);

    if (!this.selectedCoordinates) {
      console.warn(`Координаты с id ${this.coordinatesId} не найдены`);
    }
  }

  private createCoordinates(coordinatesRequest: CoordinatesRequest): void {
    this.isLoading = true;

    this.coordinatesService.createCoordinates(coordinatesRequest).subscribe({
      next: (createdCoordinates) => this.handleCoordinatesCreationSuccess(createdCoordinates),
      error: (error) => this.handleError('Ошибка создания', 'Ошибка при создании координат', error)
    });
  }

  private updateCoordinates(coordinatesId: number, coordinatesRequest: CoordinatesRequest): void {
    this.isLoading = true;

    this.coordinatesService.updateCoordinates(coordinatesId, coordinatesRequest).subscribe({
      next: (updatedCoordinates) => this.handleCoordinatesUpdateSuccess(updatedCoordinates),
      error: (error) => this.handleError('Ошибка обновления', 'Ошибка при обновлении координат', error)
    });
  }

  private handleCoordinatesCreationSuccess(createdCoordinates: Coordinates): void {
    this.isLoading = false;
    this.coordinatesList.push(createdCoordinates);
    this.selectedCoordinates = createdCoordinates;
    this.hideForm();

    this.coordinatesIdChange.emit(this.selectedCoordinates.id);
    this.showSuccessMessage('Координаты созданы', createdCoordinates);
  }

  private handleCoordinatesUpdateSuccess(updatedCoordinates: Coordinates): void {
    this.isLoading = false;

    this.coordinatesList = this.coordinatesList.map(coordinates =>
      coordinates.id === updatedCoordinates.id ? updatedCoordinates : coordinates
    );

    this.selectedCoordinates = updatedCoordinates;
    this.hideForm();

    this.coordinatesIdChange.emit(this.selectedCoordinates.id);
    this.showSuccessMessage('Координаты обновлены', updatedCoordinates);
  }

  private showSuccessMessage(summary: string, coordinates: Coordinates): void {
    this.messageService.add({
      severity: 'success',
      summary,
      detail: `Координаты "${this.formatCoordinates(coordinates)}" успешно созданы`
    });
  }

  private handleError(summary: string, detail: string, error: HttpErrorResponse): void {
    this.isLoading = false;
    this.messageService.add({ severity: 'error', summary, detail });
    console.error(`${summary}:`, error);
  }

  private formatCoordinates(coordinates: Coordinates): string {
    return `X: ${coordinates.x}, Y: ${coordinates.y ? coordinates.y : 'null'}`;
  }
}
