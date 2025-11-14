import { CommonModule } from '@angular/common';
import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { MessageService } from 'primeng/api';

import { LocationService } from '../../../services/core/location.service';
import { Location } from '../../../model/core/location/location.model';
import { LocationRequest } from '../../../model/core/location/location.request';
import { LocationFormComponent } from './location-form/location-form.component';
import { Button } from 'primeng/button';
import { Select } from 'primeng/select';
import { Role, User } from '../../../model/auth/user.model';
import { DialogModule } from 'primeng/dialog';
import {FormsModule} from '@angular/forms';
import {InputGroup} from 'primeng/inputgroup';
import {InputGroupAddon} from 'primeng/inputgroupaddon';
import {Tooltip} from 'primeng/tooltip';

@Component({
  selector: 'app-location-selector',
  templateUrl: './location-selector.component.html',
  styleUrls: ['../selector.component.scss'],
  imports: [CommonModule, Select, Button, LocationFormComponent, DialogModule, FormsModule, InputGroup, InputGroupAddon, Tooltip],
  standalone: true
})
export class LocationSelectorComponent implements OnInit {
  // Inputs
  @Input() disabled = false;
  @Input() locationId?: number;
  @Input() currentUser?: User;

  // Outputs
  @Output() locationIdChange = new EventEmitter<number | undefined>();

  // State
  locations: Location[] = [];
  selectedLocation?: Location;
  showForm = false;
  formLocation: Location | null = null;
  isLoading = false;

  // Services
  private locationService = inject(LocationService);
  private messageService = inject(MessageService);

  ngOnInit() {
    this.loadLocations();
  }

  // Public methods
  loadLocations(): void {
    this.isLoading = true;

    this.locationService.getAllLocations().subscribe({
      next: (locations) => {
        this.locations = locations;
        this.syncSelectedLocationWithInput();
        this.isLoading = false;
      },
      error: (error: HttpErrorResponse) => {
        this.handleError('Локации не загрузились', 'Сервер недоступен, повторите попытку позже.', error);
        this.isLoading = false;
      }
    });
  }

  showCreateForm(): void {
    this.formLocation = null;
    this.showForm = true;
  }

  showEditForm(): void {
    if (!this.selectedLocation) return;
    this.formLocation = this.selectedLocation;
    this.showForm = true;
  }

  hideForm(): void {
    this.showForm = false;
    this.formLocation = null;
  }

  handleLocationSubmit(locationRequest: LocationRequest): void {
    this.formLocation
      ? this.updateLocation(this.formLocation.id, locationRequest)
      : this.createLocation(locationRequest);
  }

  emitLocationChange(event: any): void {
    this.locationIdChange.emit(event.id);
  }

  canUpdate(): boolean {
    if (!this.currentUser || !this.selectedLocation) return true;
    const isOwner = this.selectedLocation.createdBy === this.currentUser.username;
    const isSystem = this.selectedLocation.createdBy === 'system';
    const isAdmin = this.currentUser.roles.includes(Role.ROLE_ADMIN);
    return isOwner || isSystem || isAdmin;
  }

  // Private methods
  private syncSelectedLocationWithInput(): void {
    if (!this.locationId || this.locations.length === 0) {
      this.selectedLocation = undefined;
      return;
    }

    this.selectedLocation = this.locations.find(location => location.id === this.locationId);

    if (!this.selectedLocation) {
      console.warn(`Локация с id ${this.locationId} не найдена`);
    }
  }

  private createLocation(locationRequest: LocationRequest): void {
    this.isLoading = true;

    this.locationService.createLocation(locationRequest).subscribe({
      next: (createdLocation) => this.handleLocationCreationSuccess(createdLocation),
      error: (error) => this.handleError('Ошибка создания', 'Ошибка при создании локации', error)
    });
  }

  private updateLocation(locationId: number, locationRequest: LocationRequest): void {
    this.isLoading = true;

    this.locationService.updateLocation(locationId, locationRequest).subscribe({
      next: (updatedLocation) => this.handleLocationUpdateSuccess(updatedLocation),
      error: (error) => this.handleError('Ошибка обновления', 'Ошибка при обновлении локации', error)
    });
  }

  private handleLocationCreationSuccess(createdLocation: Location): void {
    this.isLoading = false;
    this.locations.push(createdLocation);
    this.selectedLocation = createdLocation;
    this.hideForm();

    this.locationIdChange.emit(this.selectedLocation.id);
    this.showSuccessMessage('Локация создана', createdLocation);
  }

  private handleLocationUpdateSuccess(updatedLocation: Location): void {
    this.isLoading = false;

    this.locations = this.locations.map(location =>
      location.id === updatedLocation.id ? updatedLocation : location
    );

    this.selectedLocation = updatedLocation;
    this.hideForm();

    this.locationIdChange.emit(this.selectedLocation.id);
    this.showSuccessMessage('Локация обновлена', updatedLocation);
  }

  private showSuccessMessage(summary: string, location: Location): void {
    this.messageService.add({
      severity: 'success',
      summary,
      detail: `Локация "${this.formatLocation(location)}" успешно создана`
    });
  }

  private handleError(summary: string, detail: string, error: HttpErrorResponse): void {
    this.isLoading = false;
    this.messageService.add({ severity: 'error', summary, detail });
    console.error(`${summary}:`, error);
  }

  private formatLocation(location: Location): string {
    return `${location.x ?? 'null'}, ${location.y}, ${location.z}`;
  }
}
