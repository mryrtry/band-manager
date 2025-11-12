import { CommonModule } from '@angular/common';
import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { MessageService } from 'primeng/api';

import { AlbumService } from '../../../services/core/album.service';
import { Album } from '../../../model/core/album/album.model';
import { AlbumRequest } from '../../../model/core/album/album.request';
import { AlbumFormComponent } from './album-form/album-form.component';
import { Button } from 'primeng/button';
import { Select } from 'primeng/select';
import { Role, User } from '../../../model/auth/user.model';
import { DialogModule } from 'primeng/dialog';
import { FormsModule } from '@angular/forms';
import {InputGroup} from 'primeng/inputgroup';
import {InputGroupAddon} from 'primeng/inputgroupaddon';

@Component({
  selector: 'app-album-selector',
  templateUrl: './album-selector.component.html',
  styleUrls: ['../selector.component.scss'],
  imports: [CommonModule, Select, Button, AlbumFormComponent, DialogModule, FormsModule, InputGroup, InputGroupAddon],
  standalone: true
})
export class AlbumSelectorComponent implements OnInit {
  // Inputs
  @Input() disabled = false;
  @Input() albumId?: number;
  @Input() currentUser?: User;

  // Outputs
  @Output() albumIdChange = new EventEmitter<number | undefined>();

  // State
  albums: Album[] = [];
  selectedAlbum?: Album;
  showForm = false;
  formAlbum: Album | null = null;
  isLoading = false;

  // Services
  private albumService = inject(AlbumService);
  private messageService = inject(MessageService);

  ngOnInit() {
    this.loadAlbums();
  }

  // Public methods
  loadAlbums(): void {
    this.isLoading = true;

    this.albumService.getAllAlbums().subscribe({
      next: (albums) => {
        this.albums = albums;
        this.syncSelectedAlbumWithInput();
        this.isLoading = false;
      },
      error: (error: HttpErrorResponse) => {
        this.handleError('Альбомы не загрузились', 'Сервер недоступен, повторите попытку позже.', error);
        this.isLoading = false;
      }
    });
  }

  showCreateForm(): void {
    this.formAlbum = null;
    this.showForm = true;
  }

  showEditForm(): void {
    if (!this.selectedAlbum) return;
    this.formAlbum = this.selectedAlbum;
    this.showForm = true;
  }

  hideForm(): void {
    this.showForm = false;
    this.formAlbum = null;
  }

  handleAlbumSubmit(albumRequest: AlbumRequest): void {
    this.formAlbum
      ? this.updateAlbum(this.formAlbum.id, albumRequest)
      : this.createAlbum(albumRequest);
  }

  emitAlbumChange(event: any): void {
    this.albumIdChange.emit(event.id);
  }

  canUpdate(): boolean {
    if (!this.currentUser || !this.selectedAlbum) return true;
    const isOwner = this.selectedAlbum.createdBy === this.currentUser.username;
    const isSystem = this.selectedAlbum.createdBy === 'system';
    const isAdmin = this.currentUser.roles.includes(Role.ROLE_ADMIN);
    return isOwner || isSystem || isAdmin;
  }

  // Private methods
  private syncSelectedAlbumWithInput(): void {
    if (!this.albumId || this.albums.length === 0) {
      this.selectedAlbum = undefined;
      return;
    }

    this.selectedAlbum = this.albums.find(album => album.id === this.albumId);

    if (!this.selectedAlbum) {
      console.warn(`Альбом с id ${this.albumId} не найден`);
    }
  }

  private createAlbum(albumRequest: AlbumRequest): void {
    this.isLoading = true;

    this.albumService.createAlbum(albumRequest).subscribe({
      next: (createdAlbum) => this.handleAlbumCreationSuccess(createdAlbum),
      error: (error) => this.handleError('Ошибка создания', 'Ошибка при создании альбома', error)
    });
  }

  private updateAlbum(albumId: number, albumRequest: AlbumRequest): void {
    this.isLoading = true;

    this.albumService.updateAlbum(albumId, albumRequest).subscribe({
      next: (updatedAlbum) => this.handleAlbumUpdateSuccess(updatedAlbum),
      error: (error) => this.handleError('Ошибка обновления', 'Ошибка при обновлении альбома', error)
    });
  }

  private handleAlbumCreationSuccess(createdAlbum: Album): void {
    this.isLoading = false;
    this.albums.push(createdAlbum);
    this.selectedAlbum = createdAlbum;
    this.hideForm();

    this.albumIdChange.emit(this.selectedAlbum.id);
    this.showSuccessMessage('Альбом создан', createdAlbum);
  }

  private handleAlbumUpdateSuccess(updatedAlbum: Album): void {
    this.isLoading = false;

    this.albums = this.albums.map(album =>
      album.id === updatedAlbum.id ? updatedAlbum : album
    );

    this.selectedAlbum = updatedAlbum;
    this.hideForm();

    this.albumIdChange.emit(this.selectedAlbum.id);
    this.showSuccessMessage('Альбом обновлен', updatedAlbum);
  }

  private showSuccessMessage(summary: string, album: Album): void {
    this.messageService.add({
      severity: 'success',
      summary,
      detail: `Альбом "${this.formatAlbum(album)}" успешно создан`
    });
  }

  private handleError(summary: string, detail: string, error: HttpErrorResponse): void {
    this.isLoading = false;
    this.messageService.add({ severity: 'error', summary, detail });
    console.error(`${summary}:`, error);
  }

  private formatAlbum(album: Album): string {
    return `${album.name} (${album.tracks} треков, ${album.sales} продаж)`;
  }
}
