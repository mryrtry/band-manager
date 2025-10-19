import {Component, EventEmitter, HostListener, Input, OnChanges, Output, SimpleChanges,} from '@angular/core';
import {FormsModule} from '@angular/forms';

export interface ValidationResult {
  isValid: boolean;
  message?: string;
}

export interface Validator {
  validate: (value: any) => ValidationResult;
}

@Component({
  selector: 'app-input',
  imports: [FormsModule],
  templateUrl: './input.component.html',
  styleUrls: ['./input.component.scss'],
})
export class InputComponent implements OnChanges {
  @Input() label: string = '';
  @Input() placeholder: string = '';
  @Input() type: string = 'text';
  @Input() value: any = '';
  @Input() disabled: boolean = false;
  @Input() clearable?: boolean = true;
  @Input() validator?: Validator;

  @Output() valueChange = new EventEmitter<any>();
  @Output() onChange = new EventEmitter<any>();
  @Output() onValidate = new EventEmitter<ValidationResult>();

  public currentValue?: string | null = '';
  public errorMessage?: string;
  public isValid: boolean = true;

  public ngOnChanges(changes: SimpleChanges): void {
    if (changes['value']) {
      this.currentValue = changes['value'].currentValue ?? '';
      this.validate();
    }
  }

  public validate(): void {
    this.errorMessage = undefined;
    if (this.validator) {
      let validationResult = this.validator.validate(this.currentValue);
      this.isValid = validationResult.isValid;
      this.errorMessage = validationResult.message;
    } else {
      this.isValid = true;
    }
  }

  private updateValue(): void {
    this.validate();
    if (!this.isValid) return;
    this.value = this.currentValue;
    this.valueChange.emit(this.value);
    this.onChange.emit(this.value);
  }

  @HostListener('keydown', ['$event'])
  protected onKeyDown(event: KeyboardEvent): void {
    if (event.key == 'Enter') {
      this.updateValue();
      return;
    }
    if (this.type == "number" && event.key == '.') {
      event.preventDefault();
      return;
    }
  }

  public onInput(): void {
    if (!this.currentValue) this.currentValue = null;
    this.validate();
  }

  public onClear(): void {
    this.currentValue = null;
    this.updateValue();
  }

}
