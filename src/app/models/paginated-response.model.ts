export interface PaginatedResponse<T> {
  content: T[];
  page: Page
}

export interface Page {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
