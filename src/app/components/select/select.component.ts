import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-custom-select',
  templateUrl: './select.component.html',
  styleUrls: ['./select.component.css']
})
export class SelectComponent {
  @Input() options: { label: string; value: any }[] = [];
  @Input() selected: any;
  @Input() placeholder: string = 'Выберите...';
  @Output() selectedChange = new EventEmitter<any>();

  isOpen = false;

  toggleDropdown() {
    this.isOpen = !this.isOpen;
  }

  selectOption(option: any) {
    this.selected = option.value;
    this.selectedChange.emit(option.value);
    this.isOpen = false;
  }

  get selectedLabel(): string {
    const found = this.options.find(o => o.value === this.selected);
    return found ? found.label : this.placeholder;
  }

  closeDropdown() {
    this.isOpen = false;
  }
}
