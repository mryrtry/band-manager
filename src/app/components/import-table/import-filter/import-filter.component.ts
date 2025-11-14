import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Panel} from 'primeng/panel';
import {InputTextModule} from 'primeng/inputtext';
import {InputNumberModule} from 'primeng/inputnumber';
import {SelectModule} from 'primeng/select';
import {MessageModule} from 'primeng/message';
import {ImportFilter} from '../../../model/import/import-filter.model';
import {ImportStatus} from '../../../model/import/import.model';
import {DatePickerModule} from 'primeng/datepicker';
import {Button} from 'primeng/button';

interface ValidationError {
  field: string;
  message: string;
}

@Component({
  selector: 'app-import-filter',
  standalone: true,
  imports: [CommonModule, FormsModule, Panel, InputTextModule, InputNumberModule, SelectModule, DatePickerModule, MessageModule, Button],
  templateUrl: './import-filter.component.html',
  styles: [`
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
export class ImportFilterComponent {
  @Input() disabled: boolean = false;
  @Input() isAdmin: boolean = false;
  @Output() filterChange = new EventEmitter<ImportFilter>();

  filter: ImportFilter = {};

  statuses = Object.entries(ImportStatus).map(([key, value]) => ({
    label: key, value: value
  }));

  private validationRules = [// Проверка значений >= 0
    {
      condition: () => this.filter.createdEntitiesFrom !== undefined && this.filter.createdEntitiesFrom < 0,
      field: 'createdEntitiesFrom',
      message: 'Значение не может быть отрицательным'
    }, {
      condition: () => this.filter.createdEntitiesTo !== undefined && this.filter.createdEntitiesTo < 0,
      field: 'createdEntitiesTo',
      message: 'Значение не может быть отрицательным'
    },

    // Проверка min/max логики (только если оба поля установлены)
    {
      condition: () => this.filter.createdEntitiesFrom !== undefined && this.filter.createdEntitiesTo !== undefined && this.filter.createdEntitiesFrom > this.filter.createdEntitiesTo,
      field: 'createdEntitiesFrom',
      message: 'Мин. не может быть больше Макс.'
    },

    // Проверка дат (только если обе даты установлены)
    {
      condition: () => this.filter.startedAfter && this.filter.startedBefore && new Date(this.filter.startedAfter) > new Date(this.filter.startedBefore),
      field: 'startedAfter',
      message: 'Дата "после" не может быть позже даты "до"'
    }, {
      condition: () => this.filter.completedAfter && this.filter.completedBefore && new Date(this.filter.completedAfter) > new Date(this.filter.completedBefore),
      field: 'completedAfter',
      message: 'Дата "после" не может быть позже даты "до"'
    }];

  get filterFieldsCount(): number {
    return Object.values(this.filter).filter(value => value !== undefined && value !== null && value !== '').length;
  }

  applyFilters(): void {
    if (!this.hasErrors()) {
      this.filterChange.emit({...this.filter});
    }
  }

  resetFilters(): void {
    this.filter = {};
    this.filterChange.emit({...this.filter});
  }

  clearFilters(): void {
    this.resetFilters();
  }

  hasErrors(): boolean {
    return this.getValidationErrors().length > 0;
  }

  getValidationErrors(): ValidationError[] {
    return this.validationRules
      .filter(rule => rule.condition())
      .map(rule => ({field: rule.field, message: rule.message}));
  }

  getError(field: string): string | null {
    const error = this.getValidationErrors().find(err => err.field === field);
    return error ? error.message : null;
  }

  emptyFilter(): boolean {
    return this.filterFieldsCount === 0;
  }
}
