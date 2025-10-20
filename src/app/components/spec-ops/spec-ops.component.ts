import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { MusicBandService } from '../../services/music-band.service';
import { BestBandAwardService } from '../../services/best-band-award.service';
import { MusicBand } from '../../models/music-band.model';
import { BestBandAward } from '../../models/best-band-award.model';
import { BestBandAwardRequest } from '../../models/requests/best-band-award-request.model';
import { getMusicGenreOptions } from '../../models/enums/music-genre.model';

@Component({
  selector: 'app-music-band-operations',
  templateUrl: './spec-ops.component.html',
  styleUrls: ['./spec-ops.component.scss'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule]
})
export class MusicBandOperationsComponent implements OnInit {
  private musicBandService = inject(MusicBandService);
  private bestBandAwardService = inject(BestBandAwardService);
  private fb = inject(FormBuilder);

  // Загрузка (общая для UX)
  isLoading = signal(false);

  // --- Состояния по операциям ---
  removeParticipantResult = signal<MusicBand | null>(null);
  removeParticipantError = signal<string | null>(null);
  removeParticipantSuccess = signal<string | null>(null);

  maxCoordinatesBand = signal<MusicBand | null>(null);
  maxCoordinatesError = signal<string | null>(null);
  maxCoordinatesSuccess = signal<string | null>(null);

  establishedBeforeBands = signal<MusicBand[]>([]);
  establishedBeforeError = signal<string | null>(null);
  establishedBeforeSuccess = signal<string | null>(null);

  uniqueAlbumsCount = signal<number[]>([]);
  uniqueAlbumsError = signal<string | null>(null);
  uniqueAlbumsSuccess = signal<string | null>(null);

  bestBandAwardResult = signal<BestBandAward | null>(null);
  bestBandAwardError = signal<string | null>(null);
  bestBandAwardSuccess = signal<string | null>(null);

  // --- Формы ---
  removeParticipantForm: FormGroup;
  establishedBeforeForm: FormGroup;
  createAwardForm: FormGroup;

  musicGenreOptions = getMusicGenreOptions();

  constructor() {
    this.removeParticipantForm = this.fb.group({
      bandId: ['', [Validators.required, Validators.min(1)]],
    });
    this.establishedBeforeForm = this.fb.group({
      date: ['', [Validators.required]],
    });
    this.createAwardForm = this.fb.group({
      musicBandId: ['', [Validators.required, Validators.min(1)]],
      genre: ['', [Validators.required]],
    });
  }

  ngOnInit() {}

  // --- Операции ---
  async onRemoveParticipant(): Promise<void> {
    if (this.removeParticipantForm.invalid) {
      this.markFormGroupTouched(this.removeParticipantForm);
      return;
    }

    this.isLoading.set(true);
    this.removeParticipantError.set(null);
    this.removeParticipantSuccess.set(null);
    this.removeParticipantResult.set(null);

    try {
      const bandId = Number(this.removeParticipantForm.value.bandId);
      const result = await firstValueFrom(this.musicBandService.removeParticipant(bandId));
      this.removeParticipantResult.set(result);
      this.removeParticipantSuccess.set('Участник успешно удалён из группы.');
      this.removeParticipantForm.reset();
    } catch (error: any) {
      this.removeParticipantError.set(this.extractError(error, 'Ошибка при удалении участника'));
    } finally {
      this.isLoading.set(false);
    }
  }

  async onGetMaxCoordinates(): Promise<void> {
    this.isLoading.set(true);
    this.maxCoordinatesError.set(null);
    this.maxCoordinatesSuccess.set(null);
    this.maxCoordinatesBand.set(null);

    try {
      const result = await firstValueFrom(this.musicBandService.getMusicBandWithMaxCoordinates());
      this.maxCoordinatesBand.set(result);
      this.maxCoordinatesSuccess.set('Группа найдена.');
    } catch (error: any) {
      this.maxCoordinatesError.set(this.extractError(error, 'Ошибка при поиске группы'));
    } finally {
      this.isLoading.set(false);
    }
  }

