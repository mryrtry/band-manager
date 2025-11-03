import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Router, RouterModule} from '@angular/router';
import {AuthService} from '../../services/auth.service';
import {Role} from '../../models/user.model';

@Component({
  selector: 'app-header', standalone: true, imports: [CommonModule, RouterModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent {

  private authService = inject(AuthService);

  private router = inject(Router);

  currentUser$ = this.authService.currentUser$;

  isAuthenticated$ = this.authService.isAuthenticated$;

  getRoleDisplayName(role: Role): string {
    if (this.authService.isAdmin()) {
      return 'Администратор'
    } return 'Пользователь'
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/login']).then();
      }
    });
  }

}
