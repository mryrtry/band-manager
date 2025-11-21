import {MusicGenre} from '../music-genre.enum';

export interface BestBandAwardFilter {
  bandId?: number;
  bandName?: string;
  genre?: MusicGenre;
  createdAtBefore?: Date;
  createdAtAfter?: Date;
}
