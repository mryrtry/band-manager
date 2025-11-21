import {MusicGenre} from '../music-genre.enum';

export interface BestBandAward {
  id: number;
  bandId: number;
  bandName: string;
  genre: MusicGenre;
  createdBy: string;
  createdDate: Date;
  lastModifiedBy: string;
  lastModifiedDate: Date;
}
