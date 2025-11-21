import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MusicBandService} from '../../../../services/music-band.service';
import {MusicBand} from '../../../../model/core/music-band/music-band.model';
import {HttpErrorResponse} from '@angular/common/http';
import {Panel} from 'primeng/panel';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {Button} from 'primeng/button';

@Component({
  selector: 'music-band-max-coordinates',
  templateUrl: 'music-band-max-coordinates.html',
  styleUrls: ['../operation.scss'],
  standalone: true,
  imports: [CommonModule, Panel, FormsModule, ReactiveFormsModule, Button]
})
export class MusicBandMaxCoordinates {
  musicBandService: MusicBandService = inject(MusicBandService);
  musicBand?: MusicBand;
  isLoading: boolean = false;
  errorMessage?: string;

  maxCoordinates() {
    this.isLoading = true;
    this.musicBandService.getMaxCoordinates().subscribe({
        next: (band: MusicBand) => {
          this.musicBand = band;
          this.isLoading = false;
          this.errorMessage = undefined;
        },
        error: (err: HttpErrorResponse)  => {
          if (err.status === 404) {
            this.errorMessage = `Группа с максимальными координатами не найдена`
          } else if (err.status === 400) {
            this.errorMessage = err.error.details[0].message;
          }
          this.isLoading = false;
        }
      }
    )
  }

  onMaxCoordinates() {
    this.maxCoordinates();
  }

}
