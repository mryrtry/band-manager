import {Component, inject} from '@angular/core';
import {HeaderComponent} from './components/header/header.component';
import {NavigationComponent} from './components/nav/nav.component';
import {Router, RouterOutlet} from '@angular/router';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, HeaderComponent, RouterOutlet, NavigationComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class AppComponent {
  private router = inject(Router);

  showMainLayout(): boolean {
    const currentRoute = this.router.url;
    return !(currentRoute.includes('/login') || currentRoute.includes('/register'));
  }
}
