// best-band-award-table.component.ts
import { Component, inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableModule } from 'primeng/table';
import { PaginatorModule } from 'primeng/paginator';
import { Panel } from 'primeng/panel';
import { PaginatorComponent } from '../common/paginator/paginator.component';
import { ConfirmationService, MenuItem, MessageService } from 'primeng/api';
import { HttpErrorResponse } from '@angular/common/http';
import { Button } from 'primeng/button';
import { ContextMenu } from 'primeng/contextmenu';
import { UserService } from '../../services/auth/user.service';
import { Role, User } from '../../model/auth/user.model';
import { Dialog } from 'primeng/dialog';
import { TooltipModule } from 'primeng/tooltip';
import { BestBandAwardFilterComponent } from './best-band-award-filter/best-band-award-filter.component';
import { BestBandAwardFilter } from '../../model/core/best-band-award/best-band-award-filter.model';
import { BestBandAward } from '../../model/core/best-band-award/best-band-award.model';
import { PageableRequest } from '../../model/pageable-request.model';
import { BestBandAwardService } from '../../services/core/best-band-award.service';
import { BestBandAwardFormComponent } from '../forms/best-band-award-form/best-band-award-form.component';

@Component({
  selector: 'app-best-band-award-table',
  standalone: true,
  imports: [
    CommonModule,
    TableModule,
    PaginatorModule,
    Panel,
    PaginatorComponent,
    Button,
    ContextMenu,
    Dialog,
    BestBandAwardFormComponent,
    TooltipModule,
    BestBandAwardFilterComponent,
    BestBandAwardFormComponent,
  ],
  templateUrl: './best-band-award-table.component.html',
  styleUrls: ['./best-band-award-table.component.scss']
})
export class BestBandAwardTableComponent implements OnInit, OnDestroy {
  // --- Data ---
  awards: BestBandAward[] = [];
  selectedAwards: BestBandAward[] = [];
  totalRecords = 0;
  currentUser: User | undefined;

  // --- State ---
  loading = true;
  showDialog = false;
  dialogType: 'show' | 'edit' | 'create' = 'create';
  dialogAward: BestBandAward | null = null;
  generalError?: string;

  // --- Filter ---
  filter: BestBandAwardFilter = {};

  // --- Context Menu ---
  contextMenuItems: MenuItem[] = [];
  contextMenuAward: BestBandAward | null = null;

  // --- Pageable Request ---
  pageableRequest: PageableRequest = {
    page: 0,
    size: 5
  };

  // --- ViewChild ---
  @ViewChild('cm') private contextMenu!: ContextMenu;
  @ViewChild('bf') private filterComponent!: BestBandAwardFilterComponent;

  // --- Services ---
  private readonly bestBandAwardService = inject(BestBandAwardService);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly userService = inject(UserService);

  get isFilterEmpty(): boolean {
    if (!this.filterComponent) return true;
    return this.filterComponent.emptyFilter();
  }

  // --- Lifecycle ---
  ngOnInit(): void {
    this.loadCurrentUser();
    this.loadAwards();
    this.startPolling();
  }

  ngOnDestroy() {
    this.stopPolling();
  }

