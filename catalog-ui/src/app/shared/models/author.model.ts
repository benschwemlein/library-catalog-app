export interface Author {
  id: number;
  firstName: string;
  lastName: string;
  bio?: string;
  birthDate?: string;
  nationality?: string;
  bookCount: number;
}

export interface CreateAuthorRequest {
  firstName: string;
  lastName: string;
  bio?: string;
  birthDate?: string;
  nationality?: string;
}
