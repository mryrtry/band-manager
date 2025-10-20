// location-selector-demo.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {LocationSelectorComponent} from '../selectors/location-selector/location-selector.component';

@Component({
  selector: 'app-location-selector-demo',
  template: `
    <div class="demo-container">
      <h2>Демо компонента выбора локации</h2>

      <div class="demo-section">
        <h3>Компонент выбора локации:</h3>
        <app-location-selector
          (locationSelected)="onLocationSelected($event)">
        </app-location-selector>
      </div>

      <div class="demo-info">
        <h4>Текущее состояние:</h4>
        <p>Выбранный ID локации: {{ selectedLocationId || 'не выбран' }}</p>
      </div>
    </div>
  `,
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
  standalone: true,
  imports: [CommonModule, LocationSelectorComponent]
})
export class LocationSelectorDemoComponent {
  selectedLocationId: number | null = null;

  onLocationSelected(locationId: number | null) {
    this.selectedLocationId = locationId;
    console.log('Выбрана локация с ID:', locationId);
  }
}
