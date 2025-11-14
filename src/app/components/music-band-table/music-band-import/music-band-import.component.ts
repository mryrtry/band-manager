import {Component, inject, Input, OnInit, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {DialogModule} from 'primeng/dialog';
import {
  FileSelectEvent,
  FileUpload,
  FileUploadModule
} from 'primeng/fileupload';
import {ProgressBarModule} from 'primeng/progressbar';
import {ButtonModule} from 'primeng/button';
import {BadgeModule} from 'primeng/badge';
import {ImportService} from '../../../services/import.service';
import {MessageService} from 'primeng/api';
import {
  ImportOperation,
  ImportStatus
} from '../../../model/import/import.model';
import {Panel} from 'primeng/panel';

@Component({
  selector: 'music-band-import',
  templateUrl: './music-band-import.component.html',
  styleUrls: ['./music-band-import.component.scss'],
  standalone: true,
  imports: [CommonModule, DialogModule, FileUploadModule, ProgressBarModule, ButtonModule, BadgeModule, Panel]
})
export class MusicBandImportComponent implements OnInit {
  @Input() disabled?: boolean;
  @ViewChild('fu') fu!: FileUpload;

  importService: ImportService = inject(ImportService);
  messageService: MessageService = inject(MessageService);
  formType: 'import' | 'polling' = 'import';

  supportedFormats: string[] = [];
  loading: boolean = false;
  errorMessage?: string;
  showDialog: boolean = false;
  files: File[] = [];
  importOperation?: ImportOperation;
  pollId?: number;

  get hasFiles(): boolean {
    return this.files.length > 0;
  }

  get statusDescription(): string {
    if (this.importOperation) {
      switch (this.importOperation.status) {
        case ImportStatus.PENDING:
          return 'Операция ожидает своей очереди'
        case ImportStatus.PROCESSING:
          return 'Операция выполняется'
        case ImportStatus.COMPLETED:
          return 'Операция выполнена успешно'
        case ImportStatus.FAILED:
          return 'Неверный формат файла / неверная структура данных'
        case ImportStatus.VALIDATION_FAILED:
          return this.importOperation.errorMessage ? this.importOperation.errorMessage : 'Валидация сущностей провалена';
      }
    }
    return ''
  }

  get progressBarColor(): string {
    if (this.importOperation) {
      switch (this.importOperation.status) {
        case ImportStatus.PENDING:
          return 'var(--p-yellow-400)'
        case ImportStatus.PROCESSING:
          return 'var(--p-text-color)'
        case ImportStatus.COMPLETED:
          return 'var(--p-primary-color)'
        case ImportStatus.FAILED:
          return 'var(--p-red-400)'
        case ImportStatus.VALIDATION_FAILED:
          return 'var(--p-red-400)'
      }
    }
    return 'red';
  }

  get progressBarValue(): number {
    if (this.importOperation) {
      switch (this.importOperation.status) {
        case ImportStatus.PENDING:
          return 25;
        case ImportStatus.PROCESSING:
          return 50;
        case ImportStatus.COMPLETED:
          return 100;
        case ImportStatus.FAILED:
          return 100;
        case ImportStatus.VALIDATION_FAILED:
          return 100;
      }
    }
    return 0;
  }

  ngOnInit(): void {
    this.loadSupportedFormats();
  }

  loadSupportedFormats(): void {
    this.loading = true;
    this.importService.getSupportedFormats().subscribe({
      next: formats => {
        this.supportedFormats = formats;
        this.loading = false;
      }, error: error => {
        this.errorMessage = error;
        this.loading = false;
      }
    })
  }

  pollImportOperation(id: number): void {
    this.loading = true;
    this.importService.getImportOperation(id).subscribe({
      next: operation => {
        this.importOperation = operation;
      }
    })
  }

  openDialog(): void {
    this.formType = 'import';
    if (this.fu) {
      this.fu.clear();
    }
    this.files = [];
    this.showDialog = true;
  }

  hideDialog(): void {
    this.showDialog = false;
  }

  choose(): void {
    this.fu.choose();
  }

  upload() {
    if (this.files.length > 0) {
      this.loading = true;
      this.importService.importMusicBands(this.files[0]).subscribe({
        next: operation => {
          this.importOperation = operation;
          this.formType = 'polling';
          this.loading = false;
        }, error: error => {
          this.messageService.add({
            severity: 'error', summary: 'Ошибка', detail: error.message,
          });
          this.loading = false;
        }
      })
    }
  }

  select($event: FileSelectEvent) {
    if (!this.validateFile($event.files[0])) {
      this.fu.clear();
      return;
    }
    this.files = $event.currentFiles;
  }

  toImport(): void {
    this.importOperation = undefined;
    this.files = [];
    this.formType = 'import';
  }

  validateFile(file: File): boolean {
    if (!this.supportedFormats.some(mime => file.type === mime)) {
      this.messageService.add({
        severity: 'error',
        summary: 'Неподдерживаемый формат',
        detail: `Формат '${file.type}' не поддерживается`
      });
      return false;
    }
    if (file.size >= 10 * 1024 * 1024) {
      this.messageService.add({
        severity: 'error',
        summary: 'Слишком большой файл',
        detail: `Размер файла: ${this.formatFileSize(file.size)}, больше максимального (10 МБ)`
      });
      return false;
    }
    return true;
  }

  poll(): void {
    if (this.importOperation) {
      this.pollImportOperation(this.importOperation.id);
      if ([ImportStatus.FAILED, ImportStatus.COMPLETED, ImportStatus.VALIDATION_FAILED].indexOf(this.importOperation.status) > -1) {
        this.stopPolling();
      }
    }
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Б';
    const k = 1024;
    const sizes = ['Б', 'КБ', 'МБ', 'ГБ', 'ТБ'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  startPolling(): void {
    setInterval(this.poll.bind(this), 1000)
  }

  stopPolling(): void {
    if (this.pollId) {
      clearInterval(this.pollId);
    }
  }

  protected readonly ImportStatus = ImportStatus;
}
