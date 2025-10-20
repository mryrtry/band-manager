import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MusicBandOperationsComponent} from '../../components/spec-ops/spec-ops.component';

@Component({
  selector: 'special',
  standalone: true,
  imports: [CommonModule, FormsModule, MusicBandOperationsComponent],
  templateUrl: './special.page.html',
  styleUrls: ['./special.page.css']
})
export class SpecialOperationsPage {
}
