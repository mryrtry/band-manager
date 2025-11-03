import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {UserTableComponent} from '../../components/user-table/user-table.component';

@Component({
  selector: 'app-users-page',
  standalone: true,
  imports: [CommonModule, UserTableComponent],
  templateUrl: './users.page.html',
  styleUrls: ['./users.page.scss']
})
export class UsersPage {
}
