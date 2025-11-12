import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MusicBand } from '../../model/core/music-band/music-band.model';
import { PageableRequest } from '../../model/pageable-request.model';
import { Table, TableModule } from 'primeng/table';
import { PaginatorModule } from 'primeng/paginator';
import { Panel } from 'primeng/panel';
import { PaginatorComponent } from '../common/paginator/paginator.component';
import { ConfirmationService, MenuItem, MessageService } from 'primeng/api';
import { HttpErrorResponse } from '@angular/common/http';
import { SplitButton } from 'primeng/splitbutton';
import { Button } from 'primeng/button';
import { ContextMenu } from 'primeng/contextmenu';
import { MusicBandService } from '../../services/core/music-band.service';
import { UserService } from '../../services/auth/user.service';
import { Role, User } from '../../model/auth/user.model';
import { Dialog } from 'primeng/dialog';
import { MusicBandFormComponent } from '../forms/music-band-form/music-band-form.component';
import { TooltipModule } from 'primeng/tooltip';

@Component({
  selector: 'app-music-band-table',
  standalone: true,
  imports: [
    CommonModule,
    TableModule,
    PaginatorModule,
    Panel,
    PaginatorComponent,
    SplitButton,
    Button,
    ContextMenu,
    Dialog,
    MusicBandFormComponent,
    TooltipModule,
  ],
  templateUrl: './music-band-table.component.html',
  styleUrls: ['./music-band-table.component.scss']
})
export class MusicBandTableComponent implements OnInit {
  @ViewChild('dt') dt!: Table;
  @ViewChild('cm') cm!: ContextMenu;

  private readonly musicBandService = inject(MusicBandService);
  private readonly messageService = inject(MessageService);
  private readonly confirmService = inject(ConfirmationService);
  private readonly userService = inject(UserService);

  bands: MusicBand[] = [];
  selectedBands: MusicBand[] = [];
  totalRecords = 0;

  pageableRequest: PageableRequest = {
    page: 0,
    size: 10
  };

  contextMenuItems: MenuItem[] = [];
  contextMenuBand: MusicBand | null = null;

  canDeleteSelectedBands = true;
  private _selectItemsAction: MenuItem[] = [];

  generalError?: string;
  loading = true;
  currentUser: User | undefined;

  showDialog = false;
  dialogType: 'show' | 'edit' | 'create' = 'create';
  dialogMusicBand: MusicBand | null = null;

  isSorted: boolean | null = null;
  currentSortField = '';

  get selectItemsAction(): MenuItem[] {
    return this._selectItemsAction;
  }

  ngOnInit(): void {
    this.updateSelectItemsAction();
    this.loadMusicBands();
    this.loadCurrentUser();
  }

  private updateSelectItemsAction(): void {
    this._selectItemsAction = [
      {
        label: 'Удалить',
        icon: 'pi pi-trash',
        severity: 'danger',
        command: () => this.confirmDelete(this.selectedBands),
        disabled: !this.canDeleteSelectedBands,
      },
      {
        label: 'Экспорт в CSV',
        icon: 'pi pi-file-export',
        command: () => this.confirmExport(this.selectedBands)
      }
    ];
  }

  private loadCurrentUser(): void {
    this.userService.getCurrentUser().subscribe({
      next: (user) => this.currentUser = user,
      error: () => console.error('Failed to load current user')
    });
  }

  canUpdate(band: MusicBand): boolean {
    if (!this.currentUser || !band) return false;

    const isOwner = band.createdBy === this.currentUser.username;
    const isSystem = band.createdBy === 'system';
    const isAdmin = this.currentUser.roles.includes(Role.ROLE_ADMIN);

    return isOwner || isSystem || isAdmin;
  }

