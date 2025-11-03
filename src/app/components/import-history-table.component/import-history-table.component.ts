import {Component, inject, OnInit} from '@angular/core';
import {CommonModule, DatePipe} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {ImportGetConfig, ImportOperation, ImportPagination, ImportService, ImportSorting, ImportStatus} from '../../services/import.service';
import {CustomSelectComponent} from '../select/select.component';
import {CustomButtonComponent} from '../button/button.component';
import {PaginatedResponse} from '../../models/paginated-response.model';

@Component({
  selector: 'app-import-history-table',
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule, CustomSelectComponent, CustomButtonComponent],
  templateUrl: './import-history-table.component.html',
  styleUrls: ['./import-history-table.component.scss']
})
export class ImportHistoryTableComponent implements OnInit {
  private importService = inject(ImportService);

  protected importOperations: ImportOperation[] = [];
  protected loading: boolean = false;
  protected error: string | null = null;

  protected totalElements: number = 0;
  protected totalPages: number = 0;
  protected pageSizeOptions: number[] = [5, 10, 20, 50];

  protected sortableFields = [
    {key: 'id', label: 'ID'},
    {key: 'filename', label: 'Файл'},
    {key: 'status', label: 'Статус'},
    {key: 'createdEntitiesCount', label: 'Создано'},
    {key: 'startedAt', label: 'Начало'},
    {key: 'completedAt', label: 'Завершение'},
    {key: 'createdBy', label: 'Пользователь'}
  ];

  protected paginationOption: ImportPagination = {page: 0, size: 10};
  protected sortOptions: ImportSorting = {sort: ['startedAt'], direction: 'desc'};

  protected pageSizeSelectOptions = this.pageSizeOptions.map(size => ({label: `${size} / стр.`, value: size}));

  protected selectedErrorOperation: ImportOperation | null = null;

  ngOnInit() {
    this.getImportOperations();
  }

  protected getConfig(): ImportGetConfig {
    return {
      pagination: this.paginationOption,
      sorting: this.sortOptions
    };
  }

  protected getImportOperations(): void {
    this.loading = true;
    this.error = null;

    this.importService.getImportOperations(this.getConfig()).subscribe({
      next: (response: PaginatedResponse<ImportOperation>) => {
        this.importOperations = response.content;
        this.totalElements = response.page.totalElements;
        this.totalPages = response.page.totalPages;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Ошибка при загрузке истории импорта';
        this.loading = false;
        console.error('Error loading import operations:', error);
      }
    });
  }

  protected onPageSizeChange(): void {
    this.paginationOption.page = 0;
    this.getImportOperations();
  }

  protected goToPage(page: number) {
    this.paginationOption.page = page;
    this.getImportOperations();
  }

  protected setSorting(field: string): void {
    if (this.sortOptions.sort[0] === field) {
      this.sortOptions.direction = this.sortOptions.direction === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortOptions.direction = 'desc';
    }
    this.sortOptions.sort = [field];
    this.paginationOption.page = 0;
    this.getImportOperations();
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

  protected getStatusText(status: ImportStatus): string {
    const statusMap: { [key: string]: string } = {
      'PENDING': 'В ожидании',
      'PROCESSING': 'В обработке',
      'COMPLETED': 'Завершено',
      'FAILED': 'Ошибка',
      'VALIDATION_FAILED': 'Ошибка валидации'
    };
    return statusMap[status] || status;
  }

  protected showErrorDetails(operation: ImportOperation): void {
    this.selectedErrorOperation = operation;
  }

  protected closeErrorDetails(): void {
    this.selectedErrorOperation = null;
  }

  protected getSortAfter(field: string): string {
    if (this.sortOptions.sort[0] != field) return '';
    else {
      if (this.sortOptions.direction == 'desc') return '"↑"';
      else return '"↓"';
    }
  }

  protected getSkeletonSizeArray(): number[] {
    return Array(this.paginationOption.size || 10).fill(0).map((_ignored, i) => i);
  }
}
