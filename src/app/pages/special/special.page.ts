import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'special',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './special.page.html',
  styleUrls: ['./special.page.css']
})
export class SpecialOperationsPage {
}