  loadAwards(quite: boolean = false): void {
    this.generalError = undefined;
    this.loading = !quite;
    this.bestBandAwardService.getAllBestBandAwards(this.filter, this.pageableRequest).subscribe({
      next: (page) => {
        this.awards = page.content;
        this.totalRecords = page.page.totalElements;
        this.loading = false;
      },
      error: (_: HttpErrorResponse) => {
        this.generalError = 'Сервер недоступен, повторите попытку позже.';
        this.loading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Награды не загрузились',
          detail: this.generalError
        });
      }
    });
  }

  // --- Pagination ---
  onPageChange(event: any): void {
    this.pageableRequest.page = event.page ?? 0;
    this.pageableRequest.size = event.size ?? 5;
    this.loadAwards();
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
    this.loadAwards();
  }

  // --- Selection ---
  onSelectionChange(): void {
    // No specific logic for selection actions in this simplified version
  }

  // --- Actions: CRUD Dialog ---
  openViewDialog(award: BestBandAward): void {
    this.dialogAward = award;
    this.dialogType = 'show';
    this.showDialog = true;
  }

  openEditDialog(award: BestBandAward): void {
    this.dialogAward = award;
    this.dialogType = 'edit';
    this.showDialog = true;
  }

  openCreateDialog(): void {
    this.dialogAward = null;
    this.dialogType = 'create';
    this.showDialog = true;
  }

  closeAwardDialog(): void {
    this.showDialog = false;
    this.dialogAward = null;
  }

  onDialogHide(): void {
    this.closeAwardDialog();
  }

  handleAwardSubmit(): void {
    this.loadAwards();
    this.closeAwardDialog();
  }

  handleModeChange(mode: 'show' | 'edit' | 'create'): void {
    this.dialogType = mode;
  }

  handleChangesOccurred(changes: boolean): void {
    if (changes) {
      this.loadAwards();
    }
  }

  // --- Context Menu ---
  onRowRightClick(award: BestBandAward, event: MouseEvent): void {
    this.contextMenuAward = award;
    this.contextMenuItems = [{
      label: `
          <div class="custom-profile-item">
            <p>Created by: ${award.createdBy || '—'}</p>
            <p>Created date: ${award.createdDate ? new Date(award.createdDate).toLocaleDateString('en-US') : '—'}</p>
            <p>Last modified by: ${award.lastModifiedBy || '—'}</p>
            <p>Last modified date: ${award.lastModifiedDate ? new Date(award.lastModifiedDate).toLocaleDateString('en-US') : '—'}</p>
          </div>
        `,
      escape: false,
      styleClass: 'non-clickable-container',
      disabled: true
    }, { separator: true }, {
      label: 'Просмотр',
      icon: 'pi pi-eye',
      command: () => this.openViewDialog(award)
    }, {
      label: 'Редактировать',
      icon: 'pi pi-pencil',
      disabled: true,
      command: () => this.openEditDialog(award),
    }, { separator: true }, {
      label: 'Удалить',
      icon: 'pi pi-trash',
      command: () => this.confirmDelete([award]),
      disabled: !this.canUpdate(award)
    }];
    this.contextMenu.show(event);
  }

  // --- Permissions ---
  canUpdate(award: BestBandAward): boolean {
    if (!this.currentUser || !award) return false;
    const isOwner = award.createdBy === this.currentUser.username;
    const isSystem = award.createdBy === 'system';
    const isAdmin = this.currentUser.roles.includes(Role.ROLE_ADMIN);
    return isOwner || isSystem || isAdmin;
  }

  // --- Filter ---
  onFilter(filter: BestBandAwardFilter): void {
    this.filter = filter;
    this.loadAwards();
  }

  onFilterClear(): void {
    this.filterComponent.clearFilters();
  }

  // --- Utility ---
  trackByFn(_: number, award: BestBandAward): number {
    return award.id;
  }

  // --- Data Loading ---
  private loadCurrentUser(): void {
    this.userService.getCurrentUser().subscribe({
      next: (user) => this.currentUser = user,
      error: () => console.error('Failed to load current user')
    });
  }

  private confirmDelete(awards: BestBandAward[]): void {
    this.confirmationService.confirm({
      message: `Вы действительно хотите удалить объекты? (${awards.length})`,
      header: 'Подтверждение удаления',
      acceptLabel: 'Удалить',
      rejectLabel: 'Нет',
      acceptButtonProps: { severity: 'danger' },
      rejectButtonProps: { severity: 'secondary', outlined: true },
      accept: () => this.deleteAwards(awards),
      reject: () => { }
    });
  }

  private deleteAwards(awards: BestBandAward[]): void {
    if (awards.length === 0) return;
    const id = awards[0].id; // Assuming single delete for simplicity, as deleteMany might not exist
    this.loading = true;
    this.bestBandAwardService.deleteBestBandAward(id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Успех',
          detail: `Награда удалена`
        });
        this.selectedAwards = [];
        this.loadAwards();
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Ошибка',
          detail: 'Не удалось удалить награду'
        });
        this.loading = false;
      }
    });
  }

  // -- Polling --
  private pollId?: number;
  startPolling(): void {
    this.pollId = setInterval(() => {
      this.loadAwards(true);
    }, 5000);
  }

  stopPolling(): void {
    clearInterval(this.pollId);
    this.pollId = undefined;
  }
}
