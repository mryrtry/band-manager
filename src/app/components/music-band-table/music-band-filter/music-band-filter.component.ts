import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { FormsModule } from '@angular/forms';
import { MessageModule } from 'primeng/message';
import { DatePicker } from 'primeng/datepicker';
import { Select } from 'primeng/select';
import {
  MusicBandFilter
} from '../../../model/core/music-band/music-band-filter.model';
import {MusicGenre} from '../../../model/core/music-genre.enum';
import {Panel} from 'primeng/panel';

interface ValidationError {
  field: string;
  message: string;
}

@Component({
  selector: 'app-music-band-filter',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CardModule,
    InputTextModule,
    InputNumberModule,
    DatePicker,
    ButtonModule,
    MessageModule,
    Select,
    Panel,
  ],
  templateUrl: './music-band-filter.component.html',
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
export class MusicBandFilterComponent {
  @Input() disabled: boolean = false;
  @Output() filterChange = new EventEmitter<MusicBandFilter>();

  filter: MusicBandFilter = {};

  genres = Object.entries(MusicGenre).map(([key, value]) => ({
    label: key,
    value: value
  }));

  private validationRules = [
    // Проверка значений >= 0
    {
      condition: () => this.filter.minParticipants !== undefined && this.filter.minParticipants < 0,
      field: 'minParticipants',
      message: 'Значение не может быть отрицательным'
    },
    {
      condition: () => this.filter.maxParticipants !== undefined && this.filter.maxParticipants < 0,
      field: 'maxParticipants',
      message: 'Значение не может быть отрицательным'
    },
    {
      condition: () => this.filter.minSingles !== undefined && this.filter.minSingles < 0,
      field: 'minSingles',
      message: 'Значение не может быть отрицательным'
    },
    {
      condition: () => this.filter.maxSingles !== undefined && this.filter.maxSingles < 0,
      field: 'maxSingles',
      message: 'Значение не может быть отрицательным'
    },
    {
      condition: () => this.filter.minAlbumsCount !== undefined && this.filter.minAlbumsCount < 0,
      field: 'minAlbumsCount',
      message: 'Значение не может быть отрицательным'
    },
    {
      condition: () => this.filter.maxAlbumsCount !== undefined && this.filter.maxAlbumsCount < 0,
      field: 'maxAlbumsCount',
      message: 'Значение не может быть отрицательным'
    },
    {
      condition: () => this.filter.minCoordinateX !== undefined && this.filter.minCoordinateX <= -148,
      field: 'minCoordinateX',
      message: 'Должно быть > -148'
    },
    {
      condition: () => this.filter.maxCoordinateX !== undefined && this.filter.maxCoordinateX <= -148,
      field: 'maxCoordinateX',
      message: 'Должно быть > -148'
    },

    // Проверка min/max логики (только если оба поля установлены)
    {
      condition: () => this.filter.minParticipants !== undefined &&
        this.filter.maxParticipants !== undefined &&
        this.filter.minParticipants > this.filter.maxParticipants,
      field: 'minParticipants',
      message: 'Мин. не может быть больше Макс.'
    },
    {
      condition: () => this.filter.minSingles !== undefined &&
        this.filter.maxSingles !== undefined &&
        this.filter.minSingles > this.filter.maxSingles,
      field: 'minSingles',
      message: 'Мин. не может быть больше Макс.'
    },
    {
      condition: () => this.filter.minAlbumsCount !== undefined &&
        this.filter.maxAlbumsCount !== undefined &&
        this.filter.minAlbumsCount > this.filter.maxAlbumsCount,
      field: 'minAlbumsCount',
      message: 'Мин. не может быть больше Макс.'
    },
    {
      condition: () => this.filter.minCoordinateX !== undefined &&
        this.filter.maxCoordinateX !== undefined &&
        this.filter.minCoordinateX > this.filter.maxCoordinateX,
      field: 'minCoordinateX',
      message: 'Мин. не может быть больше Макс.'
    },
    {
      condition: () => this.filter.minCoordinateY !== undefined &&
        this.filter.maxCoordinateY !== undefined &&
        this.filter.minCoordinateY > this.filter.maxCoordinateY,
      field: 'minCoordinateY',
      message: 'Мин. не может быть больше Макс.'
    },

    // Проверка дат (только если обе даты установлены)
    {
      condition: () => this.filter.establishmentDateAfter &&
        this.filter.establishmentDateBefore &&
        this.filter.establishmentDateAfter > this.filter.establishmentDateBefore,
      field: 'establishmentDateAfter',
      message: 'Дата "после" не может быть позже даты "до"'
    }
  ];

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

  private getValidationErrors(): ValidationError[] {
    return this.validationRules
      .filter(rule => rule.condition())
      .map(rule => ({ field: rule.field, message: rule.message }));
  }

  getError(field: string): string | null {
    const error = this.getValidationErrors().find(err => err.field === field);
    return error ? error.message : null;
  }

  get filterFieldsCount(): number {
    return Object.values(this.filter).filter(value =>
      value !== undefined &&
      value !== null &&
      value !== ''
    ).length;
  }

  emptyFilter(): boolean {
    return this.filterFieldsCount === 0;
  }
}
