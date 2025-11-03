import {User} from '../user.model';

export interface AuthResponse {
  user: User;
  tokens: TokenPair;
}

export interface TokenPair {
  access_token: string;
  refresh_token: string;
}

export interface TokenValidationResponse {
  valid: boolean;
  username: string;
}
