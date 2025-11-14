import {Component, inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MusicBand} from '../../model/core/music-band/music-band.model';
import {PageableRequest} from '../../model/pageable-request.model';
import {TableModule} from 'primeng/table';
import {PaginatorModule} from 'primeng/paginator';
import {Panel} from 'primeng/panel';
import {PaginatorComponent} from '../common/paginator/paginator.component';
import {ConfirmationService, MenuItem, MessageService} from 'primeng/api';
import {HttpErrorResponse} from '@angular/common/http';
import {SplitButton} from 'primeng/splitbutton';
import {Button} from 'primeng/button';
import {ContextMenu} from 'primeng/contextmenu';
import {MusicBandService} from '../../services/core/music-band.service';
import {UserService} from '../../services/auth/user.service';
import {Role, User} from '../../model/auth/user.model';
import {Dialog} from 'primeng/dialog';
import {
  MusicBandFormComponent
} from '../forms/music-band-form/music-band-form.component';
import {TooltipModule} from 'primeng/tooltip';
import {
  MusicBandFilterComponent
} from './music-band-filter/music-band-filter.component';
import {
  MusicBandFilter
} from '../../model/core/music-band/music-band-filter.model';
import {MusicBandImportComponent} from './music-band-import/music-band-import.component';

@Component({
  selector: 'app-music-band-table',
  standalone: true,
  imports: [CommonModule, TableModule, PaginatorModule, Panel, PaginatorComponent, SplitButton, Button, ContextMenu, Dialog, MusicBandFormComponent, TooltipModule, MusicBandFilterComponent, MusicBandImportComponent, MusicBandImportComponent,],
  templateUrl: './music-band-table.component.html',
  styleUrls: ['./music-band-table.component.scss']
})
export class MusicBandTableComponent implements OnInit, OnDestroy {

