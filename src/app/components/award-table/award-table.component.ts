import { Component, HostListener, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BestBandAwardService } from '../../services/best-band-award.service';
import { BestBandAward } from '../../models/best-band-award.model';
import { MusicGenre } from '../../models/enums/music-genre.model';
import { parseMusicGenre } from '../../models/enums/music-genre.model';
import { CustomSelectComponent } from '../select/select.component';
import { CustomButtonComponent } from '../button/button.component';
import { InputComponent, Validator } from '../input/input.component';
import { PaginatedResponse } from '../../models/paginated-response.model';

interface BestBandAwardFilter {
  genre?: MusicGenre | null;
  bandId?: number | null;
}

interface BestBandAwardSorting {
  sort: string[];
  direction: 'asc' | 'desc';
}

interface BestBandAwardPagination {
  page: number;
  size: number;
}

interface BestBandAwardGetConfig {
  filter: BestBandAwardFilter;
  sorting: BestBandAwardSorting;
  pagination: BestBandAwardPagination;
}

@Component({
  selector: 'app-best-band-award-table',
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule, CustomSelectComponent, InputComponent, CustomButtonComponent],
  templateUrl: './award-table.component.html',
  styleUrls: ['./award-table.component.scss']
})
export class BestBandAwardTableComponent implements OnInit, OnDestroy {

  private bestBandAwardService: BestBandAwardService = inject(BestBandAwardService);

  protected awards: BestBandAward[] = [];
  protected loading: boolean = false;
  protected error: string | null = null;

  protected totalElements: number = 0;
  protected totalPages: number = 0;
  protected pageSizeOptions: number[] = [5, 10, 20, 50, 100];

  protected selectedAwards = new Set<number>();
  protected isAllSelected: boolean = false;

  protected sortableFields = [
    { key: 'id', label: 'ID' },
    { key: 'bandName', label: 'Название группы' },
    { key: 'genre', label: 'Жанр' },
    { key: 'createdAt', label: 'Дата награждения' }
  ];

  protected clearFilterOptions: BestBandAwardFilter = {
    genre: null,
    bandId: null
  };

  protected paginationOption: BestBandAwardPagination = { page: 0, size: 5 };
  protected filterOptions: BestBandAwardFilter = { ...this.clearFilterOptions };
  protected sortOptions: BestBandAwardSorting = { sort: ['id'], direction: 'asc' };

  protected pageSizeSelectOptions = this.pageSizeOptions.map(size => ({ label: `${size} / стр.`, value: size }));

  protected filtersToggle: boolean = false;
  protected filtersOpen: boolean = true;

  private refreshIntervalId: any;

  ngOnInit() {
    this.checkScreenSize();
    this.restoreStateFromLocalStorage();
    this.getBestBandAwards();

    // Тихое обновление каждые 5 секунд
    this.refreshIntervalId = setInterval(() => {
      this.getBestBandAwards(true); // true = "тихое" обновление
    }, 2000);
  }

  ngOnDestroy() {
    if (this.refreshIntervalId) {
      clearInterval(this.refreshIntervalId);
    }
  }

  private restoreStateFromLocalStorage(): void {
    const pagination = localStorage.getItem('bestBandAwardsPagination');
    const sorting = localStorage.getItem('bestBandAwardsSorting');
    const filters = localStorage.getItem('bestBandAwardsFilters');

    if (pagination) this.paginationOption = JSON.parse(pagination);
    if (sorting) this.sortOptions = JSON.parse(sorting);
    if (filters) this.filterOptions = JSON.parse(filters);
  }

  private saveStateToLocalStorage(): void {
    localStorage.setItem('bestBandAwardsPagination', JSON.stringify(this.paginationOption));
    localStorage.setItem('bestBandAwardsSorting', JSON.stringify(this.sortOptions));
    localStorage.setItem('bestBandAwardsFilters', JSON.stringify(this.filterOptions));
  }

  protected getConfig(): BestBandAwardGetConfig {
    return {
      filter: this.filterOptions,
      sorting: this.sortOptions,
      pagination: this.paginationOption
    };
  }

  protected getBestBandAwards(silent: boolean = false): void {
    if (!silent) {
      this.loading = true;
      this.error = null;
      this.saveStateToLocalStorage();
    }

    const { filter, pagination, sorting } = this.getConfig();

    this.bestBandAwardService.getAll(
      filter.genre || undefined,
      filter.bandId || undefined,
      pagination.page,
      pagination.size,
      sorting.sort[0],
      sorting.direction
    ).subscribe({
      next: (response: PaginatedResponse<BestBandAward>) => {
        this.awards = response.content;
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
        console.error('Error loading best band awards:', error);
      }
    });
  }

  protected onPageSizeChange(): void {
    this.paginationOption.page = 0;
    this.getBestBandAwards();
  }

  protected applyFilters(): void {
    this.paginationOption.page = 0;
    this.getBestBandAwards();
  }

  protected resetFilters(): void {
    this.filterOptions = { ...this.clearFilterOptions };
    this.getBestBandAwards();
  }

  protected goToPage(page: number) {
    this.paginationOption.page = page;
    this.getBestBandAwards();
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
      this.getBestBandAwards();
    }
  }

  protected toggleAwardSelection(awardId: number): void {
    if (this.selectedAwards.has(awardId)) {
      this.selectedAwards.delete(awardId);
    } else {
      this.selectedAwards.add(awardId);
    }
    this.updateSelectAllState();
  }

  protected toggleSelectAll(): void {
    if (this.isAllSelected) {
      this.selectedAwards.clear();
    } else {
      this.awards.forEach(award => this.selectedAwards.add(award.id));
    }
    this.isAllSelected = !this.isAllSelected;
  }

  protected isAwardSelected(awardId: number): boolean {
    return this.selectedAwards.has(awardId);
  }

  protected updateSelectAllState(): void {
    this.isAllSelected = this.awards.length > 0 && this.awards.every(award => this.selectedAwards.has(award.id));
  }

  protected clearSelection(): void {
    this.selectedAwards.clear();
    this.isAllSelected = false;
  }

  protected getSelectedCount(): number {
    return this.selectedAwards.size;
  }

  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    if (event.key === 'Escape') {
      this.clearSelection();
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

  protected readonly parseMusicGenre = parseMusicGenre;
}
