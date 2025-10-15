import {Component} from '@angular/core';
import {RouterLink, RouterLinkActive} from '@angular/router';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-navigation',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './nav.component.html',
  styleUrls: ['./nav.component.css']
})
export class NavigationComponent {
  navItems = [
    {path: '/', label: 'Основная страница', icon: 'home.svg'},
    {path: '/special', label: 'Специальные операции', icon: 'bolt.svg'}
  ];
}
