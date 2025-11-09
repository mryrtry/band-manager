import { Component, Input, Output, EventEmitter, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PaginatorModule, PaginatorState } from 'primeng/paginator';
import { BreakpointObserver, BreakpointState } from '@angular/cdk/layout';
import { Subscription } from 'rxjs';

export const CustomBreakpoints = {
  XSmall: '(max-width: 575.98px)',
  Small: '(min-width: 576px) and (max-width: 767.98px)',
  Medium: '(min-width: 768px) and (max-width: 991.98px)',
  Large: '(min-width: 992px) and (max-width: 1199.98px)',
  XLarge: '(min-width: 1200px)'
};

@Component({
  selector: 'app-paginator',
  standalone: true,
  imports: [CommonModule, PaginatorModule],
  template: `
    <div class="flex justify-content-between align-items-center mt-2">
      <p-paginator
        (onPageChange)="onPageChange($event)"
        [first]="first"
        [rowsPerPageOptions]="rowsPerPageOptions"
        [rows]="size"
        [totalRecords]="totalRecords"
        [showFirstLastIcon]="showFirstLastButtons"
        [showPageLinks]="showPageLinks"
        [showCurrentPageReport]="showCurrentPageReport"
        [showJumpToPageDropdown]="showJumpToPageDropdown"
        [currentPageReportTemplate]="currentPageReportTemplate"
        [pageLinkSize]="pageLinkSize"
      >
      </p-paginator>
    </div>
  `,
})
export class PaginatorComponent implements OnInit, OnDestroy {
  private breakpointObserver = inject(BreakpointObserver);
  private breakpointSubscription!: Subscription;

  @Input() page: number = 0;
  @Input() size: number = 10;
  @Input() totalRecords: number = 0;

  @Output() pageChange = new EventEmitter<{ page: number; size: number }>();

  rowsPerPageOptions: number[] = [10, 25, 50];
  showFirstLastButtons: boolean = true;
  showPageLinks: boolean = true;
  showCurrentPageReport: boolean = false;
  showJumpToPageDropdown: boolean = true;
  currentPageReportTemplate: string = 'Total: {totalRecords} ({first} - {last})';
  pageLinkSize: number = 5;

  ngOnInit(): void {
    this.setupBreakpointObserver();
  }

  ngOnDestroy(): void {
    if (this.breakpointSubscription) {
      this.breakpointSubscription.unsubscribe();
    }
  }

  private setupBreakpointObserver(): void {
    this.breakpointSubscription = this.breakpointObserver
      .observe([
        CustomBreakpoints.XSmall,
        CustomBreakpoints.Small,
        CustomBreakpoints.Medium,
        CustomBreakpoints.Large,
        CustomBreakpoints.XLarge
      ])
      .subscribe((state: BreakpointState) => {
        if (state.breakpoints[CustomBreakpoints.XSmall]) {
          this.rowsPerPageOptions = [5, 10];
          this.showFirstLastButtons = true;
          this.showPageLinks = true;
          this.pageLinkSize = 3;
          this.showCurrentPageReport = false;
          this.showJumpToPageDropdown = false;
        } else if (state.breakpoints[CustomBreakpoints.Small]) {
          this.rowsPerPageOptions = [5, 10, 25];
          this.showFirstLastButtons = true;
          this.showPageLinks = true;
          this.showCurrentPageReport = false;
          this.showJumpToPageDropdown = false;
        } else if (state.breakpoints[CustomBreakpoints.Medium]) {
          this.rowsPerPageOptions = [10, 25, 50];
          this.showFirstLastButtons = true;
          this.showPageLinks = true;
          this.showCurrentPageReport = false;
          this.showJumpToPageDropdown = false;
        } else {
          this.rowsPerPageOptions = [10, 25, 50, 100];
          this.showFirstLastButtons = true;
          this.showPageLinks = true;
          this.showCurrentPageReport = true;
          this.showJumpToPageDropdown = false;
        }
      });
  }

  get first(): number {
    return (this.page || 0) * (this.size || 10)
  }

  onPageChange(event: PaginatorState): void {
    const page = event.first && event.rows ? Math.floor(event.first / event.rows) : 0;
    const size = event.rows || 10;

    this.pageChange.emit({ page, size });
  }
}
