import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MusicBandService} from '../../../../services/music-band.service';
import {Panel} from 'primeng/panel';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {Button} from 'primeng/button';

@Component({
  selector: 'music-band-distinct-albums',
  templateUrl: 'music-band-distinct-albums.html',
  styleUrls: ['../operation.scss'],
  standalone: true,
  imports: [CommonModule, Panel, FormsModule, ReactiveFormsModule, Button]
})
export class MusicBandDistinctAlbums {
  musicBandService: MusicBandService = inject(MusicBandService);
  uniqueAlbumCount?: string;
  isLoading: boolean = false;
  errorMessage?: string;

  distinctAlbums() {
    this.isLoading = true;
    this.musicBandService.getUniqueAlbumsCount().subscribe({
        next: (counts: number[]) => {
          this.uniqueAlbumCount = counts.join(', ');
          this.isLoading = false;
          this.errorMessage = undefined;
        }
      }
    )
  }

  onDistinctAlbums() {
    this.distinctAlbums();
  }

}
