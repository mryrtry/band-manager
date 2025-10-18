import {Component, Input, Output, EventEmitter, forwardRef, HostListener, ElementRef, inject} from '@angular/core';
import {NG_VALUE_ACCESSOR, ControlValueAccessor, FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';

export interface SelectOption {
  label: string;
  value: any;
  disabled?: boolean;
}

@Component({
  selector: 'app-custom-select',
  templateUrl: './select.component.html',
  styleUrls: ['./select.component.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule],
  providers: [{
    provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => CustomSelectComponent), multi: true
  }]
})
export class CustomSelectComponent implements ControlValueAccessor {
  private elementRef = inject(ElementRef);

  @Input() options: SelectOption[] = [];
  @Input() placeholder: string = 'Выберите...';
  @Input() label: string = '';
  @Input() disabled: boolean = false;
  @Input() required: boolean = false;
  @Input() searchable: boolean = false;
  @Output() selectedChange = new EventEmitter<any>();

  isOpen = false;
  selectedValue: any = null;
  searchQuery: string = '';
  focusedIndex = -1;

  private onChange: any = () => {
  };
  private onTouched: any = () => {
  };

  @HostListener('document:click', ['$event']) onClickOutside(event: Event) {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.close();
    }
  }

  @HostListener('keydown', ['$event']) onKeyDown(event: KeyboardEvent) {
    if (this.disabled) return;

    switch (event.key) {
      case 'ArrowDown':
        event.preventDefault();
        if (!this.isOpen) {
          this.open();
        } else {
          this.focusNext();
        }
        break;

      case 'ArrowUp':
        event.preventDefault();
        if (!this.isOpen) {
          this.open();
        } else {
          this.focusPrevious();
        }
        break;

      case 'Enter':
        event.preventDefault();
        if (this.isOpen && this.focusedIndex >= 0) {
          this.selectOption(this.filteredOptions[this.focusedIndex]);
        } else {
          this.toggle();
        }
        break;

      case 'Escape':
        event.preventDefault();
        this.close();
        break;

      case 'Tab':
        this.close();
        break;

      case 'Backspace':
        if (this.searchable && this.isOpen) {
          this.searchQuery = this.searchQuery.slice(0, -1);
        }
        break;

      default:
        if (this.searchable && this.isOpen && event.key.length === 1) {
          this.searchQuery += event.key;
          this.focusedIndex = 0;
        }
        break;
    }
  }

  get filteredOptions(): SelectOption[] {
    if (!this.searchQuery) return this.options;

    return this.options.filter(option => option.label.toLowerCase().includes(this.searchQuery.toLowerCase()));
  }

  get selectedLabel(): string {
    if (this.selectedValue === null || this.selectedValue === undefined) {
      return this.placeholder;
    }

    const selectedOption = this.options.find(opt => opt.value === this.selectedValue);
    return selectedOption?.label || this.placeholder;
  }

  toggle() {
    if (this.disabled) return;

    this.isOpen ? this.close() : this.open();
  }

  open() {
    if (this.disabled) return;

    this.isOpen = true;
    this.searchQuery = '';
    this.focusedIndex = this.findSelectedIndex();
  }

  close() {
    this.isOpen = false;
    this.searchQuery = '';
    this.focusedIndex = -1;
  }

  selectOption(option: SelectOption) {
    if (option.disabled) return;

    this.selectedValue = option.value;
    this.onChange(option.value);
    this.onTouched();
    this.selectedChange.emit(option.value);
    this.close();
  }

  private findSelectedIndex(): number {
    return this.filteredOptions.findIndex(opt => opt.value === this.selectedValue);
  }

  private focusNext() {
    if (this.filteredOptions.length === 0) return;

    this.focusedIndex = (this.focusedIndex + 1) % this.filteredOptions.length;
    this.scrollToFocused();
  }

  private focusPrevious() {
    if (this.filteredOptions.length === 0) return;

    this.focusedIndex = this.focusedIndex <= 0 ? this.filteredOptions.length - 1 : this.focusedIndex - 1;
    this.scrollToFocused();
  }

  private scrollToFocused() {
  }

  writeValue(value: any): void {
    this.selectedValue = value;
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

}
