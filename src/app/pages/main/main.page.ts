import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from '../../components/header/header.component';
import {
  MusicBandTableComponent
} from '../../components/music-band-table/music-band-table.component';
import { Tab, TabList, TabPanel, TabPanels, Tabs } from 'primeng/tabs';
import { UserService } from '../../services/auth/user.service';
import { MusicBandService } from '../../services/core/music-band.service';
import { MusicBand } from '../../model/core/music-band/music-band.model';
import { MusicBandRequest } from '../../model/core/music-band/music-band.request';
import { DialogModule } from 'primeng/dialog';
import { HttpErrorResponse } from '@angular/common/http';
import {
  MusicBandSpecialComponent
} from '../../components/music-band-special/music-band-special.component';
import {
  ImportTableComponent
} from '../../components/import-table/import-table.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, HeaderComponent, MusicBandTableComponent, Tabs, TabPanels, TabPanel, TabList, Tab, DialogModule, MusicBandSpecialComponent, ImportTableComponent
  ],
  templateUrl: './main.page.html',
})
export class MainPage {
  active: boolean = true;
  showDialog: boolean = false;
  dialogMusicBand: MusicBand | null = null;
  private userService = inject(UserService);
  private musicBandService = inject(MusicBandService); // Inject MusicBandService

  get currentUser() {
    let user = this.userService.getCurrentUserSync();
    return user ? user : undefined;
  }

  get isAdmin() {
    return this.userService.isAdminSync();
  }

  switchActive() {
    this.active = !this.active;
  }

  // Dialog methods
  openMusicBandDialog(musicBand?: MusicBand) {
    this.dialogMusicBand = musicBand || null;
    this.showDialog = true;
  }

  // New method to load a music band by ID and open the dialog
  loadAndOpenMusicBand(id: number) {
    this.musicBandService.getMusicBand(id).subscribe({
      next: (band) => {
        console.log('Loaded music band:', band);
        this.openMusicBandDialog(band); // Open dialog with the loaded band
      },
      error: (error: HttpErrorResponse) => {
        console.error(`Error loading music band with ID ${id}:`, error);
        // Optionally, show an error message to the user
        // this.messageService.add({ severity: 'error', summary: 'Ошибка', detail: `Не удалось загрузить группу с ID ${id}` });
      }
    });
  }

  closeMusicBandDialog() {
    this.showDialog = false;
    this.dialogMusicBand = null;
  }

  onDialogHide() {
    // Optional: Handle dialog close via X button or backdrop click
    this.closeMusicBandDialog();
  }

  handleMusicBandSubmit(request: MusicBandRequest) {
    if (this.dialogMusicBand && this.dialogMusicBand.id) {
      // Update existing music band
      this.musicBandService.updateMusicBand(this.dialogMusicBand.id, request).subscribe({
        next: (updatedBand) => {
          console.log('Music band updated successfully:', updatedBand);
          // Optionally, emit an event or update the table
          this.closeMusicBandDialog();
        }, error: (error) => {
          console.error('Error updating music band:', error);
          // Handle error, show message, etc.
        }
      });
    } else {
      // Create new music band
      this.musicBandService.createMusicBand(request).subscribe({
        next: (newBand) => {
          console.log('Music band created successfully:', newBand);
          // Optionally, emit an event or update the table
          this.closeMusicBandDialog();
        }, error: (error) => {
          console.error('Error creating music band:', error);
          // Handle error, show message, etc.
        }
      });
    }
  }
}
