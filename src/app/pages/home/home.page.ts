import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MusicBandListComponent} from '../../components/music-band-list/music-band-list.component';

@Component({
  selector: 'home',
  standalone: true,
  imports: [CommonModule, MusicBandListComponent],
  templateUrl: './home.page.html',
  styleUrls: ['./home.page.css']
})
export class MainPageComponent {
}
