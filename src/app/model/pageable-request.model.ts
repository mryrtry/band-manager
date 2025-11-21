export interface PageableRequest {
  page: number;
  size: number;
  sort?: string[];
  direction?: 'DESC' | 'ASC';
}
