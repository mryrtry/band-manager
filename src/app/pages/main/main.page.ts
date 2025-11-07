import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HeaderComponent} from '../../components/header/header.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, HeaderComponent],
  templateUrl: './main.page.html',
})
export class MainPage {
}
