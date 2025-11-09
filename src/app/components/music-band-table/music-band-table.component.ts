import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MusicBandService } from '../../services/music-band.service';
import { MusicBand } from '../../model/core/music-band/music-band.model';
import { MusicBandFilter } from '../../model/core/music-band/music-band-filter.model';
import { PageableRequest } from '../../model/pageable-request.model';
import { TableModule, Table } from 'primeng/table';
import { PaginatorModule } from 'primeng/paginator';
import { Panel } from 'primeng/panel';
import {PaginatorComponent} from '../common/paginator/paginator.component';

@Component({
  selector: 'app-music-band-table',
  standalone: true,
  imports: [CommonModule, TableModule, PaginatorModule, Panel, PaginatorComponent],
  templateUrl: './music-band-table.component.html',
  styleUrls: ['./music-band-table.component.scss']
})
export class MusicBandTableComponent implements OnInit {
  @ViewChild('dt') dt!: Table;

  musicBandService: MusicBandService = inject(MusicBandService);

  bands: MusicBand[] = [];
  selectedBands: MusicBand[] = [];
  totalRecords: number = 0;

  pageableRequest: PageableRequest = {
    page: 0,
    size: 10
  };

  generalError?: string;
  loading = false;

  bandFilter: MusicBandFilter = {};

  isSorted: boolean | null = null;
  currentSortField: string = '';

  ngOnInit(): void {
    this.loadMusicBands();
  }

  loadMusicBands(): void {
    this.loading = true;

    this.musicBandService.getAllMusicBands({}, this.pageableRequest)
      .subscribe({
        next: (page) => {
          this.bands = page.content;
          this.totalRecords = page.page.totalElements;
          this.loading = false;
        },
        error: (error) => {
          this.generalError = error.message;
          this.loading = false;
        }
      });
  }

  onPageChange($event: any) {
    this.pageableRequest.page = $event.page ? $event.page : 0;
    this.pageableRequest.size = $event.size ? $event.size : 10;
    this.loadMusicBands();
  }

  customSort(event: any) {
    if (!this.currentSortField || this.currentSortField !== event.field) {
      this.isSorted = true;
      this.currentSortField = event.field;
      this.applySort(event.field, 'ASC')
    } else
    if (this.isSorted === null || this.isSorted === undefined) {
      this.isSorted = true;
      this.applySort(event.field, 'ASC');
    } else if (this.isSorted) {
      this.isSorted = false;
      this.applySort(event.field, 'DESC');
    } else if (!this.isSorted) {
      this.isSorted = null;
      this.resetSort();
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
    if (this.dt) {
      this.dt.reset();
    }
    this.loadMusicBands();
  }

  clearSort() {
    this.isSorted = null;
    this.currentSortField = '';
    this.resetSort();
  }
}
