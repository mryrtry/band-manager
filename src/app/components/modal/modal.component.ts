import {AfterViewInit, Component, ElementRef, EventEmitter, HostListener, Input, Output, ViewChild} from '@angular/core';

@Component({
  selector: 'app-modal', templateUrl: './modal.component.html', styleUrls: ['./modal.component.scss']
})
export class ModalComponent implements AfterViewInit {
  @ViewChild('modalContainer') modalContainer!: ElementRef<HTMLDivElement>;

  @Input() backdropClose: boolean = true;
  @Output() opened = new EventEmitter<void>();
  @Output() closed = new EventEmitter<void>();

  isOpen = false;

  private focusableElements: HTMLElement[] = [];
  private firstFocusable!: HTMLElement;
  private lastFocusable!: HTMLElement;

  open() {
    this.isOpen = true;
    this.opened.emit();
    setTimeout(() => this.setFocusTrap(), 0);
  }

  close() {
    this.isOpen = false;
    this.closed.emit();
  }

  ngAfterViewInit() {
    if (this.isOpen) {
      this.setFocusTrap();
    }
  }

  private setFocusTrap() {
    const focusableSelectors = ['a[href]', 'button', 'textarea', 'input', 'select', '[tabindex]:not([tabindex="-1"])'];
    this.focusableElements = Array.from(this.modalContainer.nativeElement.querySelectorAll(focusableSelectors.join(','))) as HTMLElement[];

    if (this.focusableElements.length) {
      this.firstFocusable = this.focusableElements[0];
      this.lastFocusable = this.focusableElements[this.focusableElements.length - 1];
      this.firstFocusable.focus();
    }
  }

  @HostListener('document:keydown', ['$event']) handleKeyboard(event: KeyboardEvent) {
    if (!this.isOpen) return;

    if (event.key === 'Escape') {
      this.close();
    }

    if (event.key === 'Tab') {
      if (this.focusableElements.length === 0) {
        event.preventDefault();
        return;
      }

      const active = document.activeElement as HTMLElement;

      if (event.shiftKey) { // shift + tab
        if (active === this.firstFocusable) {
          event.preventDefault();
          this.lastFocusable.focus();
        }
      } else { // tab
        if (active === this.lastFocusable) {
          event.preventDefault();
          this.firstFocusable.focus();
        }
      }
    }
  }

  onBackdropClick(event: MouseEvent) {
    if (this.backdropClose && event.target === event.currentTarget) {
      this.close();
    }
  }
}
