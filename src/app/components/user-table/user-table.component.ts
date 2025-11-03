import {Component, HostListener, inject, OnDestroy, OnInit} from '@angular/core';
import {CommonModule, DatePipe} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {UserFilter, UserGetConfig, UserPagination, UserService, UserSorting} from '../../services/user.service';
import {AuthService} from '../../services/auth.service';
import {User} from '../../models/user.model';
import {CustomSelectComponent} from '../select/select.component';
import {CustomButtonComponent} from '../button/button.component';
import {InputComponent, Validator} from '../input/input.component';
import {PaginatedResponse} from '../../models/paginated-response.model';
import {ModalService} from '../../services/modal.service';

@Component({
  selector: 'app-user-table',
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule, CustomSelectComponent, InputComponent, CustomButtonComponent],
  templateUrl: './user-table.component.html',
  styleUrls: ['./user-table.component.scss']
})
export class UserTableComponent implements OnInit, OnDestroy {
  private userService = inject(UserService);
  private authService = inject(AuthService);
  private modalService = inject(ModalService);

  protected users: User[] = [];
  protected loading: boolean = false;
  protected error: string | null = null;

  protected totalElements: number = 0;
  protected totalPages: number = 0;
  protected pageSizeOptions: number[] = [5, 10, 20, 50, 100];

  protected selectedUsers = new Set<number>();
  protected isAllSelected: boolean = false;

  protected sortableFields = [{key: 'id', label: 'ID'}, {key: 'username', label: 'Имя пользователя'}, {key: '', label: "Роли"}, {key: 'createdAt', label: 'Дата создания'}, {key: 'updatedAt', label: 'Дата обновления'}];

  protected clearFilterOptions: UserFilter = {
    id: null, username: null, createdAt: null, updatedAt: null
  };

  protected paginationOption: UserPagination = {page: 0, size: 5};
  protected filterOptions: UserFilter = {...this.clearFilterOptions};
  protected sortOptions: UserSorting = {sort: ['id'], direction: 'asc'};

  protected pageSizeSelectOptions = this.pageSizeOptions.map(size => ({label: `${size} / стр.`, value: size}));

  protected filtersToggle: boolean = false;
  protected filtersOpen: boolean = true;

  private refreshIntervalId: any;

  ngOnInit() {
    this.checkScreenSize();
    this.restoreStateFromLocalStorage();
    this.getUsers();

    this.refreshIntervalId = setInterval(() => {
      this.getUsers(true);
    }, 5000); // Обновление каждые 5 секунд
  }

  ngOnDestroy() {
    if (this.refreshIntervalId) {
      clearInterval(this.refreshIntervalId);
    }
  }

  private restoreStateFromLocalStorage(): void {
    const pagination = localStorage.getItem('usersPagination');
    const sorting = localStorage.getItem('usersSorting');
    const filters = localStorage.getItem('usersFilters');

    if (pagination) this.paginationOption = JSON.parse(pagination);
    if (sorting) this.sortOptions = JSON.parse(sorting);
    if (filters) this.filterOptions = JSON.parse(filters);
  }

  private saveStateToLocalStorage(): void {
    localStorage.setItem('usersPagination', JSON.stringify(this.paginationOption));
    localStorage.setItem('usersSorting', JSON.stringify(this.sortOptions));
    localStorage.setItem('usersFilters', JSON.stringify(this.filterOptions));
  }

  protected getConfig(): UserGetConfig {
    return {
      filter: this.filterOptions, sorting: this.sortOptions, pagination: this.paginationOption
    };
  }

  protected getUsers(silent: boolean = false): void {
    if (!silent) {
      this.loading = true;
      this.error = null;
      this.saveStateToLocalStorage();
    }

    this.userService.getUsers(this.getConfig()).subscribe({
      next: (response: PaginatedResponse<User>) => {
        this.users = response.content;
        this.totalElements = response.page.totalElements;
        this.totalPages = response.page.totalPages;

        if (!silent) {
          this.loading = false;
          this.clearSelection();
        }
      }, error: (error) => {
        if (!silent) {
          this.error = 'Ошибка при загрузке пользователей';
          this.loading = false;
        }
        console.error('Error loading users:', error);
      }
    });
  }

