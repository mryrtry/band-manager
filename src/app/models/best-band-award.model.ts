import {MusicGenre} from './enums/music-genre.model';

export interface BestBandAward {
  id: number;
  bandId: number;
  bandName: string;
  genre: MusicGenre;
  createdAt: string;
}
