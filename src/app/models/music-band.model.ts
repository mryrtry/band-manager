import {MusicGenre} from './enums/music-genre.model';
import {Album} from './album.model';
import {Coordinates} from './coordinates.model';
import {Person} from './person.model';

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
  establishmentDate: string;
  frontMan: Person;
  creationDate: string;
}
