import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HeaderComponent} from '../../components/header/header.component';
import {
  MusicBandTableComponent
} from '../../components/music-band-table/music-band-table.component';
import {Tab, TabList, TabPanel, TabPanels, Tabs} from 'primeng/tabs';
import {UserService} from '../../services/user.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, HeaderComponent, MusicBandTableComponent, Tabs, TabPanels, TabPanel, TabList, Tab],
  templateUrl: './main.page.html',
})
export class MainPage {
  userService: UserService = inject(UserService);

  get isAdmin() {
    return  this.userService.isAdminSync();
  }
}
