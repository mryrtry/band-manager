import {MusicGenre} from '../music-genre.enum';

export interface MusicBandRequest {
  name: string;
  coordinatesId: number;
  genre: MusicGenre;
  numberOfParticipants: number;
  singlesCount: number;
  description: string;
  bestAlbumId: number;
  albumsCount: number;
  establishmentDate: Date;
  frontManId: number;
}
