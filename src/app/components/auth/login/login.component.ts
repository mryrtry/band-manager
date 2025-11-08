import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import {AuthService} from '../../../services/auth.service';
import {Router} from '@angular/router';
import {MessageService} from 'primeng/api';
import {ButtonDirective, ButtonLabel} from 'primeng/button';
import {InputText} from 'primeng/inputtext';
import {Message} from 'primeng/message';
import {LoginRequest} from '../../../model/auth/request/login.request';
import {ErrorDetail} from '../../../model/error-response.model';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonLabel,
    ButtonDirective,
    InputText,
    Message,
  ]
})
export class LoginComponent {

  loginForm: FormGroup;

  authService: AuthService = inject(AuthService);

  messageService: MessageService = inject(MessageService);

  isLoading: boolean = false;

  constructor(private router: Router,
              private fb: FormBuilder) {
    this.loginForm = this.fb.group({
      username: ['', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(50),
      ]],
      password: ['', [
        Validators.required,
        Validators.minLength(6),
      ]],
      rememberMe: [false]
    })
  }

  isInvalid(controlName: string): boolean {
    const control = this.loginForm.get(controlName);
    return control ? (control.invalid && (control.dirty || control.touched)) : false;
  }

  onSubmit() {
    this.loginForm.markAllAsTouched();

    if (this.loginForm.valid) {
      this.isLoading = true;
      let loginRequest: LoginRequest = {
        username: this.loginForm.value.username,
        password: this.loginForm.value.password
      }
      this.authService.login(loginRequest).subscribe({
        next: (_ignored) => {
          this.isLoading = false;
          this.router.navigate(['/']).then();
        },
        error: (error) => {
          this.isLoading = false;
          if (error.error.details) {
            const details = error.error.details as ErrorDetail[];
            details.forEach(detail => {
              const control = this.loginForm.get(detail.field)
              if (control) {
                control.setErrors({ serverError: detail.message })
              }
            })
          } else {
            this.messageService.add({
              severity: 'error',
              summary: 'Login failed',
              detail: 'Server unavailable'
            });
          }
        }
      });
    } else {
      this.messageService.add({
        severity: 'error',
        summary: 'Validation Error',
        detail: 'Please fix the errors in the form'
      });
    }
  }
}
