import {Component} from '@angular/core';
import {HeaderComponent} from './components/header/header.component';
import {NavigationComponent} from './components/nav/nav.component'
import {RouterOutlet} from '@angular/router';
import {LocationSelectorDemoComponent} from './demo/ location-selector-demo.component';
import {PersonSelectorDemoComponent} from './demo/person-selector-demo.component';
import {MusicBandFormDemoComponent} from './demo/music-band-form-demo.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [HeaderComponent, RouterOutlet, NavigationComponent, LocationSelectorDemoComponent, MusicBandFormDemoComponent, PersonSelectorDemoComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class AppComponent {
}
