import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MusicBandTableComponent} from '../../components/music-band-table/music-band-table.component';

@Component({
  selector: 'home',
  standalone: true,
  imports: [CommonModule, MusicBandTableComponent],
  templateUrl: './home.page.html',
  styleUrls: ['./home.page.css']
})
export class MainPage {
}
