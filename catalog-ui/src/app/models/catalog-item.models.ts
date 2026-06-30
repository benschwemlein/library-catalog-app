import { UserRequestDTO, UserResponseDTO } from './authentication.models';

// catalog-item.models.ts
export interface CatalogItemRequestDTO {
  // Add properties that match the Java DTO
  id?: number; // Example property
  title: string;
  description: string;
  createdBy: UserRequestDTO;
  catalogIds: CatalogId[];
  // More properties...
}

export interface CatalogItemResponseDTO {
  id: number;
  title: string;
  description: string;
  catalogIds: CatalogId[];
  createdBy?: UserResponseDTO;
  checkout?: Checkout;
}

export interface Checkout {
  checkoutDateTime: string;
  checkedoutBy?: UserResponseDTO;
}

export interface CatalogId {
  value: string;
  type?: Type;
}

export interface CatalogIdType {
  name: string;
  maxLength: number;
  formatRegex: string;
}

export interface Type {
  name: string;
}
