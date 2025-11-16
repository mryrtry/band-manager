import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { DatePicker } from 'primeng/datepicker';
import { Panel } from 'primeng/panel';
import { UserFilter } from '../../../model/auth/user-filter.model';

@Component({
  selector: 'app-user-filter',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    Panel,
    InputTextModule,
    ButtonModule,
    MessageModule,
  ],
  templateUrl: './user-filter.component.html',
  styles: [`
    ::ng-deep p-card .p-card-body {
      padding: 0;
    }
    label {
      text-transform: uppercase;
      font-size: 10px;
      opacity: 0.7;
      letter-spacing: -0.05em;
      margin-bottom: 4px;
      display: block;
    }
    p-message {
      display: block;
      margin-top: 5px;
      text-transform: uppercase;
      --p-message-text-font-size: 10px;
      max-width: 100%;
      font-size: 10px;
    }
    .p-fluid .field {
      margin-bottom: 0;
    }
    .p-grid > .field {
      padding-left: 0.5rem;
      padding-right: 0.5rem;
    }
    .p-grid > .field:first-child {
      padding-left: 0;
    }
    .p-grid > .field:last-child {
      padding-right: 0;
    }
  `]
})
export class UserFilterComponent {
  @Input() disabled: boolean = false;
  @Output() filterChange = new EventEmitter<UserFilter>();

  filter: UserFilter = {};

  get filterFieldsCount(): number {
    return Object.values(this.filter).filter(value => value !== undefined && value !== null && value !== '').length;
  }

  applyFilters(): void {
    this.filterChange.emit({ ...this.filter });
  }

  resetFilters(): void {
    this.filter = {};
    this.filterChange.emit({ ...this.filter });
  }

  clearFilters(): void {
    this.resetFilters();
    this.applyFilters(); // Вызываем применение фильтров после очистки
  }

  emptyFilter(): boolean {
    return this.filterFieldsCount === 0;
  }
}
