import {Component, OnInit} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {SelectButton} from 'primeng/selectbutton';

@Component({
  selector: 'app-theme-toggle',
  template: `
    <p-selectbutton (onChange)="onThemeChange()"
                    [(ngModel)]="theme"
                    [options]="themeOptions"
                    optionLabel="theme"
                    optionValue="theme">
      <ng-template #item let-item>
        <i [class]="item.icon"></i>
      </ng-template>
    </p-selectbutton>
  `,
  standalone: true,
  imports: [
    FormsModule,
    SelectButton
  ]
})
export class ThemeToggleComponent implements OnInit {

  protected theme: 'dark' | 'light' = 'dark';

  ngOnInit(): void {
    this.loadTheme();
  }

  protected themeOptions = [
    {icon: 'pi pi-sun', theme: 'light'},
    {icon: 'pi pi-moon', theme: 'dark'}
  ];

  protected onThemeChange(): void {
    this.applyTheme();
    this.saveTheme();
  }

  private loadTheme(): void {
    const savedTheme = localStorage.getItem('preferred-theme');
    if (savedTheme === 'light' || savedTheme === 'dark') {
      this.theme = savedTheme;
    }
    this.applyTheme();
  }

  private applyTheme(): void {
    const htmlElement = document.documentElement;
    htmlElement.classList.remove('my-app-dark');
    if (this.theme == 'dark') {
      htmlElement.classList.add(`my-app-dark`);
    } else {
      htmlElement.classList.remove(`my-app-dark`);
    }
  }

  private saveTheme(): void {
    localStorage.setItem('preferred-theme', this.theme);
  }

}
