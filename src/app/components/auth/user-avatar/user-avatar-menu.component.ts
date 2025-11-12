import {Component, inject, OnInit, ViewChild} from '@angular/core';
import {AvatarModule} from 'primeng/avatar';
import {TieredMenu, TieredMenuModule} from 'primeng/tieredmenu';
import {MenuItem} from 'primeng/api';
import {Router} from '@angular/router';
import {Observable} from 'rxjs';
import {AuthService} from '../../../services/auth/auth.service';
import {UserService} from "../../../services/auth/user.service";
import {Role, User} from '../../../model/auth/user.model';

@Component({
  selector: 'app-user-avatar-menu',
  templateUrl: './user-avatar-menu.component.html',
  standalone: true,
  imports: [AvatarModule, TieredMenuModule],
  styleUrls: ['./user-avatar-menu.component.scss']
})
export class UserAvatarMenuComponent implements OnInit {
  @ViewChild('menu') menu!: TieredMenu;

  userService: UserService = inject(UserService);
  authService: AuthService = inject(AuthService);
  router = inject(Router);

  currentUser$: Observable<User | null> = this.userService.currentUser$;
  userLetter = '?';
  menuItems: MenuItem[] = [];

  ngOnInit(): void {
    this.currentUser$.subscribe(user => {
      if (user) {
        this.userLetter = user.username[0].toUpperCase();
        this.buildMenu(user);
      } else {
        this.menuItems = [{ label: 'Загрузка...', disabled: true }];
      }
    });

    this.userService.getCurrentUser(true).subscribe();
  }

  private buildMenu(user: User): void {
    this.menuItems = [
      {
        label: `
          <div class="custom-profile-item">
            <p style="margin:0; font-size:12px; opacity:0.7;"><span>id:</span> ${user.id}</p>
            <p style="margin:0; font-size:12px; opacity:0.7;"><span>username:</span> ${user.username}</p>
            <p style="margin:0; font-size:12px; opacity:0.7;" class="role">
              ${user.roles.map(r => this.getRoleDisplayName(r)).join('<br/>')}
            </p>
          </div>
        `,
        escape: false,
        styleClass: 'non-clickable-container',
        disabled: true
      },
      { separator: true },
      { label: 'Выйти', icon: 'pi pi-sign-out', command: () => this.logout() }
    ];
  }

  getRoleDisplayName(role: Role): string {
    switch (role) {
      case Role.ROLE_ADMIN: return 'Администратор';
      case Role.ROLE_USER: return 'Пользователь';
      default: return 'Не определена';
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth']).then();
  }

  onMenuToggle($event: PointerEvent) {
    this.userService.getCurrentUser(true).subscribe();
    this.menu.toggle($event);
  }

  protected readonly alert = alert;
}
