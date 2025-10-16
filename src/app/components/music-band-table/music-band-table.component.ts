import {Component, inject} from '@angular/core';
import {CommonModule, DatePipe} from '@angular/common';
import {MusicBandService} from '../../services/music-band.service';
import {MusicBand} from '../../models/music-band.model';
import {PaginatedResponse, MusicBandFilter} from '../../services/music-band.service';
import {parseMusicGenre} from '../../models/enums/music-genre.model';
import {FormsModule} from '@angular/forms';
import {SelectComponent} from '../select/select.component';

@Component({
  selector: 'app-music-table',
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule, SelectComponent],
  templateUrl: './music-band-table.component.html',
  styleUrls: ['./music-band-table.component.css']
})
export class MusicBandTableComponent {

  constructor() {
    this.getMusicBands();
  }

  private musicBandService: MusicBandService = inject(MusicBandService);

  protected bands: MusicBand[] = [];
  protected loading: boolean = false;
  protected error: string | null = null;

  protected totalElements: number = 0;
  protected totalPages: number = 0;
  protected pageSizeOptions: number[] = [5, 10, 20, 50];

  protected selectedBands = new Set<number>();
  protected isAllSelected: boolean = false;

  protected sortableFields = [
    {key: 'id', label: 'ID'},
    {key: 'name', label: 'Название'},
    {key: 'frontMan.name', label: 'Лидер'},
    {key: 'genre', label: 'Жанр'},
    {key: 'bestAlbum.name', label: 'Лучший альбом'},
    {key: 'albumsCount', label: 'Альбомы'},
    {key: 'singlesCount', label: 'Синглы'},
    {key: 'numberOfParticipants', label: 'Участники'},
    {key: 'establishmentDate', label: 'Дата основания'},
    {key: 'coordinates.x', label: 'Координаты'}
  ];

  protected filter: MusicBandFilter = {
    name: undefined,
    description: undefined,
    genre: undefined,
    frontManName: undefined,
    bestAlbumName: undefined,
    minParticipants: undefined,
    maxParticipants: undefined,
    minSingles: undefined,
    maxSingles: undefined,
    minAlbumsCount: undefined,
    maxAlbumsCount: undefined,
    minCoordinateX: undefined,
    maxCoordinateX: undefined,
    minCoordinateY: undefined,
    maxCoordinateY: undefined,
    page: 0,
    size: 5,
    sort: ['id'],
    direction: 'asc',
  }

  protected getMusicBands(refresh?: boolean) {
    if (this.bands.length <= 0) {
      this.loading = true;
    }
    if (refresh) {
      this.loading = true;
    }
    this.error = null;
    this.musicBandService.getMusicBands(this.filter).subscribe({
      next: (response: PaginatedResponse<MusicBand>) => {
        this.bands = response.content;
        this.totalElements = response.page.totalElements;
        this.totalPages = response.page.totalPages;
        this.loading = false;
        this.clearSelection();
      },
      error: (error) => {
        this.error = 'Ошибка при загрузке данных';
        this.loading = false;
        console.error('Error loading music bands:', error);
      }
    });
  }

  protected goToPage(page: number) {
    this.filter.page = page;
    this.getMusicBands();
  }

  protected getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxVisiblePages = 5;
    if (this.totalPages <= 1) return [0];
    let startPage = Math.max(0, this.filter.page - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(this.totalPages - 1, startPage + maxVisiblePages - 1);
    if (endPage - startPage + 1 < maxVisiblePages) {
      startPage = Math.max(0, endPage - maxVisiblePages + 1);
    }
    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }
    return pages;
  }

  protected onPageSizeChange(): void {
    this.filter.page = 0;
    this.getMusicBands();
  }

  protected setSorting(field: string): void {
    if (this.filter.sort[0] == field)  this.toggleSorting();
    else this.filter.direction = 'asc';
    this.filter.sort = [field];
    this.filter.page = 0;
    this.getMusicBands();
  }

  private toggleSorting(): void {
    if (this.filter.direction == 'asc') {
      this.filter.direction = 'desc';
    } else this.filter.direction = 'asc';
  }


  protected toggleBandSelection(bandId: number): void {
    if (this.selectedBands.has(bandId)) {
      this.selectedBands.delete(bandId);
    } else {
      this.selectedBands.add(bandId);
    }
    this.updateSelectAllState();
  }

  protected toggleSelectAll(): void {
    if (this.isAllSelected) {
      this.selectedBands.clear();
    } else {
      this.bands.forEach(band => this.selectedBands.add(band.id));
    }
    this.isAllSelected = !this.isAllSelected;
  }

  protected isBandSelected(bandId: number): boolean {
    return this.selectedBands.has(bandId);
  }

  protected updateSelectAllState(): void {
    this.isAllSelected = this.bands.length > 0 && this.bands.every(band => this.selectedBands.has(band.id));
  }

  protected clearSelection(): void {
    this.selectedBands.clear();
    this.isAllSelected = false;
  }

  protected getSelectedCount(): number {
    return this.selectedBands.size;
  }

  protected deleteSelectedBands(): void {
    if (this.selectedBands.size === 0) return;
    const selectedIds = Array.from(this.selectedBands);
    const confirmMessage = `Вы уверены, что хотите удалить выбранные группы (${selectedIds.length})?`;
    if (confirm(confirmMessage)) {
      this.loading = true;
      this.musicBandService.deleteMusicBands(selectedIds).subscribe({
        next: () => {
          this.loading = false;
          this.clearSelection();
          this.getMusicBands();
        },
        error: (error) => {
          this.loading = false;
          this.error = 'Ошибка при удалении групп';
          console.error('Error deleting music bands:', error);
        }
      });
    }
  }

  protected deleteBand(bandId: number, event: Event): void {
    event.stopPropagation();
    if (confirm('Вы уверены, что хотите удалить эту группу?')) {
      this.musicBandService.deleteMusicBand(bandId).subscribe({
        next: () => {
          this.getMusicBands();
        },
        error: (error) => {
          this.error = 'Ошибка при удалении группы';
          console.error('Error deleting music band:', error);
        }
      });
    }
  }

  protected pageSizeSelectOptions = this.pageSizeOptions.map(size => ({
    label: `${size} / стр.`,
    value: size,
  }));

  protected readonly parseMusicGenre = parseMusicGenre;
}
