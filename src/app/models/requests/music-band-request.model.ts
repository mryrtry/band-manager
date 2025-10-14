import {MusicGenre} from '../enums/music-genre.model';

export interface MusicBandRequest {
  name: string;
  coordinatesId: number;
  genre: MusicGenre;
  numberOfParticipants: number;
  singlesCount: number;
  description: string;
  bestAlbumId: number;
  albumsCount: number;
  establishmentDate: string;
  frontManId: number;
}
