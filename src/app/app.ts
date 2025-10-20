import {Component} from '@angular/core';
import {HeaderComponent} from './components/header/header.component';
import {NavigationComponent} from './components/nav/nav.component'
import {RouterOutlet} from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [HeaderComponent, RouterOutlet, NavigationComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class AppComponent {
}
