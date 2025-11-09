import {Color} from '../color.enum';
import {Country} from '../country.enum';

export interface PersonRequest {
  name: string;
  eyeColor: Color;
  hairColor: Color;
  locationId: number;
  weight: number;
  nationality: Country;
}
