import {Color} from '../color.enum';
import {Country} from '../country.enum';

export interface Person {
  id: number;
  name: string;
  eyeColor: Color;
  hairColor: Color;
  location: Location;
  weight: number;
  nationality: Country;
  createdBy: string;
  createdDate: Date;
  lastModifiedBy: string;
  lastModifiedDate: Date;
}
