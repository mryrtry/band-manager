import {Component, inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TableModule} from 'primeng/table';
import {PaginatorModule} from 'primeng/paginator';
import {Panel} from 'primeng/panel';
import {PaginatorComponent} from '../common/paginator/paginator.component';
import {ConfirmationService, MenuItem, MessageService} from 'primeng/api';
import {HttpErrorResponse} from '@angular/common/http';
import {Button} from 'primeng/button';
import {ContextMenu} from 'primeng/contextmenu';
import {UserService} from '../../services/auth/user.service';
import {Role, User} from '../../model/auth/user.model';
import {Dialog} from 'primeng/dialog';
import {TooltipModule} from 'primeng/tooltip';
import {UserFilterComponent} from './user-filter/user-filter.component';
import {UserFilter} from '../../model/auth/user-filter.model';
import {PageableRequest} from '../../model/pageable-request.model';
import {UserFormComponent} from '../forms/user-form/user-form.component';

@Component({
  selector: 'app-user-table',
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
    UserFormComponent,
    TooltipModule,
    UserFilterComponent,
  ],
  templateUrl: './user-table.component.html',
  styleUrls: ['./user-table.component.scss']
})
export class UserTableComponent implements OnInit, OnDestroy {
  users: User[] = [];
  totalRecords = 0;
  currentUser: User | undefined;

  loading = true;
  showDialog = false;
  dialogUser: User | null = null;
  generalError?: string;

  filter: UserFilter = {};

  contextMenuItems: MenuItem[] = [];
  contextMenuUser: User | null = null;

  pageableRequest: PageableRequest = {
    page: 0,
    size: 5
  };

  @ViewChild('cm') private contextMenu!: ContextMenu;
  @ViewChild('uf') private filterComponent!: UserFilterComponent;

  private readonly userService = inject(UserService);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);

  get isFilterEmpty(): boolean {
    if (!this.filterComponent) return true;
    return this.filterComponent.emptyFilter();
  }

  ngOnInit(): void {
    this.loadCurrentUser();
    this.loadUsers();
    this.startPolling();
  }

  ngOnDestroy() {
    this.stopPolling();
  }

  loadUsers(quite: boolean = false): void {
    this.generalError = undefined;
    this.loading = !quite;
    this.userService.getAllUsers(this.filter, this.pageableRequest).subscribe({
      next: (page) => {
        this.users = page.content;
        this.totalRecords = page.page.totalElements;
        this.loading = false;
      },
      error: (_: HttpErrorResponse) => {
        this.generalError = 'Сервер недоступен, повторите попытку позже.';
        this.loading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Пользователи не загрузились',
          detail: this.generalError
        });
      }
    });
  }

  onPageChange(event: any): void {
    this.pageableRequest.page = event.page ?? 0;
    this.pageableRequest.size = event.size ?? 5;
    this.loadUsers();
  }

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
    this.loadUsers();
  }

  openEditDialog(user: User): void {
    if (this.canUpdate(user)) {
      this.dialogUser = user;
      this.showDialog = true;
    }
  }

  closeUserDialog(): void {
    this.showDialog = false;
    this.dialogUser = null;
  }

  onDialogHide(): void {
    this.closeUserDialog();
  }

  handleUserSubmit(updatedUser: User): void {
    if (this.currentUser && this.currentUser.id === updatedUser.id) {
      this.userService.refreshCurrentUser().subscribe();
    }
    this.loadUsers();
    this.closeUserDialog();
  }

  handleChangesOccurred(changes: boolean): void {
    if (changes) {
      this.loadUsers();
    }
  }

  onRowRightClick(user: User, event: MouseEvent): void {
    this.contextMenuUser = user;
    this.contextMenuItems = [{
      label: `
          <div class="custom-profile-item">
            <p>Created at: ${user.createdAt ? new Date(user.createdAt).toLocaleDateString('en-US') : '—'}</p>
            <p>Updated at: ${user.updatedAt ? new Date(user.updatedAt).toLocaleDateString('en-US') : '—'}</p>
          </div>
        `,
      escape: false,
      styleClass: 'non-clickable-container',
      disabled: true
    }, { separator: true }, {
      label: 'Редактировать',
      icon: 'pi pi-pencil',
      command: () => this.openEditDialog(user),
      disabled: !this.canUpdate(user)
    }, { separator: true }, {
      label: 'Удалить',
      icon: 'pi pi-trash',
      command: () => this.confirmDelete(user),
      disabled: !this.canUpdate(user)
    }];
    this.contextMenu.show(event);
  }

  canUpdate(user: User): boolean {
    if (!this.currentUser || !user) return false;
    if (this.currentUser.id === user.id) return false;

    return this.currentUser.roles.includes(Role.ROLE_ADMIN);
  }

  onFilter(filter: UserFilter): void {
    this.filter = filter;
    this.loadUsers();
  }

  onFilterClear(): void {
    this.filterComponent.clearFilters();
  }

  trackByFn(_: number, user: User): number {
    return user.id;
  }

  private loadCurrentUser(): void {
    this.userService.getCurrentUser().subscribe({
      next: (user) => this.currentUser = user,
      error: () => console.error('Failed to load current user')
    });
  }

  protected confirmDelete(user: User): void {
    this.confirmationService.confirm({
      message: `Вы действительно хотите удалить пользователя "${user.username}"?`,
      header: 'Подтверждение удаления',
      acceptLabel: 'Удалить',
      rejectLabel: 'Нет',
      acceptButtonProps: { severity: 'danger' },
      rejectButtonProps: { severity: 'secondary', outlined: true },
      accept: () => this.deleteUser(user),
      reject: () => { }
    });
  }

  private deleteUser(user: User): void {
    this.loading = true;
    this.userService.deleteUser(user.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Успех',
          detail: `Пользователь "${user.username}" удалён`
        });
        this.loadUsers();
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Ошибка',
          detail: 'Не удалось удалить пользователя'
        });
        this.loading = false;
      }
    });
  }

  private pollId?: number;
  startPolling(): void {
    this.pollId = setInterval(() => {
      this.loadUsers(true);
    }, 5000);
  }

  stopPolling(): void {
    clearInterval(this.pollId);
    this.pollId = undefined;
  }
}
