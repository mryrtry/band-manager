import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../../services/auth.service';
import { LoginRequest } from '../../../model/auth/request/login.request';
import { CustomErrorStateMatcher } from '../../../util/error-state-manager.util';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent {
  private formBuilder = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  protected loading = signal(false);
  protected generalError = signal('');

  protected usernameErrorMatcher = new CustomErrorStateMatcher();
  protected passwordErrorMatcher = new CustomErrorStateMatcher();

  protected loginForm = this.formBuilder.group({
    username: ['', [
      Validators.required,
      Validators.minLength(3),
      Validators.maxLength(50)
    ]],
    password: ['', [
      Validators.required,
      Validators.minLength(6)
    ]],
  });

  protected get username(): FormControl {
    return this.loginForm.get('username') as FormControl;
  }

  protected get password(): FormControl {
    return this.loginForm.get('password') as FormControl;
  }

  protected onSubmit(): void {
    if (this.loginForm.invalid) {
      this.markFormGroupTouched();
      return;
    }

    this.loading.set(true);
    this.clearErrors();

    const loginData: LoginRequest = this.loginForm.value as LoginRequest;

    this.authService.login(loginData).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/']);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.handleError(error);
      },
    });
  }

  private handleError(error: HttpErrorResponse): void {
    if (error.status === 0) {
      this.generalError.set('Ошибка соединения с сервером');
      return;
    }

    if (error.error?.details) {
      error.error.details.forEach((detail: any) => {
        const control = this.loginForm.get(detail.field);
        control?.setErrors({ server: detail.message });
      });
    } else if (error.error?.message) {
      this.generalError.set(error.error.message);
    } else {
      this.generalError.set('Произошла неизвестная ошибка');
    }
  }

  private clearErrors(): void {
    Object.values(this.loginForm.controls).forEach((control) => {
      const errors = control.errors;
      if (errors?.['server']) {
        delete errors['server'];
        control.setErrors(Object.keys(errors).length ? errors : null);
      }
    });
    this.generalError.set('');
  }

  private markFormGroupTouched(): void {
    Object.values(this.loginForm.controls).forEach((control) => control.markAsTouched());
  }

  protected getUsernameHint(): string {
    const currentLength = this.username.value?.length || 0;
    return `${currentLength}/50`;
  }

}