  protected onPageSizeChange(): void {
    this.paginationOption.page = 0;
    this.getUsers();
  }

  protected applyFilters(): void {
    this.paginationOption.page = 0;
    this.getUsers();
  }

  protected resetFilters(): void {
    this.filterOptions = {...this.clearFilterOptions};
    this.getUsers();
  }

  protected goToPage(page: number) {
    this.paginationOption.page = page;
    this.getUsers();
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
      this.getUsers();
    }
  }

  protected toggleUserSelection(userId: number): void {
    if (this.selectedUsers.has(userId)) {
      this.selectedUsers.delete(userId);
    } else {
      this.selectedUsers.add(userId);
    }
    this.updateSelectAllState();
  }

  protected toggleSelectAll(): void {
    if (this.isAllSelected) {
      this.selectedUsers.clear();
    } else {
      this.users.forEach(user => this.selectedUsers.add(user.id));
    }
    this.isAllSelected = !this.isAllSelected;
  }

  protected isUserSelected(userId: number): boolean {
    return this.selectedUsers.has(userId);
  }

  protected updateSelectAllState(): void {
    this.isAllSelected = this.users.length > 0 && this.users.every(user => this.selectedUsers.has(user.id));
  }

  protected clearSelection(): void {
    this.selectedUsers.clear();
    this.isAllSelected = false;
  }

  protected getSelectedCount(): number {
    return this.selectedUsers.size;
  }

  @HostListener('document:keydown', ['$event']) handleKeyboardEvent(event: KeyboardEvent) {
    if (event.key === 'Escape') {
      this.clearSelection();
    }
  }

  protected deleteSelectedUsers(): void {
    if (this.selectedUsers.size === 0) return;

    const selectedIds = Array.from(this.selectedUsers);
    if (confirm(`Вы уверены, что хотите удалить выбранных пользователей (${selectedIds.length})?`)) {
      this.loading = true;

      // Удаляем пользователей последовательно
      const deletePromises = selectedIds.map(id => this.userService.deleteUser(id).toPromise());

      Promise.all(deletePromises).then(() => {
        this.loading = false;
        this.clearSelection();
        this.getUsers();
      }).catch((error) => {
        this.loading = false;
        this.error = 'Ошибка при удалении пользователей';
        console.error('Error deleting users:', error);
      });
    }
  }

  protected deleteUser(userId: number, event: Event): void {
    event.stopPropagation();
    if (confirm('Вы уверены, что хотите удалить этого пользователя?')) {
      this.userService.deleteUser(userId).subscribe({
        next: () => this.getUsers(), error: (error) => {
          this.error = 'Ошибка при удалении пользователя';
          console.error('Error deleting user:', error);
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
        if (!value) return {isValid: true};
        if (integer && !(/^-?\d+$/.test(value))) return {isValid: false, message: `${field} целое`};
        if (Number(value) <= min) return {isValid: false, message: `${field} <= ${min}`};
        return {isValid: true};
      }
    };
  }

  protected getSkeletonSizeArray(): number[] {
    return Array(this.paginationOption.size || 5).fill(0).map((_ignored, i) => i);
  }

  @HostListener('window:resize') protected checkScreenSize(): void {
    this.filtersToggle = window.innerWidth < 400;
    this.filtersOpen = !this.filtersToggle;
  }

  protected toggleFiltersOpen(): void {
    if (this.filtersToggle) {
      this.filtersOpen = !this.filtersOpen;
    }
  }

  protected getSortAfter(field: string): string {
    if (this.sortOptions.sort[0] != field) return ''; else {
      if (this.sortOptions.direction == 'desc') return '"↑"'; else return '"↓"';
    }
  }

  async openEditModal(userId: number): Promise<void> {
    try {
      console.log('Edit user:', userId);
    } catch (error) {
      console.error('Ошибка при редактировании пользователя:', error);
    }
  }
}
