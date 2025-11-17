import { Component, inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PageableRequest } from '../../model/pageable-request.model';
import { TableModule } from 'primeng/table';
import { PaginatorModule } from 'primeng/paginator';
import { PaginatorComponent } from '../common/paginator/paginator.component';
import { Button } from 'primeng/button';
import { ImportOperation, ImportStatus } from '../../model/import/import.model';
import {ImportFilter} from '../../model/import/import-filter.model';
import { UserService } from '../../services/auth/user.service';
import { User } from '../../model/auth/user.model';
import { HttpErrorResponse } from '@angular/common/http';
import { MessageService } from 'primeng/api';
import { TooltipModule } from 'primeng/tooltip';
import { ImportService } from '../../services/import.service';
import { Panel } from 'primeng/panel';
import { ImportFilterComponent } from './import-filter/import-filter.component';

@Component({
  selector: 'app-import-table',
  standalone: true,
  imports: [
    CommonModule,
    TableModule,
    PaginatorModule,
    PaginatorComponent,
    Button,
    TooltipModule,
    Panel,
    ImportFilterComponent,
  ],
  templateUrl: './import-table.component.html',
  styleUrls: ['./import-table.component.scss'],
})
export class ImportTableComponent implements OnInit, OnDestroy {
  // --- Data ---
  imports: ImportOperation[] = [];
  selectedImports: ImportOperation[] = [];
  totalRecords = 0;
  currentUser: User | undefined;

  // --- State ---
  loading = true;
  showDialog = false;
  dialogType: 'create' = 'create';
  generalError?: string;

  // --- Filter ---
  filter: ImportFilter = {};

  // --- Pageable Request ---
  pageableRequest: PageableRequest = {
    page: 0,
    size: 5,
  };

  // --- ViewChild ---
  @ViewChild('filterComponent') private filterComponent!: ImportFilterComponent;

  // --- Services ---
  private readonly importService = inject(ImportService);
  private readonly messageService = inject(MessageService);
  private readonly userService = inject(UserService);

  // -- Poll --
  private pollId?: number;

  get isFilterEmpty(): boolean {
    if (!this.filterComponent) return true;
    return this.filterComponent.emptyFilter();
  }

  // --- Lifecycle ---
  ngOnInit(): void {
    this.loadCurrentUser();
    this.loadImportOperations();
    this.startPolling();
  }

  ngOnDestroy() {
    this.stopPolling();
  }

  loadImportOperations(quite: boolean = false): void {
    this.generalError = undefined;
    this.loading = !quite;

    this.importService.getImportOperations(this.filter, this.pageableRequest).subscribe({
      next: (page) => {
        this.imports = page.content;
        this.totalRecords = page.page.totalElements;
        this.loading = false;
      },
      error: (_: HttpErrorResponse) => {
        this.generalError = 'Сервер недоступен, повторите попытку позже.';
        this.loading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Операции импорта не загрузились',
          detail: this.generalError,
        });
      },
    });
  }

  // --- Pagination ---
  onPageChange(event: any): void {
    this.pageableRequest.page = event.page ?? 0;
    this.pageableRequest.size = event.size ?? 5;
    this.loadImportOperations();
  }

  // --- Sorting ---
  customSort(event: any): void {
    const field = event.field;
    if (this.pageableRequest.sort?.[0] === field) {
      if (this.pageableRequest.direction === 'ASC') {
        this.pageableRequest.direction = 'DESC';
      } else {
        this.pageableRequest.direction = 'ASC';
      }
    } else {
      this.pageableRequest.sort = [field];
      this.pageableRequest.direction = 'ASC';
    }
    this.pageableRequest.page = 0;
    this.loadImportOperations();
  }

  // --- Selection ---
  onSelectionChange(): void {
    // No specific logic needed for selection in this component
  }

  // --- Actions: CRUD Dialog ---
  openCreateDialog(): void {
    this.dialogType = 'create';
    this.showDialog = true;
  }

  closeImportDialog(): void {
    this.showDialog = false;
  }

  onDialogHide(): void {
    this.closeImportDialog();
  }

  handleImportSubmit(): void {
    this.loadImportOperations();
    this.closeImportDialog();
  }

  // --- Filter ---
  onFilter(filter: ImportFilter): void {
    this.filter = filter;
    this.loadImportOperations();
  }

  onFilterClear(): void {
    this.filterComponent.clearFilters();
  }

  // --- Utility ---
  trackByFn(_: number, imp: ImportOperation): number {
    return imp.id;
  }

  formatDate(dateString: string): string {
    if (!dateString) return '—';
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU');
  }

  formatTime(dateString: string): string {
    if (!dateString) return '—';
    const date = new Date(dateString);
    return date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
  }

  getStatusSeverity(status: ImportStatus): string {
    switch (status) {
      case ImportStatus.PENDING:
        return 'warning';
      case ImportStatus.PROCESSING:
        return 'info';
      case ImportStatus.COMPLETED:
        return 'success';
      case ImportStatus.FAILED:
      case ImportStatus.VALIDATION_FAILED:
        return 'danger';
      default:
        return 'secondary';
    }
  }

  // --- Data Loading ---
  private loadCurrentUser(): void {
    this.userService.getCurrentUser().subscribe({
      next: (user) => (this.currentUser = user),
      error: () => console.error('Failed to load current user'),
    });
  }

  // -- Polling --
  startPolling(): void {
    this.pollId = window.setInterval(() => {
      this.loadImportOperations(true);
    }, 5000);
  }

  stopPolling(): void {
    if (this.pollId) {
      clearInterval(this.pollId);
      this.pollId = undefined;
    }
  }

  // -- RealTime class --
  rowClass(operation: ImportOperation): string {
    switch (operation.status) {
      case ImportStatus.PENDING:
        return 'pending-row';
      case ImportStatus.PROCESSING:
        return 'process-row';
      case ImportStatus.COMPLETED:
        return 'success-row';
      case ImportStatus.FAILED:
      case ImportStatus.VALIDATION_FAILED:
        return 'failed-row';
      default:
        return '';
    }
  }

  // -- User --
  get isCurrentUserAdmin(): boolean {
    return this.userService.isAdminSync();
  }

}
