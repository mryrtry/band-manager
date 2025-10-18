import {Component, EventEmitter, HostListener, Input, OnChanges, Output, SimpleChanges,} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {Observable} from 'rxjs';

export interface ValidationResult {
  isValid: boolean;
  message?: string;
}

export interface Validator {
  validate: (value: any) => ValidationResult | Promise<ValidationResult> | Observable<ValidationResult>;
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
  @Input() validateOn: 'change' | 'blur' | 'realtime' = 'realtime';
  @Input() width?: string = '200px';
  @Input() minValue?: string;
  @Input() maxValue?: string;
  @Input() pattern?: string | RegExp;

  @Output() valueChange = new EventEmitter<any>();
  @Output() onChange = new EventEmitter<any>();
  @Output() onValidate = new EventEmitter<ValidationResult>();

  public currentValue?: string | null = '';
  public errorMessage: string | undefined;
  protected defaultValue: any;
  protected validating = false;
  public isValid: boolean = true;

  public async ngOnChanges(changes: SimpleChanges): Promise<void> {
    if (changes['value']) {
      this.currentValue = changes['value'].currentValue ?? '';
      this.defaultValue = changes['value'].currentValue;
      if (this.validateOn === 'change' || this.validateOn === 'realtime') {
        await this.validate();
      }
    }
  }

  public async validate(): Promise<boolean> {
    this.errorMessage = undefined;
    let valid = true;
    const valueStr = this.currentValue != null ? String(this.currentValue) : '';
    if (!valueStr) {
      this.isValid = true;
      this.onValidate.emit({isValid: true, message: undefined});
      return true;
    }
    if (this.pattern) {
      let regex: RegExp;
      if (this.pattern instanceof RegExp) {
        regex = new RegExp(`^${this.pattern.source}$`);
      } else {
        regex = new RegExp(`^${this.pattern}$`);
      }

      if (!regex.test(valueStr)) {
        valid = false;
        this.errorMessage = 'Неверный формат';
      }
    }
    if (this.type === 'number') {
      const num = Number(valueStr);
      if (isNaN(num)) {
        valid = false;
        this.errorMessage = 'Должно быть числом';
      } else {
        if (this.minValue != null && num < Number(this.minValue)) {
          valid = false;
          this.errorMessage = `Минимум ${this.minValue}`;
        }
        if (this.maxValue != null && num > Number(this.maxValue)) {
          valid = false;
          this.errorMessage = `Максимум ${this.maxValue}`;
        }
      }
    }
    if (this.validator) {
      this.validating = true;
      try {
        const result = await this.executeValidation();
        valid = valid && result.isValid;
        if (!result.isValid) {
          this.errorMessage = result.message;
        }
        this.onValidate.emit(result);
      } finally {
        this.validating = false;
      }
    } else {
      this.onValidate.emit({isValid: valid, message: this.errorMessage});
    }

    this.isValid = valid;
    return valid;
  }


  private async executeValidation(): Promise<ValidationResult> {
    const result = this.validator!.validate(this.currentValue);
    if (result instanceof Promise) return await result;
    if (result && typeof (result as any).subscribe === 'function') {
      return await new Promise((resolve) => (result as any).subscribe(resolve));
    }
    return result as ValidationResult;
  }

  private updateValue(): void {
    if (!this.isValid) return;

    if (!this.currentValue || this.currentValue.trim() === '') {
      this.value = null;
    } else {
      const normalized = this.currentValue.replace(',', '.').trim();
      this.value = this.type === 'number' ? Number(normalized) : this.currentValue;
    }
    this.valueChange.emit(this.value);
    this.onChange.emit(this.value);
  }

  @HostListener('keydown.enter', ['$event'])
  protected onEnterDown(_ignored: Event): void {
    this.updateValue();
  }

  public async onInput(): Promise<void> {
    if (this.validateOn === 'realtime') {
      await this.validate();
    }
  }

  public async onBlur(): Promise<void> {
    if (this.validateOn === 'blur' || this.validateOn === 'realtime') {
      await this.validate();
    }
  }

  public async onClear(): Promise<void> {
    this.currentValue = null;
    if (this.validateOn === 'realtime') await this.validate();
    this.updateValue();
  }
}
