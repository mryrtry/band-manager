import {Component, inject} from '@angular/core';
import { CommonModule } from '@angular/common';
import { AccordionModule } from 'primeng/accordion';
import {Panel} from 'primeng/panel';
import {MusicBandService} from '../../services/music-band.service';
import {
  MusicBandRemoveParticipantComponent
} from './operations/music-band-remove-participant/music-band-remove-participant.component';
import {
  MusicBandMaxCoordinates
} from './operations/music-band-max-coordinates/music-band-max-coordinates';
import {
  MusicBandDistinctAlbums
} from './operations/music-band-distinct-albums/music-band-distinct-albums';
import {
  MusicBandEstablishedBefore
} from './operations/music-band-established-before/music-band-established-before';

@Component({
  selector: 'app-music-band-special',
  templateUrl: './music-band-special.component.html',
  styleUrls: ['./music-band-special.component.scss'],
  standalone: true,
  imports: [CommonModule, AccordionModule, Panel, MusicBandRemoveParticipantComponent, MusicBandMaxCoordinates, MusicBandDistinctAlbums, MusicBandEstablishedBefore]
})
export class MusicBandSpecialComponent {
  musicBandService: MusicBandService = inject(MusicBandService);
}