  // --- Data ---
  bands: MusicBand[] = [];
  selectedBands: MusicBand[] = [];
  totalRecords = 0;
  currentUser: User | undefined;
  // --- State ---
  loading = true;
  showDialog = false;
  dialogType: 'show' | 'edit' | 'create' = 'create';
  dialogMusicBand: MusicBand | null = null;
  generalError?: string;
  // --- Filter ---
  filter: MusicBandFilter = {};
  // --- Context Menu ---
  contextMenuItems: MenuItem[] = [];
  contextMenuBand: MusicBand | null = null;
  // --- Actions ---
  selectItemsAction: MenuItem[] = [];
  canDeleteSelectedBands = true;
  // --- Pageable Request ---
  pageableRequest: PageableRequest = {
    page: 0, size: 5
  };
  // --- ViewChild ---
  @ViewChild('cm') private contextMenu!: ContextMenu;
  @ViewChild('mf') private filterComponent!: MusicBandFilterComponent;
  // --- Services ---
  private readonly musicBandService = inject(MusicBandService);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);
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
    this.updateSelectItemsAction();
    this.loadMusicBands();
    this.startPolling();
  }

  ngOnDestroy() {
    this.stopPolling();
  }

  loadMusicBands(quite: boolean = false): void {
    this.generalError = undefined;
    this.loading = !quite;

    this.musicBandService.getAllMusicBands(this.filter, this.pageableRequest).subscribe({
      next: (page) => {
        this.bands = page.content;
        this.totalRecords = page.page.totalElements;
        this.loading = false;
        this.updateCanDeleteSelectedBands();
      }, error: (_: HttpErrorResponse) => {
        this.generalError = 'Сервер недоступен, повторите попытку позже.';
        this.loading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Группы не загрузились',
          detail: this.generalError
        });
      }
    });
  }

  // --- Pagination ---
  onPageChange(event: any): void {
    this.pageableRequest.page = event.page ?? 0;
    this.pageableRequest.size = event.rows ?? 5;
    this.loadMusicBands();
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
    this.loadMusicBands();
  }

  // --- Selection ---
  onSelectionChange(): void {
    this.updateCanDeleteSelectedBands();
    this.updateSelectItemsAction();
  }

  // --- Actions: Delete ---
  confirmDelete(bands: MusicBand[]): void {
    this.confirmationService.confirm({
      message: `Вы действительно хотите удалить объекты? (${bands.length})`,
      header: 'Подтверждение удаления',
      acceptLabel: 'Удалить',
      rejectLabel: 'Нет',
      acceptButtonProps: {severity: 'danger'},
      rejectButtonProps: {severity: 'secondary', outlined: true},
      accept: () => this.deleteBands(bands),
      reject: () => {
      }
    });
  }

  // --- Actions: Export ---
  confirmExport(bands: MusicBand[]): void {
    this.confirmationService.confirm({
      message: `Вы действительно хотите экспортировать объекты? (${bands.length})`,
      header: 'Подтверждение экспорта',
      acceptLabel: 'Экспортировать',
      rejectLabel: 'Нет',
      acceptButtonProps: {severity: 'primary'},
      rejectButtonProps: {severity: 'secondary', outlined: true},
      accept: () => {
        this.exportBandsCSV(bands);
        this.selectedBands = [];
      },
      reject: () => {
      }
    });
  }

  // --- Actions: CRUD Dialog ---
  openViewDialog(band: MusicBand): void {
    this.dialogMusicBand = band;
    this.dialogType = 'show';
    this.showDialog = true;
  }

  openEditDialog(band: MusicBand): void {
    this.dialogMusicBand = band;
    this.dialogType = 'edit';
    this.showDialog = true;
  }

  openCreateDialog(): void {
    this.dialogMusicBand = null;
    this.dialogType = 'create';
    this.showDialog = true;
  }

  closeMusicBandDialog(): void {
    this.showDialog = false;
    this.dialogMusicBand = null;
  }

  onDialogHide(): void {
    this.closeMusicBandDialog();
  }

  handleMusicBandSubmit(): void {
    this.loadMusicBands();
    this.closeMusicBandDialog();
  }

  handleModeChange(mode: 'show' | 'edit' | 'create'): void {
    this.dialogType = mode;
  }

  handleChangesOccurred(changes: boolean): void {
    if (changes) {
      this.loadMusicBands();
    }
  }

  // --- Context Menu ---
  onRowRightClick(band: MusicBand, event: MouseEvent): void {
    this.contextMenuBand = band;
    this.contextMenuItems = [{
      label: `
          <div class="custom-profile-item">
            <p>Created by: ${band.createdBy || '—'}</p>
            <p>Created date: ${band.createdDate ? new Date(band.createdDate).toLocaleDateString('en-US') : '—'}</p>
            <p>Last modified by: ${band.lastModifiedBy || '—'}</p>
            <p>Last modified date: ${band.lastModifiedDate ? new Date(band.lastModifiedDate).toLocaleDateString('en-US') : '—'}</p>
          </div>
        `, escape: false, styleClass: 'non-clickable-container', disabled: true
    }, {separator: true}, {
      label: 'Просмотр',
      icon: 'pi pi-eye',
      command: () => this.openViewDialog(band)
    }, {
      label: 'Редактировать',
      icon: 'pi pi-pencil',
      command: () => this.openEditDialog(band),
      disabled: !this.canUpdate(band)
    }, {separator: true}, {
      label: 'Экспорт в CSV',
      icon: 'pi pi-file-export',
      command: () => this.confirmExport([band])
    }, {
      label: 'Удалить',
      icon: 'pi pi-trash',
      command: () => this.confirmDelete([band]),
      disabled: !this.canUpdate(band)
    }];
    this.contextMenu.show(event);
  }

  // --- Permissions ---
  canUpdate(band: MusicBand): boolean {
    if (!this.currentUser || !band) return false;
    const isOwner = band.createdBy === this.currentUser.username;
    const isSystem = band.createdBy === 'system';
    const isAdmin = this.currentUser.roles.includes(Role.ROLE_ADMIN);
    return isOwner || isSystem || isAdmin;
  }

  // --- Filter ---
  onFilter(filter: MusicBandFilter): void {
    this.filter = filter;
    this.loadMusicBands();
  }

  onFilterClear(): void {
    this.filterComponent.clearFilters();
  }

  // --- Utility ---
  trackByFn(_: number, band: MusicBand): number {
    return band.id;
  }

  // --- Data Loading ---
  private loadCurrentUser(): void {
    this.userService.getCurrentUser().subscribe({
      next: (user) => this.currentUser = user,
      error: () => console.error('Failed to load current user')
    });
  }

  private updateCanDeleteSelectedBands(): void {
    if (!this.selectedBands.length) {
      this.canDeleteSelectedBands = true;
      return;
    }
    this.canDeleteSelectedBands = this.selectedBands.every(band => this.canUpdate(band));
  }

  private deleteBands(bands: MusicBand[]): void {
    this.loading = true;
    const ids = bands.map(band => band.id);
    this.musicBandService.deleteMusicBands(ids).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Успех',
          detail: `Выбранные группы удалены (${ids.length})`
        });
        this.selectedBands = [];
        this.loadMusicBands();
      }, error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Ошибка',
          detail: 'Не удалось удалить группы'
        });
        this.loading = false;
      }
    });
  }

  private exportBandsCSV(bands: MusicBand[]): void {
    const csv = this.musicBandService.exportToCsv(bands);
    const blob = new Blob(['\ufeff' + csv], {type: 'text/csv;charset=utf-8;'});
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `music-bands-${new Date().toISOString().slice(0, 10)}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
    this.messageService.add({
      severity: 'success',
      summary: 'Успех',
      detail: `Выбранные группы экспортированы (${bands.length})`
    });
  }

  // --- Actions: Selection Menu ---
  private updateSelectItemsAction(): void {
    this.selectItemsAction = [{
      label: 'Удалить',
      icon: 'pi pi-trash',
      severity: 'danger',
      command: () => this.confirmDelete(this.selectedBands),
      disabled: !this.canDeleteSelectedBands,
    }, {
      label: 'Экспорт в CSV',
      icon: 'pi pi-file-export',
      command: () => this.confirmExport(this.selectedBands)
    }];
  }

  // -- Polling --
  startPolling(): void {
    this.pollId = setInterval(() => {
      this.loadMusicBands(true)
    }, 5000)
  }

  stopPolling(): void {
    clearInterval(this.pollId);
    this.pollId = undefined;
  }

}
