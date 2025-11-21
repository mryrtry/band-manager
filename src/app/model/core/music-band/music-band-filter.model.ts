import {MusicGenre} from '../music-genre.enum';

export interface MusicBandFilter {
  name?: string;
  description?: string;
  genre?: MusicGenre;
  frontManName?: string;
  bestAlbumName?: string;
  minParticipants?: number;
  maxParticipants?: number;
  minSingles?: number;
  maxSingles?: number;
  minAlbumsCount?: number;
  maxAlbumsCount?: number;
  minCoordinateX?: number;
  maxCoordinateX?: number;
  minCoordinateY?: number;
  maxCoordinateY?: number;
  establishmentDateBefore?: Date;
  establishmentDateAfter?: Date;
  createdBy?: string;
}
