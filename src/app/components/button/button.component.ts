import {Component, Input, Output, EventEmitter} from '@angular/core';
import {CommonModule} from '@angular/common';

export type ButtonVariant = 'primary' | 'danger' | 'icon' | 'default';

@Component({
  selector: 'app-custom-button',
  templateUrl: './button.component.html',
  styleUrls: ['./button.component.scss'],
  standalone: true,
  imports: [CommonModule]
})
export class CustomButtonComponent {
  @Input() variant: ButtonVariant = 'default';
  @Input() disabled: boolean = false;
  @Input() type: string = 'button';
  @Input() title: string = '';

  @Output() clicked = new EventEmitter<Event>();

  onClick(event: Event) {
    if (!this.disabled) {
      this.clicked.emit(event);
    }
  }

  get buttonClasses(): string {
    return [
      'custom-button',
      `custom-button--${this.variant}`,
      this.disabled ? 'custom-button--disabled' : ''
    ].join(' ').trim();
  }
}
