import { UserResponseDTO } from './authentication.models';
import { CatalogItemResponseDTO } from './catalog-item.models';

export interface CheckInOutRequestDTO {
  itemId: number;
  userEmail: string | null; // Assuming both are numbers; modify types if necessary
}

export interface CheckoutResponseDTO {
  catalogItem: CatalogItemResponseDTO;
  checkoutDateTime: string;
  checkinDateTime: string;
  checkedoutBy: UserResponseDTO;
}
