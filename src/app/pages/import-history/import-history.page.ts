import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {ImportHistoryTableComponent} from '../../components/import-history-table.component/import-history-table.component';

@Component({
  selector: 'app-import-history-page',
  standalone: true,
  imports: [CommonModule, ImportHistoryTableComponent],
  templateUrl: './import-history.page.html',
})
export class ImportHistoryPage {
}
