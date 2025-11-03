import {Component, HostListener, inject, OnDestroy, OnInit} from '@angular/core';
import {CommonModule, DatePipe} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {MusicBandFilter, MusicBandGetConfig, MusicBandPagination, MusicBandService, MusicBandSorting,} from '../../services/music-band.service';
import {ImportService} from '../../services/import.service';
import {MusicBand} from '../../models/music-band.model';
import {parseMusicGenre} from '../../models/enums/music-genre.model';
import {CustomSelectComponent} from '../select/select.component';
import {CustomButtonComponent} from '../button/button.component';
import {InputComponent, Validator} from '../input/input.component';
import {PaginatedResponse} from '../../models/paginated-response.model';
import {ModalService} from '../../services/modal.service';

@Component({
  selector: 'app-music-table',
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule, CustomSelectComponent, InputComponent, CustomButtonComponent],
  templateUrl: './music-band-table.component.html',
  styleUrls: ['./music-band-table.component.scss']
})
export class MusicBandTableComponent implements OnInit, OnDestroy {
  private modalService = inject(ModalService);
  private musicBandService: MusicBandService = inject(MusicBandService);
  private importService = inject(ImportService);
  private router = inject(Router);

  protected bands: MusicBand[] = [];
  protected loading: boolean = false;
  protected error: string | null = null;

  protected totalElements: number = 0;
  protected totalPages: number = 0;
  protected pageSizeOptions: number[] = [5, 10, 20, 50, 100];

  protected selectedBands = new Set<number>();
  protected isAllSelected: boolean = false;

  protected sortableFields = [
    { key: 'id', label: 'ID' },
    { key: 'name', label: 'Название' },
    { key: 'frontMan.name', label: 'Лидер' },
    { key: 'genre', label: 'Жанр' },
    { key: 'bestAlbum.name', label: 'Лучший альбом' },
    { key: 'albumsCount', label: 'Альбомы' },
    { key: 'singlesCount', label: 'Синглы' },
    { key: 'numberOfParticipants', label: 'Участники' },
    { key: 'establishmentDate', label: 'Дата основания' },
    { key: 'coordinates.x', label: 'Координаты' }
  ];

  protected clearFilterOptions: MusicBandFilter = {
    name: null, description: null, genre: null,
    frontManName: null, bestAlbumName: null,
    minParticipants: null, maxParticipants: null,
    minSingles: null, maxSingles: null,
    minAlbumsCount: null, maxAlbumsCount: null,
    minCoordinateX: null, maxCoordinateX: null,
    minCoordinateY: null, maxCoordinateY: null
  };

  protected paginationOption: MusicBandPagination = { page: 0, size: 5 };
  protected filterOptions: MusicBandFilter = { ...this.clearFilterOptions };
  protected sortOptions: MusicBandSorting = { sort: ['id'], direction: 'asc' };

  protected pageSizeSelectOptions = this.pageSizeOptions.map(size => ({ label: `${size} / стр.`, value: size }));

  protected filtersToggle: boolean = false;
  protected filtersOpen: boolean = true;

  // Импорт
  protected importLoading: boolean = false;
  protected importError: string | null = null;
  protected supportedFormats: string[] = ['.json', 'application/json'];

  private refreshIntervalId: any;

  ngOnInit() {
    this.checkScreenSize();
    this.restoreStateFromLocalStorage();
    this.getMusicBands();
    this.loadSupportedFormats();

    this.refreshIntervalId = setInterval(() => {
      this.getMusicBands(true);
    }, 2000);
  }

  ngOnDestroy() {
    if (this.refreshIntervalId) {
      clearInterval(this.refreshIntervalId);
    }
  }

  private restoreStateFromLocalStorage(): void {
    const pagination = localStorage.getItem('musicBandsPagination');
    const sorting = localStorage.getItem('musicBandsSorting');
    const filters = localStorage.getItem('musicBandsFilters');

    if (pagination) this.paginationOption = JSON.parse(pagination);
    if (sorting) this.sortOptions = JSON.parse(sorting);
    if (filters) this.filterOptions = JSON.parse(filters);
  }

  private saveStateToLocalStorage(): void {
    localStorage.setItem('musicBandsPagination', JSON.stringify(this.paginationOption));
    localStorage.setItem('musicBandsSorting', JSON.stringify(this.sortOptions));
    localStorage.setItem('musicBandsFilters', JSON.stringify(this.filterOptions));
  }

  private loadSupportedFormats(): void {
    this.importService.getSupportedFormats().subscribe({
      next: (formats) => {
        this.supportedFormats = formats;
      },
      error: (error) => {
        console.error('Error loading supported formats:', error);
      }
    });
  }

  protected getConfig(): MusicBandGetConfig {
    return {
      filter: this.filterOptions,
      sorting: this.sortOptions,
      pagination: this.paginationOption
    };
  }

  protected getMusicBands(silent: boolean = false): void {
    if (!silent) {
      this.loading = true;
      this.error = null;
      this.saveStateToLocalStorage();
    }

    this.musicBandService.getMusicBands(this.getConfig()).subscribe({
      next: (response: PaginatedResponse<MusicBand>) => {
        this.bands = response.content;
        this.totalElements = response.page.totalElements;
        this.totalPages = response.page.totalPages;

        if (!silent) {
          this.loading = false;
          this.clearSelection();
        }
      },
      error: (error) => {
        if (!silent) {
          this.error = 'Ошибка при загрузке данных';
          this.loading = false;
        }
        console.error('Error loading music bands:', error);
      }
    });
  }

