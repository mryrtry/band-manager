import {Coordinates} from '../coordinates/coordinates.model';
import {MusicGenre} from '../music-genre.enum';
import {Album} from '../album/album.model';
import {Person} from '../person/person.model';

export interface MusicBand {
  id: number;
  name: string;
  coordinates: Coordinates;
  genre: MusicGenre;
  numberOfParticipants: number;
  singlesCount: number;
  description: string;
  bestAlbum: Album;
  albumsCount: number;
  establishmentDate: Date;
  frontMan: Person;
  createdBy: string;
  createdDate: Date;
  lastModifiedBy: string;
  lastModifiedDate: Date;
}

