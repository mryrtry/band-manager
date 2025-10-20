import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {BestBandAwardTableComponent} from '../../components/award-table/award-table.component';

@Component({
  selector: 'award',
  standalone: true,
  imports: [CommonModule, BestBandAwardTableComponent],
  templateUrl: './award.page.html',
  styleUrls: ['./award.page.css']
})
export class AwardPage {
}
