import {MusicGenre} from '../music-genre.enum';

export interface BestBandAwardRequest {
  musicBandId: number;
  genre: MusicGenre;
}
