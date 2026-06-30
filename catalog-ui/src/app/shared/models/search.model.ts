import { BookSummary } from './book.model';

export interface SearchRequest {
  query: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
}

export interface AdvancedSearchRequest {
  title?: string;
  author?: string;
  isbn?: string;
  genre?: string;
  subject?: string;
  publisher?: string;
  language?: string;
  yearFrom?: number;
  yearTo?: number;
  availableOnly?: boolean;
  branchId?: number;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
}

export interface SearchResult {
  books: BookSummary[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
  query?: string;
  facets?: SearchFacets;
}

export interface SearchFacets {
  genres: { name: string; count: number }[];
  languages: { name: string; count: number }[];
  publicationYears: { year: number; count: number }[];
  authors: { name: string; count: number }[];
}
