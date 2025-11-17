import {ErrorStateMatcher} from '@angular/material/core';
import {FormControl} from '@angular/forms';

export class CustomErrorStateMatcher implements ErrorStateMatcher {
  isErrorState(control: FormControl | null): boolean {
    const isTouched = control?.touched || control?.dirty;
    const hasServerError = control?.hasError('server');
    return !!(control?.invalid && (isTouched || hasServerError));
  }
}
