// modal.service.ts
import { Injectable, ApplicationRef, ComponentRef, createComponent, EnvironmentInjector } from '@angular/core';
import { MusicBand } from '../models/music-band.model';
import {MusicBandModalComponent} from '../components/modal/modal.component';

export interface ModalOptions {
  mode: 'create' | 'edit';
  musicBandId?: number;
  initialData?: MusicBand;
}

@Injectable({
  providedIn: 'root'
})
export class ModalService {
  private modalRef: ComponentRef<MusicBandModalComponent> | null = null;

  constructor(
    private appRef: ApplicationRef,
    private environmentInjector: EnvironmentInjector
  ) {}

  openMusicBandModal(options: ModalOptions): Promise<MusicBand | undefined> {
    return new Promise((resolve) => {
      // Закрываем предыдущее модальное окно, если есть
      this.close();

      // Создаем компонент программно
      this.modalRef = createComponent(MusicBandModalComponent, {
        environmentInjector: this.environmentInjector
      });

      // Добавляем в DOM
      document.body.appendChild(this.modalRef.location.nativeElement);
      this.appRef.attachView(this.modalRef.hostView);

      // Настраиваем модальное окно
      this.modalRef.instance.open(
        {
          mode: options.mode,
          musicBandId: options.musicBandId,
          initialData: options.initialData
        },
        {
          onSubmit: (result) => resolve(result),
          onCancel: () => resolve(undefined),
          onDismiss: () => resolve(undefined)
        }
      );
    });
  }

  close(): void {
    if (this.modalRef) {
      this.appRef.detachView(this.modalRef.hostView);
      this.modalRef.destroy();
      this.modalRef = null;
    }
  }
}
