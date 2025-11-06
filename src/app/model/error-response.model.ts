export interface ErrorResponse {
  status: number;
  message: string;
  details: ErrorDetail[];
  timestamp: Date;
}

export interface ErrorDetail {
  field: string;
  message: string;
  rejectedValue: any;
  errorType: string;
}
