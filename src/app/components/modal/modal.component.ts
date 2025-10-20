// music-band-modal.component.ts
import {
  Component,
  Inject,
  OnInit,
  OnDestroy,
  ViewChild,
  ElementRef,
  AfterViewInit,
  signal
} from '@angular/core';
import { CommonModule, DOCUMENT } from '@angular/common';
import { MusicBand } from '../../models/music-band.model';
import {MusicBandFormComponent} from '../music-band-form/music-band-form.component';

export interface MusicBandModalConfig {
  mode: 'create' | 'edit';
  musicBandId?: number;
  initialData?: MusicBand;
}

@Component({
  selector: 'app-music-band-modal',
  templateUrl: './modal.component.html',
  styleUrls: ['./modal.component.scss'],
  standalone: true,
  imports: [CommonModule, MusicBandFormComponent]
})
export class MusicBandModalComponent implements OnInit, OnDestroy, AfterViewInit {
  @ViewChild('modalOverlay') modalOverlay!: ElementRef<HTMLDivElement>;
  @ViewChild('modalContent') modalContent!: ElementRef<HTMLDivElement>;
  @ViewChild(MusicBandFormComponent) formComponent!: MusicBandFormComponent;

  isOpen = signal(false);
  isLoading = signal(false);

  // Конфигурация модального окна
  config: MusicBandModalConfig = {
    mode: 'create'
  };

  // Callbacks для внешнего кода
  private onSubmitCallback?: (result: MusicBand) => void;
  private onCancelCallback?: () => void;
  private onDismissCallback?: () => void;

  constructor(@Inject(DOCUMENT) private document: Document) {}

  ngOnInit() {
    this.disableBodyScroll();
  }

  ngOnDestroy() {
    this.enableBodyScroll();
  }

  ngAfterViewInit() {
    // Фокусируемся на модальном окне после инициализации
    setTimeout(() => {
      this.focusModal();
    });
  }

  // Public API для открытия модального окна
  open(config: MusicBandModalConfig, callbacks: {
    onSubmit?: (result: MusicBand) => void;
    onCancel?: () => void;
    onDismiss?: () => void;
  } = {}): void {
    this.config = { ...config };
    this.onSubmitCallback = callbacks.onSubmit;
    this.onCancelCallback = callbacks.onCancel;
    this.onDismissCallback = callbacks.onDismiss;

    this.isOpen.set(true);
    this.disableBodyScroll();

    // Фокусируемся на модальном окне после открытия
    setTimeout(() => {
      this.focusModal();
    }, 100);
  }

  close(): void {
    this.isOpen.set(false);
    this.enableBodyScroll();
    this.onDismissCallback?.();
  }

  // Обработчики событий формы
  onFormSubmit(result: MusicBand): void {
    this.onSubmitCallback?.(result);
    this.close();
  }

  onFormCancel(): void {
    this.onCancelCallback?.();
    this.close();
  }

  // Обработка клика на оверлей
  onOverlayClick(event: MouseEvent): void {
    if (event.target === this.modalOverlay.nativeElement) {
      this.close();
    }
  }

  // Обработка нажатия клавиш
  onKeydown(event: KeyboardEvent): void {
    switch (event.key) {
      case 'Escape':
        event.preventDefault();
        this.close();
        break;
      case 'Tab':
        this.handleTabKey(event);
        break;
    }
  }

  // Управление фокусом внутри модального окна
  private handleTabKey(event: KeyboardEvent): void {
    const focusableElements = this.getFocusableElements();
    if (focusableElements.length === 0) return;

    const firstElement = focusableElements[0];
    const lastElement = focusableElements[focusableElements.length - 1];

    if (event.shiftKey) {
      if (document.activeElement === firstElement) {
        lastElement.focus();
        event.preventDefault();
      }
    } else {
      if (document.activeElement === lastElement) {
        firstElement.focus();
        event.preventDefault();
      }
    }
  }

  private getFocusableElements(): HTMLElement[] {
    const selector = `
      button:not([disabled]),
      [href],
      input:not([disabled]),
      select:not([disabled]),
      textarea:not([disabled]),
      [tabindex]:not([tabindex="-1"])
    `;

    return Array.from(
      this.modalContent.nativeElement.querySelectorAll(selector)
    ) as HTMLElement[];
  }

  private focusModal(): void {
    if (this.modalContent) {
      // Фокусируемся на первом фокусируемом элементе
      const focusableElements = this.getFocusableElements();
      if (focusableElements.length > 0) {
        focusableElements[0].focus();
      } else {
        this.modalContent.nativeElement.focus();
      }
    }
  }

  private disableBodyScroll(): void {
    const body = this.document.body;
    body.style.overflow = 'hidden';
    body.style.paddingRight = this.getScrollbarWidth() + 'px';
  }

  private enableBodyScroll(): void {
    const body = this.document.body;
    body.style.overflow = '';
    body.style.paddingRight = '';
  }

  private getScrollbarWidth(): number {
    const outer = this.document.createElement('div');
    outer.style.visibility = 'hidden';
    outer.style.overflow = 'scroll';
    this.document.body.appendChild(outer);

    const inner = this.document.createElement('div');
    outer.appendChild(inner);

    const scrollbarWidth = outer.offsetWidth - inner.offsetWidth;

    outer.parentNode?.removeChild(outer);

    return scrollbarWidth;
  }

  // Геттеры для шаблона
  get modalTitle(): string {
    return this.config.mode === 'edit'
      ? 'Редактирование музыкальной группы'
      : 'Создание музыкальной группы';
  }
}