  loadMusicBands(): void {
    this.generalError = undefined;
    this.loading = true;

    this.musicBandService.getAllMusicBands({}, this.pageableRequest).subscribe({
      next: (page) => {
        this.bands = page.content;
        this.totalRecords = page.page.totalElements;
        this.loading = false;
        this.updateCanDeleteSelectedBands();
      },
      error: (_: HttpErrorResponse) => {
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

  onPageChange($event: any) {
    this.pageableRequest.page = $event.page ?? 0;
    this.pageableRequest.size = $event.size ?? 10;
    this.loadMusicBands();
  }

  customSort(event: any) {
    const field = event.field;
    if (this.currentSortField !== field) {
      this.isSorted = true;
      this.currentSortField = field;
      this.applySort(field, 'ASC');
    } else {
      if (this.isSorted === true) {
        this.isSorted = false;
        this.applySort(field, 'DESC');
      } else {
        this.isSorted = null;
        this.resetSort();
      }
    }
  }

  private applySort(field: string, direction: 'ASC' | 'DESC') {
    this.pageableRequest.sort = [field];
    this.pageableRequest.direction = direction;
    this.pageableRequest.page = 0;
    this.loadMusicBands();
  }

  private resetSort() {
    this.pageableRequest.sort = undefined;
    this.pageableRequest.direction = undefined;
    this.pageableRequest.page = 0;
    this.currentSortField = '';
    this.loadMusicBands();
  }

  confirmDelete(bands: MusicBand[]): void {
    this.confirmService.confirm({
      message: `Вы действительно хотите удалить объекты? (${bands.length})`,
      header: 'Подтверждение удаления',
      acceptLabel: 'Удалить',
      rejectLabel: 'Нет',
      acceptButtonProps: { severity: 'danger' },
      rejectButtonProps: { severity: 'secondary', outlined: true },
      accept: () => this.deleteBands(bands),
      reject: () => { }
    });
  }

  private updateCanDeleteSelectedBands(): void {
    if (!this.selectedBands.length) {
      this.canDeleteSelectedBands = true;
      return;
    }
    this.canDeleteSelectedBands = this.selectedBands.every(band => this.canUpdate(band));
  }

  onSelectionChange(): void {
    this.updateCanDeleteSelectedBands();
    this.updateSelectItemsAction();
  }

  confirmExport(bands: MusicBand[]): void {
    this.confirmService.confirm({
      message: `Вы действительно хотите экспортировать объекты? (${bands.length})`,
      header: 'Подтверждение экспорта',
      acceptLabel: 'Экспортировать',
      rejectLabel: 'Нет',
      acceptButtonProps: { severity: 'primary' },
      rejectButtonProps: { severity: 'secondary', outlined: true },
      accept: () => {
        this.exportBandsCSV(bands);
        this.selectedBands = [];
      },
      reject: () => { }
    });
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
      },
      error: () => {
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
    const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' });
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

  trackByFn(_: number, band: MusicBand): number {
    return band.id;
  }

  onRowRightClick(band: MusicBand, $event: MouseEvent): void {
    this.contextMenuBand = band;
    this.contextMenuItems = [
      {
        label: `
          <div class="custom-profile-item">
            <p>Created by: ${band.createdBy || '—'}</p>
            <p>Created date: ${band.createdDate ? new Date(band.createdDate).toLocaleDateString('en-US') : '—'}</p>
            <p>Last modified by: ${band.lastModifiedBy || '—'}</p>
            <p>Last modified date: ${band.lastModifiedDate ? new Date(band.lastModifiedDate).toLocaleDateString('en-US') : '—'}</p>
          </div>
        `,
        escape: false,
        styleClass: 'non-clickable-container',
        disabled: true
      },
      { separator: true },
      {
        label: 'Просмотр',
        icon: 'pi pi-eye',
        command: () => this.openViewDialog(band)
      },
      {
        label: 'Редактировать',
        icon: 'pi pi-pencil',
        command: () => this.openEditDialog(band),
        disabled: !this.canUpdate(band)
      },
      { separator: true },
      {
        label: 'Экспорт в CSV',
        icon: 'pi pi-file-export',
        command: () => this.confirmExport([band])
      },
      {
        label: 'Удалить',
        icon: 'pi pi-trash',
        command: () => this.confirmDelete([band]),
        disabled: !this.canUpdate(band)
      }
    ];
    this.cm.show($event);
  }

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
}
