import {Component} from '@angular/core';
import {Toolbar} from 'primeng/toolbar';
import {AvatarModule} from 'primeng/avatar';
import {FormsModule} from '@angular/forms';
import {SelectButtonModule} from 'primeng/selectbutton';
import {ButtonDirective, ButtonIcon, ButtonLabel} from 'primeng/button';
import {RouterLink} from '@angular/router';
import {ThemeToggleComponent} from './theme-toggle/theme-toggle.component';
import {Fieldset} from 'primeng/fieldset';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  standalone: true,
  imports: [Toolbar, AvatarModule, FormsModule, SelectButtonModule, ButtonLabel, RouterLink, ButtonDirective, ButtonIcon, ThemeToggleComponent, Fieldset]
})
export class HeaderComponent {

}
