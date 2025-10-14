import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MusicBandService, MusicBandFilter } from '../../services/music-band.service';
import { MusicBand } from '../../models/music-band.model';
import { MusicGenre } from '../../models/enums/music-genre.model';

@Component({
  selector: 'app-music-band-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './music-band-list.component.html',
  styleUrls: ['./music-band-list.component.css']
})
export class MusicBandListComponent implements OnInit {
  private musicBandService = inject(MusicBandService);

  bands: MusicBand[] = [];
  loading = false;
  error: string | null = null;

  // Фильтры
  filter: MusicBandFilter = {
    page: 0,
    size: 20,
    sort: 'id',
    direction: 'asc'
  };

  // Доступные жанры
  genres = Object.values(MusicGenre);

  ngOnInit() {
    this.loadBands();
  }

  loadBands() {
    this.loading = true;
    this.error = null;

    this.musicBandService.getMusicBands(this.filter).subscribe({
      next: (response) => {
        this.bands = response.content;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Ошибка при загрузке групп';
        this.loading = false;
        console.error('Error loading bands:', err);
      }
    });
  }

  onFilterChange() {
    this.filter.page = 0; // Сбрасываем на первую страницу при изменении фильтров
    this.loadBands();
  }

  onPageChange(page: number) {
    this.filter.page = page;
    this.loadBands();
  }

  deleteBand(id: number) {
    if (confirm('Вы уверены что хотите удалить эту группу?')) {
      this.musicBandService.deleteMusicBand(id).subscribe({
        next: () => {
          this.bands = this.bands.filter(band => band.id !== id);
        },
        error: (err) => {
          this.error = 'Ошибка при удалении группы';
          console.error('Error deleting band:', err);
        }
      });
    }
  }

  removeParticipant(id: number) {
    this.musicBandService.removeParticipant(id).subscribe({
      next: (updatedBand) => {
        const index = this.bands.findIndex(band => band.id === id);
        if (index !== -1) {
          this.bands[index] = updatedBand;
        }
      },
      error: (err) => {
        this.error = 'Ошибка при удалении участника';
        console.error('Error removing participant:', err);
      }
    });
  }
}
