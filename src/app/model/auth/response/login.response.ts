import {Tokens} from './tokens.response';
import {User} from '../../user/user.model';

export interface LoginResponse {
  user: User;
  tokens: Tokens;
}
