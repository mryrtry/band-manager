import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {CommonModule} from '@angular/common';
import {Toast} from 'primeng/toast';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, Toast],
  templateUrl: './app.html',
  styleUrls: ['./app.scss']
})
export class AppComponent {
}
