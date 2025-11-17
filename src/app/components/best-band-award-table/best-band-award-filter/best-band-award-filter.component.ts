import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { MessageModule } from 'primeng/message';
import { Select } from 'primeng/select';
import { Panel } from 'primeng/panel';
import { BestBandAwardFilter } from '../../../model/core/best-band-award/best-band-award-filter.model';
import { MusicGenre } from '../../../model/core/music-genre.enum';

interface ValidationError {
  field: string;
  message: string;
}

@Component({
  selector: 'app-best-band-award-filter',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    Panel,
    InputTextModule,
    InputNumberModule,
    ButtonModule,
    MessageModule,
    Select,
  ],
  templateUrl: './best-band-award-filter.component.html',
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
export class BestBandAwardFilterComponent {
  @Input() disabled: boolean = false;
  @Output() filterChange = new EventEmitter<BestBandAwardFilter>();

  filter: BestBandAwardFilter = {};

  genres = Object.entries(MusicGenre).map(([key, value]) => ({
    label: key,
    value: value
  }));

  private validationRules = [
    {
      condition: () => this.filter.createdAtAfter && this.filter.createdAtBefore && this.filter.createdAtAfter > this.filter.createdAtBefore,
      field: 'createdDateAfter',
      message: 'Дата "после" не может быть позже даты "до"'
    },
  ];

  get filterFieldsCount(): number {
    return Object.values(this.filter).filter(value => value !== undefined && value !== null && value !== '').length;
  }

  applyFilters(): void {
    if (!this.hasErrors()) {
      this.filterChange.emit({ ...this.filter });
    }
  }

  resetFilters(): void {
    this.filter = {};
    this.filterChange.emit({ ...this.filter });
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
      .map(rule => ({ field: rule.field, message: rule.message }));
  }

  getError(field: string): string | null {
    const error = this.getValidationErrors().find(err => err.field === field);
    return error ? error.message : null;
  }

  emptyFilter(): boolean {
    return this.filterFieldsCount === 0;
  }
}
