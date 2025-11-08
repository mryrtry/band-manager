import {Component} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {Fieldset} from 'primeng/fieldset';
import {SelectButtonModule} from 'primeng/selectbutton';
import {AvatarModule} from 'primeng/avatar';
import {
  UserAvatarMenuComponent
} from '../auth/user-avatar/user-avatar-menu.component';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  standalone: true,
  imports: [FormsModule, Fieldset, SelectButtonModule, AvatarModule, UserAvatarMenuComponent],
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent {
}
