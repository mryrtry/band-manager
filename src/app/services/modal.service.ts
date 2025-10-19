import {Injectable, ApplicationRef, createComponent, EnvironmentInjector, inject, ComponentRef} from '@angular/core';
import {ModalComponent} from '../components/modal/modal.component';

@Injectable({ providedIn: 'root' })
export class ModalService {
  private appRef = inject(ApplicationRef);
  private injector = inject(EnvironmentInjector);

  open(content: string | HTMLElement, options?: { backdropClose?: boolean }) {
    const modalRef: ComponentRef<any> = createComponent(ModalComponent, {
      environmentInjector: this.injector,
    });

    if (options?.backdropClose !== undefined) {
      modalRef.instance.backdropClose = options.backdropClose;
    }

    this.appRef.attachView(modalRef.hostView);
    document.body.appendChild(modalRef.location.nativeElement);

    setTimeout(() => {
      const modalEl = modalRef.location.nativeElement as HTMLElement;
      const contentContainer = modalEl.querySelector('.modal-content');
      if (contentContainer) {
        if (typeof content === 'string') {
          contentContainer.innerHTML = content; // вставляем HTML
        } else {
          contentContainer.appendChild(content);
        }
      }
      modalRef.instance.open();
    });

    modalRef.instance.closed.subscribe(() => {
      this.appRef.detachView(modalRef.hostView);
      modalRef.destroy();
    });

    return modalRef.instance;
  }
}
