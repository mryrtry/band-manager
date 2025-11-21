import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MusicBandService} from '../../../../services/music-band.service';
import {MusicBand} from '../../../../model/core/music-band/music-band.model';
import {HttpErrorResponse} from '@angular/common/http';
import {Panel} from 'primeng/panel';
import {InputNumber} from 'primeng/inputnumber';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {Message} from 'primeng/message';
import {Button} from 'primeng/button';

@Component({
  selector: 'music-band-remove-participant',
  templateUrl: 'music-band-remove-participant.component.html',
  styleUrls: ['../operation.scss'],
  standalone: true,
  imports: [CommonModule, Panel, InputNumber, FormsModule, Message, ReactiveFormsModule, Button]
})
export class MusicBandRemoveParticipantComponent {
  musicBandService: MusicBandService = inject(MusicBandService);
  musicBandId?: number;
  musicBand?: MusicBand;
  isLoading: boolean = false;
  errorMessage?: string;

  removeParticipant(id: number) {
    this.isLoading = true;
    this.musicBandService.removeParticipant(id).subscribe({
      next: (band: MusicBand) => {
        this.musicBand = band;
        this.isLoading = false;
        this.errorMessage = undefined;
      },
      error: (err: HttpErrorResponse)  => {
        if (err.status === 404) {
          this.errorMessage = `Группа с id: ${id} не найдена`
        } else if (err.status === 400) {
          this.errorMessage = err.error.details[0].message;
        }
        this.isLoading = false;
      }
      }
    )
  }

  onRemoveParticipant() {
    if (!this.musicBandId) {
      this.errorMessage = 'ID группы не выбран'
      return;
    }
    this.removeParticipant(this.musicBandId);
  }

  onInput() {
    this.errorMessage = undefined;
  }

}
