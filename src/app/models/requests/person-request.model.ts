import {Color} from '../enums/color.model';
import {Country} from '../enums/country.model';

export interface PersonRequest {
  name: string;
  eyeColor: Color;
  hairColor: Color;
  locationId: number;
  weight: number;
  nationality: Country;
}
