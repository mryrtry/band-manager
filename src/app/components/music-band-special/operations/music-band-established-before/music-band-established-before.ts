import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MusicBandService} from '../../../../services/music-band.service';
import {MusicBand} from '../../../../model/core/music-band/music-band.model';
import {HttpErrorResponse} from '@angular/common/http';
import {Panel} from 'primeng/panel';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {Message} from 'primeng/message';
import {Button} from 'primeng/button';
import {DatePicker} from 'primeng/datepicker';
import {ScrollPanel} from 'primeng/scrollpanel';

@Component({
  selector: 'music-band-established-before',
  templateUrl: 'music-band-established-before.html',
  styleUrls: ['../operation.scss'],
  standalone: true,
  imports: [CommonModule, Panel, FormsModule, Message, ReactiveFormsModule, Button, DatePicker, ScrollPanel]
})
export class MusicBandEstablishedBefore {
  musicBandService: MusicBandService = inject(MusicBandService);
  establishedBeforeDate?: Date;
  musicBands?: MusicBand[];
  isLoading: boolean = false;
  errorMessage?: string;

  establishedBefore(dateBefore: Date) {
    this.isLoading = true;
    this.musicBandService.getBandsEstablishedBefore(dateBefore).subscribe({
        next: (bands: MusicBand[]) => {
          if (bands.length > 0) {
            this.musicBands = bands;
            this.isLoading = false;
            this.errorMessage = undefined;
          } else {
            this.isLoading = false;
            this.errorMessage = `Группы основанные до ${dateBefore} не найдены`
          }
        },
        error: (err: HttpErrorResponse)  => {
          if (err.status === 404) {
            this.errorMessage = `Группы основанные до ${dateBefore} не найдены`
          } else if (err.status === 400) {
            this.errorMessage = err.error.details[0].message;
          }
          this.isLoading = false;
        }
      }
    )
  }

  onRemoveParticipant() {
    if (!this.establishedBeforeDate) {
      this.errorMessage = 'Дата основания не выбрана'
      return;
    }
    this.establishedBefore(this.establishedBeforeDate);
  }

  onInput() {
    this.errorMessage = undefined;
  }

}
