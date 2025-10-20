import {PersonSelectorComponent} from '../selectors/person-selector/person-selector.component';
import {CommonModule} from '@angular/common';
import {Component} from '@angular/core';

@Component({
  selector: 'app-person-selector-demo',
  template: `
    <div class="demo-container">
      <h2>Демо компонента выбора персоны</h2>

      <div class="demo-section">
        <h3>Компонент выбора персоны:</h3>
        <app-person-selector
          (personSelected)="onPersonSelected($event)">
        </app-person-selector>
      </div>

      <div class="demo-info">
        <h4>Текущее состояние:</h4>
        <p>Выбранный ID персоны: {{ selectedPersonId || 'не выбран' }}</p>
      </div>
    </div>
  `,
  styles: [`
    .demo-container {
      max-width: 600px;
      margin: 20px auto;
      padding: 20px;

      h2 {
        margin-bottom: 10px;
      }
    }
    .demo-section {
      padding: 10px;
      margin-bottom: 30px;
    }
    .demo-info {
      padding: 15px;
      background: var(--hover-bg);
      border-radius: var(--border-radius);
    }
  `],
  standalone: true,
  imports: [CommonModule, PersonSelectorComponent]
})
export class PersonSelectorDemoComponent {
  selectedPersonId: number | null = null;

  onPersonSelected(personId: number | null) {
    this.selectedPersonId = personId;
    console.log('Выбрана персона с ID:', personId);
  }
}
