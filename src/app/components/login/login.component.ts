import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Router, RouterModule} from '@angular/router';
import {AuthService} from '../../services/auth.service';

@Component({
  selector: 'app-login', standalone: true, imports: [CommonModule, FormsModule, RouterModule], templateUrl: './login.component.html', styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  credentials = {
    username: '', password: ''
  };

  isLoading = false;
  errorMessage = '';

  onSubmit(): void {
    if (!this.credentials.username || !this.credentials.password) {
      this.errorMessage = 'Пожалуйста, заполните все поля';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(this.credentials).subscribe({
      next: (response) => {
        if (response) {
          this.router.navigate(['/']).then();
        } else {
          this.isLoading = false;
          this.errorMessage = 'Неверное имя пользователя или пароль';
        }
      }, error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Ошибка авторизации';
      }
    });
  }
}
