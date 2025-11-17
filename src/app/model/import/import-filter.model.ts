import {ImportStatus} from './import.model';

export interface ImportFilter {
  username?: string;
  filename?: string;
  importStatus?: ImportStatus;
  createdEntitiesFrom?: number;
  createdEntitiesTo?: number;
  startedBefore?: Date;
  startedAfter?: Date;
  completedBefore?: Date;
  completedAfter?: Date;
}