  protected onPageSizeChange(): void {
    this.paginationOption.page = 0;
    this.getMusicBands();
  }

  protected applyFilters(): void {
    this.paginationOption.page = 0;
    this.getMusicBands();
  }

  protected resetFilters(): void {
    this.filterOptions = { ...this.clearFilterOptions };
    this.getMusicBands();
  }

  protected goToPage(page: number) {
    this.paginationOption.page = page;
    this.getMusicBands();
  }

  protected setSorting(field: string): void {
    if (this.sortOptions.sort[0] === field) {
      this.sortOptions.direction = this.sortOptions.direction === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortOptions.direction = 'asc';
    }
    this.sortOptions.sort = [field];
    this.paginationOption.page = 0;
    if (!this.loading) {
      this.getMusicBands();
    }
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

  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    if (event.key === 'Escape') {
      this.clearSelection();
    }
  }

  protected deleteSelectedBands(): void {
    if (this.selectedBands.size === 0) return;

    const selectedIds = Array.from(this.selectedBands);
    if (confirm(`Вы уверены, что хотите удалить выбранные группы (${selectedIds.length})?`)) {
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
        next: () => this.getMusicBands(),
        error: (error) => {
          this.error = 'Ошибка при удалении группы';
          console.error('Error deleting music band:', error);
        }
      });
    }
  }

  protected getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxVisiblePages = 5;
    if (this.totalPages <= 1) return [0];
    let startPage = Math.max(0, this.paginationOption.page - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(this.totalPages - 1, startPage + maxVisiblePages - 1);
    if (endPage - startPage + 1 < maxVisiblePages) {
      startPage = Math.max(0, endPage - maxVisiblePages + 1);
    }
    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }
    return pages;
  }

  protected isFiltersClear(): boolean {
    return JSON.stringify(this.clearFilterOptions) == JSON.stringify(this.filterOptions);
  }

  protected numberValid(min: number, field: string, integer?: boolean): Validator {
    return {
      validate: (value) => {
        if (!value) return { isValid: true };
        if (integer && !(/^-?\d+$/.test(value))) return { isValid: false, message: `${field} целое` };
        if (Number(value) <= min) return { isValid: false, message: `${field} <= ${min}` };
        return { isValid: true };
      }
    };
  }

  protected genreValid(): Validator {
    return {
      validate: (value) => {
        if (!value) return { isValid: true };
        if (!parseMusicGenre(value)) return { isValid: false, message: "Жанр не существует" };
        return { isValid: true };
      }
    };
  }

  protected getSkeletonSizeArray(): number[] {
    return Array(this.paginationOption.size).fill(0).map((_ignored, i) => i);
  }

  @HostListener('window:resize')
  protected checkScreenSize(): void {
    this.filtersToggle = window.innerWidth < 400;
    this.filtersOpen = !this.filtersToggle;
  }

  protected toggleFiltersOpen(): void {
    if (this.filtersToggle) {
      this.filtersOpen = !this.filtersOpen;
    }
  }

  protected getSortAfter(field: string): string {
    if (this.sortOptions.sort[0] != field) return '';
    else {
      if (this.sortOptions.direction == 'desc') return '"↑"';
      else return '"↓"';
    }
  }

  async openCreateModal(): Promise<void> {
    try {
      const result = await this.modalService.openMusicBandModal({ mode: 'create' });
      if (result) this.getMusicBands();
    } catch (error) {
      console.error('Ошибка при создании группы:', error);
    }
  }

  async openEditModal(bandId: number): Promise<void> {
    try {
      const result = await this.modalService.openMusicBandModal({ mode: 'edit', musicBandId: bandId });
      if (result) this.getMusicBands();
    } catch (error) {
      console.error('Ошибка при редактировании группы:', error);
    }
  }

  // Импорт методов
  protected async importFile(event: any): Promise<void> {
    const file = event.target.files[0];
    if (!file) return;

    const fileExtension = file.name.toLowerCase().split('.').pop();
    const fileType = file.type;

    if (!this.supportedFormats.includes(`.${fileExtension}`) &&
      !this.supportedFormats.includes(fileType)) {
      this.importError = `Неподдерживаемый формат файла. Поддерживаемые форматы: ${this.supportedFormats.join(', ')}`;
      event.target.value = '';
      return;
    }

    this.importLoading = true;
    this.importError = null;

    try {
      this.importService.importMusicBands(file).subscribe({
        next: (operation) => {
          this.importLoading = false;
          console.log('Import started:', operation);
          alert(`Импорт начат. Статус: ${this.getImportStatusText(operation.status)}`);
          // Обновляем таблицу через некоторое время
          setTimeout(() => this.getMusicBands(), 3000);
        },
        error: (error) => {
          this.importLoading = false;
          this.importError = error.error?.message || 'Ошибка при импорте файла';
          console.error('Import error:', error);
        }
      });
    } catch (error) {
      this.importLoading = false;
      this.importError = 'Ошибка при импорте файла';
      console.error('Import error:', error);
    }

    event.target.value = '';
  }

  protected navigateToImportHistory(): void {
    this.router.navigate(['/import-history']);
  }

  protected getImportStatusText(status: string): string {
    const statusMap: { [key: string]: string } = {
      'PENDING': 'В ожидании',
      'PROCESSING': 'В обработке',
      'COMPLETED': 'Завершено',
      'FAILED': 'Ошибка',
      'VALIDATION_FAILED': 'Ошибка валидации'
    };
    return statusMap[status] || status;
  }

  protected readonly parseMusicGenre = parseMusicGenre;

  protected readonly document = document;
}
