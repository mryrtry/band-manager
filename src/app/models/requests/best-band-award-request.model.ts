import {MusicGenre} from '../enums/music-genre.model';

export interface BestBandAwardRequest {
  musicBandId: number;
  genre: MusicGenre;
}
