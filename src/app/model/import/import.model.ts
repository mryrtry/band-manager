export interface ImportOperation {
  id: number;
  filename: string;
  storageObjectKey?: string;
  contentType?: string;
  fileSize?: number;
  downloadUrl?: string;
  status: ImportStatus;
  createdEntitiesCount?: number;
  errorMessage?: string;
  startedAt?: string;
  completedAt?: string;
  createdBy: string;
  createdDate: string;
  lastModifiedBy?: string;
  lastModifiedDate?: string;
}

export enum ImportStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  VALIDATION_FAILED = 'VALIDATION_FAILED'
}
