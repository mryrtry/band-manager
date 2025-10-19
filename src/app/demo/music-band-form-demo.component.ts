// music-band-form-demo.component.ts
import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {MusicBandFormComponent} from '../components/music-band-form/music-band-form.component';
import {MusicBand} from '../models/music-band.model';
import {MusicGenre} from '../models/enums/music-genre.model';
import {Color} from '../models/enums/color.model';
import {Country} from '../models/enums/country.model';

@Component({
  selector: 'app-music-band-form-demo',
  styles: [`
    .demo-container {
      max-width: 600px;
      margin: 20px auto;
      padding: 20px;
      h2, h1, h3 {
        margin-bottom: 10px;
      }
    }
    .demo-section {
      margin-bottom: 30px;
    }
    .demo-info {
      padding: 15px;
      background: var(--hover-bg);
      border-radius: var(--border-radius);
    }
  `],
  template: `
    <div class="demo-container">
      <div class="demo-controls">
        <div class="mode-selector">
          <button
            class="btn"
            [class.active]="!isEditMode()"
            (click)="switchMode('create')">
            Режим создания
          </button>
          <button
            class="btn"
            [class.active]="isEditMode()"
            (click)="switchMode('edit')">
            Режим редактирования
          </button>
        </div>

        @if (isEditMode()) {
          <div class="initial-data-info">
            <h4>Исходные данные для редактирования:</h4>
            <pre>{{ initialData() | json }}</pre>
          </div>
        }
      </div>

      <div class="demo-section">
        <app-music-band-form
          [isEditMode]="isEditMode()"
          [initialData]="isEditMode() ? initialData() : undefined"
          (formSubmit)="onFormSubmit($event)"
          (formCancel)="onFormCancel()">
        </app-music-band-form>
      </div>

      <div class="demo-info">
        <h4>Статус:</h4>
        <div class="status-item">
          <strong>Режим:</strong> {{ isEditMode() ? 'редактирование' : 'создание' }}
        </div>
        <div class="status-item">
          <strong>Последнее действие:</strong> {{ lastAction() }}
        </div>
        <div class="status-item">
          <strong>Созданные/измененные группы:</strong> {{ createdBands().length }}
        </div>

        @if (createdBands().length > 0) {
          <div class="bands-list">
            <h4>История:</h4>
            @for (band of createdBands(); track band.id; let i = $index) {
              <div class="band-item">
                <strong>#{{ i + 1 }}</strong> {{ band.name }} (ID: {{ band.id }}) - {{ band.genre }}
              </div>
            }
          </div>
        }
      </div>
    </div>
  `,

  standalone: true,
  imports: [CommonModule, MusicBandFormComponent]
})
export class MusicBandFormDemoComponent {
  isEditMode = signal(false);
  lastAction = signal('Нет действий');
  createdBands = signal<MusicBand[]>([]);

  initialData = signal<MusicBand>({
    id: 1,
    name: 'The Demo Rockers',
    description: 'Очень крутая рок-группа для демонстрации',
    genre: MusicGenre.ROCK,
    numberOfParticipants: 4,
    singlesCount: 15,
    albumsCount: 3,
    establishmentDate: '2018-06-15T00:00:00',
    creationDate: '2023-01-01T00:00:00',
    coordinates: {
      id: 1,
      x: 100,
      y: 200
    },
    bestAlbum: {
      id: 1,
      name: 'First Demo Album',
      tracks: 12,
      sales: 50000
    },
    frontMan: {
      id: 1,
      name: 'John Demo',
      eyeColor: Color.BLUE,
      hairColor: Color.BLACK,
      weight: 75.5,
      nationality: Country.USA,
      location: {
        id: 1,
        x: 10,
        y: 20,
        z: 30
      }
    }
  } as MusicBand);

  switchMode(mode: 'create' | 'edit') {
    this.isEditMode.set(mode === 'edit');
    this.lastAction.set(`Переключен в режим ${mode === 'edit' ? 'редактирования' : 'создания'}`);
  }

  onFormSubmit(musicBand: MusicBand) {
    const action = this.isEditMode() ? 'изменена' : 'создана';
    this.lastAction.set(`Группа успешно ${action}: "${musicBand.name}"`);

    if (this.isEditMode()) {
      // Обновляем данные в списке если редактировали существующую
      this.createdBands.update(bands =>
        bands.map(band => band.id === musicBand.id ? musicBand : band)
      );
    } else {
      // Добавляем новую группу в список
      this.createdBands.update(bands => [...bands, musicBand]);
    }

    console.log('Form submitted:', musicBand);
  }

  onFormCancel() {
    this.lastAction.set('Действие отменено пользователем');
    console.log('Form cancelled');
  }
}
