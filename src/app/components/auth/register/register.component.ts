import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
  AbstractControl
} from '@angular/forms';
import {Router} from '@angular/router';
import {AuthService} from '../../../services/auth/auth.service';
import {MessageService} from 'primeng/api';
import {ButtonDirective, ButtonLabel} from 'primeng/button';
import {InputText} from 'primeng/inputtext';
import {Message} from 'primeng/message';
import {UserRequest} from '../../../model/auth/request/user.request';
import {ErrorDetail} from '../../../model/error-response.model';
import {Password} from 'primeng/password';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonLabel,
    ButtonDirective,
    InputText,
    Message,
    Password,
  ],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {

  registerForm: FormGroup;
  authService: AuthService = inject(AuthService);
  messageService: MessageService = inject(MessageService);
  isLoading: boolean = false;

  constructor(private fb: FormBuilder, private router: Router) {
    this.registerForm = this.fb.group({
      username: ['', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(50),
      ]],
      password: ['', [
        Validators.required,
        Validators.minLength(6),
      ]],
      confirmPassword: ['', [
        Validators.required,
      ]]
    }, { validators: this.passwordMatchValidator });
  }

  private passwordMatchValidator(group: AbstractControl): null | object {
    const password = group.get('password')?.value;
    const confirm = group.get('confirmPassword')?.value;
    return password === confirm ? null : { passwordMismatch: true };
  }

  isInvalid(controlName: string): boolean {
    const control = this.registerForm.get(controlName);
    return control ? (control.invalid && (control.dirty || control.touched)) : false;
  }

  onSubmit() {
    this.registerForm.markAllAsTouched();

    if (this.registerForm.valid) {
      this.isLoading = true;
      const userRequest: UserRequest = {
        username: this.registerForm.value.username,
        password: this.registerForm.value.password
      };

      this.authService.register(userRequest).subscribe({
        next: (_response) => {
          this.isLoading = false;
          this.messageService.add({
            severity: 'success',
            summary: 'Регистрация успешна',
            detail: 'Вы вошли в систему'
          });
          this.router.navigate(['/']).then();
        },
        error: (error) => {
          this.isLoading = false;
          if (error.error?.details) {
            const details = error.error.details as ErrorDetail[];
            details.forEach(detail => {
              const control = this.registerForm.get(detail.field);
              if (control) {
                control.setErrors({ serverError: detail.message });
              }
            });
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
