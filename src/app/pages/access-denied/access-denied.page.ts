import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Fieldset} from 'primeng/fieldset';
import {Router, RouterLink} from '@angular/router';
import {ButtonDirective, ButtonIcon, ButtonLabel} from 'primeng/button';
import {
  ThemeToggleComponent
} from '../../components/header/theme-toggle/theme-toggle.component';

@Component({
  selector: 'app-access-denied',
  standalone: true,
  imports: [CommonModule, Fieldset, RouterLink, ButtonDirective, ButtonIcon, ButtonLabel, ThemeToggleComponent],
  templateUrl: './access-denied.page.html',
})
export class AccessDeniedPage implements OnInit {
  errorData: any = null;

  constructor(private router: Router) {
  }

  ngOnInit() {
    const navigation = this.router.lastSuccessfulNavigation;
    this.errorData = navigation?.extras?.state || null;
  }

}
