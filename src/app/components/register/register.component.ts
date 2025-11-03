// components/register/register.component.ts
import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Router, RouterModule} from '@angular/router';
import {AuthService} from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  credentials = {
    username: '',
    password: '',
    confirmPassword: ''
  };

  isLoading = false;
  errorMessage = '';

  onSubmit(): void {
    if (!this.credentials.username || !this.credentials.password) {
      this.errorMessage = 'Пожалуйста, заполните все поля';
      return;
    }

    if (this.credentials.password !== this.credentials.confirmPassword) {
      this.errorMessage = 'Пароли не совпадают';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const {confirmPassword, ...registerData} = this.credentials;

    this.authService.register(registerData).subscribe({
      next: () => {
        this.router.navigate(['/']).then();
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Ошибка регистрации';
      }
    });
  }
}
