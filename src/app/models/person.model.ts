import {Color} from './enums/color.model';
import {Country} from './enums/country.model';
import {Location} from './location.model';

export interface Person {
  id: number;
  name: string;
  eyeColor: Color;
  hairColor: Color;
  location: Location;
  weight: number;
  nationality: Country;
}