  async onGetEstablishedBefore(): Promise<void> {
    if (this.establishedBeforeForm.invalid) {
      this.markFormGroupTouched(this.establishedBeforeForm);
      return;
    }

    this.isLoading.set(true);
    this.establishedBeforeError.set(null);
    this.establishedBeforeSuccess.set(null);
    this.establishedBeforeBands.set([]);

    try {
      const date = this.establishedBeforeForm.value.date;
      const result = await firstValueFrom(this.musicBandService.getBandsEstablishedBefore(date));
      this.establishedBeforeBands.set(result);
      this.establishedBeforeSuccess.set(`Найдено ${result.length} групп`);
    } catch (error: any) {
      this.establishedBeforeError.set(this.extractError(error, 'Ошибка при поиске групп'));
    } finally {
      this.isLoading.set(false);
    }
  }

  async onGetUniqueAlbumsCount(): Promise<void> {
    this.isLoading.set(true);
    this.uniqueAlbumsError.set(null);
    this.uniqueAlbumsSuccess.set(null);
    this.uniqueAlbumsCount.set([]);

    try {
      const result = await firstValueFrom(this.musicBandService.getUniqueAlbumsCount());
      this.uniqueAlbumsCount.set(result);
      this.uniqueAlbumsSuccess.set('Данные получены.');
    } catch (error: any) {
      this.uniqueAlbumsError.set(this.extractError(error, 'Ошибка при получении данных'));
    } finally {
      this.isLoading.set(false);
    }
  }

  async onCreateBestBandAward(): Promise<void> {
    if (this.createAwardForm.invalid) {
      this.markFormGroupTouched(this.createAwardForm);
      return;
    }

    this.isLoading.set(true);
    this.bestBandAwardError.set(null);
    this.bestBandAwardSuccess.set(null);
    this.bestBandAwardResult.set(null);

    try {
      const { musicBandId, genre } = this.createAwardForm.value;
      const request: BestBandAwardRequest = {
        musicBandId: Number(musicBandId),
        genre,
      };

      const result = await firstValueFrom(this.bestBandAwardService.create(request));
      this.bestBandAwardResult.set(result);
      this.bestBandAwardSuccess.set('Награда успешно создана!');
      this.createAwardForm.reset();
    } catch (error: any) {
      this.bestBandAwardError.set(this.extractError(error, 'Ошибка при создании награды'));
    } finally {
      this.isLoading.set(false);
    }
  }

  // --- Вспомогательные методы ---
  private extractError(error: any, fallback: string): string {
    if (error?.error?.details) {
      const d = error.error.details;
      return Array.isArray(d) ? d.map((x) => x.message).join(', ') : d;
    }
    if (error?.status === 404) return 'Запись не найдена';
    if (error?.status === 400) return 'Неверный запрос';
    return fallback;
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach((key) =>
      formGroup.get(key)?.markAsTouched()
    );
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('ru-RU');
  }

  hasFieldError(form: FormGroup, field: string): boolean {
    const c = form.get(field);
    return !!(c?.invalid && (c.touched || c.dirty));
  }

  // --- Геттеры ошибок для форм ---
  get removeParticipantFieldError(): string {
    const c = this.removeParticipantForm.get('bandId');
    if (!c) return '';
    if (c.errors?.['required']) return 'ID группы обязателен';
    if (c.errors?.['min']) return 'ID должен быть положительным';
    return '';
  }

  get establishedBeforeFieldError(): string {
    const c = this.establishedBeforeForm.get('date');
    if (!c) return '';
    if (c.errors?.['required']) return 'Дата обязательна';
    return '';
  }

  get awardBandIdError(): string {
    const c = this.createAwardForm.get('musicBandId');
    if (!c) return '';
    if (c.errors?.['required']) return 'ID группы обязательно';
    if (c.errors?.['min']) return 'ID должен быть положительным';
    return '';
  }

  get awardGenreError(): string {
    const c = this.createAwardForm.get('genre');
    if (!c) return '';
    if (c.errors?.['required']) return 'Жанр обязателен';
    return '';
  }
}
